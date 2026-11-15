# Keep Kotlinx Serialization routes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.mediaarchive.app.navigation.**$$serializer { *; }
-keepclassmembers class com.mediaarchive.app.navigation.** {
    *** Companion;
}
