package edu.umg.programacion2.controlgastos.datos

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * La base de datos: qué tablas tiene y qué DAOs ofrece.
 * Si algún día cambias una Entity (agregas un campo), hay que subir version
 * (o desinstalar la app del emulador mientras desarrollas).
 */
@Database(
    entities = [GastoEntity::class, IngresoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gastoDao(): GastoDao
    abstract fun ingresoDao(): IngresoDao
}
