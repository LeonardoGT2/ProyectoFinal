package edu.umg.programacion2.controlgastos.datos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface IngresoDao {

    @Insert
    suspend fun insertar(ingreso: IngresoEntity): Long

    @Query("SELECT * FROM ingresos ORDER BY id")
    suspend fun listar(): List<IngresoEntity>

    @Query("SELECT * FROM ingresos WHERE id = :id")
    suspend fun buscarPorId(id: Int): IngresoEntity?

    @Update
    suspend fun actualizar(ingreso: IngresoEntity): Int

    @Query("DELETE FROM ingresos WHERE id = :id")
    suspend fun eliminar(id: Int): Int
}
