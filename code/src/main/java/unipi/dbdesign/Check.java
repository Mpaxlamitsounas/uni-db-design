package unipi.dbdesign;
import java.sql.*;

public class Check {

    private static final int FALSE = 0, TRUE = 1, ERROR = -1;

	public static int UIDisCapitalLetters(String query) {

		int index = 0;

		// Jumps to where "VALUES" is located in the query
		index = query.indexOf("VALUES");
		index += "VALUES".length();

		// Moves through the string until it encounters the start of warehouse_uid
        char chr = query.charAt(index);
        while (!Character.isLetterOrDigit(chr)) {
            index++;
            if (checkOutOfBounds(query, index) == true)
                return ERROR;

            chr = query.charAt(index);
        }

		chr = query.charAt(index);
		// UID value is 4 characters long
        for (int i = 0; i < 4; i++) {
			// Gets the character and appends it to the type string
            if (!Character.isAlphabetic(chr) || !Character.isUpperCase(chr))
				return FALSE;
            index++;
			if (checkOutOfBounds(query, index))
                return ERROR;

            chr = query.charAt(index);
        }

		return TRUE;
	}

	public static int isManagedBySupervisor(String query, Statement st) {

		int employee_uid = 0, index = 0;

		// Moves to employee_uid value in query and gets it
		index = getNextIntValuePos(query, index);
		employee_uid = getIntFromString(query, index);

		if (index == ERROR)
			return ERROR;

		// Check if supervisor is already being supervised
		String checkQuery = "SELECT COUNT(*) FROM supervisor WHERE supervisor = " + employee_uid;
		int count = 1;
		try {

			ResultSet res = st.executeQuery(checkQuery);
			res.next();

			count = res.getInt("COUNT(*)");

		} catch (SQLException e) {
			return ERROR;
		}

		// Employee managing the warehouse must be a supervisor
		if (count >= 1)
			return TRUE;
		else
			return FALSE;
	}

	public static int existsNoNestedSupervision(String query, Statement st) {

		int supervisor = 0, index = 0;

		// Goes to subordinate value
		index = getNextIntValuePos(query, index);
		
		// Moves to Supervisor value in query and gets it
		index = getNextIntValuePos(query, index);
		supervisor = getIntFromString(query, index);

		if (index == ERROR)
			return ERROR;

		// Check if supervisor is already being supervised
		String checkQuery = "SELECT COUNT(*) FROM supervisor WHERE subordinate = " + supervisor;
		int count = 1;
		try {

			ResultSet res = st.executeQuery(checkQuery);
			res.next();

			count = res.getInt("COUNT(*)");

		} catch (SQLException e) {
			return ERROR;
		}

		// If no lines contain supervisor as subordinate, there is no circular or nested supervision
		if (count == 0)
			return TRUE;
		else
			return FALSE;
	}

    public static int isDifferentPerson(String query) {

        int supervisor = 0, subordinate = 0, index = 0; 

		// Gets the value of subordinate
        index = getNextIntValuePos(query, index);
        subordinate = getIntFromString(query, index);

		// Gets the value of supervisor
        index = getNextIntValuePos(query, index);
        supervisor = getIntFromString(query, index);

		if (index == ERROR)
			return ERROR;

		// Employee cannot supervise self
        if (supervisor != subordinate)
            return TRUE;
        else
            return FALSE;
    }

	public static int hasCapSizeHierarchyBin(String query) {

		int total_cap = 0, remaining_cap = 0, index = 0;

		// Moves to first value
		index = getNextIntValuePos(query, index);

		// Goes to and gets value of total_cap from query
		index = getNextIntValuePos(query, index);
		total_cap = getIntFromString(query, index);

		// Goes to and gets value of remaining_cap from query
		index = getNextIntValuePos(query, index);
		remaining_cap = getIntFromString(query, index);

		if (index == ERROR)
			return ERROR;

		if (total_cap >= remaining_cap)
			return TRUE;
		else
			return FALSE;
	}

	public static int batchNumUnderBatchSize(String query, Statement st) {

		int batch_num = 0, batch_uid = 0, batch_size = 0, index = 0;

		// Get the value of batch_num
		index = getNextIntValuePos(query, index);
		batch_num = getIntFromString(query, index);

		// Get the value of the batch's uid
		index = getNextIntValuePos(query, index);
		batch_uid = getIntFromString(query, index);

		if (index == ERROR)
			return ERROR;

		// Get the size of the specific batch
		String checkQuery = "SELECT batch_size FROM batch WHERE batch_uid = " + batch_uid;
		try {

			ResultSet res = st.executeQuery(checkQuery);
			res.next();

			batch_size = res.getInt("batch_size");

		} catch (SQLException e) {
			return ERROR;
		}

		// If condition holds, return TRUE
		if (batch_num <= batch_size)
			return TRUE;
		else
			return FALSE;
	}

