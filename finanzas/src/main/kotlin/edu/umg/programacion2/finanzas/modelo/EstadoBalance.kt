package edu.umg.programacion2.finanzas.modelo

/** Estado del mes. Cada estado trae su propio mensaje para mostrar. */
enum class EstadoBalance(val mensaje: String) {
    SIN_MOVIMIENTOS("Sin movimientos este mes"),
    POSITIVO("Vas bien: tus ingresos cubren tus gastos"),
    CUIDADO("Cuidado: ya gastaste el 80 % o más de tus ingresos"),
    NEGATIVO("¡Vamos mal! Los gastos superan a los ingresos")
}
