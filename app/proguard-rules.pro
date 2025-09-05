##############################################
# GENERALES / ANDROIDX / KOTLIN
##############################################
-keep,allowobfuscation @androidx.annotation.Keep class *
-keepclassmembers class * { @androidx.annotation.Keep *; }
-keepattributes *Annotation*,Signature,EnclosingMethod,InnerClasses,SourceFile,LineNumberTable
-dontwarn org.jetbrains.annotations.**
-dontwarn kotlin.**
-dontwarn javax.annotation.**

##############################################
# COMPOSE (generalmente no requiere reglas, evitamos warnings)
##############################################
-dontwarn androidx.compose.**
-dontwarn androidx.activity.compose.**

##############################################
# COROUTINES (por seguridad)
##############################################
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

##############################################
# KOIN (inyección por reflexión)
##############################################
-keep class org.koin.** { *; }
-dontwarn org.koin.**

##############################################
# ROOM (reflexión + clases generadas)
##############################################
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase
# Impl generadas por Room (DAOs/DB)
-keep class **_Impl
# Mantén métodos/fields anotados por Room
-keepclassmembers class * {
  @androidx.room.* <methods>;
  @androidx.room.* <fields>;
}

##############################################
# DATASTORE
##############################################
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

##############################################
# WORKMANAGER
##############################################
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

##############################################
# KTOR (engine Android + plugins)
##############################################
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# (Opcional) si en algún módulo usas CIO u OkHttp, añade sus paquetes:
# -keep class io.ktor.client.engine.cio.** { *; }
# -keep class io.ktor.client.engine.okhttp.** { *; }
# -dontwarn okhttp3.**
# -dontwarn okio.**

##############################################
# KOTLINX SERIALIZATION (ContentNegotiation: json)
##############################################
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**
-keep @kotlinx.serialization.Serializable class ** { *; }
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
    @kotlinx.serialization.Transient <fields>;
}

##############################################
# COIL (imágenes)
##############################################
-keep class coil.** { *; }
-dontwarn coil.**

##############################################
# GSON / MOSHI (descomenta si aplican)
##############################################
# GSON: si parseas por reflexión pura, conserva tus modelos
# -keep class com.tuapp.model.** { *; }
# -dontwarn com.google.gson.**

# MOSHI:
# -keep class com.squareup.moshi.** { *; }
# -keep @com.squareup.moshi.JsonClass class ** { *; }
# -dontwarn com.squareup.moshi.**

##############################################
# OPCIONALES / UTILES
##############################################
# Si accedes a clases por reflexión manual, consérvalas:
# -keep class com.tuapp.reflection.** { *; }

# Si comparas enums por nombre
-keepclassmembers enum * { *; }


-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.impl.StaticMDCBinder
