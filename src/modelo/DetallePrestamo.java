/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author USER
 */
public class DetallePrestamo {
    private final String idPrestamo;
    private final String idLibro;
    private final int cantidad;

    public DetallePrestamo(String idPrestamo, String idLibro, int cantidad) {
        if (idPrestamo == null || idPrestamo.isBlank()) {
            throw new IllegalArgumentException("El ID del préstamo es obligatorio.");
        }
        if (idLibro == null || idLibro.isBlank()) {
            throw new IllegalArgumentException("El ID del libro es obligatorio.");
        }
        if (idPrestamo.contains("|") || idPrestamo.contains("\n") || idPrestamo.contains("\r")
                || idLibro.contains("|") || idLibro.contains("\n") || idLibro.contains("\r")) {
            throw new IllegalArgumentException("Los IDs no pueden contener | ni saltos de línea.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        this.idPrestamo = idPrestamo.strip();
        this.idLibro = idLibro.strip();
        this.cantidad = cantidad;
    }

    public String getIdPrestamo() {
        return idPrestamo;
    }

    public String getIdLibro() {
        return idLibro;
    }

    public int getCantidad() {
        return cantidad;
    }
}
