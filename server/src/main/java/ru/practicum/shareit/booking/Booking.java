package ru.practicum.shareit.booking;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.dto.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "booker_id", nullable = false)
    private User booker;

    @NotNull
    @Column(name = "start_booking", nullable = false)
    private LocalDateTime startTimeBooking;

    @NotNull
    @Column(name = "end_booking", nullable = false)
    private LocalDateTime endTimeBooking;

    @Enumerated(EnumType.STRING)
    private Status status;
}