	public static int existsSingularActiveOrder(String query, Statement st) {

        char chr;
		int index = 0;
        String type = "";

		// Moves through first two values
		index = getNextIntValuePos(query, index);
        index = getNextIntValuePos(query, index);
		index++;

		if (index == ERROR)
			return ERROR;
		
		// Moves through the string until it encounters the start of type value
        chr = query.charAt(index);
        while (!Character.isLetterOrDigit(chr)) {
            index++;
            if (checkOutOfBounds(query, index) == true)
                return ERROR;

            chr = query.charAt(index);
        }

		// Type value is 5 characters long
        for (int i = 0; i < 5; i++) {
			// Gets the character and appends it to the type string
            type += chr;
            index++;
			if (checkOutOfBounds(query, index))
                return ERROR;

            chr = query.charAt(index);
        }

		// Gets how many active orders ordered type
		String checkQuery = "SELECT COUNT(*) FROM `order` WHERE type = '" + type + "'";
		int count = 0;
		try {

			ResultSet res = st.executeQuery(checkQuery);
			res.next();

			count = res.getInt("COUNT(*)");

		} catch (SQLException e) {
			return ERROR;
		}

		// If no other order of type is active, continue insert
		if (count == 0)
			return TRUE;
		else 
			return FALSE;
    }

	public static int existsSingularOrderUID(String query, Statement st) {

		int order_uid = 0, index = 0;
		
		// Gets the value of order_uid
		index = getNextIntValuePos(query, index);
		order_uid = getIntFromString(query, index);

		if (index == ERROR)
			return ERROR;

		// Looks to see if it's present in the archive table
		String checkQuery = "SELECT COUNT(*) FROM order_archive WHERE order_uid = " + order_uid;
		int count = 0;
		try {

			ResultSet res = st.executeQuery(checkQuery);
			res.next();

			count = res.getInt("COUNT(*)");

		} catch (SQLException e) {
			return ERROR;
		}

		// If it's not present, continue with insert
		if (count == 0)
			return TRUE;
		else 
			return FALSE;
    }

	public static int hasCapSizeHierarchyOrder(String query) {

		int size = 0, remain = 0, index = 0;

		// Moves to 4th argument
		index = getNextIntValuePos(query, index);
		index = getNextIntValuePos(query, index);
		index++;

		char chr = query.charAt(index);
		int counter = 0;
		do {
			if (chr == ',')
				counter++;

			if (counter == 2)
				break;

			index++;
			if (checkOutOfBounds(query, index) == true)
                return ERROR;
				
			chr = query.charAt(index);
		} while (true);

		index = getNextIntValuePos(query, index);

		// Moves to 5th argument and gets value of size from query
		index = getNextIntValuePos(query, index);
		size = getIntFromString(query, index);

		// Get value of remain from query
		index = getNextIntValuePos(query, index);
		remain = getIntFromString(query, index);

		if (index == ERROR)
			return ERROR;

		if (size >= remain)
			return TRUE;
		else
			return FALSE;
	}

	public static int existsEmptyOrder(String query, Statement st) {
        
		int index = 0, order_uid = 0, batch_size = 0, remaining = 0;

		// Move to 3rd argument
		index = getNextIntValuePos(query, index);
		index = getNextIntValuePos(query, index);

		// Goes to and gets value of order_uid from insert query
		index = getNextIntValuePos(query, index);
        order_uid = getIntFromString(query, index);

		// Moves to date argument
		index = getNextIntValuePos(query, index);

		// Goes to and gets value of batch_size from insert query
        index = getNextIntValuePos(query, index);
        batch_size = getIntFromString(query, index);

		if (index == ERROR)
			return ERROR;

		// Get remaining parts to be delivered
        String checkQuery = "SELECT remain FROM `order` WHERE order_uid = " + order_uid;
		try {

			ResultSet res = st.executeQuery(checkQuery);
			res.next();

			remaining = res.getInt("remain");

		} catch (SQLException e) {
			return ERROR;
		}

        if (remaining - batch_size <= 0) 
			// order_uid might start with 0 and collide with FALSE value
            return order_uid + TRUE;
        else {

			String updateQuery = "UPDATE `order` SET remain = " + String.valueOf(remaining-batch_size) + " WHERE order_uid = " + order_uid;
			try {

				st.executeUpdate(updateQuery);
				System.out.println("Changed remaining for 1 order\n");

			} catch (SQLException e) {
				return ERROR;
			}

			return FALSE;
		}
    }

