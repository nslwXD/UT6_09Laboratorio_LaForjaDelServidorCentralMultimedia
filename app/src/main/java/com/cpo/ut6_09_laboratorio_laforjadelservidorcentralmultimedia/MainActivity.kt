package com.cpo.ut6_09_laboratorio_laforjadelservidorcentralmultimedia

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

// consola de mando de celebrimbor: se elige un canal en el spinner,
// exoplayer lo reproduce en el playerview y un listener vigila el estado del bufer.
// cuando la transmision termina suena una alarma con mediaplayer en paralelo
class MainActivity : AppCompatActivity() {

    // lateinit para poder usar las variables antes de que
    // sean asignadas con datos
    private lateinit var spCanales: Spinner
    private lateinit var pvMonitor: PlayerView
    private lateinit var tvEstado: TextView

    // los dos "motores" de hardware. son nullables (?) porque
    // al liberarlos los ponemos a null
    private var exoPlayer: ExoPlayer? = null
    private var alarma: MediaPlayer? = null

    // direccion del video de gondor guardado en res/raw.
    // android.resource://paquete/id es la forma de apuntar a un recurso de la app.
    // by lazy: se calcula la primera vez que se usa (ya dentro de oncreate),
    // porque antes de eso la actividad aun no conoce su packagename
    private val videoGondor by lazy { "android.resource://$packageName/${R.raw.gondor_video}" }

    // mapa de canales: nombre del puesto -> direccion del video.
    // los tres puestos emiten el video de gondor.
    // mapof mantiene el orden de insercion, asi el spinner los muestra en este orden
    private val canales by lazy {
        mapOf(
            "Minas Tirith" to videoGondor,
            "Isengard" to videoGondor,
            "Rivendell" to videoGondor
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // enlazamos las variables con las vistas del xml usando su id
        spCanales = findViewById(R.id.spCanales)
        pvMonitor = findViewById(R.id.pvMonitor)
        tvEstado = findViewById(R.id.tvEstado)

        // ---------- exoplayer ----------

        // builder: construimos el motor exoplayer
        exoPlayer = ExoPlayer.Builder(this).build()

        // el playerview es solo la "pantalla", el que reproduce es exoplayer
        pvMonitor.player = exoPlayer

        // listener: objeto de escucha al que exoplayer avisa cada vez que cambia algo.
        // onplaybackstatechanged se llama en cada mutacion del estado del bufer
        exoPlayer?.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    // buffering: esta descargando/procesando datos del canal
                    Player.STATE_BUFFERING -> tvEstado.text = "Cargando Búfer..."
                    // ready: hay datos suficientes y ya se esta viendo
                    Player.STATE_READY -> tvEstado.text = "Transmitiendo en vivo"
                    // ended: el video ha terminado -> aviso por texto y alarma sonora
                    Player.STATE_ENDED -> {
                        tvEstado.text = "Conexión terminada"
                        dispararAlarma()
                    }
                }
            }

            // el enunciado pide avisar tambien si la transmision se corta
            // (error de red, url caida...), asi que suena la misma alarma
            override fun onPlayerError(error: PlaybackException) {
                tvEstado.text = "Conexión terminada"
                dispararAlarma()
            }
        })

        // ---------- spinner ----------

        // arrayadapter: adaptador sencillo que convierte una lista de textos en filas.
        // usamos las claves del mapa (los nombres de los puestos).
        // simple_spinner_item y simple_spinner_dropdown_item son layouts que ya trae android
        val adaptador = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            canales.keys.toList()
        )
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCanales.adapter = adaptador

        // onitemselected se ejecuta al elegir un canal (y tambien una vez al arrancar
        // con el primero, asi el monitor empieza ya sintonizado)
        spCanales.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // con la posicion sacamos el nombre y con el nombre la url del mapa
                val nombre = parent?.getItemAtPosition(position) as String
                val url = canales[nombre] ?: return

                // actualizamos la fuente de datos del reproductor
                exoPlayer?.setMediaItem(MediaItem.fromUri(url))
                // prepare empieza a llenar el bufer (dispara STATE_BUFFERING)
                exoPlayer?.prepare()
                // playwhenready: en cuanto este listo empieza a reproducir solo
                exoPlayer?.playWhenReady = true
            }

            // obligatorio implementarlo aunque no lo usemos
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // ---------- alarma acustica (mediaplayer) ----------

    // mediaplayer es independiente de exoplayer, asi que suena en paralelo
    // sin bloquear el video ni la interfaz
    private fun dispararAlarma() {
        // si ya habia una alarma sonando la liberamos antes de crear otra
        alarma?.release()

        // create carga el archivo de res/raw y deja el mediaplayer preparado
        alarma = MediaPlayer.create(this, R.raw.campanas_alarma)
        alarma?.start()
    }

    // ---------- liberacion de memoria ----------

    // al finalizar la actividad liberamos la ram de los dos motores.
    // si no se hace, el audio/video seguiria ocupando memoria y hardware
    override fun onDestroy() {
        super.onDestroy()
        alarma?.release()
        alarma = null
        exoPlayer?.release()
        exoPlayer = null
    }
}
