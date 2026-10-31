package edu.umg.programacion2.finanzas.modelo

/** Un ingreso. Igual que Gasto, pero con TipoIngreso. */
data class Ingreso(
    val id: Int = 0,
    val descripcion: String,
    val monto: Double,
    val tipo: TipoIngreso,
    val fecha: String
)
