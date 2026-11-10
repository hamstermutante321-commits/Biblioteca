package pruebas;

import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import modelo.Libro;
import servicios.ServicioLibros;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** Ejecutar con java pruebas.PruebaServicioLibros. Solo usa carpetas temporales. */
public class PruebaServicioLibros {
    private static int comprobaciones;
    private interface Operacion { void ejecutar() throws Exception; }
    private static void comprobar(boolean condicion) {
        comprobaciones++;
        if (!condicion) throw new AssertionError("Falló la comprobación " + comprobaciones);
    }
    private static void esperarError(Class<? extends Exception> tipo, Operacion op) throws Exception {
        try { op.ejecutar(); }
        catch (Exception e) {
            if (!tipo.isInstance(e)) throw e;
            comprobaciones++;
            return;
        }
        throw new AssertionError("Se esperaba " + tipo.getSimpleName());
    }
    public static void main(String[] args) throws Exception {
        Path carpeta = Files.createTempDirectory("prueba-catalogo-");
        Path archivo = carpeta.resolve("libros.txt");
        ServicioLibros servicio = new ServicioLibros(archivo);
        comprobar(servicio.listar().isEmpty());
        Libro primero = servicio.registrar(" El principito ", " Saint-Exupéry ", 3);
        comprobar(primero.getId().equals("L00001"));
        comprobar(primero.getTitulo().equals("El principito"));
        comprobar(new ServicioLibros(archivo).buscarPorId("L00001").getAutor().equals("Saint-Exupéry"));
        comprobar(servicio.buscar("PRINCIPITO").size() == 1);
        comprobar(servicio.buscar("inexistente").isEmpty());
        esperarError(ValidacionException.class, () -> servicio.buscarPorId("L99999"));
        esperarError(ValidacionException.class, () -> servicio.registrar("", "Autor", 1));
        esperarError(ValidacionException.class, () -> servicio.registrar("Título|otro", "Autor", 1));
        esperarError(ValidacionException.class, () -> servicio.registrar("Título", "Autor", -1));
        servicio.prestarEjemplares("L00001", 2);
        comprobar(servicio.buscarPorId("L00001").getEjemplaresDisponibles() == 1);
        esperarError(ValidacionException.class, () -> servicio.prestarEjemplares("L00001", 2));
        esperarError(ValidacionException.class, () -> servicio.eliminar("L00001"));
        esperarError(ValidacionException.class, () -> servicio.modificar("L00001", "Título", "Autor", 1));
        servicio.modificar("L00001", "Nuevo título", "Autor", 5);
        comprobar(servicio.buscarPorId("L00001").getEjemplaresDisponibles() == 3);
        servicio.modificar("L00001", "Nuevo título", "Autor", 2);
        comprobar(servicio.buscarPorId("L00001").getEjemplaresDisponibles() == 0);
        servicio.devolverEjemplares("L00001", 2);
        esperarError(ValidacionException.class, () -> servicio.devolverEjemplares("L00001", 1));
        servicio.eliminar("L00001");
        comprobar(servicio.listar().isEmpty());
        comprobar(new ServicioLibros(archivo).registrar("Otro libro", "Autor", 0).getId().equals("L00002"));
        String contenido = Files.readString(archivo, StandardCharsets.UTF_8);
        comprobar(contenido.contains("L00002|Otro libro|Autor|0|0"));
        Files.writeString(archivo, "L00002|Libro|Autor|x|1", StandardCharsets.UTF_8);
        esperarError(PersistenciaException.class, () -> servicio.listar());
        esperarError(PersistenciaException.class, () -> servicio.registrar("Nuevo", "Autor", 1));
        comprobar(Files.readString(archivo).contains("|x|"));
        Files.writeString(archivo, "L00002|Uno|Autor|1|1\nL00002|Dos|Autor|1|1", StandardCharsets.UTF_8);
        esperarError(PersistenciaException.class, () -> servicio.listar());
        Files.writeString(archivo, contenido, StandardCharsets.UTF_8);
        Files.writeString(carpeta.resolve("libros-secuencia.txt"), "contador inválido", StandardCharsets.UTF_8);
        esperarError(PersistenciaException.class, () -> servicio.registrar("Nuevo", "Autor", 1));
        comprobar(Files.readString(archivo).equals(contenido));
        Path bloqueado = carpeta.resolve("no-es-carpeta");
        Files.writeString(bloqueado, "archivo");
        esperarError(PersistenciaException.class, () -> new ServicioLibros(bloqueado.resolve("libros.txt")).registrar("Libro", "Autor", 1));
        Libro modelo = new Libro("L00100", "Modelo", "Autor", 5, 3);
        modelo.setEjemplaresTotales(3);
        comprobar(modelo.getEjemplaresDisponibles() == 1);
        System.out.println("OK: " + comprobaciones + " comprobaciones. Datos de prueba en " + carpeta);
    }
}
