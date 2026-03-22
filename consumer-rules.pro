# Keep all serializable model classes
-keep class dev.cosmicduck.sdk.models.** { *; }
-keepclassmembers class dev.cosmicduck.sdk.models.** { *; }

# Keep kotlinx.serialization generated serializers
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class dev.cosmicduck.sdk.**$$serializer { *; }
-keepclassmembers class dev.cosmicduck.sdk.** {
    *** Companion;
}
-keepclasseswithmembers class dev.cosmicduck.sdk.** {
    kotlinx.serialization.KSerializer serializer(...);
}
