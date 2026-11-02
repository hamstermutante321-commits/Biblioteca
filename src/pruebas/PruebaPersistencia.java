package pruebas;

import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import java.util.ArrayList;
import java.util.List;
import persistencia.ArchivosDatos;

public class PruebaPersistencia {

    // Ejecuta este archivo con Shift + F6 en NetBeans.
    public static void main(String[] args) {
        ArchivosDatos archivos = new ArchivosDatos("datos-prueba");

        List<String> usuarios = new ArrayList<>();
        usuarios.add("U001|Ana María|12345678");
        usuarios.add("U002|José|87654321");

        try {
            archivos.guardarUsuarios(usuarios);
            System.out.println("Usuarios guardados en datos-prueba/usuarios.txt");

            List<String> recuperados = archivos.leerUsuarios();
            System.out.println("Usuarios leidos del TXT:");
            for (String linea : recuperados) {
                System.out.println(linea);
            }

            // El indice 0 corresponde al primer registro de la lista.
            recuperados.set(0, "U001|Ana Torres|12345678");
            archivos.guardarUsuarios(recuperados);

            System.out.println("Usuarios despues de editar y volver a leer:");
            for (String linea : archivos.leerUsuarios()) {
                System.out.println(linea);
            }
        } catch (PersistenciaException e) {
            System.out.println("Error con el TXT: " + e.getMessage());
        }

        // Ejemplo independiente. Esta regla ira en servicios cuando lo integren.
        try {
            String nombre = "";
            if (nombre.isBlank()) {
                throw new ValidacionException("El nombre es obligatorio.");
            }
            System.out.println("Nombre valido.");
        } catch (ValidacionException e) {
            System.out.println("Error de validacion: " + e.getMessage());
        }
    }
}
