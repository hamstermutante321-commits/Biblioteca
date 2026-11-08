package servicios;

import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import modelo.Libro;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Catálogo de libros. Las operaciones releen el archivo para evitar listas obsoletas. */
public class ServicioLibros {
    private static final Object BLOQUEO = new Object();
    private final Path archivo;
    private final Path secuencia;

    public ServicioLibros() {
        this(Paths.get("datos", "libros.txt"));
    }

    /** Permite probar el servicio con una carpeta distinta de los datos reales. */
    public ServicioLibros(Path archivo) {
        this.archivo = Objects.requireNonNull(archivo).toAbsolutePath().normalize();
        this.secuencia = this.archivo.resolveSibling("libros-secuencia.txt");
    }

    public List<Libro> listar() throws PersistenciaException {
        synchronized (BLOQUEO) {
            return leer();
        }
    }

    public Libro buscarPorId(String id) throws PersistenciaException, ValidacionException {
        synchronized (BLOQUEO) {
            return encontrar(leer(), id);
        }
    }

    public List<Libro> buscar(String titulo) throws PersistenciaException {
        String filtro = titulo == null ? "" : titulo.strip().toLowerCase(Locale.ROOT);
        List<Libro> resultado = new ArrayList<>();
        for (Libro libro : listar()) {
            if (libro.getTitulo().toLowerCase(Locale.ROOT).contains(filtro)) resultado.add(libro);
        }
        return resultado;
    }

    public Libro registrar(String titulo, String autor, int total)
            throws PersistenciaException, ValidacionException {
        synchronized (BLOQUEO) {
            List<Libro> libros = leer();
            long siguiente = ultimoNumero(libros) + 1;
            if (siguiente > 99999) throw new ValidacionException("Se agotaron los IDs L00001 a L99999.");
            Libro nuevo = construir(String.format(Locale.ROOT, "L%05d", siguiente), titulo, autor, total, total);
            // Reservar primero evita reutilizar un ID si falla después el guardado del catálogo.
            escribirAtomico(secuencia, Collections.singletonList(Long.toString(siguiente)));
            libros.add(nuevo);
            guardar(libros);
            return nuevo;
        }
    }

    public void modificar(String id, String titulo, String autor, int total)
            throws PersistenciaException, ValidacionException {
        synchronized (BLOQUEO) {
            List<Libro> libros = leer();
            Libro anterior = encontrar(libros, id);
            int prestados = anterior.getEjemplaresTotales() - anterior.getEjemplaresDisponibles();
            if (total < prestados) throw new ValidacionException("El total no puede ser menor que los ejemplares prestados: " + prestados);
            Libro actualizado = construir(anterior.getId(), titulo, autor, total, total - prestados);
            libros.set(libros.indexOf(anterior), actualizado);
            guardar(libros);
        }
    }

    public void eliminar(String id) throws PersistenciaException, ValidacionException {
        synchronized (BLOQUEO) {
            List<Libro> libros = leer();
            Libro libro = encontrar(libros, id);
            if (libro.getEjemplaresDisponibles() != libro.getEjemplaresTotales()) {
                throw new ValidacionException("No se puede eliminar un libro con ejemplares prestados.");
            }
            // Conservar el máximo antes de borrar también protege catálogos importados sin contador.
            escribirAtomico(secuencia, Collections.singletonList(Long.toString(ultimoNumero(libros))));
            libros.remove(libro);
            guardar(libros);
        }
    }

    public void prestarEjemplares(String id, int cantidad) throws PersistenciaException, ValidacionException {
        cambiarDisponibilidad(id, cantidad, false);
    }

    public void devolverEjemplares(String id, int cantidad) throws PersistenciaException, ValidacionException {
        cambiarDisponibilidad(id, cantidad, true);
    }

    private void cambiarDisponibilidad(String id, int cantidad, boolean devolver)
            throws PersistenciaException, ValidacionException {
        synchronized (BLOQUEO) {
            List<Libro> libros = leer();
            Libro libro = encontrar(libros, id);
            try {
                if (devolver) libro.devolverEjemplares(cantidad);
                else libro.prestarEjemplares(cantidad);
            } catch (IllegalArgumentException e) {
                throw new ValidacionException(e.getMessage(), e);
            }
            guardar(libros);
        }
    }

