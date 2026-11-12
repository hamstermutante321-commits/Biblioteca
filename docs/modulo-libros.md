# Módulo de libros — Rama-Libro

## Archivos y responsabilidades

- `src/modelo/Libro.java`: datos y reglas de un libro; cambios de inventario preservan los ejemplares prestados.
- `src/servicios/ServicioLibros.java`: CRUD, búsqueda, generación de IDs y persistencia del catálogo.
- `src/vista/LibroView.java`: formulario Swing, tabla y mensajes para el usuario.
- `src/pruebas/PruebaServicioLibros.java`: pruebas ejecutables sin bibliotecas externas, usando carpetas temporales.

Los cuatro modelos son clases independientes, cada uno con un `id` privado e inmutable, validado en su constructor y consultado mediante `getId()`. No hay una clase base de identidad. Se usan `ValidacionException` y `PersistenciaException` existentes. `Crud` no se cambia: su contrato actual no declara las excepciones comprobadas del servicio. `PanelLibros` es el formulario anterior; la pantalla funcional del catálogo es `LibroView`.

## Integración en la carpeta principal

`biblioteca.Biblioteca` abre `VentanaPrincipal` en el hilo de eventos de Swing. Su menú **Catálogo > Libros** abre `LibroView` y reutiliza la ventana mientras siga abierta. `SistemaBiblioteca` proporciona la instancia de `ServicioLibros` que utiliza esa vista. Cerrar el catálogo permite seguir en el menú principal; reabrirlo carga los registros actuales. La navegación se configura fuera del código generado por NetBeans; el título de la etiqueta coincide en el `.java` y el `.form`.

Esta integración habilita el catálogo. `ServicioUsuarios`, `ServicioPrestamos` y `DetallePrestamo` todavía están incompletos en este checkout: no hay un flujo completo de préstamos de la biblioteca. La política de historial y la coordinación entre archivos siguen siendo tareas del equipo.

## Vista del catálogo

La pantalla usa una tabla, botones de acción y un formulario a la derecha. Emplea los mismos componentes Swing básicos que las vistas de préstamos y detalles. La ventana principal permite abrir los demás módulos.

La búsqueda filtra por título, autor o ID, sin distinguir mayúsculas, al pulsar **Buscar** o Enter. **Nuevo libro** limpia el formulario y **Guardar libro** registra el libro. El ID se asigna al guardar. Al seleccionar una fila se cargan sus datos para editar; los botones **Editar**, **Ver detalle** y **Eliminar** actúan sobre esa fila. **Cancelar** limpia el formulario, **Ver todos** elimina el filtro y **Volver** regresa a la ventana principal.

La tabla muestra los estados Disponible, Pocos disponibles y No disponible. La disponibilidad del formulario es de solo lectura porque depende de los préstamos y las devoluciones.

## Datos

Ejecutar desde la raíz del proyecto para que las rutas sean consistentes.

`datos/libros.txt`, texto UTF-8, un libro por línea:

```text
L00001|El principito|Antoine de Saint-Exupéry|5|3
```

Orden: `id|titulo|autor|ejemplaresTotales|ejemplaresDisponibles`.

Los títulos y autores no admiten `|`, retornos de carro ni saltos de línea. Los espacios de los extremos se eliminan. Los registros con campos inválidos o IDs repetidos detienen la lectura con archivo y número de línea; no se ignoran ni se sobrescriben al guardar.

`datos/libros-secuencia.txt` contiene el último número reservado, por ejemplo `1`. Debe conservarse y respaldarse junto al catálogo: permite que un ID eliminado no vuelva a asignarse, incluso después de reiniciar. Este contador agrega un quinto archivo permanente al conjunto de los cuatro archivos de negocio del grupo. Pueden existir saltos de numeración si un guardado falla después de reservar un ID; no se reutilizan esos números. El formato de cinco dígitos admite L00001 a L99999.

Si todavía no existe `datos/libros.txt`, pero existe el antiguo `libros.txt` en la raíz, la primera lectura valida y copia su contenido a la nueva ubicación. El original se conserva como respaldo y deja de usarse una vez creado el nuevo catálogo. Si existen ambos, prevalece el de `datos`. No editar ambos archivos como catálogos independientes.

