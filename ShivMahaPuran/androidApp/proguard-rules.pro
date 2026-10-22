# kotlinx.serialization (navigation routes and content DTOs)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class com.shivmahapuran.reader.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.shivmahapuran.reader.**$$serializer { *; }
-keepclassmembers class com.shivmahapuran.reader.** {
    *** Companion;
}
