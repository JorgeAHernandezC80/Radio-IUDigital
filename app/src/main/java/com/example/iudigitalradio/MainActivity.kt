package com.example.iudigitalradio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.TeeAudioProcessor
import com.example.iudigitalradio.audio.AudioSpectrumAnalyzer
import com.example.iudigitalradio.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

// Modelo de una emisora del catálogo
data class Estacion(
    val nombre: String,
    val genero: String,
    val streamUrl: String,
    val region: String,
    // Banda a la que pertenece: "FM", "AM" o "WEB" (emisoras que solo existen en internet).
    val banda: String = "WEB",
)

val catalogoEmisoras = listOf(
    Estacion("Radio Lounge", "Chill / Lounge", "https://ice1.somafm.com/groovesalad-128-mp3", "Estados Unidos"),
    Estacion("Radio Electrónica", "Deep House", "https://ice1.somafm.com/beatblender-128-mp3", "Estados Unidos"),
    Estacion("Radio Swiss Jazz", "Jazz", "http://stream.srg-ssr.ch/m/rsj/mp3_128", "Europa"),
    Estacion("Radio Swiss Pop", "Pop", "http://stream.srg-ssr.ch/m/rsp/mp3_128", "Europa"),
    Estacion("Radio UNAL Bogotá", "Radio universitaria / Cultura", "https://radio.unal.edu.co/streaming/bogota/;stream.mp3", "Colombia", banda = "FM"),
    // --- Nuevas emisoras (se agregan al final para no alterar los índices de favoritos) ---
    Estacion("Drone Zone", "Ambient", "https://ice1.somafm.com/dronezone-128-mp3", "Estados Unidos"),
    Estacion("Secret Agent", "Downtempo / Lounge", "https://ice1.somafm.com/secretagent-128-mp3", "Estados Unidos"),
    Estacion("Indie Pop Rocks", "Indie Pop", "https://ice1.somafm.com/indiepop-128-mp3", "Estados Unidos"),
    Estacion("Underground 80s", "Synthpop / New Wave", "https://ice1.somafm.com/u80s-128-mp3", "Estados Unidos"),
    Estacion("Left Coast 70s", "Rock 70s", "https://ice1.somafm.com/seventies-128-mp3", "Estados Unidos"),
    Estacion("Radio Paradise", "Ecléctico / Rock", "https://stream.radioparadise.com/mp3-128", "Estados Unidos"),
    Estacion("Radio Swiss Classic", "Clásica", "http://stream.srg-ssr.ch/m/rsc_de/mp3_128", "Europa"),
    Estacion("FIP", "Ecléctico / Jazz", "https://icecast.radiofrance.fr/fip-midfi.mp3", "Europa", banda = "FM"),
    Estacion("FIP Jazz", "Jazz", "https://icecast.radiofrance.fr/fipjazz-midfi.mp3", "Europa"),
    Estacion("FIP Groove", "Funk / Soul", "https://icecast.radiofrance.fr/fipgroove-midfi.mp3", "Europa"),
    // --- Emisoras de FM y AM tradicionales que también transmiten por internet ---
    Estacion("WNYC 93.9 FM", "Noticias / Cultura", "https://fm939.wnyc.org/wnycfm", "Estados Unidos", banda = "FM"),
    Estacion("KEXP 90.3 FM", "Indie / Alternativo", "https://kexp.streamguys1.com/kexp160.aac", "Estados Unidos", banda = "FM"),
    Estacion("WNYC AM 820", "Noticias / Talk", "https://am820.wnyc.org/wnycam", "Estados Unidos", banda = "AM"),
    Estacion("WQXR 105.9 FM", "Música Clásica", "https://stream.wqxr.org/wqxr", "Estados Unidos", banda = "FM")
)

/**
 * Índices del catálogo que corresponden a la banda elegida en el selector.
 * "" (ninguna banda activa) = todas las emisoras.
 */
fun indicesParaBanda(banda: String, favoritos: Set<String>): List<Int> =
    catalogoEmisoras.indices.filter { i ->
        val e = catalogoEmisoras[i]
        when (banda) {
            "FAVORITOS" -> e.streamUrl in favoritos
            "FM", "AM", "WEB" -> e.banda == banda
            else -> true
        }
    }

// ============================================================
//  PERFIL DE USUARIO: la foto se guarda como archivo en el
//  almacenamiento interno de la app y el nombre en SharedPreferences,
//  así se conservan aunque se cierre la app.
// ============================================================

private const val ARCHIVO_FOTO_PERFIL = "foto_perfil.jpg"

fun cargarFotoPerfil(context: Context): Bitmap? {
    val archivo = File(context.filesDir, ARCHIVO_FOTO_PERFIL)
    return if (archivo.exists()) BitmapFactory.decodeFile(archivo.absolutePath) else null
}

fun guardarFotoPerfil(context: Context, bitmap: Bitmap) {
    File(context.filesDir, ARCHIVO_FOTO_PERFIL).outputStream().use { salida ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, salida)
    }
}

fun borrarFotoPerfil(context: Context) {
    File(context.filesDir, ARCHIVO_FOTO_PERFIL).delete()
}

/** Lee una imagen de la galería reduciéndola (~512 px) para no gastar memoria con fotos grandes. */
fun cargarBitmapDesdeUri(context: Context, uri: Uri): Bitmap? = try {
    val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, medidas) }
    var muestra = 1
    while (medidas.outWidth / (muestra * 2) >= 512 && medidas.outHeight / (muestra * 2) >= 512) muestra *= 2
    val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
} catch (e: Exception) {
    null
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            IUDigitalRadioTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = RadioDeepBlue) {
                    PantallaConBienvenida()
                }
            }
        }
    }
}

fun vibrarDispositivo(context: Context) {
    val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    if (vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(45)
        }
    }
}

// ============================================================
//  BIENVENIDA (splash en Compose): logo nítido + nombre de la
//  emisora, con animación de entrada. Se muestra un instante
//  apenas abre la app y luego cede el paso al reproductor.
// ============================================================

@Composable
fun PantallaConBienvenida() {
    var mostrarBienvenida by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1900)
        mostrarBienvenida = false
    }

    Crossfade(targetState = mostrarBienvenida, animationSpec = tween(550), label = "bienvenida") { mostrando ->
        if (mostrando) SplashPersonalizado() else PantallaPrincipal()
    }
}

@Composable
fun SplashPersonalizado() {
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progreso.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF020711), RadioDeepBlue, Color(0xFF06152A)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                // Resplandor detrás del logo
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .graphicsLayer { alpha = progreso.value }
                        .background(
                            Brush.radialGradient(listOf(RadioCyan.copy(alpha = .30f), Color.Transparent))
                        )
                )
                Image(
                    painter = painterResource(R.drawable.splash_hero),
                    contentDescription = "Radio IU Digital",
                    modifier = Modifier
                        .size(172.dp)
                        .graphicsLayer {
                            val escala = 0.82f + 0.18f * progreso.value
                            scaleX = escala
                            scaleY = escala
                            alpha = progreso.value
                        }
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.graphicsLayer { alpha = progreso.value }
            ) {
                Text("RADIO", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                Spacer(Modifier.width(8.dp))
                Text("IU DIGITAL", color = RadioCyan, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "TU MÚSICA, SIEMPRE",
                color = RadioTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp,
                modifier = Modifier.graphicsLayer { alpha = progreso.value }
            )

            Spacer(Modifier.height(36.dp))

            // Barra de carga estilizada
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF14202E))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progreso.value.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(2.dp))
                        .background(Brush.horizontalGradient(listOf(RadioBlue, RadioCyan)))
                )
            }
        }
    }
}

