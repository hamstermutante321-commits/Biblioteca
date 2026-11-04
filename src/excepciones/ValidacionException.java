package excepciones;

/**
 * Indica que un dato o una operacion no cumple las reglas del sistema.
 * Por ejemplo: nombre vacio, documento repetido o libro sin ejemplares.
 */
public class ValidacionException extends Exception {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }

    public ValidacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
