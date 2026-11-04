package servicios;

import modelo.Libro;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ServicioLibros {
    private final String RUTA_ARCHIVO = "libros.txt";

    // Generar el siguiente ID automáticamente (Ej. L00001)
    public String generarId() {
        List<Libro> libros = listar();
        if (libros.isEmpty()) {
            return "L00001";
        }
        Libro ultimoLibro = libros.get(libros.size() - 1);
        String ultimoId = ultimoLibro.getId(); // Ej: L00003
        try {
            int numero = Integer.parseInt(ultimoId.substring(1));
            numero++;
            return String.format("L%05d", numero);
        } catch (NumberFormatException e) {
            return "L00001";
        }
    }

    // Listar todos los libros desde el archivo de texto
    public List<Libro> listar() {
        List<Libro> libros = new ArrayList<>();
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists()) {
            return libros;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("\\|");
                if (partes.length == 5) {
                    Libro libro = new Libro(
                        partes[0],
                        partes[1],
                        partes[2],
                        Integer.parseInt(partes[3]),
                        Integer.parseInt(partes[4])
                    );
                    libros.add(libro);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
        return libros;
    }

    // Guardar un nuevo libro
    public void guardar(Libro libro) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            pw.println(libro.getId() + "|" + libro.getTitulo() + "|" + libro.getAutor() + "|" +
                       libro.getEjemplaresTotales() + "|" + libro.getEjemplaresDisponibles());
        } catch (IOException e) {
            System.err.println("Error al guardar el libro: " + e.getMessage());
        }
    }

    // Modificar un libro existente
    public void modificar(Libro libroModificado) {
        List<Libro> libros = listar();
        try (PrintWriter pw = new PrintWriter(new FileWriter(RUTA_ARCHIVO, false))) {
            for (Libro libro : libros) {
                if (libro.getId().equals(libroModificado.getId())) {
                    pw.println(libroModificado.getId() + "|" + libroModificado.getTitulo() + "|" + 
                               libroModificado.getAutor() + "|" + libroModificado.getEjemplaresTotales() + "|" + 
                               libroModificado.getEjemplaresDisponibles());
                } else {
                    pw.println(libro.getId() + "|" + libro.getTitulo() + "|" + libro.getAutor() + "|" + 
                               libro.getEjemplaresTotales() + "|" + libro.getEjemplaresDisponibles());
                }
            }
        } catch (IOException e) {
            System.err.println("Error al modificar el libro: " + e.getMessage());
        }
    }

    // Buscar libros por coincidencia parcial en el título
    public List<Libro> buscar(String filtroTitulo) {
        List<Libro> resultados = new ArrayList<>();
        List<Libro> todos = listar();
        String filtroLower = filtroTitulo.toLowerCase();

        for (Libro libro : todos) {
            if (libro.getTitulo().toLowerCase().contains(filtroLower)) {
                resultados.add(libro);
            }
        }
        return resultados;
    }
}