El servicio genera los archivos al usarlos. No se incluyen registros reales ni catálogos vacíos en el commit.

## Contrato para los otros módulos

```java
Libro registrar(String titulo, String autor, int total);
List<Libro> listar();
List<Libro> buscar(String titulo);
Libro buscarPorId(String id);
void modificar(String id, String titulo, String autor, int total);
void eliminar(String id);
void prestarEjemplares(String id, int cantidad);
void devolverEjemplares(String id, int cantidad);
```

Las firmas reales declaran `PersistenciaException` y, cuando corresponde, `ValidacionException`. `buscarPorId` lanza `ValidacionException` si no existe; no devuelve null. `buscar` busca coincidencias parciales en título, autor e ID y devuelve una lista vacía sin coincidencias. `registrar` genera el ID antes de construir el objeto, lo guarda y devuelve el libro registrado. Los getters no modifican archivos: modificar el objeto que devuelve una búsqueda tampoco actualiza el catálogo; usar el servicio para persistir operaciones.

Un libro nuevo tiene todos sus ejemplares disponibles. La modificación del total preserva los prestados: si hay 5 totales y 3 disponibles, hay 2 prestados; aumentar el total a 7 deja 5 disponibles. No se puede reducir el total por debajo de esos 2 prestados. La vista no permite escribir directamente la disponibilidad.

`eliminar` rechaza libros con ejemplares prestados. El integrador debe decidir además si permite eliminar libros citados por detalles históricos o si agrega una política de conservación de historial antes de exponer la eliminación en el sistema completo.

El módulo de detalle debe llamar al servicio al prestar/devolver. Estas llamadas actualizan solo el catálogo: el coordinador debe validar todos los libros antes de registrar un préstamo y encargarse de la consistencia y recuperación si falla después el guardado de préstamos o detalles. No hay una transacción entre varios archivos implementada por este módulo.

Cada escritura usa un archivo temporal y reemplazo del destino; se intenta movimiento atómico y se usa reemplazo normal si el sistema no lo soporta. No se trunca primero el catálogo original. Las instancias del servicio se coordinan dentro de un mismo proceso Java; este diseño está pensado para una única aplicación de escritorio, no para varios procesos escribiendo simultáneamente los mismos TXT.

## Ejecutar y verificar

En NetBeans: abrir la carpeta principal `Biblioteca` y usar Run Project. Entrar a **Catálogo > Libros**. Para probar solo el catálogo, ejecutar `vista.LibroView` con Run File.

Con un JDK 17 o superior y PowerShell, desde la raíz:

```powershell
New-Item -ItemType Directory -Force build/classes | Out-Null
javac -encoding UTF-8 -d build/classes (Get-ChildItem src -Filter '*.java' -Recurse | ForEach-Object FullName)
java -cp build/classes pruebas.PruebaServicioLibros
java -cp build/classes pruebas.PruebaIntegracionLibros
java -cp build/classes biblioteca.Biblioteca
```

Las pruebas cubren CRUD, reapertura, búsquedas, préstamos/devoluciones, ID después de borrar el máximo, UTF-8, datos corruptos, contador inválido y fallos de escritura. La prueba imprime la ruta temporal que utiliza.

`PruebaIntegracionLibros` requiere un escritorio gráfico. Comprueba el arranque de `Biblioteca.main`, la apertura desde el menú, la reutilización de la ventana, registro y modificación desde Swing, actualización de disponibilidad, reapertura y acciones sobre la fila seleccionada: consulta de solo lectura, edición y eliminación cancelada o confirmada. Sus registros se guardan en otra carpeta temporal.

Comprobación manual de la vista: registrar un libro, seleccionarlo, modificarlo, buscar por título, autor e ID, probar cantidades no numéricas, cancelar/confirmar una eliminación, cerrar y reabrir. Los datos de la tabla no se editan directamente; los botones bajo la tabla actúan sobre el libro seleccionado. Usar **Nuevo libro** para iniciar otro registro.
