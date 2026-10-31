package edu.umg.programacion2.controlgastos.datos

import androidx.room.Entity
import androidx.room.PrimaryKey
import edu.umg.programacion2.finanzas.modelo.Categoria
import edu.umg.programacion2.finanzas.modelo.Gasto

/**
 * Una FILA de la tabla "gastos" en SQLite.
 * Gasto (librería, Kotlin puro) no conoce Room, así que Android tiene su clase "espejo"
 * y la convierte de ida y vuelta:  Gasto <-- aGasto() / desde() --> GastoEntity
 */
@Entity(tableName = "gastos")
data class GastoEntity(
    // autoGenerate = AUTO_INCREMENT: si llega id = 0, SQLite asigna el siguiente
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val descripcion: String,
    val monto: Double,
    // SQLite no conoce enums: se guarda el NOMBRE ("DESPENSA"), no toString() ni ordinal
    val categoria: String,
    val fecha: String
) {
    /** Fila de la tabla -> objeto de la librería. */
    fun aGasto(): Gasto = Gasto(
        id = id,
        descripcion = descripcion,
        monto = monto,
        categoria = Categoria.valueOf(categoria),
        fecha = fecha
    )

    companion object {
        /** Objeto de la librería -> fila de la tabla. */
        fun desde(gasto: Gasto): GastoEntity = GastoEntity(
            id = gasto.id,
            descripcion = gasto.descripcion,
            monto = gasto.monto,
            categoria = gasto.categoria.name,
            fecha = gasto.fecha
        )
    }
}
