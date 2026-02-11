package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOwnerId(Long ownerId);

    @Query("""
                select i
                from Item i
                where i.isAvailable = true
                  and (lower(i.name) like lower(concat('%', :text, '%'))
                       or lower(i.description) like lower(concat('%', :text, '%')))
            """)
    List<Item> searchAvailableByText(@Param("text") String text);

    List<Item> findAllByItemRequest_Id(Long requestId);

    List<Item> findAllByItemRequest_IdIn(Collection<Long> requestIds);
}
