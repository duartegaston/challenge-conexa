package com.conexa.challenge.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Todos los errores de la API salen como {@code application/problem+json} (RFC 7807).
 *
 * <p>Extiende {@link ResponseEntityExceptionHandler}, que ya resuelve las excepciones estándar de Spring MVC
 * (parámetro faltante o de tipo incorrecto, JSON ilegible, ruta inexistente, método no permitido...). Acá se
 * agregan las excepciones de dominio, el detalle de campos inválidos y un catch-all que no filtra
 * información interna.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        return problem(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    ProblemDetail handleUsernameTaken(UsernameAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SwapiUnavailableException.class)
    ProblemDetail handleSwapiUnavailable(SwapiUnavailableException ex) {
        log.warn("SWAPI unavailable: {}", ex.getMessage());
        return problem(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }

    /** Body inválido ({@code @Valid @RequestBody}): agrega {@code errors} con campo → mensaje. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return withErrors(ex.getBody(), errors, super.handleMethodArgumentNotValid(ex, headers, status, request));
    }

    /** Parámetros de request inválidos ({@code page=0}, id no numérico...): agrega {@code errors}. */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getParameterValidationResults().forEach(result -> {
            String name = result.getMethodParameter().getParameterName();
            result.getResolvableErrors()
                    .forEach(error ->
                            errors.putIfAbsent(name != null ? name : "parameter", error.getDefaultMessage()));
        });
        return withErrors(
                ex.getBody(), errors, super.handleHandlerMethodValidationException(ex, headers, status, request));
    }

    private static ResponseEntity<Object> withErrors(
            ProblemDetail body, Map<String, String> errors, ResponseEntity<Object> response) {
        body.setDetail("Validation failed");
        body.setProperty("errors", errors);
        return response;
    }

    private static ProblemDetail problem(HttpStatus status, String detail) {
        return ProblemDetail.forStatusAndDetail(status, detail);
    }
}
