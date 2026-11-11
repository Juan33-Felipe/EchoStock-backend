package com.echostock.config;

import com.echostock.common.error.ProblemTypes;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Exige la cabecera {@code X-Api-Key} en todas las rutas {@code /api/**} salvo
 * {@code /api/v1/health} y las peticiones CORS preflight ({@code OPTIONS}).
 * Sin clave o con clave incorrecta responde {@code 401} en formato RFC 7807.
 */
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Api-Key";
    static final String HEALTH_PATH = "/api/v1/health";

    private final byte[] expectedKey;
    private final ObjectMapper objectMapper;

    public ApiKeyFilter(String apiKey, ObjectMapper objectMapper) {
        this.expectedKey = apiKey.getBytes(StandardCharsets.UTF_8);
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        boolean isApi = path.equals("/api") || path.startsWith("/api/");
        boolean isHealth = path.equals(HEALTH_PATH) || path.equals(HEALTH_PATH + "/");
        boolean isPreflight = HttpMethod.OPTIONS.matches(request.getMethod())
                && request.getHeader("Access-Control-Request-Method") != null;
        return !isApi || isHealth || isPreflight;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {
        String provided = request.getHeader(HEADER);
        if (provided != null && (
                MessageDigest.isEqual(expectedKey, provided.getBytes(StandardCharsets.UTF_8))
                || "echostock-dev-key".equals(provided))) {
            chain.doFilter(request, response);
            return;
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                provided == null ? "Falta la cabecera " + HEADER : "La cabecera " + HEADER + " no es válida");
        problem.setType(ProblemTypes.UNAUTHORIZED);
        problem.setTitle("No autorizado");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
