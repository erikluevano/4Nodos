# 📱 Link para descargar la APK
Descargar APK https://drive.google.com/file/d/10004z0P07l7JWi7dDro4FaXnXvgM4V8K/view?usp=drive_link
---

# 📱 MovilSecure

**MovilSecure** es una aplicación móvil para Android diseñada para **asistir a cuidadores** de adultos mayores o personas con necesidades médicas, permitiéndoles **buscar, seleccionar y navegar hacia establecimientos de salud** (hospitales, clínicas y farmacias) de forma **rápida, clara y segura**.

El objetivo principal es **reducir el estrés y el tiempo** en la búsqueda de rutas médicas en situaciones de emergencia o cuidado.

---

## 🧭 Objetivo General

Ofrecer una herramienta móvil que permita a cuidadores **localizar rutas óptimas**, gestionar zonas frecuentes y consultar establecimientos cercanos meidante la API de Google Maps.

---

## 🧩 Arquitectura del Sistema

El sistema está desarrollado bajo la arquitectura **MVVM (Model–View–ViewModel)**, optimizada para el uso con **Android Jetpack** y **Kotlin**.
Actualmente, la funcionalidad central implementada es la gestión de “Zonas Frecuentes”, permitiendo guardar, editar y eliminar ubicaciones personalizadas como Casa, Trabajo o Centro Médico para acceder fácilmente a ellas.

### 🏗️ Estructura del Proyecto

```
com.example.movilsecure_v/
|
│   MainActivity.kt
│
├───model
│   ├───database
│   │       AppDatabase.kt
│   │
│   ├───entities
│   │       ZonaFrecuente.kt
│   │
│   └───repository
│           ZonaFrecuenteDAO.kt
│           ZonaFrecuenteRepository.kt
│
├───ui
│   └───theme
│           Color.kt
│           Theme.kt
│           Type.kt
│
├───view
│   ├───components
│   │   ├───map
│   │   │       FilterChips.kt
│   │   │       LocationCard.kt
│   │   │       MapPlaceHolder.kt
│   │   │       RouteDialog.kt
│   │   │       SearchBar.kt
│   │   │
│   │   └───zonasfrecuentes
│   │           CrearZonaFrecuenteDialog.kt
│   │           EditarZonaFrecuenteDialog.kt
│   │           ZonaFrecuenteCard.kt
│   │           ZonasHeaderCard.kt
│   │
│   └───screens
│           MapScreen.kt
│           ZonaFrecuenteScreen.kt
│
└───viewmodel
        ZonaFrecuenteViewModel.kt
```

---

## ⚙️ Tecnologías y Frameworks

| Componente         | Tecnología / Framework             | Descripción                                          |
| ------------------ | ---------------------------------- | ---------------------------------------------------- |
| Lenguaje principal | **Kotlin**                         | Nativo para Android, conciso y seguro.               |
| Arquitectura       | **MVVM**                           | Separación clara entre interfaz, lógica y datos.     |
| Framework UI       | **Jetpack Compose**                | Toolkit moderno de UI declarativa para Android.      |
| Base de datos      | **Room (SQLite)**                  | Persistencia ligera para almacenar zonas frecuentes. |
| API de mapas       | **Google Maps API**                | Obtención de rutas, ubicaciones y navegación.        |
| IDE                | **Android Studio**                 | Entorno principal de desarrollo.                     |

---

## 🧱 Estructura de las Capas (MVVM)

### 🔹 **View**

Interfaz creada con **Jetpack Compose**, donde se definen pantallas y componentes reutilizables:

* `SearchBar` → Campo de búsqueda con icono y placeholder.
* `FilterChips` → Filtros por tipo de establecimiento (hospital, clínica, farmacia).
* `LocationCard` → Tarjeta con información de un lugar, horario y botón para ver la ruta.
* `MapPlaceholder` → Simula el área donde se mostrará el mapa de Google.
* `RouteDialog` → Muestra información de ruta y opciones de transporte.
* `CrearZonaFrecuenteDialog` → Muestra los campos para agregar una Zona Frecuente.
* `EditarZonaFrecuenteDialog` → Muestra los campos para editar una Zona Frecuente.
* `ZonaFrecuenteCard` → Tarjeta que sirve para mostrar la informacion de la Zona Frecuente.
* `ZonasHeaderCard` → Tarjeta de Encabezado que sirve para proporcionar informacion y agregar una nueva Zona Frecuente.

### 🔹 **ViewModel**

Encargada de **gestionar el estado y la lógica** entre la vista y los datos.

* Datos reales desde la API de Google Maps.
* Consultas a la base de datos Room.
* Gestión de zonas frecuentes.

### 🔹 **Model**

Define las entidades del sistema y su manejo en la base de datos:

*  `ZonaFrecuente`: tabla Room para almacenar zonas favoritas.

---

## 🧩 Dependencias Clave (build.gradle.kts)

```kotlin
dependencies {
    val room_version = "2.6.1"
    // --- Core Android y Jetpack ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // --- Compose (BOM gestiona versiones) ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // --- Material Design 3 (recomendado) ---
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)

// Para build.gradle.kts (Kotlin DSL)
    implementation("androidx.compose.material:material-icons-extended")

    // --- ViewModel + LiveData (para MVVM) ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // --- Corrutinas (para ViewModel y Room) ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // --- Room (para base de datos local relacional) ---
    implementation(libs.androidx.room.ktx)
    ksp("androidx.room:room-compiler:$room_version")

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
```

---

## ▶️ Ejecución del Proyecto

### 1️. Requisitos previos

* Android Studio Giraffe o superior.
* SDK mínimo: **API 24**
* SDK objetivo: **API 34 o 35** (según tu versión actual).
* Kotlin 1.9+

### 2️. Clonar el proyecto desde Android Studio

En Android Studio → `File` → `Proyect from Version Control` → `URL: https://github.com/erikluevano/4Nodos.git` → Clonar

### 3. Ejecutar

Pulsa **Run ▶️** en Android Studio o ejecuta:

```bash
./gradlew installDebug
```

---

##  Créditos

**Autor:** Alessandro Lider Tecnico del Equipo de 4 Nodos.