    private Libro construir(String id, String titulo, String autor, int total, int disponibles)
            throws ValidacionException {
        try {
            return new Libro(id, titulo, autor, total, disponibles);
        } catch (IllegalArgumentException e) {
            throw new ValidacionException(e.getMessage(), e);
        }
    }

    private Libro encontrar(List<Libro> libros, String id) throws ValidacionException {
        if (id == null || id.isBlank()) throw new ValidacionException("Indica un ID de libro.");
        for (Libro libro : libros) if (libro.getId().equals(id.strip())) return libro;
        throw new ValidacionException("No existe un libro con ID " + id.strip() + ".");
    }

    private List<Libro> leer() throws PersistenciaException {
        List<Libro> libros = new ArrayList<>();
        Path anterior = Paths.get("libros.txt").toAbsolutePath().normalize();
        boolean migrar = archivo.equals(Paths.get("datos", "libros.txt").toAbsolutePath().normalize())
                && !Files.exists(archivo) && Files.exists(anterior);
        Path fuente = migrar ? anterior : archivo;
        if (!Files.exists(fuente)) return libros;
        try {
            List<String> lineas = Files.readAllLines(fuente, StandardCharsets.UTF_8);
            Set<String> ids = new HashSet<>();
            for (int i = 0; i < lineas.size(); i++) {
                String linea = lineas.get(i);
                if (linea.isBlank()) continue;
                String[] campos = linea.split("\\|", -1);
                try {
                    if (campos.length != 5 || !campos[0].matches("L[0-9]{5}") || campos[0].equals("L00000")) {
                        throw new IllegalArgumentException("Se esperaban ID L00001 y cinco campos separados por |.");
                    }
                    Libro libro = new Libro(campos[0], campos[1], campos[2], Integer.parseInt(campos[3]), Integer.parseInt(campos[4]));
                    if (!ids.add(libro.getId())) throw new IllegalArgumentException("ID duplicado: " + libro.getId());
                    libros.add(libro);
                } catch (IllegalArgumentException e) {
                    throw new PersistenciaException("Registro inválido en " + fuente + ", línea " + (i + 1) + ": " + e.getMessage(), e);
                }
            }
            if (migrar) {
                escribirAtomico(secuencia, Collections.singletonList(Long.toString(ultimoNumero(libros))));
                guardar(libros);
            }
            return libros;
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer " + archivo, e);
        }
    }

    private long ultimoNumero(List<Libro> libros) throws PersistenciaException {
        long maximo = 0;
        for (Libro libro : libros) maximo = Math.max(maximo, Long.parseLong(libro.getId().substring(1)));
        if (Files.exists(secuencia)) {
            try {
                long guardado = Long.parseLong(Files.readString(secuencia, StandardCharsets.UTF_8).strip());
                if (guardado < 0 || guardado > 99999) throw new NumberFormatException();
                maximo = Math.max(maximo, guardado);
            } catch (IOException | NumberFormatException e) {
                throw new PersistenciaException("No se pudo leer el contador " + secuencia + ".", e);
            }
        }
        return maximo;
    }

    private void guardar(List<Libro> libros) throws PersistenciaException {
        List<String> lineas = new ArrayList<>();
        for (Libro libro : libros) {
            lineas.add(libro.getId() + "|" + libro.getTitulo() + "|" + libro.getAutor() + "|"
                    + libro.getEjemplaresTotales() + "|" + libro.getEjemplaresDisponibles());
        }
        escribirAtomico(archivo, lineas);
    }

    private void escribirAtomico(Path destino, List<String> lineas) throws PersistenciaException {
        Path temporal = null;
        try {
            Files.createDirectories(destino.getParent());
            temporal = Files.createTempFile(destino.getParent(), "libros-", ".tmp");
            Files.write(temporal, lineas, StandardCharsets.UTF_8);
            try {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo guardar " + destino + ".", e);
        } finally {
            if (temporal != null) {
                try { Files.deleteIfExists(temporal); }
                catch (IOException e) { temporal.toFile().deleteOnExit(); }
            }
        }
    }
}
