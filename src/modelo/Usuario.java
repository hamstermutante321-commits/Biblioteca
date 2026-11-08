/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author USER
 */
public class Usuario {
    private final String id;

    private String nombre;
    private String dni;
    private String correo;

    public Usuario(String id, String nombre, String dni, String correo) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id es obligatorio.");
        }
        this.id = id.strip();
        setCorreo(correo);
        setDni(dni);
        setNombre(nombre);
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        this.nombre = nombre.strip();
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        if (dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("El DNI es obligatorio");
        }
        this.dni = dni.strip();
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }

        String correoLimpio = correo.strip();

        if (!correoLimpio.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("El correo debe tener un formato válido.");
        }

        this.correo = correoLimpio;
    }

    public String getEtiqueta() {
        return this.nombre;
    }

    @Override
    public String toString() {
        return id + "--" + getEtiqueta();
    }


}
