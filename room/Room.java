package room;

public class Room {
    protected String roomType;
    protected double roomPrice;
    protected boolean isAvailable;
    protected String guestName;
    protected String guestContact;
    protected CheckIn checkIn;

    public Room(String roomType, double roomPrice) {
        this.roomType = roomType;
        this.roomPrice = roomPrice;
        this.isAvailable = true;
        this.guestName = null;
        this.guestContact = null;
        this.checkIn = null;
    }

    public void displayStatus(int roomNumber) {
        System.out.println("Room " + roomNumber + ": " + roomType +
                " - $" + roomPrice +
                " - " + (isAvailable ? "Available" : "Occupied") +
                (isAvailable ? "" : " - Guest: " + guestName +
                        " - Contact: " + guestContact +
                        (checkIn != null ? " - Checked In: " + checkIn.getCheckInDate() : "")));
    }

    public void book(String guestName, String guestContact, CheckIn checkIn) {
        if (isAvailable) {
            this.isAvailable = false;
            this.guestName = guestName;
            this.guestContact = guestContact;
            this.checkIn = checkIn;
            System.out.println("Room booked successfully for " + guestName +
                    ". Contact: " + guestContact +
                    (checkIn != null ? " - Checked In: " + checkIn.getCheckInDate() : ""));
        } else {
            System.out.println("Room is already occupied. Cannot book again.");
        }
    }

    public void checkIn() {
        if (!isAvailable) {
            System.out.println("Guest " + guestName + " checking in. Welcome!");
        } else {
            System.out.println("Room is not booked. Cannot check in.");
        }
    }

    public void checkOut() {
        if (!isAvailable) {
            double totalCost = calculateTotalCost();
            System.out.println("Checking out. Thank you, " + guestName + "!");
            System.out.println("Guest Contact: " + guestContact);
            System.out.println("Total Cost: $" + totalCost);
            isAvailable = true;
            guestName = null;
            guestContact = null;
            checkIn = null;
        } else {
            System.out.println("Room is not occupied.");
        }
    }

    protected double calculateTotalCost() {
        return roomPrice * (checkIn != null ? checkIn.getDuration() : 1);
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public CheckIn getCheckIn() {
        return checkIn;
    }

    public String getStatus() {
        return null;
    }

    public boolean isCheckedIn() {
        return false;
    }
}