// ============================================================
//  PANTALLA PRINCIPAL
// ============================================================

@Composable
fun PantallaPrincipal() {
    val context = LocalContext.current
    // Foto de perfil: se carga del almacenamiento interno al abrir la app.
    var fotoPerfil by remember { mutableStateOf(cargarFotoPerfil(context)) }
    var estacionSeleccionada by rememberSaveable { mutableIntStateOf(0) }
    var estaReproduciendo by rememberSaveable { mutableStateOf(false) }
    var silenciado by rememberSaveable { mutableStateOf(false) }
    var estaCargando by remember { mutableStateOf(false) }
    var volumen by rememberSaveable { mutableFloatStateOf(0.78f) }
    // Banda activa del selector. "" = ninguna seleccionada, se muestran todas las emisoras.
    var preset by rememberSaveable { mutableStateOf("") }

    // Favoritos guardados en SharedPreferences: se conservan aunque se cierre la app.
    // Se guardan por URL del stream (no por posición), así reordenar o agregar
    // emisoras al catálogo no cambia cuáles están marcadas como favoritas.
    val prefs = remember { context.getSharedPreferences("iudigitalradio_prefs", Context.MODE_PRIVATE) }
    var favoritos by remember {
        mutableStateOf(prefs.getStringSet("favoritos", emptySet())?.toSet() ?: emptySet())
    }
    fun alternarFavorito(index: Int) {
        val url = catalogoEmisoras[index].streamUrl
        favoritos = if (url in favoritos) favoritos - url else favoritos + url
        prefs.edit().putStringSet("favoritos", favoritos).apply()
        vibrarDispositivo(context)
    }

    // Nombre del perfil de usuario.
    var nombrePerfil by remember { mutableStateOf(prefs.getString("perfil_nombre", "") ?: "") }

    // Emisoras de la banda activa. Anterior/Siguiente y la perilla SINTONIZAR
    // recorren solo esta lista (si está vacía, recorren todo el catálogo).
    val indicesVisibles = remember(preset, favoritos) { indicesParaBanda(preset, favoritos) }
    val listaNavegacion = indicesVisibles.ifEmpty { catalogoEmisoras.indices.toList() }

    fun moverEstacion(paso: Int) {
        val pos = listaNavegacion.indexOf(estacionSeleccionada)
        val nueva = if (pos == -1) {
            if (paso > 0) 0 else listaNavegacion.lastIndex
        } else {
            ((pos + paso) % listaNavegacion.size + listaNavegacion.size) % listaNavegacion.size
        }
        estacionSeleccionada = listaNavegacion[nueva]
        estaReproduciendo = true
        vibrarDispositivo(context)
    }

    fun seleccionarBanda(banda: String) {
        // Tocar la banda activa otra vez la desactiva y vuelve a mostrar todas.
        preset = if (preset == banda) "" else banda
        // Como una radio real: al cambiar de banda se sintoniza la primera emisora
        // de esa banda, si la que suena ahora no pertenece a ella.
        val nuevas = indicesParaBanda(preset, favoritos)
        if (nuevas.isNotEmpty() && estacionSeleccionada !in nuevas) {
            estacionSeleccionada = nuevas.first()
        }
        vibrarDispositivo(context)
    }

    // El espectro recibe PCM real del mismo pipeline de Media3 que reproduce la radio.
    // No se generan barras aleatorias ni una animación independiente del audio.
    val spectrumAnalyzer = remember { AudioSpectrumAnalyzer() }
    val exoPlayer = remember {
        @OptIn(UnstableApi::class)
        fun createPlayer(): ExoPlayer {
            val renderersFactory = object : DefaultRenderersFactory(context) {
                override fun buildAudioSink(
                    context: Context,
                    enableFloatOutput: Boolean,
                    enableAudioTrackPlaybackParams: Boolean
                ): AudioSink {
                    // Salida PCM 16-bit estándar: con salida float, algunos decodificadores y el
                    // emulador reportan mal el formato y el audio suena acelerado.
                    // El TeeAudioProcessor solo copia el PCM para el espectro; no altera el sonido.
                    return DefaultAudioSink.Builder(context)
                        .setAudioProcessors(arrayOf(TeeAudioProcessor(spectrumAnalyzer)))
                        .setEnableFloatOutput(false)
                        .setEnableAudioTrackPlaybackParams(false)
                        .build()
                }
            }
            renderersFactory.setEnableDecoderFallback(true)

            return ExoPlayer.Builder(context, renderersFactory).build().apply {
                // Garantiza velocidad y tono normales (1x) en cada emisora.
                setPlaybackSpeed(1f)
            }
        }
        createPlayer()
    }
    val spectrumValues by spectrumAnalyzer.spectrum.collectAsState()
    val listener = remember {
        object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                estaCargando = playbackState == Player.STATE_BUFFERING
            }
        }
    }

    DisposableEffect(Unit) {
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
            spectrumAnalyzer.close()
        }
    }

    LaunchedEffect(estacionSeleccionada) {
        val estacion = catalogoEmisoras[estacionSeleccionada]
        exoPlayer.setMediaItem(MediaItem.fromUri(estacion.streamUrl))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = estaReproduciendo
    }

    LaunchedEffect(estaReproduciendo) { exoPlayer.playWhenReady = estaReproduciendo }
    LaunchedEffect(silenciado, volumen) { exoPlayer.volume = if (silenciado) 0f else volumen }

    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            fotoPerfil = bitmap
            guardarFotoPerfil(context, bitmap)
        }
    }
    // Selector de fotos del sistema: no necesita permisos de almacenamiento.
    val lanzadorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val bitmap = cargarBitmapDesdeUri(context, uri)
            if (bitmap != null) {
                fotoPerfil = bitmap
                guardarFotoPerfil(context, bitmap)
            } else {
                Toast.makeText(context, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun abrirGaleria() {
        lanzadorGaleria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    val lanzadorPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) lanzadorCamara.launch(null)
        else Toast.makeText(context, context.getString(R.string.permiso_camara_denegado), Toast.LENGTH_SHORT).show()
    }
    fun abrirCamara() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            lanzadorCamara.launch(null)
        } else lanzadorPermiso.launch(Manifest.permission.CAMERA)
    }

    PantallaPrincipalContenido(
        estacionSeleccionada = estacionSeleccionada,
        estaReproduciendo = estaReproduciendo,
        estaCargando = estaCargando,
        silenciado = silenciado,
        volumen = volumen,
        preset = preset,
        favoritos = favoritos,
        listaNavegacion = listaNavegacion,
        spectrumValues = spectrumValues,
        fotoPerfil = fotoPerfil,
        nombrePerfil = nombrePerfil,
        onPrevious = { moverEstacion(-1) },
        onPlayPause = { estaReproduciendo = !estaReproduciendo; vibrarDispositivo(context) },
        onNext = { moverEstacion(+1) },
        onVolumenChange = { nuevo ->
            volumen = nuevo
            silenciado = false
        },
        onSintonizar = { index ->
            estacionSeleccionada = index
            estaReproduciendo = true
        },
        onBandaSelect = ::seleccionarBanda,
        onToggleSilencio = {
            silenciado = !silenciado
            vibrarDispositivo(context)
        },
        onToggleFavorito = ::alternarFavorito,
        onSeleccionarEstacion = { index ->
            estacionSeleccionada = index
            estaReproduciendo = true
            vibrarDispositivo(context)
        },
        onTomarFoto = ::abrirCamara,
        onElegirGaleria = ::abrirGaleria,
        onQuitarFoto = {
            fotoPerfil = null
            borrarFotoPerfil(context)
        },
        onGuardarNombre = { nuevo ->
            nombrePerfil = nuevo.trim()
            prefs.edit().putString("perfil_nombre", nombrePerfil).apply()
            vibrarDispositivo(context)
        }
    )
}

