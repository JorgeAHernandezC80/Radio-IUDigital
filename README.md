# 📻 Radio IU Digital

**Tu música, siempre.**

App de radio online para Android, hecha con **Kotlin** y **Jetpack Compose**. Es mi proyecto de aprendizaje para el curso **Programación de Dispositivos Móviles** de la **IU Digital de Antioquia** (Ingeniería de Software y Datos, sexto semestre). En cada entrega le agrego algo nuevo y aprendo un poco más de Compose en el camino.

La app reproduce emisoras en vivo por internet. Tiene un visualizador de espectro que reacciona al audio real (no son animaciones falsas), perillas de volumen y sintonización que se manejan con gestos, bandas FM/AM/WEB, favoritos y un perfil de usuario con foto tomada desde la cámara o la galería.

---

## Tabla de contenido

1. [Funcionalidades](#funcionalidades)
2. [Herramientas y tecnologías](#herramientas-y-tecnologías)
3. [Estructura del proyecto](#estructura-del-proyecto)
4. [Arquitectura](#arquitectura)
5. [Métodos implementados por archivo](#métodos-implementados-por-archivo)
6. [Técnicas y APIs de Android usadas](#técnicas-y-apis-de-android-usadas)
7. [Catálogo de emisoras](#catálogo-de-emisoras)
8. [Permisos](#permisos)
9. [Datos que guarda la app](#datos-que-guarda-la-app)
10. [Vistas previas (@Preview)](#vistas-previas-preview)
11. [Cómo ejecutarlo](#cómo-ejecutarlo)
12. [Limitaciones conocidas](#limitaciones-conocidas)
13. [Historial de desarrollo](#historial-de-desarrollo)
14. [Autor](#autor)
15. [Licencia y derechos de autor](#licencia-y-derechos-de-autor)

---

## Funcionalidades

| Sección | Qué hace |
|---|---|
| **Bienvenida** | Pantalla de inicio animada con el logo, el nombre de la emisora y una barra de carga. Después pasa al reproductor con un fundido. |
| **Perfil de usuario** | Tarjeta arriba del reproductor con foto y nombre. La foto se puede tomar con la **cámara** o elegir de la **galería**, y también se puede quitar. Todo queda guardado al cerrar la app. |
| **Reproductor** | Streaming en vivo con **Media3 ExoPlayer**. Botones anterior, play/pausa y siguiente, e indicador de estado (EN VIVO, CONECTANDO, EN PAUSA). |
| **Espectro de audio** | Rejilla de LEDs (verde, amarillo, naranja y rojo) con dos ondas superpuestas. Se calcula con una **FFT real** sobre el audio que está sonando. |
| **Perillas giratorias** | **VOLUMEN** y **SINTONIZAR** se controlan arrastrando hacia arriba o hacia abajo, como una radio física. |
| **Bandas** | Selector **FM / AM / WEB / FAVORITOS**. Filtra la lista, y los botones y la perilla recorren solo las emisoras de esa banda. Tocar otra vez la banda activa la desactiva. |
| **Favoritos** | Botón ♡ para la emisora actual y un corazón en cada fila. Las favoritas aparecen en su propia sección. |
| **Catálogo por secciones** | La lista se divide sola en secciones de 5 emisoras, con selector numérico y flechas, para que la pantalla no crezca con cada emisora nueva. |
| **Barra de scroll** | Barra lateral visible que se puede arrastrar, o tocar para saltar a una posición. |
| **Vibración** | Respuesta háptica corta al tocar los controles. |

---

## Herramientas y tecnologías

### Entorno de desarrollo

| Herramienta | Uso |
|---|---|
| **Android Studio** | IDE principal: edición, vistas previas, emulador y compilación |
| **Emulador de Android (AVD)** | Pruebas de la app; también se usó su cámara virtual para el perfil |
| **Gradle 8.10.2** (Kotlin DSL) | Sistema de compilación |
| **Android Gradle Plugin 8.7.0** | Plugin de compilación de Android |
| **Git + GitHub** | Control de versiones, con un commit por cada sección de la interfaz |

### Lenguaje y SDK

| Elemento | Versión |
|---|---|
| **Kotlin** | 1.9.23 |
| **Compilador de Compose** | 1.5.11 |
| `compileSdk` / `targetSdk` | 35 (Android 15) |
| `minSdk` | 24 (Android 7.0) |
| Java / JVM target | 1.8 |

### Librerías

| Librería | Versión | Para qué se usa |
|---|---|---|
| `androidx.compose:compose-bom` | 2024.04.01 | Controla las versiones de todas las librerías de Compose |
| `androidx.compose.ui:ui`, `ui-graphics` | (BOM) | Interfaz declarativa, Canvas y gráficos |
| `androidx.compose.material3:material3` | (BOM) | Componentes Material 3 (`Text`, `Icon`, `OutlinedTextField`, etc.) |
| `androidx.compose.material:material-icons-extended` | (BOM) | Íconos (Radio, Favorite, CameraAlt, PhotoLibrary, etc.) |
| `androidx.compose.ui:ui-tooling-preview` / `ui-tooling` | (BOM) | Vistas previas `@Preview` en Android Studio |
| `androidx.activity:activity-compose` | 1.9.0 | `setContent` y Activity Result API (cámara, galería, permisos) |
| `androidx.lifecycle:lifecycle-runtime-ktx` | 2.7.0 | Ciclo de vida |
| `androidx.core:core-ktx` | 1.13.1 | Extensiones de Kotlin para Android |
| `androidx.core:core-splashscreen` | 1.0.1 | Splash Screen API compatible desde API 24 |
| `androidx.media3:media3-exoplayer` | 1.3.1 | Reproducción de los streams de radio |
| `androidx.media3:media3-common` | 1.3.1 | `MediaItem`, `Player` y tipos comunes de Media3 |

No se usan layouts XML: **toda la interfaz está hecha en Compose**.

---

## Estructura del proyecto

```
Radio-IUDigital/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/iudigitalradio/
│       │   ├── MainActivity.kt                 # Actividad y lógica: ExoPlayer, cámara, galería, preferencias
│       │   ├── PantallaPrincipalContenido.kt   # Composición de la pantalla completa (sin estado) + previews
│       │   ├── Emisoras.kt                     # Modelo Estacion, catálogo y filtro por banda
│       │   ├── Bienvenida.kt                   # Pantalla de bienvenida animada
│       │   ├── PerfilUsuario.kt                # Tarjeta de perfil, foto y nombre
│       │   ├── Encabezado.kt                   # Chasis metálico, logo y estado EN VIVO
│       │   ├── EspectroAudio.kt                # Visualizador de espectro (LEDs + ondas)
│       │   ├── ControlesReproduccion.kt        # Transporte, perillas, medidores y botones
│       │   ├── PanelEstacion.kt                # Panel de la emisora sintonizada
│       │   ├── BandasSelector.kt               # Selector FM / AM / WEB / FAVORITOS
│       │   ├── CatalogoEmisoras.kt             # Lista por secciones y favoritos
│       │   ├── BarraScroll.kt                  # Barra de scroll lateral arrastrable
│       │   ├── audio/
│       │   │   └── AudioSpectrumAnalyzer.kt    # Análisis FFT del audio en tiempo real
│       │   └── ui/theme/                       # Color.kt, Theme.kt, Type.kt
│       └── res/                                # Íconos, splash, strings, colores y temas
├── gradle/                                     # Gradle Wrapper
├── build.gradle.kts
├── settings.gradle.kts
├── LICENSE
└── README.md
```

> **Nota de aprendizaje:** en la raíz quedaron algunos archivos y carpetas de versiones anteriores (`ui/`, `audio/`, `EspectroAudioBrutal.kt`, `PantallasExtra.kt`) y recursos gráficos para otras plataformas (`android/`, `ios/`, `web/`, `splash/`). No forman parte de la compilación; los dejé como referencia mientras termino de limpiar el repositorio.

---

## Arquitectura

La interfaz está separada en **dos capas**, siguiendo el patrón de *state hoisting* (elevación de estado) que recomienda Compose:

```
MainActivity
 └── PantallaConBienvenida ──► SplashPersonalizado (1,9 s)
                           └─► PantallaPrincipal            ← CON ESTADO
                                 • ExoPlayer + AudioSpectrumAnalyzer
                                 • Cámara, galería y permisos
                                 • SharedPreferences (favoritos, nombre)
                                 • Archivo interno (foto de perfil)
                                 │
                                 ▼  datos + callbacks
                               PantallaPrincipalContenido  ← SIN ESTADO
                                 ├── TarjetaPerfil
                                 ├── ChasisMetalico
                                 │    ├── HeaderRadio + EstadoPill
                                 │    ├── EspectroAudio
                                 │    ├── MedidorNiveles · ControlesReproduccion · MedidorNiveles
                                 │    ├── RotaryKnob (VOLUMEN) · PanelEstacion · RotaryKnob (SINTONIZAR)
                                 │    └── BandasSelector
                                 ├── Silencio · % volumen · Favorito
                                 ├── CatalogoCompacto
                                 │    └── ListaPorSecciones → FilaEmisora + SelectorSecciones
                                 └── BarraScroll
```

- **`PantallaPrincipal`** guarda el estado y habla con el sistema: reproductor, cámara, almacenamiento.
- **`PantallaPrincipalContenido`** solo dibuja. Recibe los datos y las acciones como parámetros. Gracias a esta separación se puede ver la app entera en `@Preview` con datos de ejemplo, sin reproductor ni cámara.

### Flujo del audio

```
Stream de internet (MP3 / AAC)
   → ExoPlayer (decodifica)
   → DefaultAudioSink
        └── TeeAudioProcessor ──► AudioSpectrumAnalyzer (copia del PCM)
   → Parlante                         │
                                      ▼
                          FFT → 36 bandas → StateFlow → EspectroAudio (Canvas)
```

El `TeeAudioProcessor` solo **copia** el audio para analizarlo; no lo modifica. La salida se configura en PCM de 16 bits, porque con salida *float* el audio sonaba acelerado en el emulador.

---

## Métodos implementados por archivo

### `MainActivity.kt`
| Método | Descripción |
|---|---|
| `MainActivity.onCreate()` | Instala la splash screen del sistema (`installSplashScreen`) y carga la interfaz con `setContent`. |
| `vibrarDispositivo(context)` | Vibración corta de 45 ms. Usa `VibratorManager` en Android 12+ y `Vibrator` en versiones anteriores. |
| `PantallaPrincipal()` | Crea el `ExoPlayer` con un `DefaultRenderersFactory` personalizado, conecta el analizador de espectro y maneja todo el estado de la pantalla. |
| `createPlayer()` | Construye el reproductor con `DefaultAudioSink` + `TeeAudioProcessor`, salida PCM de 16 bits y *decoder fallback*. |
| `alternarFavorito(index)` | Agrega o quita una emisora de favoritos y la guarda en `SharedPreferences`. |
| `moverEstacion(paso)` | Pasa a la emisora anterior o siguiente, en ciclo, dentro de la banda activa. |
| `seleccionarBanda(banda)` | Activa o desactiva una banda y sintoniza la primera emisora de esa banda si la actual no pertenece a ella. |
| `abrirCamara()` | Pide el permiso de cámara si hace falta y abre la cámara (`TakePicturePreview`). |
| `abrirGaleria()` | Abre el selector de fotos del sistema (`PickVisualMedia`, solo imágenes). |

### `Emisoras.kt`
| Elemento | Descripción |
|---|---|
| `data class Estacion` | Modelo de una emisora: `nombre`, `genero`, `streamUrl`, `region` y `banda` (FM, AM o WEB). |
| `catalogoEmisoras` | Lista con las 19 emisoras de la app. |
| `indicesParaBanda(banda, favoritos)` | Devuelve las emisoras que corresponden a la banda elegida, a los favoritos o a todas. |

### `audio/AudioSpectrumAnalyzer.kt`
| Método | Descripción |
|---|---|
| `flush(sampleRate, canales, encoding)` | Recibe el formato del audio cada vez que cambia la emisora y reinicia el análisis. |
| `handleBuffer(buffer)` | Recibe cada bloque de audio decodificado sin alterar el original (`buffer.slice()`). |
| `readPcm16()` / `readPcmFloat()` / `readPcm8()` | Leen el audio según su formato y lo mezclan a mono. |
| `appendSample()` | Acumula muestras en ventanas de 2048 con 50 % de solapamiento. |
| `analyzeFrame()` | Aplica la ventana de Hann, ejecuta la FFT, agrupa en 36 bandas logarítmicas (35 Hz a 18 kHz), convierte a dB, normaliza y suaviza (ataque rápido, caída lenta). Publica a unos 30 FPS. |
| `fft(real, imag)` | FFT iterativa **Cooley-Tukey**, escrita a mano, sin librerías externas. |
| `close()` | Limpia el estado al cerrar la pantalla. |

### `Bienvenida.kt`
| Método | Descripción |
|---|---|
| `PantallaConBienvenida()` | Muestra la bienvenida 1,9 s y pasa al reproductor con un `Crossfade`. |
| `SplashPersonalizado()` | Logo con resplandor, escala y opacidad animadas (`Animatable`), y barra de carga. |

### `PerfilUsuario.kt`
| Método | Descripción |
|---|---|
| `cargarFotoPerfil(context)` | Lee la foto guardada en el almacenamiento interno. |
| `guardarFotoPerfil(context, bitmap)` | Guarda la foto como JPEG (calidad 90) en `foto_perfil.jpg`. |
| `borrarFotoPerfil(context)` | Elimina la foto guardada. |
| `cargarBitmapDesdeUri(context, uri)` | Carga una imagen de la galería reduciéndola a unos 512 px con `inSampleSize`, para no gastar memoria. |
| `AvatarPerfil(foto, tamano)` | Foto circular recortada (`ContentScale.Crop`), o un ícono de persona si no hay foto. |
| `TarjetaPerfil(...)` | Tarjeta que se despliega: cerrada muestra foto y nombre; abierta muestra Cámara, Galería, Quitar foto y el campo del nombre. |
| `BotonPerfil(...)` | Botón con ícono y texto para las acciones del perfil. |

### `Encabezado.kt`
| Método | Descripción |
|---|---|
| `ChasisMetalico(content)` | Contenedor con degradado metálico, borde neón y sombra; es el "cuerpo" de la radio. |
| `RadioWaveIcon(tint)` | Ícono de antena con ondas, dibujado en `Canvas` con `drawCircle` y `drawArc`. |
| `HeaderRadio(playing, loading)` | Logo "RADIO IU DIGITAL", eslogan e indicador de estado. |
| `EstadoPill(playing, loading)` | Pastilla de estado: **EN VIVO**, **CONECTANDO** o **EN PAUSA**. |

### `EspectroAudio.kt`
| Método | Descripción |
|---|---|
| `EspectroAudio(playing, spectrum)` | Dibuja en `Canvas` una rejilla de 36 × 9 LEDs y dos ondas suaves: la verde es el nivel real y la roja es el nivel desfasado y animado. Cada onda tiene efecto *glow*. |
| `colorFilaLed(fila, total)` | Color de cada fila de LEDs, de verde (abajo) a rojo (arriba). |
| `trazoSuave(puntos)` | Curva suave por los puntos, usando curvas Bézier cuadráticas. |
| `espectroDeEjemplo()` | Espectro simulado para las vistas previas. |

### `ControlesReproduccion.kt`
| Método | Descripción |
|---|---|
| `MedidorNiveles(playing)` | Medidor VU animado con 9 segmentos (`rememberInfiniteTransition`). |
| `ControlesReproduccion(...)` | Botones ⏪ ▶/⏸ ⏩, con un indicador de carga mientras conecta. |
| `BotonTransporte(icon, onClick)` | Botón cuadrado para anterior y siguiente. |
| `RotaryKnob(value, onValueChange, label)` | Perilla giratoria dibujada en `Canvas`: riel, arco neón, cuerpo cromado e indicador. Se controla arrastrando en vertical (`detectVerticalDragGestures`). |
| `SmallActionButton(icon, tint, onClick)` | Botón pequeño para silenciar y para marcar favorito. |

### `PanelEstacion.kt`
| Método | Descripción |
|---|---|
| `PanelEstacion(estacion, playing, loading)` | Muestra el estado, el nombre de la emisora, la banda y el género. |
| `MiniBars(playing)` | Cuatro barritas animadas que indican que está sonando. |

### `BandasSelector.kt`
| Método | Descripción |
|---|---|
| `BandasSelector(selected, onSelect)` | Cuatro botones (FM, AM, WEB, FAVORITOS) con resaltado del activo. |

### `CatalogoEmisoras.kt`
| Método | Descripción |
|---|---|
| `CatalogoCompacto(...)` | Sección FAVORITOS y lista de emisoras de la banda activa, con mensajes cuando están vacías. |
| `ListaPorSecciones(...)` | Divide la lista en secciones de `EMISORAS_POR_SECCION` (5). Calcula solo cuántas secciones hacen falta y salta a la sección de la emisora que está sonando. |
| `SelectorSecciones(...)` | Flechas ‹ ›, números de sección y el texto "Sección X de Y". |
| `FlechaSeccion(...)` | Flecha que se desactiva en la primera y en la última sección. |
| `FilaEmisora(...)` | Fila con ícono, nombre, género, indicador de selección y corazón de favorito. |

### `BarraScroll.kt`
| Método | Descripción |
|---|---|
| `BarraScroll(scrollState)` | Barra lateral cuyo tamaño es proporcional al contenido visible. Al arrastrarla mueve la pantalla con `dispatchRawDelta`; al tocar el riel salta con `animateScrollTo`. Se oculta si todo cabe en pantalla. |

### `PantallaPrincipalContenido.kt`
| Método | Descripción |
|---|---|
| `PantallaPrincipalContenido(...)` | Arma la pantalla completa a partir de los datos y acciones que recibe; no tiene lógica propia. |
| `PantallaPrincipalDemo()` | Versión con datos de ejemplo y estado local, para las vistas previas interactivas. |

---

## Técnicas y APIs de Android usadas

- **Jetpack Compose declarativo:** `Column`, `Row`, `Box`, modificadores encadenados, `weight`, `padding`, `clip`, `border`, `shadow` y degradados con `Brush`.
- **Estado:** `remember`, `rememberSaveable` (sobrevive a la rotación de pantalla), `mutableStateOf`, `mutableIntStateOf`, `mutableFloatStateOf`, `rememberUpdatedState` y `collectAsState` sobre un `StateFlow`.
- **State hoisting:** la pantalla está separada en una parte con estado y otra sin estado.
- **Efectos:** `LaunchedEffect` (cambio de emisora, volumen, sección visible) y `DisposableEffect` (liberar el reproductor al salir).
- **Dibujo personalizado:** `Canvas` con `drawRoundRect`, `drawArc`, `drawCircle`, `drawLine` y `drawPath` (curvas Bézier).
- **Animaciones:** `Animatable`, `rememberInfiniteTransition`, `animateFloat`, `Crossfade` y `tween`.
- **Gestos:** `pointerInput` con `detectVerticalDragGestures` (perillas y barra de scroll) y `detectTapGestures`.
- **Scroll:** `verticalScroll`, `rememberScrollState`, `horizontalScroll` y una barra de scroll personalizada.
- **Media3 ExoPlayer:** `MediaItem`, `Player.Listener` para el estado de carga, volumen y `playWhenReady`.
- **Procesamiento de audio:** `TeeAudioProcessor.AudioBufferSink` y FFT propia con ventana de Hann.
- **Activity Result API:** `TakePicturePreview` (cámara), `RequestPermission` (permiso de cámara) y `PickVisualMedia` (galería sin pedir permisos de almacenamiento).
- **Persistencia:** `SharedPreferences` para favoritos y nombre, y un archivo en `filesDir` para la foto de perfil.
- **Splash Screen API** y bienvenida propia en Compose.
- **Edge-to-edge:** `systemBarsPadding()` para que el contenido no quede debajo de las barras del sistema (targetSdk 35).
- **Vibración:** `VibratorManager` y `VibrationEffect`.
- **Vistas previas:** `@Preview` por componente, vistas previas de la app completa (diseño entero y celular Pixel 7) y modo interactivo.

---

## Catálogo de emisoras

| # | Emisora | Género | Banda | Región |
|---|---|---|---|---|
| 1 | Radio Lounge (SomaFM Groove Salad) | Chill / Lounge | WEB | Estados Unidos |
| 2 | Radio Electrónica (SomaFM Beat Blender) | Deep House | WEB | Estados Unidos |
| 3 | Radio Swiss Jazz | Jazz | WEB | Europa |
| 4 | Radio Swiss Pop | Pop | WEB | Europa |
| 5 | Radio UNAL Bogotá | Radio universitaria / Cultura | FM | Colombia |
| 6 | Drone Zone (SomaFM) | Ambient | WEB | Estados Unidos |
| 7 | Secret Agent (SomaFM) | Downtempo / Lounge | WEB | Estados Unidos |
| 8 | Indie Pop Rocks (SomaFM) | Indie Pop | WEB | Estados Unidos |
| 9 | Underground 80s (SomaFM) | Synthpop / New Wave | WEB | Estados Unidos |
| 10 | Left Coast 70s (SomaFM) | Rock 70s | WEB | Estados Unidos |
| 11 | Radio Paradise | Ecléctico / Rock | WEB | Estados Unidos |
| 12 | Radio Swiss Classic | Clásica | WEB | Europa |
| 13 | FIP | Ecléctico / Jazz | FM | Europa |
| 14 | FIP Jazz | Jazz | WEB | Europa |
| 15 | FIP Groove | Funk / Soul | WEB | Europa |
| 16 | WNYC 93.9 FM | Noticias / Cultura | FM | Estados Unidos |
| 17 | KEXP 90.3 FM | Indie / Alternativo | FM | Estados Unidos |
| 18 | WNYC AM 820 | Noticias / Talk | AM | Estados Unidos |
| 19 | WQXR 105.9 FM | Música Clásica | FM | Estados Unidos |

Para agregar una emisora basta con añadir una línea al final de `catalogoEmisoras` en `Emisoras.kt`. La lista, las secciones, las bandas y la perilla se actualizan solas. Los favoritos se guardan por URL del stream, así que agregar o reordenar emisoras no los mezcla.

> Los streams pertenecen a cada emisora. La app solo los reproduce desde sus direcciones públicas; ver la sección de licencia.

---

## Permisos

| Permiso | Motivo |
|---|---|
| `INTERNET` | Reproducir los streams de radio. |
| `ACCESS_NETWORK_STATE` | Consultar el estado de la conexión. |
| `CAMERA` | Tomar la foto de perfil. Se pide en tiempo de ejecución, solo al tocar **Cámara**. |
| `VIBRATE` | Respuesta háptica en los controles. |

Además, `android:usesCleartextTraffic="true"` permite los streams que usan `http://` (por ejemplo, Radio Swiss). La cámara está declarada como **no obligatoria** (`required="false"`), así que la app también se instala en equipos sin cámara.

---

## Datos que guarda la app

Todo se guarda **solo en el dispositivo**; no se envía a ningún servidor.

| Dato | Dónde |
|---|---|
| Emisoras favoritas | `SharedPreferences` → `iudigitalradio_prefs` / `favoritos` (URLs de los streams) |
| Nombre del perfil | `SharedPreferences` → `iudigitalradio_prefs` / `perfil_nombre` |
| Foto de perfil | Almacenamiento interno de la app → `foto_perfil.jpg` |

---

## Vistas previas (@Preview)

Abre cualquier archivo de la interfaz, cambia a la vista **Split** o **Design** y haz clic en **Build & Refresh**.

| Vista previa | Archivo |
|---|---|
| App completa: diseño entero y celular | `PantallaPrincipalContenido.kt` |
| Bienvenida | `Bienvenida.kt` |
| Perfil cerrado y editando | `PerfilUsuario.kt` |
| Encabezado | `Encabezado.kt` |
| Espectro en vivo y en pausa | `EspectroAudio.kt` |
| Controles | `ControlesReproduccion.kt` |
| Panel de estación | `PanelEstacion.kt` |
| Bandas: FM activa, FAVORITOS activa y pantalla angosta | `BandasSelector.kt` |
| Catálogo de emisoras | `CatalogoEmisoras.kt` |

Con el **modo interactivo** (el ícono ▶ sobre cada vista previa) se pueden tocar botones, bandas y favoritos. En las vistas previas no suena audio.

---

## Cómo ejecutarlo

1. Clona el repositorio:
   ```bash
   git clone https://github.com/JorgeAHernandezC80/Radio-IUDigital.git
   ```
2. Ábrelo en **Android Studio** (**File → Open**).
3. Espera a que termine el **Gradle Sync**.
4. Ejecútalo en un emulador o en un dispositivo con **Android 7.0 o superior**. Necesita conexión a internet para el streaming.

**Probar el perfil en el emulador:** al tocar **Cámara**, el emulador muestra una escena virtual con la que se puede tomar la foto. Para **Galería**, arrastra primero una imagen desde el computador a la ventana del emulador.

---

## Limitaciones conocidas

- **Audio en el emulador:** en algunos equipos el emulador reproduce el audio acelerado por diferencias de frecuencia (44,1 kHz / 48 kHz) con la tarjeta de sonido del computador. Ayuda reiniciar el emulador con *Cold Boot* o poner el dispositivo de audio de Windows en 48 000 Hz.
- **Reproducción en segundo plano:** por ahora la radio se detiene si se cierra la pantalla, porque todavía no usa un servicio en primer plano (`MediaSessionService`).
- **Emisoras AM:** hay muy pocas emisoras AM con stream directo; si alguna URL deja de funcionar hay que reemplazarla.
- **Foto de la cámara:** `TakePicturePreview` entrega una imagen en baja resolución, suficiente para el avatar pero no para fotos grandes.

---

## Historial de desarrollo

El código de la interfaz está dividido en un archivo por sección, y cada sección se subió en su propio commit:

1. Estructura base del proyecto (Gradle, manifiesto, recursos, tema y analizador de audio)
2. Modelo de emisoras con bandas y catálogo ampliado
3. Pantalla de bienvenida
4. Tarjeta de perfil con cámara y galería
5. Encabezado y chasis
6. Visualizador de espectro
7. Controles de reproducción y perillas
8. Panel de estación
9. Selector de bandas
10. Catálogo por secciones y favoritos
11. Barra de scroll
12. Pantalla principal y vista previa completa
13. `MainActivity` con la lógica

---

## Autor

**Jorge A. Hernández**
Estudiante de Ingeniería de Software y Datos, IU Digital de Antioquia
Curso: Programación de Dispositivos Móviles
GitHub: [@JorgeAHernandezC80](https://github.com/JorgeAHernandezC80)

---

## Licencia y derechos de autor

### Español

**© 2026 Jorge A. Hernández. Todos los derechos reservados.**

El código fuente, el diseño de la interfaz y la documentación de este proyecto son propiedad exclusiva del autor. No se permite copiarlos, modificarlos, distribuirlos, publicarlos ni venderlos, total o parcialmente, sin su permiso previo y por escrito. Se autoriza su revisión con fines de **evaluación académica** en la IU Digital de Antioquia.

Las librerías de terceros (Jetpack Compose, AndroidX, Media3 ExoPlayer) se distribuyen bajo sus propias licencias (Apache 2.0). Los streams, nombres y marcas de las emisoras pertenecen a sus respectivos dueños.

### English

**© 2026 Jorge A. Hernández. All Rights Reserved.**

The source code, user interface design and documentation of this project are the exclusive property of the author. They may not be copied, modified, distributed, published or sold, in whole or in part, without the author's prior written permission. Review for **academic evaluation** purposes at IU Digital de Antioquia is permitted.

Third-party libraries (Jetpack Compose, AndroidX, Media3 ExoPlayer) are distributed under their own licenses (Apache 2.0). Radio streams, station names and trademarks belong to their respective owners.

See the [LICENSE](./LICENSE) file for the full terms. / Consulta el archivo [LICENSE](./LICENSE) para los términos completos.
