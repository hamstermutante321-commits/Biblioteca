/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.time.LocalDate;

public class Prestamo extends Entidad {
    private final String idUsuario;
    private final LocalDate fechaPrestamo;
    private final LocalDate fechaVencimiento;
    private LocalDate fechaDevolucion;

    public Prestamo(String id, String idUsuario, LocalDate fechaPrestamo,
                    LocalDate fechaVencimiento) {
        this(id, idUsuario, fechaPrestamo, fechaVencimiento, null);
    }

    public Prestamo(String id, String idUsuario, LocalDate fechaPrestamo,
                    LocalDate fechaVencimiento, LocalDate fechaDevolucion) {
        super(id);
        if (idUsuario == null || idUsuario.isBlank()) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio.");
        }
        if (fechaPrestamo == null || fechaVencimiento == null) {
            throw new IllegalArgumentException("Las fechas del préstamo son obligatorias.");
        }
        if (!fechaVencimiento.isAfter(fechaPrestamo)) {
            throw new IllegalArgumentException("El vencimiento debe ser posterior al préstamo.");
        }
        if (fechaDevolucion != null && fechaDevolucion.isBefore(fechaPrestamo)) {
            throw new IllegalArgumentException("La devolución no puede ser anterior al préstamo.");
        }
        this.idUsuario = idUsuario.strip();
        this.fechaPrestamo = fechaPrestamo;
        this.fechaVencimiento = fechaVencimiento;
        this.fechaDevolucion = fechaDevolucion;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public boolean estaActivo() {
        return fechaDevolucion == null;
    }

    public boolean estaVencido(LocalDate fechaActual) {
        if (fechaActual == null) {
            throw new IllegalArgumentException("La fecha de consulta es obligatoria.");
        }
        return estaActivo() && fechaActual.isAfter(fechaVencimiento);
    }

    public void registrarDevolucion(LocalDate fechaDevolucion) {
        if (!estaActivo()) {
            throw new IllegalStateException("El préstamo ya fue devuelto.");
        }
        if (fechaDevolucion == null || fechaDevolucion.isBefore(fechaPrestamo)) {
            throw new IllegalArgumentException("La fecha de devolución no es válida.");
        }
        this.fechaDevolucion = fechaDevolucion;
    }

    @Override
    public String getEtiqueta() {
        return "Préstamo del usuario " + idUsuario;
    }
}
