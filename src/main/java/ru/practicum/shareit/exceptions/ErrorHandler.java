package ru.practicum.shareit.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleDataExc(final RuntimeException e) {
        log.error(e.getMessage());
        return new ErrorResponse("Данные по выполняемому запросу отсутствуют.", e.getMessage());
    }

    @ExceptionHandler({DuplicationEmailException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleDuplicationExc(final RuntimeException e) {
        log.error(e.getMessage());
        return new ErrorResponse("Ошибка дублирования поля email пользователя.", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleThrowable(final Throwable e) {
        log.info("500 {}", e.getMessage(), e);
        return new ErrorResponse("Сервер столкнулся с неожиданной ошибкой, которая помешала ему выполнить запрос.", e.getMessage());
    }

}