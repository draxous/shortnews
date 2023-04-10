# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Kotlin
-dontwarn org.jetbrains.annotations.**
-keep class kotlin.Metadata { *; }

# Models
-keepclassmembers class com.xzentry.shorten.data.remote.model.** { *; }
#TODO change after updating package
-keepclassmembers class com.xzentry.shorten.data.remote.model.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
# For fragment names in crash logs
-keepnames class * extends com.xzentry.shorten.ui.home.HomeActivity


 -keep class com.pierfrancescosoffritti.androidyoutubeplayer**
 -keepclassmembers class com.pierfrancescosoffritti.androidyoutubeplayer** {*;}

