package servicios;

import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import modelo.Usuario;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Administra usuarios y sus archivos de datos y secuencia. */
public class ServicioUsuarios implements Crud<Usuario> {

    private static final String PREFIJO_ID = "U";
    private static final Object BLOQUEO = new Object();
    private final Path archivo;
    private final Path secuencia;

    public ServicioUsuarios() {
        this(Paths.get("datos", "usuarios.txt"));
    }

    /** Permite usar un archivo temporal o una carpeta de datos distinta. */
    public ServicioUsuarios(Path archivo) {
        this.archivo = Objects.requireNonNull(archivo, "El archivo de usuarios es obligatorio")
                .toAbsolutePath().normalize();
        this.secuencia = this.archivo.resolveSibling("usuarios-secuencia.txt");
    }

    /**
     * Registra un usuario con un ID nuevo (U00001, U00002, ...).
     * El ID queda reservado incluso si el usuario se elimina posteriormente.
     */
    public Usuario registrar(String nombre, String dni, String correo)
            throws ValidacionException, PersistenciaException {
        synchronized (BLOQUEO) {
            validarCampos(nombre, dni, correo);
            List<Usuario> usuarios = leer();
            asegurarDniDisponible(usuarios, dni, null);
            int siguiente = ultimoNumero(usuarios) + 1;
            if (siguiente > 99999) {
                throw new ValidacionException("Se agotaron los IDs de usuario U00001 a U99999.");
            }
            String id = String.format(Locale.ROOT, "%s%05d", PREFIJO_ID, siguiente);
            Usuario usuario = construir(id, nombre, dni, correo);
            guardarAtomico(secuencia, List.of(Integer.toString(siguiente)));
            usuarios.add(usuario);
            guardar(usuarios);
            return usuario;
        }
    }

    /** Busca por ID; lanza ValidacionException si no existe, igual que ServicioLibros. */
    public Usuario buscarPorId(String id) throws PersistenciaException, ValidacionException {
        synchronized (BLOQUEO) {
            if (id == null || id.isBlank()) {
                throw new ValidacionException("Indica un ID de usuario.");
            }
            for (Usuario usuario : leer()) {
                if (usuario.getId().equalsIgnoreCase(id.strip())) {
                    return usuario;
                }
            }
            throw new ValidacionException("No existe un usuario con ID " + id.strip() + ".");
        }
    }

    public Usuario buscarPorDni(String dni) throws PersistenciaException {
        if (dni == null || dni.isBlank()) {
            return null;
        }
        String dniBuscado = dni.strip();
        for (Usuario usuario : listar()) {
            if (usuario.getDni().equalsIgnoreCase(dniBuscado)) {
                return usuario;
            }
        }
        return null;
    }

    public List<Usuario> buscar(String filtro) throws PersistenciaException {
        String texto = filtro == null ? "" : filtro.strip().toLowerCase(Locale.ROOT);
        List<Usuario> coincidencias = new ArrayList<>();
        for (Usuario usuario : listar()) {
            if (usuario.getId().toLowerCase(Locale.ROOT).contains(texto)
                    || usuario.getNombre().toLowerCase(Locale.ROOT).contains(texto)
                    || usuario.getDni().toLowerCase(Locale.ROOT).contains(texto)
                    || usuario.getCorreo().toLowerCase(Locale.ROOT).contains(texto)) {
                coincidencias.add(usuario);
            }
        }
        return coincidencias;
    }

    public List<Usuario> listar() throws PersistenciaException {
        synchronized (BLOQUEO) {
            return leer();
        }
    }

