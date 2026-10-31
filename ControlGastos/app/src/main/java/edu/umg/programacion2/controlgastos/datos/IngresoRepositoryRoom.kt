package edu.umg.programacion2.controlgastos.datos

import edu.umg.programacion2.finanzas.datos.IngresoRepository
import edu.umg.programacion2.finanzas.modelo.Ingreso

/** Igual que GastoRepositoryRoom, pero para ingresos. */
class IngresoRepositoryRoom(private val dao: IngresoDao) : IngresoRepository {

    override suspend fun agregar(ingreso: Ingreso): Ingreso {
        val idNuevo = dao.insertar(IngresoEntity.desde(ingreso.copy(id = 0)))
        return ingreso.copy(id = idNuevo.toInt())
    }

    override suspend fun listar(): List<Ingreso> = dao.listar().map { it.aIngreso() }

    override suspend fun buscarPorId(id: Int): Ingreso? = dao.buscarPorId(id)?.aIngreso()

    override suspend fun actualizar(ingreso: Ingreso): Boolean =
        dao.actualizar(IngresoEntity.desde(ingreso)) > 0

    override suspend fun eliminar(id: Int): Boolean = dao.eliminar(id) > 0
}
