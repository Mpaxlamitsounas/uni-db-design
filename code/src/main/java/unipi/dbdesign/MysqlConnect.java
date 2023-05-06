package unipi.dbdesign;
import java.sql.*;

public class MysqlConnect {

	private Connection conn;
	private String url, dbName, driver, username, password;
	public Statement st;
	private ResultSet res;
	private ResultSetMetaData resMeta;
	private boolean doExtraChecks;
	private final int FALSE = 0, TRUE = 1, ERROR = -1;
	

	public MysqlConnect(String dbName, String username, String password) {
		this.conn = null;
		this.url = "jdbc:mysql://localhost:3306/";
		this.dbName = dbName;
		this.driver = "com.mysql.jdbc.Driver";
		this.username = username; 
		this.password = password;
		this.doExtraChecks = true;
		connect();
	}

	public boolean connect() {

		// attempts to connect to db with arguments
		try {
			Class.forName(driver).getDeclaredConstructor().newInstance();
			conn = DriverManager.getConnection(url+dbName,username,password);
			System.out.println("Connected to database\n");
			this.st = conn.createStatement();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean close() {
		try { conn.close(); return true; }
		catch (SQLException e) { System.out.println("Failed to close connection"); return false; }
	}

	public boolean select(String query) {

		try {

			// Prints and executes the SQL query
			System.out.println(query);
			res = st.executeQuery(query);
			resMeta = res.getMetaData();

			// Prints the column labels
			for (int i = 0; i < resMeta.getColumnCount(); i++) {
				System.out.printf("%s", resMeta.getColumnLabel(i+1));
				int maxLength;

				if (resMeta.getColumnDisplaySize(i+1) > resMeta.getColumnLabel(i+1).length())
					maxLength = resMeta.getColumnDisplaySize(i+1);
				else
					maxLength = resMeta.getColumnLabel(i+1).length();
				
				for (int j = 0; j < maxLength - resMeta.getColumnLabel(i+1).length(); j++) {
					System.out.print(" ");
				}
				System.out.print("\t");
			}
			System.out.println();

			// Gets returned values by line
			String returnStr = "";
			int returnInt = 0;
			while (res.next()) {

				for (int i = 0; i < resMeta.getColumnCount(); i++) {

					int maxLength;
					if (resMeta.getColumnDisplaySize(i+1) > resMeta.getColumnLabel(i+1).length())
						maxLength = resMeta.getColumnDisplaySize(i+1);
					else
						maxLength = resMeta.getColumnLabel(i+1).length();

					// gets and prints returned values
					if (resMeta.getColumnType(i+1) == 4) {
						returnInt = res.getInt(resMeta.getColumnLabel(i+1));
						System.out.print(returnInt);
									
						for (int j = 0; j < maxLength - String.valueOf(returnInt).length(); j++) {
							System.out.print(" ");
						}
					}

					else if (resMeta.getColumnType(i+1) == 1 || resMeta.getColumnType(i+1) == 12) {
						returnStr = res.getString(resMeta.getColumnLabel(i+1));
						System.out.print(returnStr);

						for (int j = 0; j < maxLength - returnStr.length(); j++) {
							System.out.print(" ");
						}
					}

					System.out.print("\t");
				}
				System.out.println();
			}

			// if all is successful, return true
			return true;

		} catch (Exception e) {

			// if something failed, return false
			System.out.println("SQL query not executed");
			return false;
		}
	}

	public boolean update(String query, boolean quiet) {

		if (!quiet)
			System.out.println("\n---");

		// Check if various alter conditions hold
		switch(passConditions(query)) {
			
			case FALSE: System.out.println(); return false;
			
			case ERROR: System.out.println("Failed parsing or executing query"); return false;

		}

		try {

			// Executes the query
			int i = st.executeUpdate(query);

			if (!quiet) {
				// Prints number of lines changed
				if (i == 1)
					System.out.println("Changed " + i + " line");
				else 
				System.out.println("Changed " + i + " lines");
			}

		} catch (Exception e) {
			// If something failed, inform the user
			System.out.println("SQL query not executed");
			return false;
		}

		// When a batch arrives, if the the order is fulfilled, move it to archive
		if (query.toUpperCase().contains("INSERT") && query.toUpperCase().contains("BATCH")) {

			int order_uid = Check.existsEmptyOrder(query, st);

			if (order_uid == ERROR)
				System.out.println("Failed to check if order is empty");
			else if (order_uid >= TRUE) {
				order_uid -= TRUE;
				int result = moveEmptyOrder(order_uid);

				switch(result) {
					
					case TRUE: System.out.println("Moved empty order to archive"); break;

					case ERROR: System.out.println("Failed to move empty order"); break;
				}
			}
		}

		// When a batch arrives, if the the order is fulfilled, move it to archive
		if (query.toUpperCase().contains("INSERT") && query.toUpperCase().contains("EMPLOYEE")) {

			System.out.println("New employee must work under someone or be a supervisor themselves");
			System.out.println("Enter the employee_uid the new employee works under");
			System.out.println("If the new employee is a supervisor, enter -1");
			System.out.print("? ");

			int supervisor_uid = Menu.getInt();
			int employee_uid = 0, index = 0;
			index = Check.getNextIntValuePos(query, index);
			employee_uid = Check.getIntFromString(query, index);

			if (supervisor_uid == -1) {
				System.out.println();
				return true;
			} else {
				String insertQuery = "INSERT supervisor VALUES (" + employee_uid + ", " + supervisor_uid + ")";
				boolean success = update(insertQuery, true);
				if (success)
					System.out.println("Successfully entered new employee in supervisor table\n");
				else	
					System.out.println("Failed to enter new employee in supervisor table\n");
			}
		}

		return true;
	}

	private int passConditions(String query) {

		if (query.toUpperCase().contains("INSERT") && query.toUpperCase().contains("WAREHOUSE")) {

			int check = Check.UIDisCapitalLetters(query);

			switch(check) {
					
				case FALSE: System.out.println("Warehouse UID must be 4 capital letters"); return FALSE;

				case ERROR: return ERROR;
			}

			check = Check.isManagedBySupervisor(query, st);

			switch(check) {
					
				case FALSE: System.out.println("Warehouse must be managed by a supervisor"); return FALSE;

				case ERROR: return ERROR;
			}

		}

		// If it's an insert query for supervisor table
		if (query.toUpperCase().contains("INSERT") && query.toUpperCase().contains("SUPERVISOR")) {

			// Checks if supervisor is already being supervised by another employee
			int check = Check.existsNoNestedSupervision(query, st);

			switch(check) {

				case FALSE: System.out.println("Nested or circular supervision is not allowed"); return FALSE;

				case ERROR: return ERROR;
			}

			// Check supervisor and subordinate aren't the same person
			check = Check.isDifferentPerson(query);

			switch(check) {

				case FALSE: System.out.println("Supervisor and subordinate can't be the same person"); return FALSE;

				case ERROR: return ERROR;
			}
		}

		// flag for doing checks not in specification
		if (doExtraChecks) {
			// If it's an insert or update query in table bin
			if (query.toUpperCase().contains("INSERT") && query.toUpperCase().contains("BIN")) {

				// Check if remaining_cap is smaller than total_cap
				int check = Check.hasCapSizeHierarchyBin(query);

				switch(check) {

					case FALSE: System.out.println("Remaining_cap cannot be larger than total_cap"); return FALSE;

					case ERROR: return ERROR;
				}
			}
		}

		if (doExtraChecks) {
			// If it's an insert query intable part
			if (query.toUpperCase().contains("INSERT") && query.toUpperCase().contains("PART") && !query.toUpperCase().contains("PART_TYPE")) {

				// The batch_number of a part must be smaller than the batch_size
				int check = Check.batchNumUnderBatchSize(query, st);

				switch(check) {

					case FALSE: System.out.println("The batch number of a part can't be larger than the batch's size"); return FALSE;

					case ERROR: return ERROR;
				}
			}
		}

		if (query.toUpperCase().contains("INSERT") && query.toUpperCase().contains("BATCH")) {

			int check = Check.correspondsToOrder(query, st);

			switch(check) {
					
				case FALSE: System.out.println("Order for batch does not exist"); return FALSE;

				case ERROR: return ERROR;
			}

			if (doExtraChecks) {
				check = Check.fitsInBin(query, st);

				switch(check) {
						
					case FALSE: System.out.println("Batch does not fin in bin"); return FALSE;

					case ERROR: return ERROR;
				}
			}
		}

		if (query.toUpperCase().contains("INSERT") && query.toUpperCase().contains("ORDER")) {

			int check = Check.existsSingularActiveOrder(query, st);

			switch(check) {

				case FALSE: System.out.println("There already is an active order for this part type"); return FALSE;

				case ERROR: return ERROR;
			}

			check = Check.existsSingularOrderUID(query, st);

			switch(check) {

				case FALSE: System.out.println("Order UID already exists in archive table"); return FALSE;

				case ERROR: return ERROR;
			}

			if (doExtraChecks) {
				check = Check.hasCapSizeHierarchyOrder(query);

				switch(check) {
	
					case FALSE: System.out.println("Remain cannot be larger than total_cap"); return FALSE;
	
					case ERROR: return ERROR;
				}
			}
		} 

		// If all conditions hold, return TRUE
		return TRUE;
	}

	private int moveEmptyOrder(int order_uid) {

		int employee_uid, order_size, ord_date;
		String type; 

		String selectQuery = "SELECT * FROM `order` WHERE order_uid = " + order_uid;
		try {

			// Gets the order's values
			ResultSet res = st.executeQuery(selectQuery);
			res.next();

			employee_uid = res.getInt("employee_uid");
			type = "'" + res.getString("type") + "'";
			ord_date = res.getInt("ord_date");
			order_size = res.getInt("size");

			// Inserts a line in the archive table with order's values
			String insertQuery = "INSERT order_archive VALUES (" + order_uid + ", " + employee_uid + ", " + type + ", " + ord_date + ", " + order_size + ")";
			st.executeUpdate(insertQuery);

			// Removes the order from the order table
			String removeQuery = "DELETE FROM `order` WHERE order_uid = " + order_uid;
			st.executeUpdate(removeQuery);

			return TRUE;

		} catch (SQLException e) {
			return ERROR;
		}
	}

	public void disableExtraChecks() {
		this.doExtraChecks = false;
	}

	public void enableExtraChecks() {
		this.doExtraChecks = true;
	}

	
}