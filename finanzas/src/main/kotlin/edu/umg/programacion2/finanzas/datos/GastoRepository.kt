package edu.umg.programacion2.finanzas.datos

import edu.umg.programacion2.finanzas.modelo.Gasto

/** Contrato para guardar gastos. Todas son suspend: mañana pueden leer una base de datos. */
interface GastoRepository {
    suspend fun agregar(gasto: Gasto): Gasto
    suspend fun listar(): List<Gasto>
    suspend fun buscarPorId(id: Int): Gasto?
    suspend fun actualizar(gasto: Gasto): Boolean
    suspend fun eliminar(id: Int): Boolean
}
