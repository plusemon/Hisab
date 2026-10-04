# Optimization settings
-repackageclasses ''
-allowaccessmodification

# Keep data models used by Room, JSON, and business logic
-keep class com.plusemon.hisab.data.model.** { *; }
-keepclassmembers enum com.plusemon.hisab.data.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Room database and DAOs
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.RoomDatabase

# Keep reflection/metadata needed for Kotlin Coroutines and annotations
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# OkHttp rules
-dontwarn okhttp3.**
-dontwarn okio.**

# Firebase & Google Play Services
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

