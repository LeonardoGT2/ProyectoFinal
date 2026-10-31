package edu.umg.programacion2.finanzas.modelo

/**
 * Un gasto. El id empieza en 0 y lo asigna el repositorio al guardarlo.
 * La fecha es texto con formato yyyy-MM-dd.
 */
data class Gasto(
    val id: Int = 0,
    val descripcion: String,
    val monto: Double,
    val categoria: Categoria,
    val fecha: String
)
