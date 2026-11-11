
package servicios;

import excepciones.PersistenciaException;
import excepciones.ValidacionException;

public interface Crud<T> {
    void crear(T dato) throws ValidacionException, PersistenciaException;
    void actualizar(T dato) throws ValidacionException, PersistenciaException;
    void eliminar(String id) throws ValidacionException, PersistenciaException;
}
