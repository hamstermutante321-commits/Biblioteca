# Módulo de libros — Rama-Libro

## Archivos y responsabilidades

- `src/modelo/Libro.java`: datos y reglas de un libro; cambios de inventario preservan los ejemplares prestados.
- `src/servicios/ServicioLibros.java`: CRUD, búsqueda, generación de IDs y persistencia del catálogo.
- `src/vista/LibroView.java`: formulario Swing, tabla y mensajes para el usuario.
- `src/pruebas/PruebaServicioLibros.java`: pruebas ejecutables sin bibliotecas externas, usando carpetas temporales.

Se conserva `Entidad` como clase abstracta y se usan `ValidacionException` y `PersistenciaException` existentes. `Crud` no se cambia: su contrato actual no declara las excepciones comprobadas del servicio. La integración de pantallas en `VentanaPrincipal` corresponde al responsable de integración. `PanelLibros` es el formulario anterior; ejecutar `LibroView` para este módulo.

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

Las firmas reales declaran `PersistenciaException` y, cuando corresponde, `ValidacionException`. `buscarPorId` lanza `ValidacionException` si no existe; no devuelve null. `buscar` devuelve una lista vacía sin coincidencias. `registrar` genera el ID antes de construir el objeto, lo guarda y devuelve el libro registrado. Los getters no modifican archivos: modificar el objeto que devuelve una búsqueda tampoco actualiza el catálogo; usar el servicio para persistir operaciones.

Un libro nuevo tiene todos sus ejemplares disponibles. La modificación del total preserva los prestados: si hay 5 totales y 3 disponibles, hay 2 prestados; aumentar el total a 7 deja 5 disponibles. No se puede reducir el total por debajo de esos 2 prestados. La vista no permite escribir directamente la disponibilidad.

`eliminar` rechaza libros con ejemplares prestados. El integrador debe decidir además si permite eliminar libros citados por detalles históricos o si agrega una política de conservación de historial antes de exponer la eliminación en el sistema completo.

El módulo de detalle debe llamar al servicio al prestar/devolver. Estas llamadas actualizan solo el catálogo: el coordinador debe validar todos los libros antes de registrar un préstamo y encargarse de la consistencia y recuperación si falla después el guardado de préstamos o detalles. No hay una transacción entre varios archivos implementada por este módulo.

Cada escritura usa un archivo temporal y reemplazo del destino; se intenta movimiento atómico y se usa reemplazo normal si el sistema no lo soporta. No se trunca primero el catálogo original. Las instancias del servicio se coordinan dentro de un mismo proceso Java; este diseño está pensado para una única aplicación de escritorio, no para varios procesos escribiendo simultáneamente los mismos TXT.

## Ejecutar y verificar

En NetBeans: abrir este checkout y ejecutar `vista.LibroView` con Run File. La vista puede recibir una instancia de `ServicioLibros` en su constructor para la integración posterior.

Con un JDK 17 o superior y PowerShell, desde la raíz:

```powershell
New-Item -ItemType Directory -Force build/classes | Out-Null
javac -encoding UTF-8 -d build/classes (Get-ChildItem src -Filter '*.java' -Recurse | ForEach-Object FullName)
java -cp build/classes pruebas.PruebaServicioLibros
java -cp build/classes vista.LibroView
```

Las pruebas cubren CRUD, reapertura, búsquedas, préstamos/devoluciones, ID después de borrar el máximo, UTF-8, datos corruptos, contador inválido y fallos de escritura. La prueba imprime la ruta temporal que utiliza.

Comprobación manual de la vista: registrar un libro, seleccionarlo, modificarlo, buscar por título e ID, probar cantidades no numéricas, cancelar/confirmar una eliminación, cerrar y reabrir. La tabla no es editable y el botón de registro requiere limpiar la selección antes de crear un nuevo libro.
