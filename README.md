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

## 📖 Descripción

Este proyecto consiste en una aplicación desarrollada en Java para organizar la información de una biblioteca.

El sistema permite registrar usuarios, controlar los libros disponibles y administrar los préstamos realizados.

También permite consultar la fecha de vencimiento de cada préstamo y conocer qué libros se encuentran disponibles.

---

## ✨ Funcionalidades

| Módulo | Descripción |
|---|---|
| 👤 Usuarios | Registrar, buscar, editar y eliminar usuarios |
| 📚 Libros | Administrar títulos, autores y ejemplares |
| 🔄 Préstamos | Registrar libros prestados a cada usuario |
| 📅 Vencimientos | Consultar fechas límite de devolución |
| 📦 Disponibilidad | Controlar los ejemplares disponibles |
| 🔎 Búsqueda | Buscar usuarios, libros y préstamos |

---

## 🧩 Entidades del sistema

```text
Usuario
   │
   └── Préstamo
          │
          └── DetallePrestamo
                         │
                         └── Libro