// ============================================================
//  CONTENIDO DE LA PANTALLA PRINCIPAL (sin estado)
//  Solo dibuja la interfaz con los datos que recibe. Toda la lógica
//  (ExoPlayer, cámara, SharedPreferences) vive en PantallaPrincipal.
//  Gracias a esta separación se puede ver la app completa en @Preview.
// ============================================================

@Composable
fun PantallaPrincipalContenido(
    estacionSeleccionada: Int,
    estaReproduciendo: Boolean,
    estaCargando: Boolean,
    silenciado: Boolean,
    volumen: Float,
    preset: String,
    favoritos: Set<String>,
    listaNavegacion: List<Int>,
    spectrumValues: FloatArray,
    fotoPerfil: Bitmap?,
    nombrePerfil: String,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onVolumenChange: (Float) -> Unit,
    onSintonizar: (Int) -> Unit,
    onBandaSelect: (String) -> Unit,
    onToggleSilencio: () -> Unit,
    onToggleFavorito: (Int) -> Unit,
    onSeleccionarEstacion: (Int) -> Unit,
    onTomarFoto: () -> Unit,
    onElegirGaleria: () -> Unit,
    onQuitarFoto: () -> Unit,
    onGuardarNombre: (String) -> Unit
) {
    val estacion = catalogoEmisoras[estacionSeleccionada]

    // Estado del scroll compartido entre la columna y la barra de scroll.
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            // El fondo llena toda la pantalla (incluso detrás de la barra de estado),
            // pero el contenido interactivo respeta los márgenes del sistema para no
            // quedar oculto debajo del reloj/batería en edge-to-edge (targetSdk 35).
            .background(Brush.verticalGradient(listOf(Color(0xFF020711), RadioDeepBlue, Color(0xFF06152A))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(scrollState)
                // A la derecha se deja espacio extra para la barra de scroll.
                .padding(start = 12.dp, end = 24.dp, top = 14.dp, bottom = 14.dp)
        ) {
            // ========== TARJETA DE PERFIL (por fuera del reproductor) ==========
            TarjetaPerfil(
                foto = fotoPerfil,
                nombre = nombrePerfil,
                onTomarFoto = onTomarFoto,
                onElegirGaleria = onElegirGaleria,
                onQuitarFoto = onQuitarFoto,
                onGuardarNombre = onGuardarNombre
            )
            Spacer(Modifier.height(14.dp))

            // ========== CHASIS METÁLICO: el reproductor "hardware" ==========
            ChasisMetalico {
                HeaderRadio(
                    playing = estaReproduciendo,
                    loading = estaCargando
                )
                Spacer(Modifier.height(14.dp))

                EspectroAudio(estaReproduciendo, spectrumValues)
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MedidorNiveles(estaReproduciendo, modifier = Modifier.width(34.dp).fillMaxHeight())

                    ControlesReproduccion(
                        estaReproduciendo = estaReproduciendo,
                        estaCargando = estaCargando,
                        onPrevious = onPrevious,
                        onPlayPause = onPlayPause,
                        onNext = onNext
                    )

                    MedidorNiveles(estaReproduciendo, modifier = Modifier.width(34.dp).fillMaxHeight())
                }
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Perilla de VOLUMEN: arrastra hacia arriba para subir, hacia abajo para bajar.
                    RotaryKnob(
                        value = volumen,
                        onValueChange = onVolumenChange,
                        label = "VOLUMEN",
                        diameter = 78.dp
                    )

                    PanelEstacion(
                        estacion = estacion,
                        playing = estaReproduciendo,
                        loading = estaCargando,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                    )

                    // Perilla de SINTONIZAR: arrastra para recorrer el catálogo de emisoras.
                    RotaryKnob(
                        value = (listaNavegacion.indexOf(estacionSeleccionada) + 1f) / listaNavegacion.size,
                        onValueChange = { nuevo ->
                            val pos = (nuevo * listaNavegacion.size).roundToInt()
                                .coerceIn(1, listaNavegacion.size) - 1
                            onSintonizar(listaNavegacion[pos])
                        },
                        label = "SINTONIZAR",
                        diameter = 78.dp
                    )
                }
                Spacer(Modifier.height(16.dp))

                BandasSelector(preset, onSelect = onBandaSelect)
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallActionButton(
                    if (silenciado) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    if (silenciado) RadioGold else RadioCyan,
                    onClick = onToggleSilencio
                )
                Text(
                    "${(volumen * 100).roundToInt()}%",
                    color = RadioTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                // Marca / desmarca como favorita la emisora que está sonando.
                val esFavorita = estacion.streamUrl in favoritos
                SmallActionButton(
                    if (esFavorita) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    if (esFavorita) RadioGold else RadioCyan
                ) { onToggleFavorito(estacionSeleccionada) }
            }
            Spacer(Modifier.height(18.dp))

            CatalogoCompacto(
                selected = estacionSeleccionada,
                favoritos = favoritos,
                banda = preset,
                onSelect = onSeleccionarEstacion,
                onToggleFavorite = onToggleFavorito
            )
            Spacer(Modifier.height(28.dp))
        }

        // Barra de scroll visible y arrastrable en el borde derecho.
        BarraScroll(
            scrollState = scrollState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .systemBarsPadding()
                .padding(top = 18.dp, bottom = 18.dp, end = 2.dp)
        )
    }
}

// ============================================================
//  BARRA DE SCROLL: visible a la derecha, se arrastra con el dedo
//  o se toca en cualquier punto del riel para saltar ahí.
// ============================================================

