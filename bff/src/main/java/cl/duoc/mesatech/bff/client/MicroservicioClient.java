package cl.duoc.mesatech.bff.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MicroservicioClient {

    private final RestClient solicitudes;
    private final RestClient catalogo;

    public MicroservicioClient(
            @Value("${ms.solicitudes.url}") String solicitudesUrl,
            @Value("${ms.catalogo.url}") String catalogoUrl
    ) {
        this.solicitudes = RestClient.builder().baseUrl(solicitudesUrl).build();
        this.catalogo = RestClient.builder().baseUrl(catalogoUrl).build();
    }

    public ResponseEntity<Object> getSolicitudes(String path, String authorization) {
        return solicitudes.get()
                .uri(path)
                .header("Authorization", authorization)
                .retrieve()
                .toEntity(Object.class);
    }

    public ResponseEntity<Object> postSolicitudes(String path, String authorization, Object body) {
        return solicitudes.post()
                .uri(path)
                .header("Authorization", authorization)
                .body(body)
                .retrieve()
                .toEntity(Object.class);
    }

    public ResponseEntity<Object> patchSolicitudes(String path, String authorization, Object body) {
        return solicitudes.patch()
                .uri(path)
                .header("Authorization", authorization)
                .body(body)
                .retrieve()
                .toEntity(Object.class);
    }

    public ResponseEntity<Object> getCatalogo(String path, String authorization) {
        return catalogo.get()
                .uri(path)
                .header("Authorization", authorization)
                .retrieve()
                .toEntity(Object.class);
    }

    public ResponseEntity<Object> postCatalogo(String path, String authorization, Object body) {
        return catalogo.post()
                .uri(path)
                .header("Authorization", authorization)
                .body(body)
                .retrieve()
                .toEntity(Object.class);
    }
}
