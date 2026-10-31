package edu.umg.programacion2.controlgastos

import android.app.Application

/**
 * Application = el objeto de TODA la app. Android lo crea una vez, antes que cualquier
 * Activity. Aquí se abre la base de datos para que Dependencias.control esté listo.
 * Se registra en AndroidManifest.xml con android:name=".ControlGastosApp".
 */
class ControlGastosApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Dependencias.iniciar(this)
    }
}
