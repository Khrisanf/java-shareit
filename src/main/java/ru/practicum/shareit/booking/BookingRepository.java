package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
                select b
                from Booking b
                where b.booker.id = :userId
                  and (
                      :state = 'ALL'
                      or (:state = 'CURRENT' and b.startBooking <= :now and b.endBooking >= :now)
                      or (:state = 'PAST'    and b.endBooking < :now)
                      or (:state = 'FUTURE'  and b.startBooking > :now)
                      or (:state = 'WAITING' and b.status = 'WAITING')
                      or (:state = 'REJECTED'and b.status = 'REJECTED')
                  )
                order by b.startBooking desc
            """)
    List<Booking> findAllForBooker(@Param("userId") long userId,
                                   @Param("state") String state,
                                   @Param("now") LocalDateTime now);

    @Query("""
                select b
                from Booking b
                where b.item.owner.id = :userId
                  and (
                      :state = 'ALL'
                      or (:state = 'CURRENT' and b.startBooking <= :now and b.endBooking >= :now)
                      or (:state = 'PAST'    and b.endBooking < :now)
                      or (:state = 'FUTURE'  and b.startBooking > :now)
                      or (:state = 'WAITING' and b.status = 'WAITING')
                      or (:state = 'REJECTED'and b.status = 'REJECTED')
                  )
                order by b.startBooking desc
            """)
    List<Booking> findAllForOwner(@Param("userId") long userId,
                                  @Param("state") String state,
                                  @Param("now") LocalDateTime now);
}

