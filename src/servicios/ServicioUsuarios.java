package servicios;

import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import modelo.Usuario;
import persistencia.ArchivosDatos;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Administra usuarios y su persistencia en datos/usuarios.txt. */
public class ServicioUsuarios implements Crud<Usuario> {

    private static final String PREFIJO_ID = "U";
    private final ArchivosDatos archivos;

    public ServicioUsuarios() {
        this(new ArchivosDatos());
    }

    /** Permite inyectar la carpeta de datos, por ejemplo en otra instalación. */
    public ServicioUsuarios(ArchivosDatos archivos) {
        this.archivos = Objects.requireNonNull(archivos, "El acceso a archivos es obligatorio");
    }

    /**
     * Registra un usuario y le asigna un ID nuevo (U00001, U00002, ...).
     * El ID asignado queda guardado como parte del usuario.
     */
    public synchronized Usuario registrar(String nombre, String dni, String correo)
            throws ValidacionException, PersistenciaException {
        validarCampos(nombre, dni, correo);
        if (buscarPorDni(dni) != null) {
            throw new ValidacionException("Ya existe un usuario con ese DNI.");
        }
        Usuario usuario = new Usuario(generarId(), nombre, dni, correo);
        crear(usuario);
        return usuario;
    }

    /** Devuelve null si el ID no está registrado, acuerdo usado por el módulo de préstamos. */
    public synchronized Usuario buscarPorId(String id) throws PersistenciaException {
        if (id == null || id.isBlank()) {
            return null;
        }
        for (Usuario usuario : listar()) {
            if (usuario.getId().equalsIgnoreCase(id.strip())) {
                return usuario;
            }
        }
        return null;
    }

    public synchronized Usuario buscarPorDni(String dni) throws PersistenciaException {
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

    public synchronized List<Usuario> buscar(String filtro) throws PersistenciaException {
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

    public synchronized List<Usuario> listar() throws PersistenciaException {
        List<Usuario> usuarios = new ArrayList<>();
        for (String linea : archivos.leerUsuarios()) {
            if (linea.isBlank()) {
                continue;
            }
            String[] campos = linea.split("\\|", -1);
            if (campos.length != 4) {
                throw new PersistenciaException("Registro de usuario inválido en usuarios.txt: " + linea);
            }
            try {
                usuarios.add(new Usuario(campos[0], campos[1], campos[2], campos[3]));
            } catch (IllegalArgumentException error) {
                throw new PersistenciaException("Registro de usuario inválido en usuarios.txt: " + linea, error);
            }
        }
        return usuarios;
    }

    @Override
    public synchronized void crear(Usuario usuario)
            throws ValidacionException, PersistenciaException {
        Objects.requireNonNull(usuario, "El usuario es obligatorio");
        validarCampos(usuario.getNombre(), usuario.getDni(), usuario.getCorreo());
        if (!usuario.getId().matches("U\\d{5,}")) {
            throw new ValidacionException("El ID del usuario debe tener el formato U00001.");
        }
        List<Usuario> usuarios = listar();
        for (Usuario existente : usuarios) {
            if (existente.getId().equalsIgnoreCase(usuario.getId())) {
                throw new ValidacionException("Ya existe un usuario con ese ID.");
            }
            if (existente.getDni().equalsIgnoreCase(usuario.getDni())) {
                throw new ValidacionException("Ya existe un usuario con ese DNI.");
            }
        }
        usuarios.add(usuario);
        guardar(usuarios);
    }

    @Override
    public synchronized void actualizar(Usuario usuario)
            throws ValidacionException, PersistenciaException {
        Objects.requireNonNull(usuario, "El usuario es obligatorio");
        validarCampos(usuario.getNombre(), usuario.getDni(), usuario.getCorreo());
        List<Usuario> usuarios = listar();
        boolean encontrado = false;
        for (int i = 0; i < usuarios.size(); i++) {
            Usuario existente = usuarios.get(i);
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

    @Override
    public synchronized void eliminar(String id)
            throws ValidacionException, PersistenciaException {
        if (id == null || id.isBlank()) {
            throw new ValidacionException("El ID del usuario es obligatorio.");
        }
        List<Usuario> usuarios = listar();
        boolean eliminado = usuarios.removeIf(usuario -> usuario.getId().equalsIgnoreCase(id.strip()));
        if (!eliminado) {
            throw new ValidacionException("No existe el usuario que se desea eliminar.");
        }
        guardar(usuarios);
    }

    private String generarId() throws PersistenciaException {
        int mayor = 0;
        for (Usuario usuario : listar()) {
            String id = usuario.getId();
            if (id.matches(PREFIJO_ID + "\\d{5,}")) {
                try {
                    mayor = Math.max(mayor, Integer.parseInt(id.substring(1)));
                } catch (NumberFormatException error) {
                    throw new PersistenciaException("El ID de usuario excede el límite permitido: " + id, error);
                }
            }
        }
        if (mayor == Integer.MAX_VALUE) {
            throw new PersistenciaException("Se agotaron los IDs disponibles para usuarios.");
        }
        return String.format(Locale.ROOT, "%s%05d", PREFIJO_ID, mayor + 1);
    }

    private void guardar(List<Usuario> usuarios) throws PersistenciaException {
        List<String> lineas = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            lineas.add(String.join("|", usuario.getId(), usuario.getNombre(),
                    usuario.getDni(), usuario.getCorreo()));
        }
        archivos.guardarUsuarios(lineas);
    }

    private void validarCampos(String nombre, String dni, String correo) throws ValidacionException {
        validarCampo("nombre", nombre);
        validarCampo("DNI", dni);
        validarCampo("correo", correo);
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
