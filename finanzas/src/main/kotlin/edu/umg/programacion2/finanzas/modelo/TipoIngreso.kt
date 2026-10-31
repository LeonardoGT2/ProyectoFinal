package edu.umg.programacion2.finanzas.modelo

/** Tipos de ingreso. Mismo patrón que Categoria. */
enum class TipoIngreso(val texto: String) {
    SALARIO("Salario"),
    VENTAS("Ventas"),
    OTROS_INGRESOS("Otros ingresos");

    override fun toString(): String = texto
}
