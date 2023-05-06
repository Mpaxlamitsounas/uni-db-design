package unipi.dbdesign;

public class App 
{
    public static void main(String[] args) {

        MysqlConnect connection = new MysqlConnect("exerdb", "root", "");

        final int FALSE = 0, TRUE = 1, ERROR = -1;
        String query;
        int choice;

        // insertData(connection);
        
        do {

            Menu.showMenu();
            choice = Menu.getInt();
            System.out.println();

            switch(choice) {

                case 1: System.out.print("Enter query:\n? "); query = Menu.getString(); connection.update(query, false); break;

                case 2: connection.select("SELECT * FROM warehouse"); break;

                case 3: connection.select("SELECT * FROM part ORDER BY batch_uid"); break;

                case 4: connection.select("SELECT bin_uid, warehouse_uid, total_cap, remaining_cap FROM bin ORDER BY warehouse_uid, bin_uid"); break;

                case 5: System.out.print("Enter update query:\n? "); query = Menu.getString(); connection.update(query, false); break;

                case 6: connection.select("SELECT * FROM supervisor ORDER BY supervisor"); break;

                case 7: connection.select(  "SELECT supervisor, name, surname " +
                                            "FROM supervisor, name N, employee E " +
                                            "WHERE supervisor = E.employee_uid AND E.employee_uid = N.employee_uid");
                        break;

                case 8: connection.select(  "SELECT batch_num, P.batch_uid, O.type, construct_uid " +
                                            "FROM part P " +
                                            "NATURAL JOIN batch B, `order` O, construct C " +
                                            "GROUP BY batch_num, P.batch_uid " +
                                            "ORDER BY construct_uid, O.type, P.batch_uid, batch_num");
                        break;

                case 9: connection.select(  "SELECT order_uid, name, surname " +
                                            "FROM `order` " +
                                            "NATURAL JOIN name, employee " +
                                            "GROUP BY order_uid " +
                                            "ORDER BY surname, name");
                        break;

               case 10: System.out.print("Provide employee uid:\n? ");
                        int employee_uid = Menu.getInt();

                        // Check if employee has at least 2 telephone numbers
                        int check = Check.hasMinimumNumbers(employee_uid, connection.st);
                        switch(check) {

                            case TRUE:  System.out.print("Employee has at least 2 numbers\nProvide a telephone number:\n? ");
                                        String telephone = Menu.getString();
                                        query = "DELETE FROM telephone WHERE employee_uid = " + employee_uid + " AND num = " + telephone;
                                        connection.update(query, false);
                                        break;

                            case FALSE: System.out.println("Employee needs to have at least 2 telephone numbers"); break;

                            case ERROR: System.out.println("Failed parsing or executing query"); break;
                        }

                        break;

                // Hidden option, can be used for custom select query
                case 11: System.out.print("Enter query:\n? "); query = Menu.getString(); connection.select(query); break;

                case -1: break;

                default: System.out.println("Invalid selection");
            }

            System.out.println();

        } while(choice != -1);

        connection.close();
    }

