/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author USER
 */
public abstract class Entidad {
    // Cada objeto conserva el mismo identificador durante toda su vida.
    private final String id;

    protected Entidad(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id es obligatorio.");
        }
        this.id = id.strip();
    }
    
    public String getId() {
        return id;
    }

    public abstract String getEtiqueta();
    
    @Override
    public String toString() {
        return id + "--" + getEtiqueta();
    }
    
}
