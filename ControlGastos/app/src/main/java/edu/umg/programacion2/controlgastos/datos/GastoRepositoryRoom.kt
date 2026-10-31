package edu.umg.programacion2.controlgastos.datos

import edu.umg.programacion2.finanzas.datos.GastoRepository
import edu.umg.programacion2.finanzas.modelo.Gasto

/**
 * SEGUNDA implementación de GastoRepository (la primera es GastoRepositoryEnMemoria).
 * ControlGastos solo conoce la interface, por eso se cambia sin tocar la librería.
 * Traduce: recibe/devuelve Gasto (librería) y le habla al DAO con GastoEntity (tabla).
 */
class GastoRepositoryRoom(private val dao: GastoDao) : GastoRepository {

    override suspend fun agregar(gasto: Gasto): Gasto {
        // id = 0 para que SQLite genere el id; el DAO devuelve Long y Gasto usa Int
        val idNuevo = dao.insertar(GastoEntity.desde(gasto.copy(id = 0)))
        return gasto.copy(id = idNuevo.toInt())
    }

    override suspend fun listar(): List<Gasto> = dao.listar().map { it.aGasto() }

    override suspend fun buscarPorId(id: Int): Gasto? = dao.buscarPorId(id)?.aGasto()

    override suspend fun actualizar(gasto: Gasto): Boolean =
        dao.actualizar(GastoEntity.desde(gasto)) > 0

    override suspend fun eliminar(id: Int): Boolean = dao.eliminar(id) > 0
}
