package room;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CheckIn {
    private final LocalDate checkInDate;
    private final int duration;

    public CheckIn(LocalDate checkInDate, int duration) {
        if (!isValidDate(checkInDate) || duration <= 0) {
            throw new IllegalArgumentException("Invalid CheckIn parameters");
        }

        this.checkInDate = checkInDate;
        this.duration = duration;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public int getDuration() {
        return duration;
    }

    private boolean isValidDate(LocalDate checkInDate) {
        // Assuming a standard ISO_LOCAL_DATE format (YYYY-MM-DD)
        try {
            DateTimeFormatter.ISO_LOCAL_DATE.parse(checkInDate.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
