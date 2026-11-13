package servicios;

import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import modelo.DetallePrestamo;
import modelo.Libro;
import modelo.Prestamo;

public class ServicioDetallePrestamo {

    private final ServicioLibros libros;
    private final ServicioPrestamos prestamos;
    private final Path archivo;

    public ServicioDetallePrestamo() {
        this(
                new ServicioLibros(),
                new ServicioPrestamos(),
                Path.of("datos", "detalles_prestamo.txt")
        );
    }

    /*
     * Este constructor permite usar archivos de prueba separados
     * de los datos reales.
     */
    public ServicioDetallePrestamo(
            ServicioLibros libros,
            ServicioPrestamos prestamos,
            Path archivo
    ) {
        this.libros = libros;
        this.prestamos = prestamos;
        this.archivo = archivo.toAbsolutePath();
    }

    public Libro buscarLibro(String id)
            throws PersistenciaException, ValidacionException {

        return libros.buscarPorId(id);
    }

    public List<DetallePrestamo> listarPorPrestamo(String id)
            throws IOException {

        List<DetallePrestamo> resultado = new ArrayList<>();

        for (DetallePrestamo detalle : leerTodos()) {
            if (detalle.getIdPrestamo().equals(id)) {
                resultado.add(detalle);
            }
        }

        return resultado;
    }

    public void agregar(String idPrestamo, String idLibro, int cantidad)
            throws IOException, PersistenciaException, ValidacionException {

        DetallePrestamo nuevo = new DetallePrestamo(
                idPrestamo,
                idLibro,
                cantidad
        );

        verificarPrestamoActivo(idPrestamo);

        List<DetallePrestamo> detalles = leerTodos();

        for (DetallePrestamo detalle : detalles) {
            boolean mismoPrestamo = detalle.getIdPrestamo().equals(idPrestamo);
            boolean mismoLibro = detalle.getIdLibro().equals(idLibro);

            if (mismoPrestamo && mismoLibro) {
                throw new IllegalArgumentException(
                        "Ese libro ya está en el préstamo."
                );
            }
        }

        // El servicio de libros comprueba la cantidad disponible.
        libros.prestarEjemplares(idLibro, cantidad);
        detalles.add(nuevo);

        try {
            guardarTodos(detalles);

        } catch (IOException error) {
            /*
             * Si no se pudo guardar el detalle, intentamos devolver
             * al inventario los ejemplares que acabamos de descontar.
             */
            try {
                libros.devolverEjemplares(idLibro, cantidad);

            } catch (PersistenciaException | ValidacionException falloReversion) {
                error.addSuppressed(falloReversion);

                throw new IOException(
                        "No se guardó el detalle y no se pudo revertir "
                        + "el stock. Revisa los archivos.",
                        error
                );
            }

            throw error;
        }
    }

    public void cambiarCantidad(String idPrestamo, String idLibro, int cantidad)
            throws IOException, PersistenciaException, ValidacionException {
        DetallePrestamo nuevo = new DetallePrestamo(idPrestamo, idLibro, cantidad);
        verificarPrestamoActivo(idPrestamo);
        List<DetallePrestamo> detalles = leerTodos();
        for (int i = 0; i < detalles.size(); i++) {
            DetallePrestamo anterior = detalles.get(i);
            if (!anterior.getIdPrestamo().equals(nuevo.getIdPrestamo())
                    || !anterior.getIdLibro().equals(nuevo.getIdLibro())) continue;
            int diferencia = cantidad - anterior.getCantidad();
            if (diferencia == 0) return;
            if (diferencia > 0) libros.prestarEjemplares(idLibro, diferencia);
            else libros.devolverEjemplares(idLibro, -diferencia);
            detalles.set(i, nuevo);
            try {
                guardarTodos(detalles);
            } catch (IOException error) {
                try {
                    if (diferencia > 0) libros.devolverEjemplares(idLibro, diferencia);
                    else libros.prestarEjemplares(idLibro, -diferencia);
                } catch (PersistenciaException | ValidacionException falloReversion) {
                    error.addSuppressed(falloReversion);
                    throw new IOException("No se guardó la cantidad ni se pudo corregir el inventario. Revisa los archivos.", error);
                }
                throw error;
            }
            return;
        }
        throw new IllegalArgumentException("No se encontró el detalle.");
    }

    // El llamador conserva la lista completa para restaurarla si falla el otro archivo.
    List<DetallePrestamo> retirarDetallesDePrestamo(String id) throws IOException {
        List<DetallePrestamo> anteriores = leerTodos();
        List<DetallePrestamo> restantes = new ArrayList<>(anteriores);
        restantes.removeIf(detalle -> detalle.getIdPrestamo().equals(id));
        if (restantes.size() != anteriores.size()) guardarTodos(restantes);
        return anteriores;
    }