@Composable
fun BarraScroll(scrollState: ScrollState, modifier: Modifier = Modifier) {
    // Si todo el contenido cabe en pantalla, no hace falta barra.
    if (scrollState.maxValue <= 0) return

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var altoRiel by remember { mutableFloatStateOf(0f) }
    var arrastrando by remember { mutableStateOf(false) }

    // Tamaño del "pulgar" proporcional a la parte visible del contenido.
    // El riel ocupa casi todo el alto visible, así que sirve como referencia del viewport.
    val altoMinimo = with(density) { 44.dp.toPx() }
    fun altoPulgar(): Float {
        if (altoRiel <= 0f) return altoMinimo
        val proporcion = altoRiel / (altoRiel + scrollState.maxValue)
        return (altoRiel * proporcion).coerceIn(altoMinimo, altoRiel)
    }
    val recorrido = (altoRiel - altoPulgar()).coerceAtLeast(1f)
    val posicionPulgar = recorrido * scrollState.value / scrollState.maxValue

    Box(
        modifier = modifier
            .width(20.dp) // área táctil ancha, aunque la barra visible es delgada
            .fillMaxHeight()
            .onSizeChanged { altoRiel = it.height.toFloat() }
            .pointerInput(Unit) {
                // Tocar el riel: salta a esa posición (centrando el pulgar en el toque).
                detectTapGestures { toque ->
                    val pulgar = altoPulgar()
                    val rango = (altoRiel - pulgar).coerceAtLeast(1f)
                    val fraccion = ((toque.y - pulgar / 2f) / rango).coerceIn(0f, 1f)
                    scope.launch { scrollState.animateScrollTo((fraccion * scrollState.maxValue).toInt()) }
                }
            }
            .pointerInput(Unit) {
                // Arrastrar: mueve el contenido en proporción al recorrido del pulgar.
                detectVerticalDragGestures(
                    onDragStart = { arrastrando = true },
                    onDragEnd = { arrastrando = false },
                    onDragCancel = { arrastrando = false }
                ) { change, dragAmount ->
                    change.consume()
                    val rango = (altoRiel - altoPulgar()).coerceAtLeast(1f)
                    scrollState.dispatchRawDelta(dragAmount * scrollState.maxValue / rango)
                }
            },
        contentAlignment = Alignment.TopCenter
    ) {
        // Riel
        Box(
            Modifier
                .width(4.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF14202E))
        )
        // Pulgar (la parte que se mueve)
        Box(
            Modifier
                .offset { IntOffset(0, posicionPulgar.roundToInt()) }
                .width(if (arrastrando) 8.dp else 6.dp)
                .height(with(density) { altoPulgar().toDp() })
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.verticalGradient(
                        if (arrastrando) listOf(RadioCyanSoft, RadioCyan)
                        else listOf(RadioCyan.copy(alpha = .85f), RadioBlue.copy(alpha = .85f))
                    )
                )
        )
    }
}

// ============================================================
//  CHASIS Y ENCABEZADO
// ============================================================

@Composable
fun ChasisMetalico(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 22.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = RadioCyan.copy(alpha = .35f),
                spotColor = RadioCyan.copy(alpha = .3f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF3A4C5F), Color(0xFF1C2530), Color(0xFF32424F),
                        Color(0xFF171F28), Color(0xFF44576B)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    listOf(RadioCyan.copy(alpha = .55f), Color(0xFF0E2A3D), RadioCyan.copy(alpha = .35f))
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(16.dp),
        content = content
    )
}

@Composable
fun RadioWaveIcon(modifier: Modifier = Modifier, tint: Color) {
    Canvas(modifier) {
        val centro = Offset(size.width / 2f, size.height / 2f)
        drawCircle(color = tint, radius = size.minDimension * 0.14f, center = centro)
        for (i in 1..2) {
            val factor = size.minDimension * 0.22f * (i + 1)
            val lado = androidx.compose.ui.geometry.Size(factor * 2f, factor * 2f)
            val alpha = 1f - i * 0.28f
            drawArc(
                color = tint.copy(alpha = alpha),
                startAngle = -55f,
                sweepAngle = 110f,
                useCenter = false,
                topLeft = Offset(centro.x - factor, centro.y - factor),
                size = lado,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round)
            )
            drawArc(
                color = tint.copy(alpha = alpha),
                startAngle = 125f,
                sweepAngle = 110f,
                useCenter = false,
                topLeft = Offset(centro.x - factor, centro.y - factor),
                size = lado,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun HeaderRadio(playing: Boolean, loading: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        RadioWaveIcon(modifier = Modifier.size(28.dp), tint = RadioCyan)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("RADIO", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .5.sp)
                Spacer(Modifier.width(6.dp))
                Text("IU DIGITAL", color = RadioCyan, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .5.sp)
            }
            // El eslogan de la emisora se muestra siempre; el nombre del usuario va en la tarjeta de perfil.
            Text("TU MÚSICA, SIEMPRE", color = RadioTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.4.sp)
        }
        EstadoPill(playing, loading)
    }
}

