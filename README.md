# Sistema de biblioteca

Proyecto grupal del curso de Programación Orientada a Objetos, desarrollado en 2026 con Java 26, Swing y Apache NetBeans. La aplicación de escritorio administra usuarios, libros, préstamos y detalles de préstamo mediante archivos de texto.

## Requisitos y ejecución

- JDK 26 configurado en Apache NetBeans.
- Abrir el proyecto `Biblioteca` en NetBeans y ejecutarlo con **Run Project**. La clase principal es `biblioteca.Biblioteca`.
- Si la ejecución muestra clases antiguas o no encuentra una clase, usar **Clean and Build Project** y volver a ejecutar.
- Los archivos de datos se leen y escriben con rutas relativas a la carpeta de ejecución. Mantener la carpeta `datos` junto al proyecto al ejecutarlo desde NetBeans.

## Módulos

| Módulo | Función |
| --- | --- |
| Usuarios | Registrar, buscar, modificar y eliminar usuarios. El DNI no se puede repetir y no se permite eliminar a un usuario con préstamos activos. |
| Libros | Registrar y consultar libros, modificar sus datos y controlar sus ejemplares disponibles. |
| Préstamos | Registrar un préstamo para un usuario y un libro existentes; consultar su estado y registrar su devolución. |
| Detalle del préstamo | Consultar los libros de un préstamo y agregar o quitar libros mientras el préstamo esté activo. |

La ventana principal ofrece acceso a los cuatro módulos. Cada módulo se abre en una ventana propia.

## Cómo funciona un préstamo

1. Registrar un usuario y un libro con ejemplares disponibles.
2. En **Préstamos**, ingresar los IDs del usuario y del libro. El botón de búsqueda permite consultar el título.
3. Al registrar, el sistema comprueba los IDs y la disponibilidad; guarda el préstamo, crea automáticamente el detalle del primer libro y descuenta un ejemplar.
4. Si se necesitan más libros, agregarlos en **Detalle del préstamo**, indicando el ID del préstamo, el ID del libro y la cantidad.
5. El vencimiento se calcula 14 días después de la fecha del préstamo.
6. Al devolver, el sistema registra la fecha y repone las cantidades de todos los detalles. Los detalles permanecen como historial.

Los datos se guardan en archivos para poder consultarlos nuevamente después de cerrar la aplicación. Si ocurre un error de escritura durante una operación que afecta varios archivos, el sistema intenta revertir los cambios ya realizados.

## Organización del código

```text
src/
├── biblioteca/    Punto de entrada de la aplicación
├── modelo/        Libro, Usuario, Prestamo y DetallePrestamo
├── servicios/     Reglas, operaciones y persistencia de cada módulo
├── vista/         Ventanas construidas con Swing
├── excepciones/  Errores de validación y persistencia
└── pruebas/      Programas de comprobación de libros y préstamos
```

Los modelos se relacionan mediante identificadores: un préstamo guarda el ID del usuario y cada detalle guarda los IDs del préstamo y del libro. Los servicios consultan los archivos para localizar los registros relacionados.

## Archivos de datos

Los cuatro archivos principales se encuentran en `datos/`:

- `usuarios.txt`: ID, nombre, DNI y correo.
- `libros.txt`: ID, título, autor, ejemplares totales y disponibles.
- `prestamos.txt`: ID, ID de usuario, fecha de préstamo, vencimiento y devolución.
- `detalles_prestamo.txt`: ID de préstamo, ID de libro y cantidad.

Cada registro ocupa una línea y los campos se separan con `|`. Los servicios de usuarios y libros también usan `usuarios-secuencia.txt` y `libros-secuencia.txt` para conservar la secuencia de IDs. Los archivos necesarios se crean al guardar si todavía no existen.

## Pruebas

El paquete `pruebas` incluye comprobaciones del catálogo y de la integración de préstamos. Utilizan carpetas temporales para no modificar los datos habituales de la biblioteca. Para una revisión completa, ejecutar también la interfaz y probar el registro, cierre, reapertura y devolución desde las ventanas.

Las vistas son ventanas independientes; si se deja abierta la tabla de libros durante un préstamo o devolución, puede ser necesario actualizarla para consultar la disponibilidad más reciente.
