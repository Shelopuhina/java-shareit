package ru.practicum.shareit.item.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    List<Item> findAllByOwnerIdOrderByIdAsc(int userId, Pageable pageable);

    @Query(" select i from Item i " +
            "where lower(i.name) like lower(concat('%', ?1, '%')) " +
            " or lower(i.description) like lower(concat('%', ?1, '%')) " +
            "and i.available = TRUE")
    List<Item> search(String text, Pageable pageable);
    List<Item> findByRequestId(int requestId);
}