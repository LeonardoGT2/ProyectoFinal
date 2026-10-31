package edu.umg.programacion2.finanzas.servicio

import edu.umg.programacion2.finanzas.datos.GastoRepository
import edu.umg.programacion2.finanzas.datos.IngresoRepository
import edu.umg.programacion2.finanzas.modelo.BalanceMensual
import edu.umg.programacion2.finanzas.modelo.Categoria
import edu.umg.programacion2.finanzas.modelo.Gasto
import edu.umg.programacion2.finanzas.modelo.Ingreso
import java.util.Locale

/**
 * Las reglas de la app. Solo conoce las interfaces de los repositorios.
 * No tiene println ni Toast: solo devuelve datos; la consola o Android deciden cómo mostrarlos.
 */
class ControlGastos(
    private val gastos: GastoRepository,
    private val ingresos: IngresoRepository
) {

    // ---------- Validación ----------

    /** null si el gasto está bien; si no, el primer error. */
    fun validar(gasto: Gasto): String? = validarDatos(gasto.descripcion, gasto.monto, gasto.fecha)

    /** null si el ingreso está bien; si no, el primer error. */
    fun validar(ingreso: Ingreso): String? = validarDatos(ingreso.descripcion, ingreso.monto, ingreso.fecha)

    private fun validarDatos(descripcion: String, monto: Double, fecha: String): String? = when {
        descripcion.isBlank() -> "La descripción no puede estar vacía"
        monto.isNaN() || monto.isInfinite() || monto <= 0.0 -> "El monto debe ser mayor a 0"
        !esFechaValida(fecha) -> "La fecha debe tener el formato yyyy-MM-dd (ejemplo: 2026-10-27)"
        else -> null
    }

    // ---------- Registrar y eliminar ----------

    /** Valida y guarda un gasto. Devuelve null si se guardó, o el mensaje de error. */
    suspend fun registrar(gasto: Gasto): String? {
        val error = validar(gasto)
        if (error != null) return error
        gastos.agregar(gasto.copy(descripcion = gasto.descripcion.trim()))
        return null
    }

    /** Valida y guarda un ingreso. Devuelve null si se guardó, o el mensaje de error. */
    suspend fun registrarIngreso(ingreso: Ingreso): String? {
        val error = validar(ingreso)
        if (error != null) return error
        ingresos.agregar(ingreso.copy(descripcion = ingreso.descripcion.trim()))
        return null
    }

    suspend fun eliminar(id: Int): Boolean = gastos.eliminar(id)

    suspend fun eliminarIngreso(id: Int): Boolean = ingresos.eliminar(id)

    // ---------- Listados y totales (todos los meses) ----------

    suspend fun listar(): List<Gasto> = gastos.listar()

    suspend fun listarIngresos(): List<Ingreso> = ingresos.listar()

    /** Suma de todos los gastos de todos los meses. */
    suspend fun total(): Double = gastos.listar().sumOf { it.monto }

    /** Cuánto se gastó por categoría, en todos los meses. */
    suspend fun totalPorCategoria(): Map<Categoria, Double> =
        gastos.listar().groupBy { it.categoria }.mapValues { (_, lista) -> lista.sumOf { it.monto } }

    // ---------- Control mensual ----------

    /** Solo los gastos de un mes (yyyy-MM). */
    suspend fun gastosDelMes(mes: String): List<Gasto> =
        gastos.listar().filter { mesDe(it.fecha) == mes }

    /** Solo los ingresos de un mes (yyyy-MM). */
    suspend fun ingresosDelMes(mes: String): List<Ingreso> =
        ingresos.listar().filter { mesDe(it.fecha) == mes }

    /** El control de un mes. Si no se da el mes, usa el actual (el mes nuevo arranca en Q 0.00). */
    suspend fun balanceDelMes(mes: String = mesActual()): BalanceMensual =
        construirBalance(mes, gastos.listar(), ingresos.listar())

    /** Los meses con datos, del más nuevo al más viejo. El mes actual siempre aparece. */
    suspend fun mesesConMovimientos(): List<String> =
        mesesDe(gastos.listar(), ingresos.listar())

    /** Un balance por cada mes, del más reciente al más viejo. */
    suspend fun historial(): List<BalanceMensual> {
        val todosGastos = gastos.listar()
        val todosIngresos = ingresos.listar()
        return mesesDe(todosGastos, todosIngresos).map { construirBalance(it, todosGastos, todosIngresos) }
    }

    /** Ingresos menos gastos de todos los meses juntos. */
    suspend fun saldoAcumulado(): Double =
        ingresos.listar().sumOf { it.monto } - gastos.listar().sumOf { it.monto }

    // ---------- Textos listos para un encabezado ----------

    /** Una línea con el resumen general. */
    suspend fun resumen(): String =
        "Gastos totales: ${formatearQuetzales(total())} | Saldo acumulado: ${formatearQuetzales(saldoAcumulado())}"

    /** Una línea con el resumen de un mes. */
    suspend fun resumenDelMes(mes: String = mesActual()): String {
        val b = balanceDelMes(mes)
        return "${nombreMes(mes)}: Ingresos ${formatearQuetzales(b.totalIngresos)} | " +
            "Gastos ${formatearQuetzales(b.totalGastos)} | Balance ${formatearQuetzales(b.saldo)}"
    }

    // ---------- Apoyo privado ----------

    private fun mesesDe(listaGastos: List<Gasto>, listaIngresos: List<Ingreso>): List<String> {
        val meses = mutableSetOf(mesActual())
        listaGastos.forEach { meses.add(mesDe(it.fecha)) }
        listaIngresos.forEach { meses.add(mesDe(it.fecha)) }
        return meses.sortedDescending()   // yyyy-MM ordena bien como texto
    }

    private fun construirBalance(mes: String, todosGastos: List<Gasto>, todosIngresos: List<Ingreso>): BalanceMensual {
        val gastosMes = todosGastos.filter { mesDe(it.fecha) == mes }
        val ingresosMes = todosIngresos.filter { mesDe(it.fecha) == mes }
        return BalanceMensual(
            mes = mes,
            totalIngresos = ingresosMes.sumOf { it.monto },
            totalGastos = gastosMes.sumOf { it.monto },
            ingresosPorTipo = ingresosMes.groupBy { it.tipo }
                .mapValues { (_, lista) -> lista.sumOf { it.monto } },
            gastosPorCategoria = gastosMes.groupBy { it.categoria }
                .mapValues { (_, lista) -> lista.sumOf { it.monto } },
            cantidadIngresos = ingresosMes.size,
            cantidadGastos = gastosMes.size
        )
    }
}

/** 45.5 -> "Q 45.50". Se usa en la consola y en Android. */
fun formatearQuetzales(monto: Double): String {
    val texto = String.format(Locale.US, "%,.2f", Math.abs(monto))
    return if (monto < -0.005) "-Q $texto" else "Q $texto"
}
