package unipi.dbdesign;

import java.util.Scanner;

public class Menu {

    private static Scanner scanner = new Scanner(System.in);

    public static void showMenu() {
        System.out.println("Select function:  -1 to exit");
        System.out.println(" 1) Insert/Update query");
        System.out.println(" 2) List all warehouses");
        System.out.println(" 3) List all parts by batch");
        System.out.println(" 4) List all bins' total and remaining capacity");
        System.out.println(" 5) Update bin capacity");
        System.out.println(" 6) List all employees by supervisor");
        System.out.println(" 7) List all supervisor names");
        System.out.println(" 8) List all parts that can be built into a construct");
        System.out.println(" 9) List all orders");
        System.out.println("10) Delete a employee's telephone number");
        // System.out.println("11) Custom select query");
        System.out.print("\n? ");
    }

    public static int getInt() {
        int integer;
        integer = scanner.nextInt();
        scanner.nextLine(); // Empty buffer
        return integer;
    }
    
    public static String getString() {
        String str;
        str = scanner.nextLine();
        return str;
    }

}