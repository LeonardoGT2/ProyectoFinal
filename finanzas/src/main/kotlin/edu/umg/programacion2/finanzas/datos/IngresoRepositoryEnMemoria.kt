package edu.umg.programacion2.finanzas.datos

import edu.umg.programacion2.finanzas.modelo.Ingreso

/** Guarda los ingresos en una lista privada. */
class IngresoRepositoryEnMemoria : IngresoRepository {

    private val lista = mutableListOf<Ingreso>()
    private var siguienteId = 1

    override suspend fun agregar(ingreso: Ingreso): Ingreso {
        val nuevo = ingreso.copy(id = siguienteId++)
        lista.add(nuevo)
        return nuevo
    }

    override suspend fun listar(): List<Ingreso> = lista.toList()

    override suspend fun buscarPorId(id: Int): Ingreso? {
        val posicion = buscarPosicion(id)
        return if (posicion == -1) null else lista[posicion]
    }

    override suspend fun actualizar(ingreso: Ingreso): Boolean {
        val posicion = buscarPosicion(ingreso.id)
        if (posicion == -1) return false
        lista[posicion] = ingreso
        return true
    }

    override suspend fun eliminar(id: Int): Boolean {
        val posicion = buscarPosicion(id)
        if (posicion == -1) return false
        lista.removeAt(posicion)
        return true
    }

    private fun buscarPosicion(id: Int): Int = lista.indexOfFirst { it.id == id }
}