/** Foto circular del usuario, o un ícono de persona si todavía no hay foto. */
@Composable
fun AvatarPerfil(foto: Bitmap?, tamano: Dp) {
    Box(
        modifier = Modifier
            .size(tamano)
            .clip(CircleShape)
            .background(Color(0xFF0B1A2D))
            .border(1.5.dp, RadioCyan.copy(alpha = .6f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (foto != null) {
            Image(
                bitmap = foto.asImageBitmap(),
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            Icon(Icons.Default.Person, "Sin foto de perfil", tint = RadioCyan, modifier = Modifier.size(tamano * 0.55f))
        }
    }
}

// ============================================================
//  TARJETA DE PERFIL: caja independiente arriba del reproductor.
//  Cerrada muestra foto + nombre; al tocar "editar" se despliegan
//  los botones de cámara/galería y el campo del nombre.
// ============================================================

@Composable
fun TarjetaPerfil(
    foto: Bitmap?,
    nombre: String,
    onTomarFoto: () -> Unit,
    onElegirGaleria: () -> Unit,
    onQuitarFoto: () -> Unit,
    onGuardarNombre: (String) -> Unit,
    editandoInicial: Boolean = false
) {
    var editando by rememberSaveable { mutableStateOf(editandoInicial) }
    var nombreEditado by rememberSaveable(nombre) { mutableStateOf(nombre) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF0D1B2B), Color(0xFF071321))))
            .border(1.dp, RadioCyan.copy(alpha = .35f), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        // ---------- Fila principal: foto, nombre y botón editar ----------
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(56.dp)
                    .clickable { editando = true }
            ) {
                AvatarPerfil(foto = foto, tamano = 56.dp)
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(RadioCyan)
                        .border(1.5.dp, Color(0xFF0B1A2D), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color(0xFF0B1A2D), modifier = Modifier.size(11.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("MI PERFIL", color = RadioCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(2.dp))
                Text(
                    if (nombre.isNotBlank()) nombre else "Agrega tu nombre y tu foto",
                    color = if (nombre.isNotBlank()) Color.White else RadioTextMuted,
                    fontSize = if (nombre.isNotBlank()) 17.sp else 13.sp,
                    fontWeight = if (nombre.isNotBlank()) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!editando) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0A182A))
                        .border(1.dp, RadioCyan.copy(alpha = .35f), RoundedCornerShape(12.dp))
                        .clickable { editando = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, "Editar perfil", tint = RadioCyan, modifier = Modifier.size(18.dp))
                }
            }
        }

        // ---------- Panel de edición ----------
        if (editando) {
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotonPerfil(Icons.Default.CameraAlt, "Cámara", Modifier.weight(1f), onTomarFoto)
                BotonPerfil(Icons.Default.PhotoLibrary, "Galería", Modifier.weight(1f), onElegirGaleria)
            }
            if (foto != null) {
                Text(
                    "Quitar foto",
                    color = RadioTextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onQuitarFoto)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = nombreEditado,
                onValueChange = { nombreEditado = it.take(24) },
                label = { Text("Tu nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = RadioCyan,
                    unfocusedBorderColor = Color(0xFF223140),
                    focusedLabelColor = RadioCyan,
                    unfocusedLabelColor = RadioTextMuted,
                    cursorColor = RadioCyan
                )
            )
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF223140), RoundedCornerShape(12.dp))
                        .clickable {
                            nombreEditado = nombre
                            editando = false
                        }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CERRAR", color = RadioTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(RadioBlue, RadioCyan)))
                        .clickable {
                            onGuardarNombre(nombreEditado)
                            editando = false
                        }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("GUARDAR", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
private fun BotonPerfil(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0E1620))
            .border(1.dp, RadioCyan.copy(alpha = .5f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp)
    ) {
        Icon(icon, null, tint = RadioCyan, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, color = RadioCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EstadoPill(playing: Boolean, loading: Boolean) {
    val (label, color) = when {
        loading -> "CONECTANDO" to RadioGold
        playing -> "EN VIVO" to Color(0xFFFF3B4E)
        else -> "EN PAUSA" to RadioTextMuted
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color(0xFF160B0D), RoundedCornerShape(20.dp))
            .border(1.dp, color.copy(alpha = .8f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = .6.sp)
    }
}

// ============================================================
//  ESPECTRO DE AUDIO: ecualizador de LEDs (rojo/amarillo/verde)
//  con ondas suaves superpuestas, con datos reales de FFT.
// ============================================================

/** Color LED según la fila (de arriba hacia abajo): rojo -> naranja/amarillo -> verde. */
private fun colorFilaLed(filaDesdeArriba: Int, totalFilas: Int): Color = when {
    filaDesdeArriba < (totalFilas * 0.22f) -> Color(0xFFFF3B30)
    filaDesdeArriba < (totalFilas * 0.45f) -> Color(0xFFFF9500)
    filaDesdeArriba < (totalFilas * 0.68f) -> Color(0xFFFFD500)
    else -> Color(0xFF34D058)
}

/** Curva suave (spline por cuadráticas) a través de una lista de puntos. */
private fun trazoSuave(puntos: List<Offset>): androidx.compose.ui.graphics.Path {
    val path = androidx.compose.ui.graphics.Path()
    if (puntos.isEmpty()) return path
    path.moveTo(puntos[0].x, puntos[0].y)
    for (i in 0 until puntos.size - 1) {
        val p0 = puntos[i]
        val p1 = puntos[i + 1]
        val mid = Offset((p0.x + p1.x) / 2f, (p0.y + p1.y) / 2f)
        path.quadraticBezierTo(p0.x, p0.y, mid.x, mid.y)
    }
    path.lineTo(puntos.last().x, puntos.last().y)
    return path
}

@Composable
fun EspectroAudio(playing: Boolean, spectrum: FloatArray) {
    val bars = if (spectrum.size == AudioSpectrumAnalyzer.BAR_COUNT) {
        spectrum
    } else {
        FloatArray(AudioSpectrumAnalyzer.BAR_COUNT)
    }

    // Fase animada para que la onda "roja" viaje y cruce a la onda "verde",
    // igual que en el visualizador de referencia.
    val transition = rememberInfiniteTransition(label = "ondaFase")
    val fase by transition.animateFloat(
        initialValue = 0f,
        targetValue = bars.size.toFloat(),
        animationSpec = infiniteRepeatable(tween(4200), RepeatMode.Reverse),
        label = "fase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF06070A))
            .border(1.dp, RadioCyan.copy(alpha = .45f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val filas = 9
            val gapCol = 3.dp.toPx()
            val gapFila = 3.dp.toPx()
            val colWidth = ((size.width - gapCol * (bars.size - 1)) / bars.size).coerceAtLeast(1f)
            val rowHeight = ((size.height - gapFila * (filas - 1)) / filas).coerceAtLeast(1f)

            // --- Rejilla de LEDs (encendidos desde abajo según el nivel de cada banda) ---
            bars.forEachIndexed { index, valor ->
                val nivel = if (playing) valor.coerceIn(0f, 1f) else (valor * .12f).coerceIn(0f, 1f)
                val filasEncendidas = (nivel * filas).roundToInt().coerceIn(0, filas)
                val x = index * (colWidth + gapCol)

                for (fila in 0 until filas) {
                    val filaDesdeAbajo = fila
                    val filaDesdeArriba = filas - 1 - fila
                    val encendido = filaDesdeAbajo < filasEncendidas
                    val color = colorFilaLed(filaDesdeArriba, filas)
                    val y = size.height - (fila + 1) * rowHeight - fila * gapFila

                    drawRoundRect(
                        color = if (encendido) color else color.copy(alpha = .12f),
                        topLeft = Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(colWidth, rowHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }

            // --- Ondas suaves superpuestas (verde: nivel real, roja: nivel desfasado) ---
            val centerBase = size.height * 0.52f
            val amplitud = size.height * 0.34f

            val puntosVerdes = bars.indices.map { i ->
                val x = i * (colWidth + gapCol) + colWidth / 2f
                val v = if (playing) bars[i].coerceIn(0f, 1f) else 0.08f
                Offset(x, centerBase - v * amplitud)
            }
            val puntosRojos = bars.indices.map { i ->
                val iDesfasado = ((i + fase.toInt()) % bars.size + bars.size) % bars.size
                val x = i * (colWidth + gapCol) + colWidth / 2f
                val v = if (playing) bars[iDesfasado].coerceIn(0f, 1f) else 0.05f
                Offset(x, centerBase - v * amplitud * 0.85f)
            }

            val caminoVerde = trazoSuave(puntosVerdes)
            val caminoRojo = trazoSuave(puntosRojos)

            // Glow: varias pasadas con más grosor y menos opacidad detrás de la línea nítida.
            listOf(10.dp.toPx() to 0.08f, 6.dp.toPx() to 0.16f).forEach { (grosor, alpha) ->
                drawPath(caminoVerde, color = Color(0xFF34D058).copy(alpha = alpha), style = androidx.compose.ui.graphics.drawscope.Stroke(grosor, cap = StrokeCap.Round))
                drawPath(caminoRojo, color = Color(0xFFFF4D3D).copy(alpha = alpha), style = androidx.compose.ui.graphics.drawscope.Stroke(grosor, cap = StrokeCap.Round))
            }
            drawPath(caminoVerde, color = Color(0xFF7CFF9E), style = androidx.compose.ui.graphics.drawscope.Stroke(2.2.dp.toPx(), cap = StrokeCap.Round))
            drawPath(caminoRojo, color = Color(0xFFFF8A75), style = androidx.compose.ui.graphics.drawscope.Stroke(2.2.dp.toPx(), cap = StrokeCap.Round))
        }

        Text(
            "AUDIO SPECTRUM",
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
            color = Color.White.copy(alpha = .45f),
            fontSize = 8.sp,
            letterSpacing = 1.5.sp
        )
        Text(
            if (playing) "LIVE FFT" else "STANDBY",
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            color = if (playing) RadioCyan else RadioTextMuted,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
    }
}

// ============================================================
//  VU-METERS, TRANSPORTE Y PERILLAS
// ============================================================

@Composable
fun MedidorNiveles(playing: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "nivel")
    val nivel by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (playing) 1f else 0.3f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
        label = "nivelAnim"
    )

    val segmentos = 9
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF060B12))
            .border(1.dp, Color(0xFF1B2A3A), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(segmentos) { i ->
            val umbral = 1f - (i / segmentos.toFloat())
            val activo = playing && nivel > umbral * 0.65f
            val color = when {
                i < 2 -> Color(0xFFFF3B30)
                i < 4 -> Color(0xFFFFC400)
                else -> Color(0xFF33E27A)
            }
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(6.dp)
                    .padding(vertical = 1.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (activo) color else color.copy(alpha = .16f))
            )
        }
    }
}

@Composable
fun ControlesReproduccion(
    estaReproduciendo: Boolean,
    estaCargando: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BotonTransporte(Icons.Default.FastRewind, onPrevious)

        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = CircleShape,
                    ambientColor = RadioCyan.copy(alpha = .8f),
                    spotColor = RadioCyan.copy(alpha = .8f)
                )
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF1B2733), Color(0xFF0A1119))))
                .border(2.dp, Brush.sweepGradient(listOf(RadioCyan, RadioBlue, RadioCyan, RadioCyanSoft)), CircleShape)
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center
        ) {
            if (estaCargando) {
                CircularProgressIndicator(Modifier.size(30.dp), color = RadioCyan, strokeWidth = 3.dp)
            } else {
                Icon(
                    imageVector = if (estaReproduciendo) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        BotonTransporte(Icons.Default.FastForward, onNext)
    }
}

@Composable
fun BotonTransporte(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF1C2733), Color(0xFF0C131C))))
            .border(1.dp, Color(0xFF2A3A4C), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White.copy(alpha = .92f), modifier = Modifier.size(24.dp))
    }
}

