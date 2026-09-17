package cl.duoc.mesatech.bff.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

/**
 * Reenvía el status y cuerpo del microservicio (ej. 409 de transiciones)
 * en lugar de convertirlo en 500 del BFF.
 */
@RestControllerAdvice
public class ProxyExceptionHandler {

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<byte[]> reenviar(RestClientResponseException ex) {
        HttpHeaders headers = new HttpHeaders();
        MediaType contentType = ex.getResponseHeaders() != null
                ? ex.getResponseHeaders().getContentType()
                : null;
        headers.setContentType(contentType != null ? contentType : MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(ex.getResponseBodyAsByteArray(), headers, ex.getStatusCode());
    }
}