	public static int hasMinimumNumbers(int employee_uid, Statement st) {

		// Check how many telephone numbers employee has
		String checkQuery = "SELECT COUNT(*) FROM telephone WHERE employee_uid = " + employee_uid;
		int count = 0;
		try {

			ResultSet res = st.executeQuery(checkQuery);
			res.next();

			count = res.getInt("COUNT(*)");

		} catch (SQLException e) {
			return ERROR;
		}

		// If employee has at least 2 numbers, can delete one
		if (count > 1)
			return TRUE;
		else
			return FALSE;
	}

	public static int fitsInBin(String query, Statement st) {

		int bin_uid = 0, bin_size = 0, batch_size = 0, index = 0;

		// Moves to 1st argument
		index = getNextIntValuePos(query, index);

		// Moves to 3rd argument (warehouse_uid has no numbers) and gets bin_uid value
		index = getNextIntValuePos(query, index);
		bin_uid = getIntFromString(query, index);

		// Moves to 5th argument
		index = getNextIntValuePos(query, index);
		index = getNextIntValuePos(query, index);

		// Moves to 6th argument and gets batch_size value
		index = getNextIntValuePos(query, index);
		batch_size = getIntFromString(query, index);

		if (index == ERROR)
			return ERROR;

		// Gets the remaining capacity of specific bin
        String checkQuery = "SELECT remaining_cap FROM bin WHERE bin_uid = " + bin_uid ;
		try {

			ResultSet res = st.executeQuery(checkQuery);
			res.next();

			bin_size = res.getInt("remaining_cap");

		} catch (SQLException e) {
			return ERROR;
		}

		// If bin can fit batch, continue with insert
        if (bin_size >= batch_size) 
            return TRUE;
		else
			return FALSE;
	}

	public static int correspondsToOrder(String query, Statement st) {

		int order_uid = 0, index = 0;

		// Skips first 2 numeric arguments
		index = getNextIntValuePos(query, index);
		index = getNextIntValuePos(query, index);

		// Gets value of order_uid
		index = getNextIntValuePos(query, index);
		order_uid = getIntFromString(query, index);

		if (index == ERROR)
			return ERROR;

		// Gets the remaining capacity of specific bin
        String checkQuery = "SELECT COUNT(*) FROM `order` WHERE order_uid = " + order_uid ;
		int count = 0;
		try {

			ResultSet res = st.executeQuery(checkQuery);
			res.next();

			count = res.getInt("COUNT(*)");

		} catch (SQLException e) {
			return ERROR;
		}

		// If the order exists, allow insert
        if (count == 1) 
            return TRUE;
		else
			return FALSE;
	}

	public static int getNextIntValuePos(String query, int index) {

		// Checks if the index pointer is already out of bounds
		if (checkOutOfBounds(query, index) || index == ERROR)
			return ERROR;

		char chr = query.charAt(index);

		// If it's already on a number, skips it
		if (Character.isDigit(chr)) {
			while (Character.isDigit(chr)) {
				index++;
				if (checkOutOfBounds(query, index) == true)
                    return ERROR;

				chr = query.charAt(index);
			}
		}

		// Move until you encounter a number
		while (!Character.isDigit(chr)) {
			index++;
			if (checkOutOfBounds(query, index) == true)
                return ERROR;
				
			chr = query.charAt(index);
		}

		// Returns the starting position of the number
		return index;
	}

	public static int getIntFromString(String query, int index) {

		int count = 0;
        String str = "";

		// Checks if the index pointer is already out of bounds
		if (index == ERROR)
			return ERROR;

		char chr = query.charAt(index);

		// Reads the number as a string
        while (Character.isDigit(chr)) {
            str += query.charAt(index);

            index++;
            if (checkOutOfBounds(query, index))
                break;

			chr = query.charAt(index);
        }

		// Parses the string for an integer
        count = Integer.parseInt(str);
		return count;
	}

    public static boolean checkOutOfBounds(String query, int index) {
        if (index > query.length()-1)
            return true;
        else
            return false;
    }
}