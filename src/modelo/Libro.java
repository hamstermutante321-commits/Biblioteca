/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;


public class Libro extends Entidad {
    private String titulo;
    private String autor;
    private int ejemplaresTotales;
    private int ejemplaresDisponibles;

    public Libro(String id, String titulo, String autor, int ejemplaresTotales, int ejemplaresDisponibles) {
        super(id);
        setTitulo(titulo);
        setAutor(autor);
        setEjemplaresTotales(ejemplaresTotales);
        setEjemplaresDisponibles(ejemplaresDisponibles);
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio.");
        }
        validarSeparadores(titulo);
        this.titulo = titulo.strip();
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        if (autor == null || autor.isBlank()) {
            throw new IllegalArgumentException("El autor es obligatorio.");
        }
        validarSeparadores(autor);
        this.autor = autor.strip();
    }

    public int getEjemplaresTotales() {
        return ejemplaresTotales;
    }

    public void setEjemplaresTotales(int ejemplaresTotales) {
        if (ejemplaresTotales < 0) {
            throw new IllegalArgumentException("El total de ejemplares no puede ser negativo.");
        }
        int prestados = this.ejemplaresTotales - this.ejemplaresDisponibles;
        if (ejemplaresTotales < prestados) {
            throw new IllegalArgumentException("El total no puede ser menor que los ejemplares prestados.");
        }
        this.ejemplaresTotales = ejemplaresTotales;
        this.ejemplaresDisponibles = ejemplaresTotales - prestados;
    }

    public int getEjemplaresDisponibles() {
        return ejemplaresDisponibles;
    }

    private void setEjemplaresDisponibles(int ejemplaresDisponibles) {
        if (ejemplaresDisponibles < 0 || ejemplaresDisponibles > this.ejemplaresTotales) {
            throw new IllegalArgumentException("Los ejemplares disponibles deben estar entre 0 y el total.");
        }
        this.ejemplaresDisponibles = ejemplaresDisponibles;
    }

    public void prestarEjemplares(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a prestar debe ser mayor que cero.");
        }
        if (cantidad > ejemplaresDisponibles) {
            throw new IllegalArgumentException("No hay suficientes ejemplares disponibles.");
        }
        setEjemplaresDisponibles(ejemplaresDisponibles - cantidad);
    }

    public void devolverEjemplares(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a devolver debe ser mayor que cero.");
        }
        if (cantidad > ejemplaresTotales - ejemplaresDisponibles) {
            throw new IllegalArgumentException("La devolución supera los ejemplares prestados.");
        }
        setEjemplaresDisponibles(ejemplaresDisponibles + cantidad);
    }

    @Override
    public String getEtiqueta() {
        return titulo;
    }

    private void validarSeparadores(String texto) {
        if (texto.indexOf('|') >= 0 || texto.indexOf('\n') >= 0 || texto.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("El texto no puede contener | ni saltos de línea.");
        }
    }

}
