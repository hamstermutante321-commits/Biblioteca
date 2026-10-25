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
    private String id;
    public Entidad(String id) {
        this.setId(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id es obligatorio.");
        }
        else {
            this.id = id;
        }
    }
    
    public abstract String getEtiqueta();
    
    @Override
    public String toString() {
        return id + "--" + getEtiqueta();
    }
    
}
