# CF Pesos

App Android para apuntar rápido los pesos de CrossFit, centrada en la búsqueda.

## Qué hace

- **Búsqueda primero**: la app abre con el buscador y el teclado listos. Busca por nombre o por
  cualquiera de sus otros nombres (alias), sin importar tildes ni mayúsculas, e incluso por
  iniciales (`bs` → *Back Squat*). Si no existe, un toque en «Crear «…»» lo crea.
- **Apuntar en dos toques**: el botón `+` de cada resultado abre un diálogo con peso y repeticiones
  ya rellenos con tu última serie (botones −/+ de 2,5 kg y 1 rep).
- **RM y porcentajes (opcional)**: en cada ejercicio, marca «Mostrar RM y porcentajes» para guardar tu
  peso máximo y ver la tabla del 40 % al 100 % (redondeada a 0,5 kg). Los ejercicios sin la casilla
  solo muestran el último peso.
- **Calculadora**: «si 70 kg es el 75 %» → te da el 100 % y el resto de porcentajes, y puedes guardarlo
  como RM.
- **Rendimiento**: cada serie se convierte en un RM estimado (Epley: `peso × (1 + reps / 30)`), que
  combina peso y repeticiones. La media del día es tu «rendimiento», con gráfica de los últimos
  30 días, comparación con el día anterior e historial por día (media, volumen, borrar con deshacer).

## Stack

Kotlin · Jetpack Compose (Material 3) · Room · Navigation Compose. `minSdk 26`, `targetSdk 35`.

## Abrir el proyecto

1. Android Studio → **Open** → esta carpeta.
2. Deja que sincronice Gradle (descarga Gradle 8.11.1 según `gradle/wrapper/gradle-wrapper.properties`).
3. Ejecuta la configuración `app` en un emulador o móvil.

> El repo no incluye `gradlew` ni `gradle-wrapper.jar`. Android Studio no los necesita; si quieres
> compilar desde terminal, genera el wrapper una vez con `gradle wrapper` (o desde el panel Gradle →
> *Tasks › build setup › wrapper*).

## Estructura

```
app/src/main/java/com/cftracker/
├── data/        Room: Exercise, LogEntry, DAO, repositorio
├── util/        búsqueda, cálculos (RM, %), estadísticas por día, formatos
└── ui/
    ├── search/     pantalla principal de búsqueda
    ├── exercise/   ficha: último peso, porcentajes, calculadora, rendimiento, historial
    └── components/ diálogo de serie y gráfica
```
