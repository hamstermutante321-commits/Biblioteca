<div align="center">

# 🩸 BIBLIOTECA NOCTURNA 🩸

### Sistema de control de usuarios, libros y préstamos

<img src="https://img.shields.io/badge/Java-17-8B0000?style=for-the-badge&logo=openjdk&logoColor=white">
<img src="https://img.shields.io/badge/Swing-Interfaz-000000?style=for-the-badge">
<img src="https://img.shields.io/badge/Estado-En%20desarrollo-4B0000?style=for-the-badge">

<br><br>

> “Todo conocimiento tiene un precio.  
> Cada libro prestado deja una deuda.”

</div>

---

## ☠️ Sobre el proyecto

**Biblioteca Nocturna** es una aplicación de escritorio desarrollada en Java para administrar usuarios, libros y préstamos.

El sistema permite conocer:

- Qué usuarios tienen libros prestados.
- Qué libros están disponibles.
- Qué préstamos están próximos a vencer.
- Qué usuarios tienen préstamos atrasados.
- Cuántos ejemplares quedan disponibles.

---

## 🕯️ Módulos principales

| Módulo | Función |
|---|---|
| 👤 Usuarios | Registrar y administrar usuarios |
| 📚 Libros | Controlar títulos y ejemplares |
| 🩸 Préstamos | Registrar libros prestados |
| ⏳ Vencimientos | Detectar préstamos atrasados |
| 🗃️ Persistencia | Guardar información en archivos |

---

## 🧬 Modelo del sistema

```text
Usuario
   │
   └── Préstamo
          │
          └── DetallePrestamo
                         │
                         └── Libro
