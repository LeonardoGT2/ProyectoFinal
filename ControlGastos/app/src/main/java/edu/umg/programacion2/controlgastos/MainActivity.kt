package edu.umg.programacion2.controlgastos   // <- cámbialo por el package de tu proyecto Android

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import edu.umg.programacion2.finanzas.modelo.Categoria
import edu.umg.programacion2.finanzas.modelo.EstadoBalance
import edu.umg.programacion2.finanzas.modelo.Gasto
import edu.umg.programacion2.finanzas.modelo.Ingreso
import edu.umg.programacion2.finanzas.modelo.TipoIngreso
import edu.umg.programacion2.finanzas.servicio.fechaHoy
import edu.umg.programacion2.finanzas.servicio.formatearQuetzales
import edu.umg.programacion2.finanzas.servicio.mesActual
import edu.umg.programacion2.finanzas.servicio.mesAnterior
import edu.umg.programacion2.finanzas.servicio.mesSiguiente
import edu.umg.programacion2.finanzas.servicio.nombreMes
import kotlinx.coroutines.launch

/** Un renglón de la lista: puede ser un gasto o un ingreso. Solo sirve para dibujar la pantalla. */
private data class Movimiento(
    val esGasto: Boolean,
    val id: Int,
    val fecha: String,
    val descripcion: String,
    val monto: Double,
    val tipoTexto: String,
    val icono: String
)

private fun iconoDe(categoria: Categoria): String = when (categoria) {
    Categoria.DESPENSA -> "🛒"
    Categoria.COMIDA -> "🍔"
    Categoria.UNIVERSIDAD -> "🎓"
    Categoria.TRANSPORTE -> "🚌"
    Categoria.SALUD -> "💊"
    Categoria.ENTRETENIMIENTO -> "🎬"
    Categoria.OTROS -> "📦"
}

private fun iconoDe(tipo: TipoIngreso): String = when (tipo) {
    TipoIngreso.SALARIO -> "💼"
    TipoIngreso.VENTAS -> "🛍️"
    TipoIngreso.OTROS_INGRESOS -> "💰"
}

class MainActivity : AppCompatActivity() {

    companion object {
        private const val CLAVE_MES = "mes_visto"
    }

    // Los datos viven en SQLite (Room): sobreviven a girar el teléfono y a cerrar la app
    private val control = Dependencias.control

    // Mes que se está viendo (empieza en el mes actual)
    private var mesVisto = mesActual()

    private lateinit var btnAnterior: TextView
    private lateinit var tvMes: TextView
    private lateinit var btnSiguiente: TextView
    private lateinit var cardBalance: MaterialCardView
    private lateinit var tvEtiquetaBalance: TextView
    private lateinit var tvBalance: TextView
    private lateinit var tvEstado: TextView
    private lateinit var pbGastado: ProgressBar
    private lateinit var tvPorcentaje: TextView
    private lateinit var tvIngresos: TextView
    private lateinit var tvGastos: TextView
    private lateinit var rgTipo: RadioGroup
    private lateinit var rbGasto: RadioButton
    private lateinit var rbIngreso: RadioButton
    private lateinit var spOpcion: Spinner
    private lateinit var etDescripcion: EditText
    private lateinit var etMonto: EditText
    private lateinit var btnGuardar: Button
    private lateinit var llMovimientos: LinearLayout
    private lateinit var tvVacio: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        savedInstanceState?.getString(CLAVE_MES)?.let { mesVisto = it }

        conectarVistas()
        llenarSpinner()

        // Gasto o Ingreso: cambia lo que muestra la lista desplegable
        rgTipo.setOnCheckedChangeListener { _, _ -> llenarSpinner() }

        btnGuardar.setOnClickListener { guardar() }

        btnAnterior.setOnClickListener {
            mesVisto = mesAnterior(mesVisto)
            refrescar()
        }
        btnSiguiente.setOnClickListener {
            mesVisto = mesSiguiente(mesVisto)
            refrescar()
        }

