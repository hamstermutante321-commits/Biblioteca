
package excepciones;
// LO QUE SE BUSCARÁ en persistenciaexception:
//los problemas que podrian haber al leer o escribir los TXT

public class PersistenciaException extends Exception {
    public PersistenciaException(String mensaje) {
        super(mensaje);
    }
    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
