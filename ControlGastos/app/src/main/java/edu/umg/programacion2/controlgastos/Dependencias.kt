package edu.umg.programacion2.controlgastos

import android.content.Context
import androidx.room.Room
import edu.umg.programacion2.controlgastos.datos.AppDatabase
import edu.umg.programacion2.controlgastos.datos.GastoRepositoryRoom
import edu.umg.programacion2.controlgastos.datos.IngresoRepositoryRoom
import edu.umg.programacion2.finanzas.servicio.ControlGastos

/**
 * Las piezas que TODA la app comparte (object = un solo objeto).
 * Room necesita un Context para abrir el archivo de la base; se lo damos UNA vez
 * en iniciar(), llamado desde ControlGastosApp.
 */
object Dependencias {

    lateinit var control: ControlGastos
        private set

    fun iniciar(context: Context) {
        // Abre (o crea la primera vez) finanzas.db en la carpeta privada de la app
        val baseDeDatos = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "finanzas.db")
            .build()

        // EL CAMBIO: antes eran los repositorios EnMemoria
        control = ControlGastos(
            GastoRepositoryRoom(baseDeDatos.gastoDao()),
            IngresoRepositoryRoom(baseDeDatos.ingresoDao())
        )
    }
}
