import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {

    private final List<Reservation> reservations =
            new ArrayList<>();


    // Request a new reservation
    public Reservation request(
            Reservation reservation) {

        if (checkConflict(
                reservation.getResourceId(),
                reservation.getStartTime(),
                reservation.getEndTime())) {

            reservation.setStatus(
                    ReservationStatus.REJECTED
            );

            System.out.println(
                    "Reservation rejected: Conflict found."
            );

            return reservation;
        }


        reservation.setStatus(
                ReservationStatus.PENDING
        );

        reservations.add(reservation);

        System.out.println(
                "Reservation request submitted."
        );

        return reservation;
    }


    // Check whether requested time conflicts
    // with an existing reservation
    public boolean checkConflict(
            int resourceId,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        for (Reservation existing : reservations) {

            if (existing.getResourceId() != resourceId) {
                continue;
            }

            if (existing.getStatus()
                    == ReservationStatus.CANCELLED ||
                    existing.getStatus()
                            == ReservationStatus.REJECTED) {

                continue;
            }


            boolean conflict =
                    existing.getStartTime()
                            .isBefore(endTime)
                            &&
                            existing.getEndTime()
                                    .isAfter(startTime);


            if (conflict) {
                return true;
            }
        }

        return false;
    }


    // Cancel reservation
    public boolean cancel(int reservationId) {

        for (Reservation reservation : reservations) {

            if (reservation.getId() == reservationId) {

                reservation.setStatus(
                        ReservationStatus.CANCELLED
                );

                System.out.println(
                        "Reservation cancelled."
                );

                return true;
            }
        }

        System.out.println(
                "Reservation not found."
        );

        return false;
    }


    // For testing / viewing reservations
    public List<Reservation> getReservations() {

        return new ArrayList<>(reservations);
    }
}