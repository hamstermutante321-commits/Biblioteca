package servicios;

import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import modelo.Libro;
import modelo.DetallePrestamo;
import modelo.Usuario;
import modelo.Prestamo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Guarda los préstamos como id|usuario|préstamo|vencimiento|devolución. */
public class ServicioPrestamos {
    private static final int DIAS_PRESTAMO = 14;
    private final Path archivo;

    public ServicioPrestamos() {
        this(Path.of("datos", "prestamos.txt"));
    }

    // Permite probar el servicio con un archivo independiente.
    public ServicioPrestamos(Path archivo) {
        this.archivo = archivo;
    }

    public List<Prestamo> listar() throws IOException {
        List<Prestamo> prestamos = new ArrayList<>();
        if (!Files.exists(archivo)) return prestamos;
        try (BufferedReader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            String linea;
            int numeroLinea = 0;
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank()) continue;
                String[] campos = linea.split("\\|", -1);
                try {
                    if (campos.length != 5) throw new IllegalArgumentException("Se esperaban cinco campos");
                    prestamos.add(new Prestamo(campos[0], campos[1], LocalDate.parse(campos[2]),
                            LocalDate.parse(campos[3]), campos[4].isBlank() ? null : LocalDate.parse(campos[4])));
                } catch (RuntimeException ex) {
                    throw new IOException("Datos inválidos en la línea " + numeroLinea + " de " + archivo, ex);
                }
            }
        }
        return prestamos;
    }

    public String generarId() throws IOException {
        int mayor = 0;
        Path secuencia = archivo.toAbsolutePath().resolveSibling("prestamos-secuencia.txt");
        if (Files.exists(secuencia)) {
            try {
                mayor = Integer.parseInt(Files.readString(secuencia, StandardCharsets.UTF_8).trim());
                if (mayor < 0) throw new NumberFormatException();
            } catch (NumberFormatException error) {
                throw new IOException("La secuencia de préstamos no es válida.", error);
            }
        }
        for (Prestamo prestamo : listar()) {
            String id = prestamo.getId();
            try {
                if (!id.startsWith("P")) throw new NumberFormatException();
                int numero = Integer.parseInt(id.substring(1));
                if (numero < 1) throw new NumberFormatException();
                if (numero > mayor) {
                    mayor = numero;
                }
            } catch (NumberFormatException ex) {
                throw new IOException("El ID del préstamo no es válido: " + id, ex);
            }
        }
        if (mayor == Integer.MAX_VALUE) throw new IOException("No quedan identificadores disponibles.");
        return String.format("P%05d", mayor + 1);
    }

    public Prestamo crear(String idUsuario, String idLibro, LocalDate fechaPrestamo)
            throws IOException, PersistenciaException, ValidacionException {
        Path carpeta = archivo.toAbsolutePath().getParent();
        ServicioUsuarios usuarios = new ServicioUsuarios(carpeta.resolve("usuarios.txt"));
        ServicioLibros libros = new ServicioLibros(carpeta.resolve("libros.txt"));
        ServicioDetallePrestamo detalles = new ServicioDetallePrestamo(
                libros, this, carpeta.resolve("detalles_prestamo.txt"));

        Usuario usuario = usuarios.buscarPorId(idUsuario);
        Libro libro = libros.buscarPorId(idLibro);
        if (libro.getEjemplaresDisponibles() < 1) {
            throw new ValidacionException("No hay ejemplares disponibles de este libro.");
        }

        Prestamo prestamo = crearCabecera(usuario.getId(), fechaPrestamo);
        try {
            detalles.agregar(prestamo.getId(), libro.getId(), 1);
        } catch (IOException | PersistenciaException | ValidacionException | IllegalArgumentException error) {
            // Si no se guardó el detalle, retiramos la cabecera recién creada.
            try {
                List<Prestamo> prestamos = listar();
                prestamos.removeIf(p -> p.getId().equals(prestamo.getId()));
                guardarTodos(prestamos);
            } catch (IOException errorAlRevertir) {
                throw new IOException("No se completó el préstamo ni se pudo retirar su cabecera. Revisa los datos.", errorAlRevertir);
            }
            throw error;
        }
        return prestamo;
    }

    private Prestamo crearCabecera(String idUsuario, LocalDate fechaPrestamo) throws IOException {
        if (idUsuario != null && (idUsuario.contains("|") || idUsuario.contains("\n") || idUsuario.contains("\r")))
            throw new IllegalArgumentException("El ID de usuario no puede contener | ni saltos de línea.");
        if (fechaPrestamo == null) throw new IllegalArgumentException("La fecha del préstamo es obligatoria.");
        LocalDate fechaVencimiento = fechaPrestamo.plusDays(DIAS_PRESTAMO);
        String id = generarId();
        Prestamo prestamo = new Prestamo(id, idUsuario, fechaPrestamo, fechaVencimiento);
        guardarSecuencia(id);
        List<Prestamo> prestamos = listar();
        prestamos.add(prestamo);
        guardarTodos(prestamos);
        return prestamo;
    }

    private void guardarSecuencia(String id) throws IOException {
        Path destino = archivo.toAbsolutePath().resolveSibling("prestamos-secuencia.txt");
        Files.createDirectories(destino.getParent());
        Path temporal = Files.createTempFile(destino.getParent(), "secuencia-prestamos-", ".tmp");
        try {
            Files.writeString(temporal, id.substring(1), StandardCharsets.UTF_8);
            Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporal);
        }
    }

    public void cambiarUsuario(String id, String nuevoIdUsuario)
            throws IOException, PersistenciaException, ValidacionException {
        List<Prestamo> prestamos = listar();
        for (int i = 0; i < prestamos.size(); i++) {
            Prestamo anterior = prestamos.get(i);
            if (!anterior.getId().equals(id)) continue;
            if (!anterior.estaActivo()) throw new IllegalStateException("Solo se puede editar un préstamo activo.");
            Path carpeta = archivo.toAbsolutePath().getParent();
            Usuario usuario = new ServicioUsuarios(carpeta.resolve("usuarios.txt")).buscarPorId(nuevoIdUsuario);
            prestamos.set(i, new Prestamo(anterior.getId(), usuario.getId(),
                    anterior.getFechaPrestamo(), anterior.getFechaVencimiento()));
            guardarTodos(prestamos);
            return;
        }
        throw new IllegalArgumentException("No se encontró el préstamo " + id + ".");
    }

    public void eliminar(String id) throws IOException {
        List<Prestamo> prestamos = listar();
        Prestamo encontrado = null;
        for (Prestamo prestamo : prestamos) {
            if (prestamo.getId().equals(id)) { encontrado = prestamo; break; }
        }
        if (encontrado == null) throw new IllegalArgumentException("No se encontró el préstamo " + id + ".");
        if (encontrado.estaActivo()) throw new IllegalStateException("Primero registra la devolución del préstamo.");
        // Reservar el siguiente número evita reutilizar IDs de datos anteriores a la secuencia.
        guardarSecuencia(generarId());
        Path carpeta = archivo.toAbsolutePath().getParent();
        ServicioDetallePrestamo detalles = new ServicioDetallePrestamo(
                new ServicioLibros(carpeta.resolve("libros.txt")), this, carpeta.resolve("detalles_prestamo.txt"));
        List<DetallePrestamo> anteriores = detalles.retirarDetallesDePrestamo(id);
        prestamos.remove(encontrado);
        try {
            guardarTodos(prestamos);
        } catch (IOException error) {
            try { detalles.restaurarDetalles(anteriores); }
            catch (IOException falloReversion) {
                error.addSuppressed(falloReversion);
                throw new IOException("No se eliminó el préstamo ni se pudo restaurar su detalle. Revisa los archivos.", error);
            }
            throw error;
        }
    }

    public void registrarDevolucion(String id, LocalDate fecha)
            throws IOException, PersistenciaException, ValidacionException {
        List<Prestamo> prestamos = listar();
        for (Prestamo prestamo : prestamos) {
            if (prestamo.getId().equals(id)) {
                // Primero se valida la fecha y que el préstamo siga activo.
                prestamo.registrarDevolucion(fecha);

                Path carpeta = archivo.toAbsolutePath().getParent();
                ServicioLibros libros = new ServicioLibros(carpeta.resolve("libros.txt"));
                ServicioDetallePrestamo servicioDetalles = new ServicioDetallePrestamo(
                        libros, this, carpeta.resolve("detalles_prestamo.txt"));
                List<DetallePrestamo> detalles = servicioDetalles.listarPorPrestamo(id);
                List<DetallePrestamo> devueltos = new ArrayList<>();

                try {
                    for (DetallePrestamo detalle : detalles) {
                        libros.devolverEjemplares(detalle.getIdLibro(), detalle.getCantidad());
                        devueltos.add(detalle);
                    }
                    // Los detalles permanecen guardados como historial.
                    guardarTodos(prestamos);
                } catch (IOException | PersistenciaException | ValidacionException error) {
                    // Si falla la devolución, volvemos a descontar lo ya devuelto.
                    try {
                        for (DetallePrestamo detalle : devueltos) {
                            libros.prestarEjemplares(detalle.getIdLibro(), detalle.getCantidad());
                        }
                    } catch (PersistenciaException | ValidacionException errorAlRevertir) {
                        throw new IOException("No se completó la devolución ni se pudo corregir el inventario. Revisa los datos.", errorAlRevertir);
                    }
                    throw error;
                }
                return;
            }
        }
        throw new IllegalArgumentException("No se encontró el préstamo " + id);
    }

    private void guardarTodos(List<Prestamo> prestamos) throws IOException {
        Path destino = archivo.toAbsolutePath();
        Files.createDirectories(destino.getParent());
        Path temporal = Files.createTempFile(destino.getParent(), "prestamos-", ".tmp");
        try {
            try (BufferedWriter escritor = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8)) {
                for (Prestamo p : prestamos) {
                    escritor.write(p.getId() + "|" + p.getIdUsuario() + "|" + p.getFechaPrestamo() + "|"
                            + p.getFechaVencimiento() + "|" + (p.getFechaDevolucion() == null ? "" : p.getFechaDevolucion()));
                    escritor.newLine();
                }
            }
            Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporal);
        }
    }
}
