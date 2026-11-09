package com.echostock.common.error;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduce todas las excepciones a cuerpos RFC 7807 ({@code application/problem+json})
 * con {@code type}, {@code title}, {@code status} y {@code detail}.
 *
 * <ul>
 *   <li>Cuerpo JSON bien formado pero con datos inválidos → 422.</li>
 *   <li>JSON ilegible o parámetros de consulta inválidos → 400.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final URI ABOUT_BLANK = URI.create("about:blank");

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException ex) {
        return problem(ex.getStatus(), ex.getType(), ex.getTitle(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detail = "Valor no válido para el parámetro '%s': '%s'".formatted(ex.getName(), ex.getValue());
        return problem(HttpStatus.BAD_REQUEST, ProblemTypes.BAD_REQUEST, "Petición no válida", detail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex) {
        log.error("Error no controlado", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, ProblemTypes.INTERNAL, "Error interno",
                "Se produjo un error inesperado en el servidor");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (detail.isEmpty()) {
            detail = ex.getBindingResult().getAllErrors().stream()
                    .map(MessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining("; "));
        }
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ProblemTypes.VALIDATION, "Datos no válidos", detail);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex,
            @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        return problem(HttpStatus.BAD_REQUEST, ProblemTypes.BAD_REQUEST, "Petición no válida",
                "El cuerpo de la petición no es JSON válido o contiene tipos incorrectos");
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            @NonNull MissingServletRequestParameterException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        return problem(HttpStatus.BAD_REQUEST, ProblemTypes.BAD_REQUEST, "Petición no válida",
                "Falta el parámetro obligatorio '%s'".formatted(ex.getParameterName()));
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            @NonNull HandlerMethodValidationException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        List<String> messages = ex.getParameterValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream()
                        .map(e -> r.getMethodParameter().getParameterName() + ": " + e.getDefaultMessage()))
                .toList();
        return problem(HttpStatus.BAD_REQUEST, ProblemTypes.BAD_REQUEST, "Petición no válida",
                String.join("; ", messages));
    }

    @Override
    protected ResponseEntity<Object> createResponseEntity(@Nullable Object body, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode statusCode, @NonNull WebRequest request) {
        // Resto de errores del framework (405, 406, 415, ruta inexistente...): garantizar un type propio.
        if (body instanceof ProblemDetail problem) {
            if (problem.getType() == null || ABOUT_BLANK.equals(problem.getType())) {
                problem.setType(ProblemTypes.forStatus(statusCode.value()));
            }
            problem.setInstance(null);
        }
        return super.createResponseEntity(body, headers, statusCode, request);
    }

    private static ResponseEntity<Object> problem(HttpStatus status, URI type, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(type);
        problem.setTitle(title);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem);
    }
}
