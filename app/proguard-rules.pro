# Keep Kotlinx Serialization routes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class io.github.kitoku95.mediaarchive.navigation.**$$serializer { *; }
-keepclassmembers class io.github.kitoku95.mediaarchive.navigation.** {
    *** Companion;
}
