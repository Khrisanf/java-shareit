package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.dto.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    // нормально, что они теперь такие длинные?


    // --- BOOKER ---

    List<Booking> findByBookerIdOrderByStartTimeBookingDesc(long userId);

    List<Booking> findByBookerIdAndStartTimeBookingLessThanEqualAndEndTimeBookingGreaterThanEqualOrderByStartTimeBookingDesc(
            long userId, LocalDateTime now1, LocalDateTime now2);

    List<Booking> findByBookerIdAndEndTimeBookingLessThanOrderByStartTimeBookingDesc(
            long userId, LocalDateTime now);

    List<Booking> findByBookerIdAndStartTimeBookingGreaterThanOrderByStartTimeBookingDesc(
            long userId, LocalDateTime now);

    List<Booking> findByBookerIdAndStatusOrderByStartTimeBookingDesc(
            long userId, Status status);


    // --- OWNER ---

    List<Booking> findByItemOwnerIdOrderByStartTimeBookingDesc(long ownerId);

    List<Booking> findByItemOwnerIdAndStartTimeBookingLessThanEqualAndEndTimeBookingGreaterThanEqualOrderByStartTimeBookingDesc(
            long ownerId, LocalDateTime now1, LocalDateTime now2);

    List<Booking> findByItemOwnerIdAndEndTimeBookingLessThanOrderByStartTimeBookingDesc(
            long ownerId, LocalDateTime now);

    List<Booking> findByItemOwnerIdAndStartTimeBookingGreaterThanOrderByStartTimeBookingDesc(
            long ownerId, LocalDateTime now);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartTimeBookingDesc(
            long ownerId, Status status);


    // --- EXISTS finished booking for comments ---
    boolean existsByItemIdAndBookerIdAndStatusAndEndTimeBookingLessThan(
            Long itemId, Long bookerId, Status status, LocalDateTime now);


    // --- LAST / NEXT approved ---
    Optional<Booking> findFirstByItemIdAndStatusAndStartTimeBookingLessThanEqualOrderByStartTimeBookingDesc(
            Long itemId, Status status, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndStartTimeBookingGreaterThanOrderByStartTimeBookingAsc(
            Long itemId, Status status, LocalDateTime now);

    @EntityGraph(attributePaths = "item")
    List<Booking> findByItemIdInAndStatusAndStartTimeBookingLessThanEqualOrderByItemIdAscStartTimeBookingDesc(
            List<Long> itemIds, Status status, LocalDateTime now);

    @EntityGraph(attributePaths = "item")
    List<Booking> findByItemIdInAndStatusAndStartTimeBookingGreaterThanOrderByItemIdAscStartTimeBookingAsc(
            List<Long> itemIds, Status status, LocalDateTime now);
}



