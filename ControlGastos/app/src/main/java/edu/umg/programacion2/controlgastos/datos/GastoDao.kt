package edu.umg.programacion2.controlgastos.datos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * DAO: solo escribimos la INTERFACE; Room genera GastoDao_Impl al compilar.
 * Todas las funciones son suspend: Room las ejecuta fuera del hilo principal.
 */
@Dao
interface GastoDao {

    // Devuelve el id que SQLite generó para la fila nueva
    @Insert
    suspend fun insertar(gasto: GastoEntity): Long

    // Orden de llegada, igual que la lista en memoria
    @Query("SELECT * FROM gastos ORDER BY id")
    suspend fun listar(): List<GastoEntity>

    @Query("SELECT * FROM gastos WHERE id = :id")
    suspend fun buscarPorId(id: Int): GastoEntity?

    // Devuelve cuántas filas cambió
    @Update
    suspend fun actualizar(gasto: GastoEntity): Int

    // Devuelve cuántas filas borró (0 si el id no existía)
    @Query("DELETE FROM gastos WHERE id = :id")
    suspend fun eliminar(id: Int): Int
}