/**
 * Perilla giratoria funcional: arrastra verticalmente (arriba = sube, abajo = baja).
 * Se usa tanto para VOLUMEN (controla exoPlayer.volume en tiempo real) como para
 * SINTONIZAR (recorre el catálogo de emisoras).
 */
@Composable
fun RotaryKnob(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    diameter: Dp = 84.dp
) {
    val valorActual = rememberUpdatedState(value)
    val onChangeActual = rememberUpdatedState(onValueChange)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(diameter)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        // Arrastrar hacia arriba (dragAmount negativo) sube el valor.
                        val nuevo = (valorActual.value - dragAmount / 260f).coerceIn(0f, 1f)
                        onChangeActual.value(nuevo)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(10.dp, CircleShape, ambientColor = RadioCyan.copy(alpha = .5f), spotColor = RadioCyan.copy(alpha = .5f))
            ) {
                val strokeWidth = size.minDimension * 0.1f
                val startAngle = 135f
                val sweep = 270f
                val v = value.coerceIn(0f, 1f)

                // Riel de fondo
                drawArc(
                    color = Color(0xFF0E1B26),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = StrokeCap.Round)
                )
                // Arco activo (neón)
                drawArc(
                    brush = Brush.sweepGradient(listOf(RadioBlue, RadioCyan, RadioCyanSoft, RadioCyan)),
                    startAngle = startAngle,
                    sweepAngle = sweep * v,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = StrokeCap.Round)
                )

                // Cuerpo cromado de la perilla
                val faceRadius = size.minDimension * 0.32f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEDF1F5), Color(0xFF98A4B0), Color(0xFF3B4552), Color(0xFF14181D)),
                        center = Offset(center.x - faceRadius * 0.4f, center.y - faceRadius * 0.45f),
                        radius = faceRadius * 2.1f
                    ),
                    radius = faceRadius,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = .1f),
                    radius = faceRadius * 0.92f,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                )

                // Indicador de posición
                val angleRad = Math.toRadians((startAngle + sweep * v).toDouble())
                val pStart = Offset(
                    center.x + kotlin.math.cos(angleRad).toFloat() * faceRadius * 0.2f,
                    center.y + kotlin.math.sin(angleRad).toFloat() * faceRadius * 0.2f
                )
                val pEnd = Offset(
                    center.x + kotlin.math.cos(angleRad).toFloat() * faceRadius * 0.88f,
                    center.y + kotlin.math.sin(angleRad).toFloat() * faceRadius * 0.88f
                )
                drawLine(Color.White, pStart, pEnd, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = Color(0xFFB9D5EF), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
    }
}

// ============================================================
//  PANEL DE ESTACIÓN Y SELECTOR DE BANDAS
// ============================================================

@Composable
fun PanelEstacion(estacion: Estacion, playing: Boolean, loading: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF0A1420), Color(0xFF060D16))))
            .border(1.dp, Color(0xFF1E3548), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            RadioWaveIcon(modifier = Modifier.size(15.dp), tint = if (playing) RadioCyan else RadioTextMuted)
            Spacer(Modifier.width(6.dp))
            Text(
                text = when {
                    loading -> "CONECTANDO"
                    playing -> "EN VIVO"
                    else -> "DETENIDO"
                },
                color = if (playing) RadioCyan else RadioTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = .8.sp,
                modifier = Modifier.weight(1f)
            )
            MiniBars(playing)
        }
        Spacer(Modifier.height(6.dp))
        Text(estacion.nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("${estacion.banda} · ${estacion.genero}", color = RadioTextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun MiniBars(playing: Boolean) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(4) { i ->
            val transition = rememberInfiniteTransition(label = "miniBars$i")
            val h by transition.animateFloat(
                initialValue = 0.3f,
                targetValue = if (playing) 1f else 0.35f,
                animationSpec = infiniteRepeatable(tween(480 + i * 90), RepeatMode.Reverse),
                label = "mini$i"
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((6 + h * 12).dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (playing) RadioCyan else RadioTextMuted.copy(alpha = .4f))
            )
        }
    }
}

@Composable
fun BandasSelector(selected: String, onSelect: (String) -> Unit) {
    val opciones = listOf(
        "FM" to Icons.Default.SettingsInputAntenna,
        "AM" to Icons.Default.SettingsInputAntenna,
        "WEB" to Icons.Default.Language,
        "FAVORITOS" to Icons.Default.Favorite
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        opciones.forEach { (label, icon) ->
            val activo = label == selected
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (activo) Color(0xFF06405C) else Color(0xFF0E1620))
                    .border(1.5.dp, if (activo) RadioCyan else Color(0xFF223140), RoundedCornerShape(12.dp))
                    .clickable { onSelect(label) }
                    .padding(vertical = 10.dp)
            ) {
                Icon(icon, null, tint = if (activo) RadioCyan else RadioTextMuted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.height(4.dp))
                Text(label, color = if (activo) RadioCyan else RadioTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = .4.sp, maxLines = 1)
            }
        }
    }
}

// ============================================================
//  CONTROLES SECUNDARIOS Y CATÁLOGO
// ============================================================

@Composable
fun SmallActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0A182A))
            .border(1.dp, tint.copy(alpha = .28f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(19.dp))
    }
}

