package cl.duoc.mesatech.catalogo.messaging;

public class PublicacionEventoException extends RuntimeException {

    public PublicacionEventoException(String message) {
        super(message);
    }

    public PublicacionEventoException(String message, Throwable cause) {
        super(message, cause);
    }
}