    void restaurarDetalles(List<DetallePrestamo> detalles) throws IOException {
        guardarTodos(detalles);
    }

    public void quitar(String idPrestamo, String idLibro)
            throws IOException, PersistenciaException, ValidacionException {

        verificarPrestamoActivo(idPrestamo);

        List<DetallePrestamo> detalles = leerTodos();
        DetallePrestamo encontrado = null;

        for (DetallePrestamo detalle : detalles) {
            boolean mismoPrestamo = detalle.getIdPrestamo().equals(idPrestamo);
            boolean mismoLibro = detalle.getIdLibro().equals(idLibro);

            if (mismoPrestamo && mismoLibro) {
                encontrado = detalle;
                break;
            }
        }

        if (encontrado == null) {
            throw new IllegalArgumentException(
                    "No se encontró el detalle."
            );
        }

        // Devuelve los ejemplares al inventario.
        libros.devolverEjemplares(
                idLibro,
                encontrado.getCantidad()
        );

        detalles.remove(encontrado);

        try {
            guardarTodos(detalles);

        } catch (IOException error) {
            /*
             * Si no se pudo quitar el detalle del archivo,
             * intentamos volver a descontar los ejemplares.
             */
            try {
                libros.prestarEjemplares(
                        idLibro,
                        encontrado.getCantidad()
                );

            } catch (PersistenciaException | ValidacionException falloReversion) {
                error.addSuppressed(falloReversion);

                throw new IOException(
                        "No se quitó el detalle y no se pudo revertir "
                        + "el stock. Revisa los archivos.",
                        error
                );
            }

            throw error;
        }
    }

    private void verificarPrestamoActivo(String id) throws IOException {
        for (Prestamo prestamo : prestamos.listar()) {
            if (prestamo.getId().equals(id)) {

                if (!prestamo.estaActivo()) {
                    throw new IllegalArgumentException(
                            "El préstamo ya fue devuelto."
                    );
                }

                return;
            }
        }

        throw new IllegalArgumentException(
                "No existe el préstamo " + id + "."
        );
    }

    /*
     * Cada línea tiene este formato:
     *
     * ID_PRESTAMO|ID_LIBRO|CANTIDAD
     *
     * Ejemplo:
     *
     * P00001|L00002|1
     */
    private List<DetallePrestamo> leerTodos() throws IOException {
        List<DetallePrestamo> detalles = new ArrayList<>();

        if (!Files.exists(archivo)) {
            return detalles;
        }

        List<String> lineas = Files.readAllLines(
                archivo,
                StandardCharsets.UTF_8
        );

        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);

            if (linea.isBlank()) {
                continue;
            }

            String[] partes = linea.split("\\|", -1);

            try {
                if (partes.length != 3) {
                    throw new IllegalArgumentException(
                            "Se esperaban tres campos."
                    );
                }

                DetallePrestamo detalle = new DetallePrestamo(
                        partes[0],
                        partes[1],
                        Integer.parseInt(partes[2])
                );

                // No se permite repetir el mismo libro en un préstamo.
                for (DetallePrestamo anterior : detalles) {
                    boolean mismoPrestamo = anterior.getIdPrestamo()
                            .equals(detalle.getIdPrestamo());

                    boolean mismoLibro = anterior.getIdLibro()
                            .equals(detalle.getIdLibro());

                    if (mismoPrestamo && mismoLibro) {
                        throw new IllegalArgumentException(
                                "Detalle duplicado."
                        );
                    }
                }

                detalles.add(detalle);

            } catch (IllegalArgumentException error) {
                throw new IOException(
                        "Detalle inválido en la línea " + (i + 1) + ".",
                        error
                );
            }
        }

        return detalles;
    }

    private void guardarTodos(List<DetallePrestamo> detalles)
            throws IOException {

        Files.createDirectories(archivo.getParent());

        Path temporal = Files.createTempFile(
                archivo.getParent(),
                "detalles-",
                ".tmp"
        );

        try {
            List<String> lineas = new ArrayList<>();

            for (DetallePrestamo detalle : detalles) {
                String linea = detalle.getIdPrestamo() + "|"
                        + detalle.getIdLibro() + "|"
                        + detalle.getCantidad();

                lineas.add(linea);
            }

            Files.write(
                    temporal,
                    lineas,
                    StandardCharsets.UTF_8
            );

            Files.move(
                    temporal,
                    archivo,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } finally {
            Files.deleteIfExists(temporal);
        }
    }
}