    @Override
    public void crear(Usuario usuario) throws ValidacionException, PersistenciaException {
        Objects.requireNonNull(usuario, "El usuario es obligatorio");
        synchronized (BLOQUEO) {
            validarCampos(usuario.getNombre(), usuario.getDni(), usuario.getCorreo());
            int numero = leerNumeroId(usuario.getId());
            List<Usuario> usuarios = leer();
            for (Usuario existente : usuarios) {
                if (existente.getId().equalsIgnoreCase(usuario.getId())) {
                    throw new ValidacionException("Ya existe un usuario con ese ID.");
                }
            }
            asegurarDniDisponible(usuarios, usuario.getDni(), null);
            int ultimo = ultimoNumero(usuarios);
            if (numero <= ultimo) {
                throw new ValidacionException("El ID ya fue usado o está reservado: " + usuario.getId());
            }
            guardarAtomico(secuencia, List.of(Integer.toString(numero)));
            usuarios.add(usuario);
            guardar(usuarios);
        }
    }

    @Override
    public void actualizar(Usuario usuario) throws ValidacionException, PersistenciaException {
        Objects.requireNonNull(usuario, "El usuario es obligatorio");
        synchronized (BLOQUEO) {
            validarCampos(usuario.getNombre(), usuario.getDni(), usuario.getCorreo());
            List<Usuario> usuarios = leer();
            boolean encontrado = false;
            for (Usuario existente : usuarios) {
                if (existente.getId().equalsIgnoreCase(usuario.getId())) {
                    encontrado = true;
                } else if (existente.getDni().equalsIgnoreCase(usuario.getDni())) {
                    throw new ValidacionException("Ya existe otro usuario con ese DNI.");
                }
            }
            if (!encontrado) {
                throw new ValidacionException("No existe el usuario que se desea modificar.");
            }
            for (int i = 0; i < usuarios.size(); i++) {
                if (usuarios.get(i).getId().equalsIgnoreCase(usuario.getId())) {
                    usuarios.set(i, usuario);
                    break;
                }
            }
            guardar(usuarios);
        }
    }

    @Override
    public void eliminar(String id) throws ValidacionException, PersistenciaException {
        synchronized (BLOQUEO) {
            if (id == null || id.isBlank()) {
                throw new ValidacionException("Indica un ID de usuario.");
            }
            try {
                ServicioPrestamos prestamos = new ServicioPrestamos(archivo.resolveSibling("prestamos.txt"));
                for (modelo.Prestamo prestamo : prestamos.listar()) {
                    if (prestamo.getIdUsuario().equalsIgnoreCase(id.strip()) && prestamo.estaActivo()) {
                        throw new ValidacionException("No se puede eliminar un usuario con préstamos activos.");
                    }
                }
            } catch (IOException error) {
                throw new PersistenciaException("No se pudieron consultar los préstamos del usuario.", error);
            }
            List<Usuario> usuarios = leer();
            int ultimo = ultimoNumero(usuarios);
            boolean eliminado = usuarios.removeIf(usuario -> usuario.getId().equalsIgnoreCase(id.strip()));
            if (!eliminado) {
                throw new ValidacionException("No existe el usuario que se desea eliminar.");
            }
            // Mantiene reservado el ID más alto aunque se borre el último registro.
            guardarAtomico(secuencia, List.of(Integer.toString(ultimo)));
            guardar(usuarios);
        }
    }

    private int ultimoNumero(List<Usuario> usuarios) throws PersistenciaException {
        int mayor = 0;
        for (Usuario usuario : usuarios) {
            String id = usuario.getId();
            if (id.matches(PREFIJO_ID + "\\d{5}")) {
                mayor = Math.max(mayor, Integer.parseInt(id.substring(1)));
            }
        }
        if (Files.exists(secuencia)) {
            try {
                List<String> lineas = Files.readAllLines(secuencia, StandardCharsets.UTF_8);
                if (!lineas.isEmpty()) {
                    mayor = Math.max(mayor, Integer.parseInt(lineas.get(0).strip()));
                }
            } catch (IOException | NumberFormatException error) {
                throw new PersistenciaException("No se pudo leer la secuencia de IDs de usuarios.", error);
            }
        }
        return mayor;
    }

