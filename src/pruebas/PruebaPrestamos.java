package pruebas;

import excepciones.ValidacionException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import modelo.Libro;
import modelo.Prestamo;
import modelo.Usuario;
import servicios.ServicioDetallePrestamo;
import servicios.ServicioLibros;
import servicios.ServicioPrestamos;
import servicios.ServicioUsuarios;

public class PruebaPrestamos {
    public static void main(String[] args) throws Exception {
        Path carpeta = Files.createTempDirectory("prueba-prestamos-");
        ServicioUsuarios usuarios = new ServicioUsuarios(carpeta.resolve("usuarios.txt"));
        ServicioLibros libros = new ServicioLibros(carpeta.resolve("libros.txt"));
        ServicioPrestamos prestamos = new ServicioPrestamos(carpeta.resolve("prestamos.txt"));
        ServicioDetallePrestamo detalles = new ServicioDetallePrestamo(
                libros, prestamos, carpeta.resolve("detalles_prestamo.txt"));

        Usuario usuario = usuarios.registrar("Ana", "12345678", "ana@correo.com");
        Libro libro = libros.registrar("El principito", "Antoine", 1);

        try {
            prestamos.crear("U99999", libro.getId(), LocalDate.now());
            throw new AssertionError("Se aceptó un usuario inexistente");
        } catch (ValidacionException esperado) {
            System.out.println("OK: usuario inexistente rechazado");
        }
        if (!prestamos.listar().isEmpty()) throw new AssertionError("Se guardó un préstamo inválido");

        Prestamo prestamo = prestamos.crear(usuario.getId(), libro.getId(), LocalDate.now());
        if (detalles.listarPorPrestamo(prestamo.getId()).size() != 1
                || libros.buscarPorId(libro.getId()).getEjemplaresDisponibles() != 0) {
            throw new AssertionError("No se guardó el detalle o no se descontó el libro");
        }
        try {
            prestamos.crear(usuario.getId(), libro.getId(), LocalDate.now());
            throw new AssertionError("Se prestó un libro sin ejemplares");
        } catch (ValidacionException esperado) {
            System.out.println("OK: no se presta un libro sin ejemplares");
        }
        if (prestamos.listar().size() != 1) throw new AssertionError("Se guardó un préstamo extra");
        System.out.println("OK: préstamo, detalle y stock guardados en " + carpeta);

        Libro segundoLibro = libros.registrar("Otro libro", "Otra autora", 2);
        detalles.agregar(prestamo.getId(), segundoLibro.getId(), 2);
        try {
            usuarios.eliminar(usuario.getId());
            throw new AssertionError("Se eliminó un usuario con préstamo activo");
        } catch (ValidacionException esperado) {
            System.out.println("OK: no se elimina un usuario con préstamo activo");
        }

        try {
            prestamos.registrarDevolucion(prestamo.getId(), LocalDate.now().minusDays(1));
            throw new AssertionError("Se aceptó una fecha de devolución anterior al préstamo");
        } catch (IllegalArgumentException esperado) {
            System.out.println("OK: fecha de devolución inválida rechazada");
        }
        if (libros.buscarPorId(libro.getId()).getEjemplaresDisponibles() != 0) {
            throw new AssertionError("Cambió el stock con una fecha inválida");
        }
        prestamos.registrarDevolucion(prestamo.getId(), LocalDate.now());
        if (prestamos.listar().get(0).estaActivo()
                || libros.buscarPorId(libro.getId()).getEjemplaresDisponibles() != 1
                || libros.buscarPorId(segundoLibro.getId()).getEjemplaresDisponibles() != 2
                || detalles.listarPorPrestamo(prestamo.getId()).size() != 2) {
            throw new AssertionError("No se devolvió el libro o se perdió el historial");
        }
        try {
            prestamos.registrarDevolucion(prestamo.getId(), LocalDate.now());
            throw new AssertionError("Se aceptó devolver el préstamo dos veces");
        } catch (IllegalStateException esperado) {
            System.out.println("OK: no se devuelve el mismo préstamo dos veces");
        }
        System.out.println("OK: devolución completa e historial conservado");
        usuarios.eliminar(usuario.getId());
        if (!usuarios.listar().isEmpty()) throw new AssertionError("No se eliminó el usuario");
        System.out.println("OK: se puede eliminar el usuario después de la devolución");
    }
}
