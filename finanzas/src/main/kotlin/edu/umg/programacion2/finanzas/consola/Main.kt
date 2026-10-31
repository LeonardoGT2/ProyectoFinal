package edu.umg.programacion2.finanzas.consola

import edu.umg.programacion2.finanzas.datos.GastoRepositoryEnMemoria
import edu.umg.programacion2.finanzas.datos.IngresoRepositoryEnMemoria
import edu.umg.programacion2.finanzas.modelo.BalanceMensual
import edu.umg.programacion2.finanzas.modelo.Categoria
import edu.umg.programacion2.finanzas.modelo.EstadoBalance
import edu.umg.programacion2.finanzas.modelo.Gasto
import edu.umg.programacion2.finanzas.modelo.Ingreso
import edu.umg.programacion2.finanzas.modelo.TipoIngreso
import edu.umg.programacion2.finanzas.servicio.ControlGastos
import edu.umg.programacion2.finanzas.servicio.esMesValido
import edu.umg.programacion2.finanzas.servicio.esFechaValida
import edu.umg.programacion2.finanzas.servicio.fechaHoy
import edu.umg.programacion2.finanzas.servicio.formatearQuetzales
import edu.umg.programacion2.finanzas.servicio.mesActual
import edu.umg.programacion2.finanzas.servicio.mesAnterior
import edu.umg.programacion2.finanzas.servicio.mesSiguiente
import edu.umg.programacion2.finanzas.servicio.nombreMes
import kotlinx.coroutines.runBlocking
import java.util.Locale

// ---------- Colores ANSI ----------
private const val RESET = "\u001B[0m"
private const val ROJO = "\u001B[31m"
private const val VERDE = "\u001B[32m"
private const val AMARILLO = "\u001B[33m"
private const val CIAN = "\u001B[36m"

private fun pintar(texto: String, color: String) = "$color$texto$RESET"

private fun colorDe(estado: EstadoBalance): String = when (estado) {
    EstadoBalance.NEGATIVO -> ROJO
    EstadoBalance.CUIDADO -> AMARILLO
    EstadoBalance.POSITIVO -> VERDE
    EstadoBalance.SIN_MOVIMIENTOS -> RESET
}

// ---------- Lectura segura ----------

/** Lee un monto sin que truene si escriben letras. */
private fun leerMonto(pregunta: String): Double {
    while (true) {
        print(pregunta)
        val monto = readLine()?.trim()?.replace(',', '.')?.toDoubleOrNull()
        if (monto != null) return monto
        println(pintar("Escribe un número, por ejemplo 45.50", ROJO))
    }
}

/** Lee una fecha yyyy-MM-dd. Enter = hoy. */
private fun leerFecha(pregunta: String): String {
    while (true) {
        print("$pregunta (Enter = hoy): ")
        val texto = readLine()?.trim().orEmpty()
        if (texto.isEmpty()) return fechaHoy()
        if (esFechaValida(texto)) return texto
        println(pintar("Fecha inválida. Usa el formato yyyy-MM-dd", ROJO))
    }
}

/** Elige de una lista numerada. Una sola función genérica sirve para categorías y tipos. */
private fun <T> elegir(titulo: String, opciones: List<T>): T {
    println(titulo)
    opciones.forEachIndexed { i, opcion -> println("  ${i + 1}. $opcion") }
    while (true) {
        print("Número: ")
        val n = readLine()?.trim()?.toIntOrNull()
        if (n != null && n in 1..opciones.size) return opciones[n - 1]
        println(pintar("Elige un número entre 1 y ${opciones.size}", ROJO))
    }
}

private fun leerId(pregunta: String): Int? {
    print(pregunta)
    return readLine()?.trim()?.toIntOrNull()
}

// ---------- Pantallas de texto ----------

private fun encabezado(mes: String, b: BalanceMensual) {
    val saldoTexto = pintar(formatearQuetzales(b.saldo), colorDe(b.estado))
    println()
    println("=".repeat(58))
    println(" ${nombreMes(mes).uppercase()}   Ingresos: ${formatearQuetzales(b.totalIngresos)}   " +
        "Gastos: ${formatearQuetzales(b.totalGastos)}")
    println(" Balance: $saldoTexto")
    if (b.estado != EstadoBalance.POSITIVO) println(" " + pintar(b.estado.mensaje, colorDe(b.estado)))
    println("=".repeat(58))
}

