/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servicios;

/**
 *
 * @author USER
 */
public class SistemaBiblioteca {
    private final ServicioLibros servicioLibros;

    public SistemaBiblioteca() {
        this(new ServicioLibros());
    }

    public SistemaBiblioteca(ServicioLibros servicioLibros) {
        this.servicioLibros = java.util.Objects.requireNonNull(servicioLibros);
    }

    public ServicioLibros getServicioLibros() {
        return servicioLibros;
    }
}