@Composable
fun CatalogoCompacto(
    selected: Int,
    favoritos: Set<String>,
    banda: String = "",
    onSelect: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit
) {
    val indicesFavoritos = catalogoEmisoras.indices.filter { catalogoEmisoras[it].streamUrl in favoritos }

    Column {
        // ---------- Sección FAVORITOS ----------
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Favorite, null, tint = RadioGold, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(6.dp))
            Text("FAVORITOS", color = RadioGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        }
        Spacer(Modifier.height(8.dp))
        if (indicesFavoritos.isEmpty()) {
            Text(
                "Toca ♡ en una emisora para agregarla aquí",
                color = RadioTextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )
        } else {
            ListaPorSecciones(
                indices = indicesFavoritos,
                selected = selected,
                favoritos = favoritos,
                claveReinicio = "favoritos",
                onSelect = onSelect,
                onToggleFavorite = onToggleFavorite
            )
        }

        // ---------- Emisoras de la banda activa (o todas) ----------
        // Con la banda FAVORITOS activa, solo se muestra la sección de favoritos.
        if (banda != "FAVORITOS") {
            val indicesBanda = indicesParaBanda(banda, favoritos)
            Spacer(Modifier.height(16.dp))
            Text(
                if (banda.isEmpty()) "EMISORAS" else "EMISORAS · $banda",
                color = RadioCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp
            )
            if (banda.isNotEmpty()) {
                Text("Toca $banda otra vez para ver todas", color = RadioTextMuted, fontSize = 10.sp)
            }
            Spacer(Modifier.height(8.dp))
            if (indicesBanda.isEmpty()) {
                Text(
                    "No hay emisoras $banda en el catálogo",
                    color = RadioTextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }
            ListaPorSecciones(
                indices = indicesBanda,
                selected = selected,
                favoritos = favoritos,
                // Al cambiar de banda la lista vuelve a la sección de la emisora actual (o a la 1).
                claveReinicio = "banda-$banda",
                onSelect = onSelect,
                onToggleFavorite = onToggleFavorite
            )
        }
    }
}

// ============================================================
//  LISTA POR SECCIONES
//  Divide una lista larga en secciones de EMISORAS_POR_SECCION.
//  El número de secciones se calcula solo según cuántas emisoras
//  haya: con 19 emisoras y 5 por sección salen 4 secciones.
// ============================================================

const val EMISORAS_POR_SECCION = 5

@Composable
fun ListaPorSecciones(
    indices: List<Int>,
    selected: Int,
    favoritos: Set<String>,
    claveReinicio: String,
    onSelect: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    porSeccion: Int = EMISORAS_POR_SECCION
) {
    if (indices.isEmpty()) return

    val totalSecciones = (indices.size + porSeccion - 1) / porSeccion
    var seccion by rememberSaveable(claveReinicio) { mutableIntStateOf(0) }
    // Si la lista se achica (por ejemplo, al quitar un favorito) la sección se mantiene en rango.
    val seccionActual = seccion.coerceIn(0, totalSecciones - 1)

    // Cuando cambia la emisora (botones ⏪ ⏩, perilla), muestra la sección donde está.
    LaunchedEffect(selected, claveReinicio) {
        val pos = indices.indexOf(selected)
        if (pos >= 0) seccion = pos / porSeccion
    }

    indices
        .drop(seccionActual * porSeccion)
        .take(porSeccion)
        .forEach { index ->
            FilaEmisora(
                index = index,
                selected = selected,
                esFavorita = catalogoEmisoras[index].streamUrl in favoritos,
                onSelect = onSelect,
                onToggleFavorite = onToggleFavorite
            )
        }

    if (totalSecciones > 1) {
        Spacer(Modifier.height(6.dp))
        SelectorSecciones(
            actual = seccionActual,
            total = totalSecciones,
            totalEmisoras = indices.size,
            onCambiar = { seccion = it }
        )
    }
}