private fun menu() {
    println(
        """
        1. Registrar gasto
        2. Registrar ingreso
        3. Ver ingresos y gastos del mes
        4. Gastos por categoría del mes
        5. Balance detallado
        6. Historial
        7. Cambiar de mes
        8. Eliminar un gasto
        9. Eliminar un ingreso
        0. Salir
        """.trimIndent()
    )
    print("Opción: ")
}

private fun porcentaje(parte: Double, total: Double): String =
    if (total > 0.0) String.format(Locale.US, "%.1f %%", parte / total * 100) else "0.0 %"

// ---------- Datos de ejemplo ----------

private suspend fun cargarEjemplos(control: ControlGastos) {
    val anterior = mesAnterior(mesActual())
    val actual = mesActual()
    // Mes pasado en negativo
    control.registrarIngreso(Ingreso(descripcion = "Ventas de perfumes", monto = 800.0, tipo = TipoIngreso.VENTAS, fecha = "$anterior-05"))
    control.registrar(Gasto(descripcion = "Compra de despensa", monto = 650.0, categoria = Categoria.DESPENSA, fecha = "$anterior-08"))
    control.registrar(Gasto(descripcion = "Fotocopias y libros", monto = 420.0, categoria = Categoria.UNIVERSIDAD, fecha = "$anterior-15"))
    // Mes actual en positivo
    control.registrarIngreso(Ingreso(descripcion = "Salario", monto = 3500.0, tipo = TipoIngreso.SALARIO, fecha = "$actual-01"))
    control.registrarIngreso(Ingreso(descripcion = "Ventas de perfumes", monto = 900.0, tipo = TipoIngreso.VENTAS, fecha = "$actual-02"))
    control.registrar(Gasto(descripcion = "Almuerzo", monto = 45.5, categoria = Categoria.COMIDA, fecha = "$actual-02"))
    control.registrar(Gasto(descripcion = "Pasaje", monto = 120.0, categoria = Categoria.TRANSPORTE, fecha = "$actual-03"))
    control.registrar(Gasto(descripcion = "Súper", monto = 600.0, categoria = Categoria.DESPENSA, fecha = "$actual-04"))
}

// ---------- Programa principal ----------

