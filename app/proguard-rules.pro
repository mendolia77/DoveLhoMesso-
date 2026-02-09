# Add project specific ProGuard rules here.

# Keep Room entities
-keep class com.dovelhomesso.app.data.entities.** { *; }

# Keep Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep backup data classes
-keep class com.dovelhomesso.app.data.exportimport.** { *; }
