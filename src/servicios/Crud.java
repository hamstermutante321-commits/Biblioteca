
package servicios;


public interface Crud<T> {
    void crear (T dato);
    void actualizar(T dato);
    void eliminar(String id);
}
