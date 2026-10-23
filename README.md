<div align="center">

# 📚 Sistema de Biblioteca

### Gestión de usuarios, libros y préstamos

<img src="https://img.shields.io/badge/Java-17-1565C0?style=for-the-badge&logo=openjdk&logoColor=white">
<img src="https://img.shields.io/badge/Swing-Interfaz%20gráfica-00897B?style=for-the-badge">
<img src="https://img.shields.io/badge/Estado-En%20desarrollo-F9A825?style=for-the-badge">



<br><br>

Aplicación de escritorio para administrar usuarios, libros y préstamos de una biblioteca.

</div>

---

## Descripción

Este proyecto consiste en una aplicación desarrollada en Java para organizar la información de una biblioteca.

El sistema permite registrar usuarios, controlar los libros disponibles y administrar los préstamos realizados.

También permite consultar la fecha de vencimiento de cada préstamo y conocer qué libros se encuentran disponibles.

---

## Funcionalidades

| Módulo | Descripción |
|---|---|
| 👤 Usuarios | Registrar, buscar, editar y eliminar usuarios |
| 📚 Libros | Administrar títulos, autores y ejemplares |
| 🔄 Préstamos | Registrar libros prestados a cada usuario |
| 📅 Vencimientos | Consultar fechas límite de devolución |
| 📦 Disponibilidad | Controlar los ejemplares disponibles |
| 🔎 Búsqueda | Buscar usuarios, libros y préstamos |

---

## Entidades del sistema

```text
Usuario
   │
   └── Préstamo
          │
          └── DetallePrestamo
                         │
                         └── Libro
Usuario: representa un libro registrado en el catalogo y contiene su cantidad disponible.
Préstamo: relaciona a un usuario con uno o varios libros determinados que el usuario eligio.
DetallePrestamo: representa cada libro incluido en el prestamo.
```
Se usará:
Java 17 o superior.
Java Swing.
Programación Orientada a Objetos.
Apache NetBeans.
Git y GitHub.

## Organización del proyecto

```text
src/
├── modelo/
│   ├── Entidad.java
│   ├── Usuario.java
│   ├── Libro.java
│   ├── Prestamo.java
│   └── DetallePrestamo.java
│
├── servicios/
│   ├── Crud.java
│   ├── ServicioUsuarios.java
│   ├── ServicioLibros.java
│   └── ServicioPrestamos.java
│
├── persistencia/
│   └── ArchivosDatos.java
│
├── excepciones/
│   ├── ValidacionException.java
│   └── PersistenciaException.java
│
└── vista/
    ├── VentanaPrincipal.java
    └── Estilo.java
```


## COMO FUNCIONA UN PRESTAMO?
1. Se selecciona un usuario.
2. Se selecciona uno o varios libros.
3. El sistema verifica que existan ejemplares disponibles.
4. Se registra la fecha del préstamo.
5. Se calcula la fecha de vencimiento según una fecha que considere razonable.
6. Se guarda la información en una lista.
7. Se reduce la cantidad disponible del libro.

Una vez devuelto el libro la cantidad disponible aumenta.

## REGLAS
- Un usuario no puede registrarse dos veces con el mismo documento.
- No se puede prestar un libro sin ejemplares disponibles.
- La fecha de vencimiento debe ser posterior a la fecha del préstamo.
- Los préstamos vencidos deben identificarse claramente.
- Un libro devuelto vuelve a estar disponible.
- No se puede eliminar un usuario que tenga préstamos activos.

## Persistencia de datos
Esta primera versión generara los siguientes txt:
datos/usuarios.txt
datos/libros.txt
datos/prestamos.txt
datos/detalles_prestamo.txt
Esto es más que nada para conservar los datos aún cuando se cierre la app.