        refrescar()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CLAVE_MES, mesVisto)
    }

    /** Conecta cada vista del XML con su variable por el id. */
    private fun conectarVistas() {
        btnAnterior = findViewById(R.id.btnAnterior)
        tvMes = findViewById(R.id.tvMes)
        btnSiguiente = findViewById(R.id.btnSiguiente)
        cardBalance = findViewById(R.id.cardBalance)
        tvEtiquetaBalance = findViewById(R.id.tvEtiquetaBalance)
        tvBalance = findViewById(R.id.tvBalance)
        tvEstado = findViewById(R.id.tvEstado)
        pbGastado = findViewById(R.id.pbGastado)
        tvPorcentaje = findViewById(R.id.tvPorcentaje)
        tvIngresos = findViewById(R.id.tvIngresos)
        tvGastos = findViewById(R.id.tvGastos)
        rgTipo = findViewById(R.id.rgTipo)
        rbGasto = findViewById(R.id.rbGasto)
        rbIngreso = findViewById(R.id.rbIngreso)
        spOpcion = findViewById(R.id.spOpcion)
        etDescripcion = findViewById(R.id.etDescripcion)
        etMonto = findViewById(R.id.etMonto)
        btnGuardar = findViewById(R.id.btnGuardar)
        llMovimientos = findViewById(R.id.llMovimientos)
        tvVacio = findViewById(R.id.tvVacio)
    }

    /** Gasto -> categorías. Ingreso -> tipos de ingreso. */
    private fun llenarSpinner() {
        val opciones: List<Any> =
            if (rbGasto.isChecked) Categoria.values().toList() else TipoIngreso.values().toList()
        val adaptador = ArrayAdapter(this, R.layout.item_opcion, opciones)
        adaptador.setDropDownViewResource(R.layout.item_opcion)
        spOpcion.adapter = adaptador
    }

    private fun guardar() {
        val descripcion = etDescripcion.text.toString()
        val monto = etMonto.text.toString().trim().replace(',', '.').toDoubleOrNull() ?: 0.0
        // En el mes actual se usa la fecha de hoy; en otro mes, el día 1 de ese mes
        val fecha = if (mesVisto == mesActual()) fechaHoy() else "$mesVisto-01"
        val esGasto = rbGasto.isChecked
        val seleccion = spOpcion.selectedItem

        lifecycleScope.launch {
            val error = if (esGasto) {
                control.registrar(Gasto(descripcion = descripcion, monto = monto, categoria = seleccion as Categoria, fecha = fecha))
            } else {
                control.registrarIngreso(Ingreso(descripcion = descripcion, monto = monto, tipo = seleccion as TipoIngreso, fecha = fecha))
            }

            if (error != null) {
                Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                return@launch
            }

            etDescripcion.text.clear()
            etMonto.text.clear()
            refrescar()

            if (control.balanceDelMes(mesVisto).estado == EstadoBalance.NEGATIVO) {
                Toast.makeText(this@MainActivity, "¡Vamos mal! Los gastos superan a los ingresos", Toast.LENGTH_LONG).show()
            }
        }
    }

    /** Pide el balance del mes y pinta todo: tarjeta de balance, totales y lista de movimientos. */
    private fun refrescar() {
        lifecycleScope.launch {
            val balance = control.balanceDelMes(mesVisto)

            tvMes.text = nombreMes(mesVisto)
            tvBalance.text = formatearQuetzales(balance.saldo)
            tvEstado.text = balance.estado.mensaje
            tvIngresos.text = formatearQuetzales(balance.totalIngresos)
            tvGastos.text = formatearQuetzales(balance.totalGastos)

            val porcentaje = balance.porcentajeGastado
            pbGastado.progress = porcentaje.coerceIn(0.0, 100.0).toInt()
            tvPorcentaje.text = "Gastado: ${porcentaje.toInt()} % de los ingresos"

            aplicarColoresDelEstado(balance.estado)

            // Lista del mes: ingresos y gastos juntos, del más reciente al más viejo
            val movimientos = mutableListOf<Movimiento>()
            control.ingresosDelMes(mesVisto).forEach {
                movimientos.add(Movimiento(false, it.id, it.fecha, it.descripcion, it.monto, it.tipo.texto, iconoDe(it.tipo)))
            }
            control.gastosDelMes(mesVisto).forEach {
                movimientos.add(Movimiento(true, it.id, it.fecha, it.descripcion, it.monto, it.categoria.texto, iconoDe(it.categoria)))
            }
            movimientos.sortWith(compareByDescending<Movimiento> { it.fecha }.thenByDescending { it.id })

            pintarMovimientos(movimientos)
        }
    }

    /** La tarjeta del balance cambia de color según el estado; el texto encima usa su color "fg". */
    private fun aplicarColoresDelEstado(estado: EstadoBalance) {
        val (fondoRes, textoRes) = when (estado) {
            EstadoBalance.NEGATIVO -> R.color.estado_neg_bg to R.color.estado_neg_fg
            EstadoBalance.CUIDADO -> R.color.estado_cui_bg to R.color.estado_cui_fg
            EstadoBalance.POSITIVO -> R.color.estado_pos_bg to R.color.estado_pos_fg
            EstadoBalance.SIN_MOVIMIENTOS -> R.color.estado_sin_bg to R.color.estado_sin_fg
        }
        val texto = ContextCompat.getColor(this, textoRes)
        cardBalance.setCardBackgroundColor(ContextCompat.getColor(this, fondoRes))
        tvEtiquetaBalance.setTextColor(texto)
        tvBalance.setTextColor(texto)
        tvEstado.setTextColor(texto)
        tvPorcentaje.setTextColor(texto)
        pbGastado.progressTintList = ColorStateList.valueOf(texto)
        pbGastado.progressBackgroundTintList = ColorStateList.valueOf(ColorUtils.setAlphaComponent(texto, 60))
    }

    /** Dibuja una tarjeta por movimiento. Tocarla pide confirmación para eliminar. */
    private fun pintarMovimientos(movimientos: List<Movimiento>) {
        llMovimientos.removeAllViews()
        tvVacio.visibility = if (movimientos.isEmpty()) View.VISIBLE else View.GONE

        for (m in movimientos) {
            val item = layoutInflater.inflate(R.layout.item_movimiento, llMovimientos, false)
            val colorMonto = ContextCompat.getColor(this, if (m.esGasto) R.color.gasto else R.color.ingreso)

            item.findViewById<TextView>(R.id.tvIcono).apply {
                text = m.icono
                backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this@MainActivity, if (m.esGasto) R.color.badge_gasto_bg else R.color.badge_ingreso_bg)
                )
            }
            item.findViewById<TextView>(R.id.tvDescripcionItem).text = m.descripcion
            item.findViewById<TextView>(R.id.tvDetalleItem).text = "${m.tipoTexto} · ${m.fecha}"
            item.findViewById<TextView>(R.id.tvMontoItem).apply {
                text = (if (m.esGasto) "-" else "+") + formatearQuetzales(m.monto)
                setTextColor(colorMonto)
            }

            item.setOnClickListener { confirmarEliminacion(m) }
            llMovimientos.addView(item)
        }
    }

    /** Pregunta antes de borrar: eliminar no se puede deshacer. */
    private fun confirmarEliminacion(m: Movimiento) {
        val tipo = if (m.esGasto) "gasto" else "ingreso"
        AlertDialog.Builder(this)
            .setTitle("Eliminar $tipo")
            .setMessage("¿Eliminar \"${m.descripcion}\" (${formatearQuetzales(m.monto)})?\nEsta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ -> eliminar(m) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminar(m: Movimiento) {
        lifecycleScope.launch {
            val borrado = if (m.esGasto) control.eliminar(m.id) else control.eliminarIngreso(m.id)
            val mensaje = if (borrado) "Movimiento eliminado" else "Ese movimiento ya no existe"
            Toast.makeText(this@MainActivity, mensaje, Toast.LENGTH_SHORT).show()
            refrescar()
        }
    }
}
