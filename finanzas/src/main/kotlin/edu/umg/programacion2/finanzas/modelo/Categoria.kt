package edu.umg.programacion2.finanzas.modelo

/** Categorías de gasto. El texto visible es el que se ve en consola y en el Spinner. */
enum class Categoria(val texto: String) {
    DESPENSA("Despensa"),
    COMIDA("Comida"),
    UNIVERSIDAD("Universidad"),
    TRANSPORTE("Transporte"),
    SALUD("Salud"),
    ENTRETENIMIENTO("Entretenimiento"),
    OTROS("Otros");

    // Sin esto el Spinner mostraría "DESPENSA" en vez de "Despensa"
    override fun toString(): String = texto
}
