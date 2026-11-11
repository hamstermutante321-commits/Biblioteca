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
    }
}
