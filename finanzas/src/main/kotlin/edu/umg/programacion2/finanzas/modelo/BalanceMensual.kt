package edu.umg.programacion2.finanzas.modelo

/**
 * Control de un mes (formato yyyy-MM).
 * El saldo, el porcentaje gastado y el estado se calculan solos con get().
 */
data class BalanceMensual(
    val mes: String,
    val totalIngresos: Double,
    val totalGastos: Double,
    val ingresosPorTipo: Map<TipoIngreso, Double>,
    val gastosPorCategoria: Map<Categoria, Double>,
    val cantidadIngresos: Int,
    val cantidadGastos: Int
) {
    /** Ingresos menos gastos. */
    val saldo: Double
        get() = totalIngresos - totalGastos

    /** Qué porcentaje de los ingresos ya se gastó (0 si no hubo ingresos ni gastos). */
    val porcentajeGastado: Double
        get() = when {
            totalIngresos > 0.0 -> totalGastos / totalIngresos * 100.0
            totalGastos > 0.0 -> 100.0
            else -> 0.0
        }

    val estado: EstadoBalance
        get() = when {
            cantidadIngresos == 0 && cantidadGastos == 0 -> EstadoBalance.SIN_MOVIMIENTOS
            // Medio centavo de margen por los errores de redondeo de Double
            saldo < -0.005 -> EstadoBalance.NEGATIVO
            totalIngresos > 0.0 && totalGastos >= totalIngresos * 0.8 -> EstadoBalance.CUIDADO
            else -> EstadoBalance.POSITIVO
        }
}