@Composable
private fun SelectorSecciones(actual: Int, total: Int, totalEmisoras: Int, onCambiar: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FlechaSeccion(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Sección anterior", habilitada = actual > 0) {
                onCambiar(actual - 1)
            }
            Spacer(Modifier.width(6.dp))
            // Números de sección; si fueran muchos, se pueden deslizar horizontalmente.
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .weight(1f, fill = false)
                    .horizontalScroll(rememberScrollState())
            ) {
                repeat(total) { i ->
                    val activa = i == actual
                    Box(
                        Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (activa) Brush.linearGradient(listOf(RadioBlue, RadioCyan)) else Brush.linearGradient(listOf(Color(0xFF0E1620), Color(0xFF0E1620))))
                            .border(1.dp, if (activa) RadioCyan else Color(0xFF223140), CircleShape)
                            .clickable { onCambiar(i) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${i + 1}", color = if (activa) Color.White else RadioTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.width(6.dp))
            FlechaSeccion(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Sección siguiente", habilitada = actual < total - 1) {
                onCambiar(actual + 1)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Sección ${actual + 1} de $total · $totalEmisoras emisoras",
            color = RadioTextMuted,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun FlechaSeccion(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    descripcion: String,
    habilitada: Boolean,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .size(30.dp)
            .clip(CircleShape)
            .border(1.dp, if (habilitada) RadioCyan.copy(alpha = .5f) else Color(0xFF16222F), CircleShape)
            .clickable(enabled = habilitada, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, descripcion, tint = if (habilitada) RadioCyan else Color(0xFF2A3A4C), modifier = Modifier.size(20.dp))
    }
}

/** Una fila del catálogo: ícono, nombre, género, corazón de favorito e indicador de selección. */
@Composable
private fun FilaEmisora(
    index: Int,
    selected: Int,
    esFavorita: Boolean,
    onSelect: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit
) {
    val station = catalogoEmisoras[index]
    val activa = index == selected

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (activa) Color(0xFF0D2742) else Color(0xFF071321))
            .border(1.dp, if (activa) RadioCyan.copy(alpha = .5f) else Color.Transparent, RoundedCornerShape(14.dp))
            .clickable { onSelect(index) }
            .padding(start = 13.dp, end = 6.dp, top = 11.dp, bottom = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(
                    if (activa) Brush.linearGradient(listOf(RadioBlue, RadioCyan))
                    else Brush.linearGradient(listOf(Color(0xFF15263C), Color(0xFF0A1524)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Radio, null, tint = if (activa) Color.White else RadioTextMuted, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(station.nombre, color = if (activa) Color.White else RadioTextSecondary, fontSize = 13.sp, fontWeight = if (activa) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(station.genero, color = RadioTextMuted, fontSize = 10.sp)
        }
        if (activa) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(RadioCyan))
            Spacer(Modifier.width(4.dp))
        }
        // Corazón: toca para agregar/quitar de favoritos sin cambiar de emisora.
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .clickable { onToggleFavorite(index) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (esFavorita) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (esFavorita) "Quitar de favoritos" else "Agregar a favoritos",
                tint = if (esFavorita) RadioGold else RadioTextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ============================================================
//  VISTAS PREVIAS (@Preview) para el panel Design de Android Studio.
//  No incluyen PantallaPrincipal porque crea un ExoPlayer real,
//  que no funciona dentro del editor de vistas previas.
// ============================================================

/**
 * Pantalla principal completa con datos de ejemplo y estado local.
 * En el modo interactivo de la vista previa puedes tocar botones, bandas,
 * favoritos y emisoras (no suena audio: el editor no reproduce streams).
 */
@Composable
private fun PantallaPrincipalDemo() {
    var estacion by remember { mutableIntStateOf(0) }
    var reproduciendo by remember { mutableStateOf(true) }
    var silenciado by remember { mutableStateOf(false) }
    var volumen by remember { mutableFloatStateOf(0.78f) }
    var preset by remember { mutableStateOf("") }
    var favoritos by remember {
        mutableStateOf(setOf(catalogoEmisoras[2].streamUrl, catalogoEmisoras[7].streamUrl))
    }
    var nombre by remember { mutableStateOf("Jorge") }
    val lista = indicesParaBanda(preset, favoritos).ifEmpty { catalogoEmisoras.indices.toList() }

    fun mover(paso: Int) {
        val pos = lista.indexOf(estacion)
        estacion = lista[if (pos == -1) 0 else ((pos + paso) % lista.size + lista.size) % lista.size]
    }

    IUDigitalRadioTheme {
        PantallaPrincipalContenido(
            estacionSeleccionada = estacion,
            estaReproduciendo = reproduciendo,
            estaCargando = false,
            silenciado = silenciado,
            volumen = volumen,
            preset = preset,
            favoritos = favoritos,
            listaNavegacion = lista,
            spectrumValues = if (reproduciendo) espectroDeEjemplo() else FloatArray(AudioSpectrumAnalyzer.BAR_COUNT),
            fotoPerfil = null,
            nombrePerfil = nombre,
            onPrevious = { mover(-1) },
            onPlayPause = { reproduciendo = !reproduciendo },
            onNext = { mover(+1) },
            onVolumenChange = { volumen = it; silenciado = false },
            onSintonizar = { estacion = it },
            onBandaSelect = { banda -> preset = if (preset == banda) "" else banda },
            onToggleSilencio = { silenciado = !silenciado },
            onToggleFavorito = { i ->
                val url = catalogoEmisoras[i].streamUrl
                favoritos = if (url in favoritos) favoritos - url else favoritos + url
            },
            onSeleccionarEstacion = { estacion = it },
            onTomarFoto = {},
            onElegirGaleria = {},
            onQuitarFoto = {},
            onGuardarNombre = { nombre = it }
        )
    }
}

/** Todo el diseño de arriba a abajo en una sola imagen alta (sin necesidad de scroll). */
@Preview(name = "App completa - diseño entero", showBackground = true, backgroundColor = 0xFF020711, widthDp = 400, heightDp = 1500)
@Composable
fun PantallaCompletaPreview() {
    PantallaPrincipalDemo()
}

/** Lo que se ve en un celular real: pantalla de Pixel 7 con barras del sistema y scroll. */
@Preview(name = "App completa - celular", showSystemUi = true, device = "id:pixel_7")
@Composable
fun PantallaCelularPreview() {
    PantallaPrincipalDemo()
}

@Preview(name = "Bienvenida", showBackground = true, backgroundColor = 0xFF020711)
@Composable
fun SplashPersonalizadoPreview() {
    IUDigitalRadioTheme {
        SplashPersonalizado()
    }
}

/**
 * Datos de ejemplo para la vista previa del espectro: el editor de @Preview no
 * reproduce audio, así que no hay FFT real. Se simula una curva típica de música
 * (graves altos, medios moderados, agudos que caen) para ver los LEDs y las ondas.
 */
private fun espectroDeEjemplo(): FloatArray =
    FloatArray(AudioSpectrumAnalyzer.BAR_COUNT) { i ->
        val t = i / (AudioSpectrumAnalyzer.BAR_COUNT - 1f)
        val base = 0.85f - 0.55f * t
        val variacion = 0.15f * kotlin.math.sin(i * 0.9f)
        (base + variacion).coerceIn(0.05f, 1f)
    }

@Preview(name = "Espectro en vivo", showBackground = true, backgroundColor = 0xFF020711, widthDp = 380)
@Composable
fun EspectroAudioPreview() {
    IUDigitalRadioTheme {
        Box(Modifier.padding(12.dp)) {
            EspectroAudio(playing = true, spectrum = espectroDeEjemplo())
        }
    }
}

@Preview(name = "Espectro en pausa", showBackground = true, backgroundColor = 0xFF020711, widthDp = 380)
@Composable
fun EspectroAudioPausaPreview() {
    IUDigitalRadioTheme {
        Box(Modifier.padding(12.dp)) {
            EspectroAudio(playing = false, spectrum = FloatArray(AudioSpectrumAnalyzer.BAR_COUNT))
        }
    }
}

@Preview(name = "Encabezado", showBackground = true, backgroundColor = 0xFF1C2530, widthDp = 380)
@Composable
fun HeaderRadioPreview() {
    IUDigitalRadioTheme {
        Box(Modifier.padding(12.dp)) {
            HeaderRadio(playing = false, loading = false)
        }
    }
}

@Preview(name = "Perfil - cerrado", showBackground = true, backgroundColor = 0xFF020711, widthDp = 380)
@Composable
fun TarjetaPerfilPreview() {
    IUDigitalRadioTheme {
        Box(Modifier.padding(12.dp)) {
            TarjetaPerfil(
                foto = null, nombre = "Jorge",
                onTomarFoto = {}, onElegirGaleria = {}, onQuitarFoto = {}, onGuardarNombre = {}
            )
        }
    }
}

@Preview(name = "Perfil - editando", showBackground = true, backgroundColor = 0xFF020711, widthDp = 380)
@Composable
fun TarjetaPerfilEditandoPreview() {
    IUDigitalRadioTheme {
        Box(Modifier.padding(12.dp)) {
            TarjetaPerfil(
                foto = null, nombre = "Jorge",
                onTomarFoto = {}, onElegirGaleria = {}, onQuitarFoto = {}, onGuardarNombre = {},
                editandoInicial = true
            )
        }
    }
}

@Preview(name = "Panel de estación", showBackground = true, backgroundColor = 0xFF020711)
@Composable
fun PanelEstacionPreview() {
    IUDigitalRadioTheme {
        PanelEstacion(
            estacion = catalogoEmisoras[0],
            playing = true,
            loading = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Controles", showBackground = true, backgroundColor = 0xFF020711)
@Composable
fun ControlesReproduccionPreview() {
    IUDigitalRadioTheme {
        Box(Modifier.padding(16.dp)) {
            ControlesReproduccion(
                estaReproduciendo = false,
                estaCargando = false,
                onPrevious = {},
                onPlayPause = {},
                onNext = {}
            )
        }
    }
}

@Preview(name = "Bandas - FM activa", showBackground = true, backgroundColor = 0xFF020711, widthDp = 380)
@Composable
fun BandasSelectorPreview() {
    IUDigitalRadioTheme {
        // Estado local: en el modo interactivo de la vista previa puedes tocar
        // cada banda y ver cómo cambia la selección.
        var preset by remember { mutableStateOf("FM") }
        Box(Modifier.padding(12.dp)) {
            BandasSelector(selected = preset, onSelect = { preset = it })
        }
    }
}

@Preview(name = "Bandas - FAVORITOS activa", showBackground = true, backgroundColor = 0xFF020711, widthDp = 380)
@Composable
fun BandasSelectorFavoritosPreview() {
    IUDigitalRadioTheme {
        Box(Modifier.padding(12.dp)) {
            BandasSelector(selected = "FAVORITOS", onSelect = {})
        }
    }
}

@Preview(name = "Bandas - pantalla angosta", showBackground = true, backgroundColor = 0xFF020711, widthDp = 300)
@Composable
fun BandasSelectorAngostaPreview() {
    IUDigitalRadioTheme {
        Box(Modifier.padding(12.dp)) {
            BandasSelector(selected = "WEB", onSelect = {})
        }
    }
}

@Preview(name = "Catálogo de emisoras", showBackground = true, backgroundColor = 0xFF020711, heightDp = 900)
@Composable
fun CatalogoCompactoPreview() {
    IUDigitalRadioTheme {
        Box(Modifier.padding(16.dp)) {
            CatalogoCompacto(
                selected = 0,
                favoritos = setOf(catalogoEmisoras[2].streamUrl, catalogoEmisoras[7].streamUrl),
                onSelect = {},
                onToggleFavorite = {}
            )
        }
    }
}