    private int leerNumeroId(String id) throws ValidacionException {
        if (id == null || !id.matches(PREFIJO_ID + "\\d{5}")) {
            throw new ValidacionException("El ID debe tener el formato U00001.");
        }
        int numero = Integer.parseInt(id.substring(1));
        if (numero < 1) {
            throw new ValidacionException("El ID de usuario debe ser mayor que cero.");
        }
        return numero;
    }

    private void asegurarDniDisponible(List<Usuario> usuarios, String dni, String idIgnorado)
            throws ValidacionException {
        for (Usuario existente : usuarios) {
            if (!existente.getId().equalsIgnoreCase(idIgnorado == null ? "" : idIgnorado)
                    && existente.getDni().equalsIgnoreCase(dni.strip())) {
                throw new ValidacionException("Ya existe un usuario con ese DNI.");
            }
        }
    }

    private Usuario construir(String id, String nombre, String dni, String correo)
            throws ValidacionException {
        try {
            return new Usuario(id, nombre, dni, correo);
        } catch (IllegalArgumentException error) {
            throw new ValidacionException(error.getMessage(), error);
        }
    }

    private List<Usuario> leer() throws PersistenciaException {
        List<Usuario> usuarios = new ArrayList<>();
        if (!Files.exists(archivo)) {
            return usuarios;
        }
        try {
            List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
            int numeroLinea = 0;
            for (String linea : lineas) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue;
                }
                String[] campos = linea.split("\\|", -1);
                if (campos.length != 4) {
                    throw new PersistenciaException("Formato inválido en usuarios.txt, línea " + numeroLinea + ".");
                }
                usuarios.add(construir(campos[0], campos[1], campos[2], campos[3]));
            }
            return usuarios;
        } catch (IOException error) {
            throw new PersistenciaException("No se pudo leer " + archivo + ".", error);
        } catch (ValidacionException error) {
            throw new PersistenciaException("Hay un registro inválido en " + archivo + ".", error);
        }
    }

    private void guardar(List<Usuario> usuarios) throws PersistenciaException {
        List<String> lineas = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            lineas.add(String.join("|", usuario.getId(), usuario.getNombre(),
                    usuario.getDni(), usuario.getCorreo()));
        }
        guardarAtomico(archivo, lineas);
    }

    private void guardarAtomico(Path destino, List<String> lineas) throws PersistenciaException {
        Path temporal = null;
        try {
            Path carpeta = destino.getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            Path carpetaTemporal = carpeta == null ? Paths.get(".").toAbsolutePath() : carpeta;
            temporal = Files.createTempFile(carpetaTemporal, "usuarios-", ".tmp");
            Files.write(temporal, lineas, StandardCharsets.UTF_8);
            try {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException error) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException error) {
            throw new PersistenciaException("No se pudo guardar " + destino + ".", error);
        } finally {
            if (temporal != null) {
                try {
                    Files.deleteIfExists(temporal);
                } catch (IOException ignored) {
                    // El archivo temporal queda fuera del catálogo si no se pudo borrar.
                }
            }
        }
    }

    private void validarCampos(String nombre, String dni, String correo) throws ValidacionException {
        validarCampo("nombre", nombre);
        validarCampo("DNI", dni);
        validarCampo("correo", correo);
        if (!nombre.strip().matches("[\\p{L}]+(?:[ '-][\\p{L}]+)*")) {
            throw new ValidacionException("El nombre solo puede contener letras, espacios, apóstrofes y guiones.");
        }
        if (!dni.strip().matches("[0-9]{8}")) {
            throw new ValidacionException("El DNI debe tener exactamente 8 dígitos numéricos.");
        }
        if (!correo.strip().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ValidacionException("El correo debe tener un formato válido.");
        }
    }

    private void validarCampo(String etiqueta, String valor) throws ValidacionException {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException("El " + etiqueta + " es obligatorio.");
        }
        if (valor.contains("|") || valor.contains("\n") || valor.contains("\r")) {
            throw new ValidacionException("El " + etiqueta + " no puede contener | ni saltos de línea.");
        }
    }
}
