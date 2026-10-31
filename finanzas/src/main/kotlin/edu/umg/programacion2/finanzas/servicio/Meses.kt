package edu.umg.programacion2.finanzas.servicio

import java.util.Calendar

/*
 * Funciones para trabajar con meses en formato yyyy-MM.
 * Usa Calendar y no java.time porque java.time no existe en Android antes de la API 26.
 */

private val NOMBRES_MES = arrayOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

private val PATRON_FECHA = Regex("""^\d{4}-\d{2}-\d{2}$""")
private val PATRON_MES = Regex("""^\d{4}-\d{2}$""")

/** "2026-10-03" -> true. Revisa el formato, mes 1-12 y día 1-31. */
fun esFechaValida(fecha: String): Boolean {
    if (!PATRON_FECHA.matches(fecha)) return false
    val mes = fecha.substring(5, 7).toInt()
    val dia = fecha.substring(8, 10).toInt()
    return mes in 1..12 && dia in 1..31
}

/** "2026-10" -> true. */
fun esMesValido(mes: String): Boolean {
    if (!PATRON_MES.matches(mes)) return false
    return mes.substring(5, 7).toInt() in 1..12
}

/** "2026-10-03" -> "2026-10". Es la base del control mensual. */
fun mesDe(fecha: String): String = fecha.substring(0, 7)

/** El mes de hoy. Cuando cambia el mes del calendario, cambia solo. */
fun mesActual(): String {
    val c = Calendar.getInstance()
    return "%04d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1)
}

/** La fecha de hoy en formato yyyy-MM-dd. */
fun fechaHoy(): String {
    val c = Calendar.getInstance()
    return "%04d-%02d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
}

/** "2026-01" -> "2025-12". */
fun mesAnterior(mes: String): String = moverMes(mes, -1)

/** "2026-12" -> "2027-01". */
fun mesSiguiente(mes: String): String = moverMes(mes, 1)

/** "2026-10" -> "Octubre 2026". */
fun nombreMes(mes: String): String {
    val anio = mes.substring(0, 4)
    val numero = mes.substring(5, 7).toInt()
    return "${NOMBRES_MES[numero - 1]} $anio"
}

private fun moverMes(mes: String, cantidad: Int): String {
    val c = Calendar.getInstance()
    c.clear()
    c.set(mes.substring(0, 4).toInt(), mes.substring(5, 7).toInt() - 1, 1)
    c.add(Calendar.MONTH, cantidad)
    return "%04d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1)
}
