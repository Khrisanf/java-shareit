package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
            select c from Comment c
            join fetch c.author
            where c.item.id = :itemId
            order by c.created desc
            """)
    List<Comment> findItemComments(@Param("itemId") Long itemId);

    @Query("""
            select c from Comment c
            join fetch c.author
            where c.item.id in :itemIds
            order by c.created desc
            """)
    List<Comment> findCommentsForItems(@Param("itemIds") List<Long> itemIds);
}
