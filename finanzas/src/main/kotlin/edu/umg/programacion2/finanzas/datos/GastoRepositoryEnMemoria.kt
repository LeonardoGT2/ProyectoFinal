package edu.umg.programacion2.finanzas.datos

import edu.umg.programacion2.finanzas.modelo.Gasto

/** Guarda los gastos en una lista privada. Después se puede cambiar por Room sin tocar el resto. */
class GastoRepositoryEnMemoria : GastoRepository {

    private val lista = mutableListOf<Gasto>()
    private var siguienteId = 1

    override suspend fun agregar(gasto: Gasto): Gasto {
        val nuevo = gasto.copy(id = siguienteId++)   // como el AUTO_INCREMENT de MySQL
        lista.add(nuevo)
        return nuevo
    }

    override suspend fun listar(): List<Gasto> = lista.toList()   // copia: nadie la modifica desde afuera

    override suspend fun buscarPorId(id: Int): Gasto? {
        val posicion = buscarPosicion(id)
        return if (posicion == -1) null else lista[posicion]
    }

    override suspend fun actualizar(gasto: Gasto): Boolean {
        val posicion = buscarPosicion(gasto.id)
        if (posicion == -1) return false
        lista[posicion] = gasto
        return true
    }

    override suspend fun eliminar(id: Int): Boolean {
        val posicion = buscarPosicion(id)
        if (posicion == -1) return false
        lista.removeAt(posicion)
        return true
    }

    /** Posición en la lista, o -1 si no existe. */
    private fun buscarPosicion(id: Int): Int = lista.indexOfFirst { it.id == id }
}
