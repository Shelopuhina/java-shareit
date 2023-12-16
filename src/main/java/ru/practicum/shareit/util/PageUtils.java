package ru.practicum.shareit.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exceptions.InvalidDataException;

public final class PageUtils {
    public static  Pageable getPageable(int from, int size) {
        if (from < 0 || size < 1) {
            throw new InvalidDataException("size и from поля должны соответсвовать значениям.");
        }
        int page = from / size;
        return PageRequest.of(page, size);
    }
}
