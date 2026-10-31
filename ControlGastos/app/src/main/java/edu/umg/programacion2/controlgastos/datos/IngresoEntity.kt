package edu.umg.programacion2.controlgastos.datos

import androidx.room.Entity
import androidx.room.PrimaryKey
import edu.umg.programacion2.finanzas.modelo.Ingreso
import edu.umg.programacion2.finanzas.modelo.TipoIngreso

/** Una FILA de la tabla "ingresos". Mismo patrón que GastoEntity. */
@Entity(tableName = "ingresos")
data class IngresoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val descripcion: String,
    val monto: Double,
    val tipo: String,       // nombre del enum: "SALARIO", "VENTAS", "OTROS_INGRESOS"
    val fecha: String
) {
    fun aIngreso(): Ingreso = Ingreso(
        id = id,
        descripcion = descripcion,
        monto = monto,
        tipo = TipoIngreso.valueOf(tipo),
        fecha = fecha
    )

    companion object {
        fun desde(ingreso: Ingreso): IngresoEntity = IngresoEntity(
            id = ingreso.id,
            descripcion = ingreso.descripcion,
            monto = ingreso.monto,
            tipo = ingreso.tipo.name,
            fecha = ingreso.fecha
        )
    }
}
