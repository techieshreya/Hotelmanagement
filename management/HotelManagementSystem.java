package management;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import room.CheckIn;
import room.DeluxeRoom;
import room.Room;
import room.StandardRoom;
import room.SuiteRoom;

public class HotelManagementSystem {
    private static final int MAX_ROOMS = 10;
    private static Room[] rooms = new Room[MAX_ROOMS];

    // Database connection 
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hotelmanagementsystem";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "Hotelsys@3";

    private static final String BOOKING_FILE_PATH = "booking_data.txt";
    private static final String ROOM_STATUS_FILE_PATH = "room_status.txt";

    static {
        // Loading the JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        initializeRooms();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\nHotel Management System Menu:");
                System.out.println("1. Display Room Status");
                System.out.println("2. Book a Room");
                System.out.println("3. Check In");
                System.out.println("4. Check Out");
                System.out.println("5. Exit");

                System.out.print("Enter your choice: ");

                try {
                    if (scanner.hasNextInt()) {
                        int choice = scanner.nextInt();
                        scanner.nextLine(); // Consume the newline character

                        switch (choice) {
                            case 1:
                                displayRoomStatus();
                                break;
                            case 2:
                                bookRoom(scanner);
                                break;
                            case 3:
                                checkInRoom(scanner);
                                break;
                            case 4:
                                checkOut(scanner);
                                break;
                            case 5:
                                System.out.println("Exiting the system. Goodbye!");
                                saveRoomStatusToFile();
                                System.exit(0);
                                break;
                            default:
                                System.out.println("Invalid choice. Please enter a valid option.");
                        }
                    } else {
                        System.out.println("Invalid input. Please enter a valid option.");
                        scanner.nextLine(); 
                    }
                } catch (Exception e) {
                    System.out.println("Error: Invalid input. Please enter a valid option.");
                    scanner.nextLine(); // Consume the invalid input
                }
            }
        }
    }

    private static void initializeRooms() {
        for (int i = 0; i < MAX_ROOMS; i++) {
            if (i % 3 == 0) {
                rooms[i] = new StandardRoom();
            } else if (i % 3 == 1) {
                rooms[i] = new DeluxeRoom();
            } else {
                rooms[i] = new SuiteRoom();
            }
        }
    }

    private static void displayRoomStatus() {
        System.out.println("\nRoom Status:");
        for (int i = 0; i < MAX_ROOMS; i++) {
            rooms[i].displayStatus(i + 1);
        }
    }

    private static void bookRoom(Scanner scanner) {
        System.out.print("\nEnter room number to book (1-" + MAX_ROOMS + "): ");
        int roomNumber = scanner.nextInt();
         
        if (roomNumber < 1 || roomNumber > MAX_ROOMS) {
            System.out.println("Invalid room number. Please enter a number between 1 and " + MAX_ROOMS + ".");
            return;
        }

        if (!rooms[roomNumber - 1].isAvailable()) {
            System.out.println("Room is already occupied. Cannot book again.");
            return;
        }

        System.out.println("Enter guest information:");

        scanner.nextLine(); // Consume the newline character left by nextInt()
        String guestName;
        boolean validName = false;
        do {
            System.out.print("Enter guest name (alphabet characters only, at least 2 characters): ");
            guestName = scanner.nextLine();

            if (!guestName.matches("[a-zA-Z]{2,}")) {
                System.out.println("Invalid name. Please enter alphabet characters only.");
            } else {
                validName = true;
            }
        } while (!validName);

        String guestContact;
        boolean validContact = false;
        do {
            System.out.print("Enter guest contact information (exactly 10 digits): ");
            guestContact = scanner.nextLine();
            if (guestContact.matches("\\d{10}")) {
                validContact = true;
            } else {
                System.out.println("Invalid contact information. Please enter exactly 10 digits.");
            }
        } while (!validContact);

        try {
            CheckIn checkIn = getCheckInDetails(scanner);

            
            rooms[roomNumber - 1].book(guestName, guestContact, checkIn);

            saveBookingToDatabase(roomNumber, guestName, guestContact, checkIn);

            saveBookingToFile(roomNumber, guestName, guestContact, checkIn);

            System.out.println("Room booked successfully.");
        } catch (Exception e) {
            System.out.println("Error: Invalid check-in details. Booking canceled.");
            System.out.println(e);
        }
    }

    private static void saveBookingToDatabase(int roomNumber, String guestName, String guestContact, CheckIn checkIn)
            throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String insertQuery = "INSERT INTO bookings (room_number, guest_name, guest_contact, check_in_date, stay_duration) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                preparedStatement.setInt(1, roomNumber);
                preparedStatement.setString(2, guestName);
                preparedStatement.setString(3, guestContact);
                preparedStatement.setDate(4, java.sql.Date.valueOf(checkIn.getCheckInDate()));
                preparedStatement.setInt(5, checkIn.getDuration());

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Booking information saved to the database.");
                } else {
                    System.out.println("Failed to save booking information to the database.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void saveBookingToFile(int roomNumber, String guestName, String guestContact, CheckIn checkIn)
            throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKING_FILE_PATH, true))) {
            writer.write(String.format("Room Number: %d%n", roomNumber));
            writer.write(String.format("Guest Name: %s%n", guestName));
            writer.write(String.format("Guest Contact: %s%n", guestContact));
            writer.write(String.format("Check-In Date: %s%n", checkIn.getCheckInDate()));
            writer.write(String.format("Stay Duration: %d nights%n", checkIn.getDuration()));
            writer.write("--------------------------------------------\n");
        } catch (IOException e) {
            System.out.println("Error: Unable to save booking information to file.");
            throw e; // Rethrow the exception for the calling method to handle
        }
    }

    private static void checkInRoom(Scanner scanner) {
        System.out.print("\nEnter room number to check in (1-" + MAX_ROOMS + "): ");
        int roomNumber = scanner.nextInt();

        if (roomNumber < 1 || roomNumber > MAX_ROOMS) {
            System.out.println("Invalid room number. Please enter a number between 1 and " + MAX_ROOMS + ".");
            return;
        }

        rooms[roomNumber - 1].checkIn();
    }
    
    private static void checkOut(Scanner scanner) {
        System.out.print("\nEnter room number to check out (1-" + MAX_ROOMS + "): ");
        int roomNumber = scanner.nextInt();

        if (roomNumber < 1 || roomNumber > MAX_ROOMS) {
            System.out.println("Invalid room number. Please enter a number between 1 and " + MAX_ROOMS + ".");
            return;
        }

        scanner.nextLine(); 
        rooms[roomNumber - 1].checkOut();
    }
    

    private static CheckIn getCheckInDetails(Scanner scanner) {
        try {
            LocalDate currentDate = LocalDate.now();
            LocalDate checkInDate = null; 

            do {
                System.out.print("Enter check-in date (YYYY-MM-DD): ");
                String checkInDateInput = scanner.nextLine();
                try {
                    checkInDate = LocalDate.parse(checkInDateInput, DateTimeFormatter.ISO_LOCAL_DATE);
                    if (checkInDate.isBefore(currentDate)) {
                        System.out.println("Invalid date. Please enter a date on or after today.");
                    }
                } catch (DateTimeParseException e) {
                    System.out.println("Error: Invalid date format. Please enter the date in YYYY-MM-DD format.");
                }
            } while (checkInDate == null || checkInDate.isBefore(currentDate));

            int duration;
            do {
                System.out.print("Enter stay duration (in nights, numeric value only): ");
                while (!scanner.hasNextInt()) {
                    System.out.println("Invalid input. Please enter a numeric value.");
                    scanner.next(); // Consume the invalid input
                }
                duration = scanner.nextInt();
            } while (duration <= 0);

            return new CheckIn(checkInDate, duration);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace(); // Print the stack trace for debugging
        }

        return null;
    }

    private static void saveRoomStatusToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ROOM_STATUS_FILE_PATH))) {
            writer.write("\nRoom Status:\n");
            for (int i = 0; i < MAX_ROOMS; i++) {
                writer.write("--------------------------------------------\n");
                writer.write(String.format("Room %d%n", i + 1));
                writer.write(rooms[i].getStatus() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error: Unable to save room status to file.");
        }
    }
}