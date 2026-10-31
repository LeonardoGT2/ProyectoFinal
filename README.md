# Control de gastos - Proyecto final de Programación 2 (UMG)

App de control de gastos e ingresos en Kotlin: registra movimientos, calcula el balance de cada mes y avisa cuando vas en negativo.

**Autor:** Leonardo Sebastian Solorzano Chavarria

## Estructura
- `finanzas/` - Librería Kotlin (Maven) con las reglas de la app y una consola para probarla.
  - `modelo`, `datos`, `servicio`, `consola`
- `ControlGastos/` - App Android (una pantalla, datos guardados con Room).

## Cómo ejecutarlo
1. **Consola:** abrir `finanzas/pom.xml` en IntelliJ IDEA y ejecutar `Main.kt`.
2. **Instalar la librería:** en IntelliJ, panel Maven -> Lifecycle -> `install`.
3. **Android:** abrir `ControlGastos/` en Android Studio, sincronizar Gradle y ejecutar en un emulador.
   La app usa `mavenLocal()`, por eso el paso 2 debe hacerse primero.

## Funciones
- Registrar gastos e ingresos (con validaciones)
- Balance mensual con estados: sin movimientos, bien, cuidado (80 % gastado) y negativo
- Historial por mes y cambio de mes
- Eliminar movimientos (en Android: tocar el movimiento)
- Persistencia con Room (SQLite)
- Tema claro y oscuro
