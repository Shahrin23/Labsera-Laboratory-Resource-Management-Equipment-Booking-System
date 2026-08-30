import java.time.LocalDateTime;

public class ReservationBuilder {

    private int id;
    private int resourceId;
    private int userId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private ReservationStatus status =
            ReservationStatus.PENDING;


    public ReservationBuilder(
            int resourceId,
            int userId) {

        this.resourceId = resourceId;
        this.userId = userId;
    }


    public ReservationBuilder id(int id) {

        this.id = id;
        return this;
    }


    public ReservationBuilder startTime(
            LocalDateTime startTime) {

        this.startTime = startTime;
        return this;
    }


    public ReservationBuilder endTime(
            LocalDateTime endTime) {

        this.endTime = endTime;
        return this;
    }


    public ReservationBuilder status(
            ReservationStatus status) {

        this.status = status;
        return this;
    }


    public Reservation build() {

        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException(
                    "Start time and end time are required."
            );
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "End time must be after start time."
            );
        }

        Reservation reservation =
                new Reservation();

        reservation.setId(id);
        reservation.setResourceId(resourceId);
        reservation.setUserId(userId);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setStatus(status);

        return reservation;
    }
}