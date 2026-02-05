package ru.practicum.shareit.item.comment;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = "author")
    List<Comment> findByItemIdOrderByCreatedDesc(Long itemId);

    @EntityGraph(attributePaths = "author")
    List<Comment> findByItemIdInOrderByCreatedDesc(List<Long> itemIds);
}
