# Biblioteca Virtual

Aplicación de escritorio desarrollada en Java para administrar usuarios, libros y préstamos de una biblioteca.

El proyecto aplica conceptos de Programación Orientada a Objetos, interfaces gráficas con Swing, separación entre modelo, servicios e interfaz, validaciones y almacenamiento local de información.

## Descripción

La aplicación permitirá registrar los usuarios de la biblioteca, administrar los libros disponibles y controlar los préstamos realizados.

Cada préstamo relaciona a un usuario con uno o varios libros. También se registrará la fecha en que se realiza el préstamo y la fecha límite para devolverlo.

El sistema deberá mostrar qué usuarios tienen libros prestados, qué libros están disponibles y qué préstamos están próximos a vencer o ya están atrasados.

## Funcionalidades principales

- Registrar usuarios.
- Consultar, editar y eliminar usuarios.
- Registrar libros.
- Consultar, editar y eliminar libros.
- Controlar la cantidad disponible de cada libro.
- Registrar préstamos.
- Asociar préstamos con usuarios.
- Asociar uno o varios libros a un préstamo.
- Registrar la fecha del préstamo.
- Registrar la fecha de vencimiento.
- Consultar préstamos activos.
- Identificar préstamos vencidos.
- Devolver libros al inventario disponible.
- Buscar usuarios, libros y préstamos.
- Mostrar la información mediante tablas y formularios Swing.

## Entidades principales

### Usuario

Representa a la persona que utiliza la biblioteca.

Datos principales:

- Identificador.
- Nombre completo.
- Documento de identidad.
- Correo electrónico.
- Teléfono.
- Estado del usuario.

### Libro

Representa un libro disponible en la biblioteca.

Datos principales:

- Identificador.
- Título.
- Autor.
- Categoría.
- ISBN.
- Año de publicación.
- Cantidad total.
- Cantidad disponible.

### Préstamo

Representa el préstamo realizado por un usuario.

Datos principales:

- Identificador.
- Usuario relacionado.
- Fecha del préstamo.
- Fecha de vencimiento.
- Fecha de devolución.
- Estado del préstamo.

Los estados posibles pueden ser:

- Activo.
- Devuelto.
- Vencido.

### DetallePrestamo

Representa cada libro incluido en un préstamo.

Datos principales:

- Identificador.
- Préstamo relacionado.
- Libro relacionado.
- Cantidad prestada.

Una misma operación puede contener varios libros. Por eso el préstamo funciona como la cabecera y `DetallePrestamo` contiene cada libro prestado.

## Organización del proyecto

```text
src/
├── app/
│   └── Main.java
├── modelo/
│   ├── Entidad.java
│   ├── Usuario.java
│   ├── Libro.java
│   ├── Prestamo.java
│   └── DetallePrestamo.java
├── servicios/
│   ├── Crud.java
│   ├── ServicioUsuarios.java
│   ├── ServicioLibros.java
│   ├── ServicioPrestamos.java
│   └── SistemaBiblioteca.java
├── persistencia/
│   └── ArchivosDatos.java
├── excepciones/
│   ├── ValidacionException.java
│   └── PersistenciaException.java
└── vista/
    ├── VentanaPrincipal.java
    └── Estilo.java