    public static void insertData(MysqlConnect connection) {

        // Εισαγωγή employee
        connection.update("INSERT employee VALUES (1, 'f_name_1', 'surname1')", true);
        connection.update("INSERT employee VALUES (2, 'f_name_2', 'surname2')", true);
        connection.update("INSERT employee VALUES (3, 'f_name_3', 'surname3')", true);
        connection.update("INSERT employee VALUES (4, 'f_name_4', 'surname4')", true);
        connection.update("INSERT employee VALUES (5, 'f_name_5', 'surname5')", true);

        // Παράδειγμα σφάλματος εισαγωγής στον supervisor
        connection.update("INSERT supervisor VALUES (5, 3)", true);
        connection.update("INSERT supervisor VALUES (5, 5)", true);


        // Εισαγωγη τηλεφωνικών αριθμών υπαλλήλων
        connection.update("INSERT telephone VALUES (2104411111, 1)", true);
        connection.update("INSERT telephone VALUES (2104422222, 1)", true);
        connection.update("INSERT telephone VALUES (6911232322, 1)", true);
        connection.update("INSERT telephone VALUES (2104421232, 2)", true);
        connection.update("INSERT telephone VALUES (6915456443, 4)", true);
        
        // Εισαγωγή διευθύνσεων υπαλλήλων
        connection.update("INSERT address VALUES ('main_st_1', 121, 18811, 1, 'Piraeus')", true);
        connection.update("INSERT address VALUES ('main_st_2', 122, 18811, 2, 'Piraeus')", true);
        connection.update("INSERT address VALUES ('main_st_3', 123, 18811, 3, 'Piraeus')", true);
        connection.update("INSERT address VALUES ('main_st_3', 124, 18811, 4, 'Piraeus')", true);
        connection.update("INSERT address VALUES ('main_st_4', 125, 18811, 5, 'Piraeus')", true);

        // Εισαγωγή ονομάτων υπαλλήλων
        connection.update("INSERT name VALUES ('name_11', 1)", true);
        connection.update("INSERT name VALUES ('name_12', 1)", true);
        connection.update("INSERT name VALUES ('name_13', 1)", true);
        connection.update("INSERT name VALUES ('name_2', 2)", true);
        connection.update("INSERT name VALUES ('name_3', 3)", true);
        connection.update("INSERT name VALUES ('name_4', 4)", true);
        connection.update("INSERT name VALUES ('name_51', 5)", true);
        connection.update("INSERT name VALUES ('name_52', 5)", true);

        // Εισαγωγή warehouse
        connection.update("INSERT warehouse VALUES ('ABCD', 1)", true);
        connection.update("INSERT warehouse VALUES ('BCDE', 1)", true);
        connection.update("INSERT warehouse VALUES ('CDEF', 2)", true);

        // Παράδειγμα σφάλματος
        connection.update("INSERT warehouse VALUES ('123A', 1)", true);
        connection.update("INSERT warehouse VALUES ('ASDF', 4)", true);

        // Εισαγωγή bin
        connection.update("INSERT bin VALUES (0, 'ABCD', 40, 40)", true);
        connection.update("INSERT bin VALUES (1, 'ABCD', 50, 20)", true);
        connection.update("INSERT bin VALUES (2, 'ABCD', 100, 90)", true);
        connection.update("INSERT bin VALUES (3, 'ABCD', 15, 15)", true);
        connection.update("INSERT bin VALUES (4, 'ABCD', 40, 30)", true);
        connection.update("INSERT bin VALUES (5, 'ABCD', 25, 15)", true);
        connection.update("INSERT bin VALUES (6, 'ABCD', 40, 35)", true);
        connection.update("INSERT bin VALUES (7, 'ABCD', 250, 250)", true);
        connection.update("INSERT bin VALUES (0, 'BCDE', 40, 40)", true);
        connection.update("INSERT bin VALUES (1, 'BCDE', 15, 15)", true);

        // Παράδειγμα σφάλματος
        connection.update("INSERT bin VALUES (2, 'BCDE', 40, 50)", true);

        // Δημιουργία part_type
        connection.update("INSERT part_type VALUES ('123ab')", true);
        connection.update("INSERT part_type VALUES ('12sdv')", true);
        connection.update("INSERT part_type VALUES ('v453a')", true);
        connection.update("INSERT part_type VALUES ('jioge')", true);
        connection.update("INSERT part_type VALUES ('8732v')", true);

        // Δημιουργία construct
        connection.update("INSERT construct VALUES ('123ab', 1)", true);
        connection.update("INSERT construct VALUES ('12sdv', 1)", true);
        connection.update("INSERT construct VALUES ('jioge', 1)", true);

        // Δημιουργία order
        connection.update("INSERT `order` VALUES (1, 1, '123ab', 06062022, 140, 140)", true);
        connection.update("INSERT `order` VALUES (2, 1, '12sdv', 12052022, 100, 100)", true);
        connection.update("INSERT `order` VALUES (3, 1, 'v453a', 04022022, 120, 120)", true);
        connection.update("INSERT `order` VALUES (4, 2, 'jioge', 18022022, 50, 50)", true);

        // Παράδειγμα σφάλματος
        connection.update("INSERT `order` VALUES (6, 1, '12sdv', 08062022, 50, 50)", true);
        connection.update("INSERT `order` VALUES (6, 1, '8732v', 08062022, 50, 60)", true);

        // Δημιουργία batch
        connection.update("INSERT batch VALUES (1, 'ABCD', 0, 1, 25062022, 30)", true);
        connection.update("INSERT batch VALUES (2, 'ABCD', 1, 1, 26062022, 11)", true);
        connection.update("INSERT batch VALUES (3, 'ABCD', 2, 1, 28062022, 23)", true);
        connection.update("INSERT batch VALUES (4, 'ABCD', 3, 1, 30062022, 5)", true);
        connection.update("INSERT batch VALUES (5, 'ABCD', 4, 1, 15072022, 15)", true);
        connection.update("INSERT batch VALUES (6, 'ABCD', 5, 2, 25062022, 10)", true);
        connection.update("INSERT batch VALUES (7, 'ABCD', 6, 3, 25062022, 30)", true);
        connection.update("INSERT batch VALUES (8, 'BCDE', 0, 4, 25062022, 20)", true);

        // Παράδειγμα σφάλματος
        connection.update("INSERT batch VALUES (8, 'BCDE', 0, 5, 25062022, 20)", true);
        connection.update("INSERT batch VALUES (8, 'BCDE', 0, 4, 25062022, 200)", true);

        // Δημιουργία part
        for (int i = 1; i <= 30; i++) {
            connection.update("INSERT part VALUES (" + i + ", 1)", true);
        }

        for (int i = 1; i <= 23; i++) {
            connection.update("INSERT part VALUES (" + i + ", 3)", true);
        }

        for (int i = 1; i <= 10; i++) {
            connection.update("INSERT part VALUES (" + i + ", 6)", true);
        }

        // Παράδειγμα σφάλματος
        connection.update("INSERT part VALUES (31, 1)", true);

       

    }
}