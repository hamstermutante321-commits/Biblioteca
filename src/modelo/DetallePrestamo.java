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
    // Id_prestamo, id_libro, cantidad
    private final String id;

    public DetallePrestamo(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id es obligatorio.");
        }
        this.id = id.strip();
    }

    public String getId() {
        return id;
    }
}