fun main() = runBlocking<Unit> {
    val control = ControlGastos(GastoRepositoryEnMemoria(), IngresoRepositoryEnMemoria())
    cargarEjemplos(control)
    var mes = mesActual()

    while (true) {
        encabezado(mes, control.balanceDelMes(mes))
        menu()
        when (readLine()?.trim()) {
            "1" -> {
                val descripcion = run { print("Descripción: "); readLine().orEmpty() }
                val monto = leerMonto("Monto: Q ")
                val categoria = elegir("Categoría:", Categoria.values().toList())
                val fecha = leerFecha("Fecha")
                val error = control.registrar(Gasto(descripcion = descripcion, monto = monto, categoria = categoria, fecha = fecha))
                println(if (error == null) pintar("Gasto guardado", VERDE) else pintar(error, ROJO))
            }
            "2" -> {
                val descripcion = run { print("Descripción: "); readLine().orEmpty() }
                val monto = leerMonto("Monto: Q ")
                val tipo = elegir("Tipo de ingreso:", TipoIngreso.values().toList())
                val fecha = leerFecha("Fecha")
                val error = control.registrarIngreso(Ingreso(descripcion = descripcion, monto = monto, tipo = tipo, fecha = fecha))
                println(if (error == null) pintar("Ingreso guardado", VERDE) else pintar(error, ROJO))
            }
            "3" -> {
                val ingresos = control.ingresosDelMes(mes)
                val gastos = control.gastosDelMes(mes)
                println(pintar("\nINGRESOS", CIAN))
                if (ingresos.isEmpty()) println("  (ninguno)")
                ingresos.sortedBy { it.fecha }.forEach {
                    println("  #${it.id}  ${it.fecha}  +${formatearQuetzales(it.monto)}  ${it.descripcion} (${it.tipo})")
                }
                println(pintar("\nGASTOS", CIAN))
                if (gastos.isEmpty()) println("  (ninguno)")
                gastos.sortedBy { it.fecha }.forEach {
                    println("  #${it.id}  ${it.fecha}  -${formatearQuetzales(it.monto)}  ${it.descripcion} (${it.categoria})")
                }
            }
            "4" -> {
                val b = control.balanceDelMes(mes)
                println(pintar("\nGASTOS POR CATEGORÍA", CIAN))
                if (b.gastosPorCategoria.isEmpty()) println("  (sin gastos)")
                b.gastosPorCategoria.entries.sortedByDescending { it.value }.forEach { (cat, monto) ->
                    println("  ${cat.texto.padEnd(16)} ${formatearQuetzales(monto).padStart(12)}   ${porcentaje(monto, b.totalGastos)}")
                }
            }
            "5" -> {
                val b = control.balanceDelMes(mes)
                println(pintar("\nBALANCE DETALLADO", CIAN))
                println("Ingresos por tipo:")
                if (b.ingresosPorTipo.isEmpty()) println("  (ninguno)")
                b.ingresosPorTipo.forEach { (tipo, monto) -> println("  ${tipo.texto.padEnd(16)} ${formatearQuetzales(monto)}") }
                println("Gastos por categoría:")
                if (b.gastosPorCategoria.isEmpty()) println("  (ninguno)")
                b.gastosPorCategoria.forEach { (cat, monto) -> println("  ${cat.texto.padEnd(16)} ${formatearQuetzales(monto)}") }
                println("Total ingresos: ${formatearQuetzales(b.totalIngresos)} (${b.cantidadIngresos} movimientos)")
                println("Total gastos:   ${formatearQuetzales(b.totalGastos)} (${b.cantidadGastos} movimientos)")
                println("Saldo:          " + pintar(formatearQuetzales(b.saldo), colorDe(b.estado)))
                println("Gastado:        ${String.format(Locale.US, "%.1f", b.porcentajeGastado)} % de los ingresos")
                println(pintar(b.estado.mensaje, colorDe(b.estado)))
            }
            "6" -> {
                println(pintar("\nHISTORIAL", CIAN))
                control.historial().forEach { b ->
                    val fila = "  ${nombreMes(b.mes).padEnd(18)} Ingresos ${formatearQuetzales(b.totalIngresos).padStart(12)}   " +
                        "Gastos ${formatearQuetzales(b.totalGastos).padStart(12)}   Saldo ${formatearQuetzales(b.saldo).padStart(12)}"
                    println(if (b.estado == EstadoBalance.NEGATIVO) pintar(fila, ROJO) else fila)
                }
                println("\nSaldo acumulado: ${formatearQuetzales(control.saldoAcumulado())}")
            }
            "7" -> {
                print("< anterior, > siguiente, yyyy-MM o Enter para el actual: ")
                val texto = readLine()?.trim().orEmpty()
                when {
                    texto == "<" -> mes = mesAnterior(mes)
                    texto == ">" -> mes = mesSiguiente(mes)
                    texto.isEmpty() -> mes = mesActual()
                    esMesValido(texto) -> mes = texto
                    else -> println(pintar("Mes inválido. Usa yyyy-MM, por ejemplo 2026-10", ROJO))
                }
            }
            "8" -> {
                val id = leerId("Id del gasto a eliminar: ")
                val borrado = id != null && control.eliminar(id)
                println(if (borrado) pintar("Gasto eliminado", VERDE) else pintar("No existe un gasto con ese id", ROJO))
            }
            "9" -> {
                val id = leerId("Id del ingreso a eliminar: ")
                val borrado = id != null && control.eliminarIngreso(id)
                println(if (borrado) pintar("Ingreso eliminado", VERDE) else pintar("No existe un ingreso con ese id", ROJO))
            }
            "0", null -> {
                println("¡Hasta luego!")
                return@runBlocking
            }
            else -> println(pintar("Opción inválida", ROJO))
        }
    }
}
