package ru.practicum.shareit.item;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.user.User;

@Getter
@Setter
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private String name;
    private String description;

    @Column(name = "use_count")
    private long useCount;

    @Column(name = "category")
    private String category;

    @Column(name = "available")
    private Boolean isAvailable;
}
