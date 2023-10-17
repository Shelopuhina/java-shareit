package ru.practicum.shareit.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleDataExc(final NotFoundException e) {
        log.error(e.getMessage());
        return new ErrorResponse("Данные по выполняемому запросу отсутствуют.", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleDuplicationExc(final DuplicationEmailException e) {
        log.error(e.getMessage());
        return new ErrorResponse("Ошибка дублирования поля email пользователя.", e.getMessage());
    }

}