package ru.practicum.shareit.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;


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
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicationExc(final RuntimeException e) {
        log.error(e.getMessage());
        return new ErrorResponse("Ошибка дублирования поля email пользователя.", e.getMessage());
    }

    @ExceptionHandler({BookingStateException.class, BookingStatusException.class, UnavailableItemException.class,
            BookingErrorException.class, BookingTimeException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleThrowable(final Throwable e) {
        log.info(e.getMessage());
        return new ErrorResponse(e.getMessage());
    }
}

