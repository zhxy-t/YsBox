# TV
-keep class androidx.leanback.widget.** { *; }
-keep class com.fongmi.quickjs.method.** { *; }
-keep class com.fongmi.android.tv.bean.** { *; }

# Gson
-keep class com.google.gson.** { *; }

# SimpleXML
-keep interface org.simpleframework.xml.core.Label { public *; }
-keep class * implements org.simpleframework.xml.core.Label { public *; }
-keep interface org.simpleframework.xml.core.Parameter { public *; }
-keep class * implements org.simpleframework.xml.core.Parameter { public *; }
-keep interface org.simpleframework.xml.core.Extractor { public *; }
-keep class * implements org.simpleframework.xml.core.Extractor { public *; }
-keepclassmembers,allowobfuscation class * { @org.simpleframework.xml.Path <fields>; }
-keepclassmembers,allowobfuscation class * { @org.simpleframework.xml.Root <fields>; }
-keepclassmembers,allowobfuscation class * { @org.simpleframework.xml.Text <fields>; }
-keepclassmembers,allowobfuscation class * { @org.simpleframework.xml.Element <fields>; }
-keepclassmembers,allowobfuscation class * { @org.simpleframework.xml.Attribute <fields>; }
-keepclassmembers,allowobfuscation class * { @org.simpleframework.xml.ElementList <fields>; }

# OkHttp
-dontwarn okhttp3.**
-keep class okio.** { *; }
-keep class okhttp3.** { *; }

# SLF4J
-keeppackagenames org.slf4j.**
-keep class org.slf4j.** { *; }

# Kotlin
-keeppackagenames kotlin.**
-keep class kotlin.** { *; }

# CatVod
-keep class com.github.catvod.Proxy { *; }
-keep class com.github.catvod.crawler.** { *; }
-keep class * extends com.github.catvod.crawler.Spider

# JUPnP
-dontwarn org.jupnp.**
-keep class org.jupnp.** { *; }
-keep class javax.xml.** { *; }

# Nano
-keep class fi.iki.elonen.** { *; }

# NewPipeExtractor
-keep class javax.script.** { *; }
-keep class jdk.dynalink.** { *; }
-keep class org.mozilla.javascript.* { *; }
-keep class org.mozilla.javascript.** { *; }
-keep class org.mozilla.javascript.engine.** { *; }
-keep class org.mozilla.classfile.ClassFileWriter
-keep class org.schabi.newpipe.extractor.timeago.patterns.** { *; }
-keep class org.schabi.newpipe.extractor.services.youtube.protos.** { *; }
-dontwarn org.mozilla.javascript.JavaToJSONConverters
-dontwarn org.mozilla.javascript.tools.**
-dontwarn com.google.re2j.**
-dontwarn javax.script.**
-dontwarn jdk.dynalink.**

# richtext.aar 的可选依赖（Markdown / DiskLruCache 缓存路径未使用，R8 只需别因缺类报错）
-dontwarn com.jakewharton.disklrucache.**
-dontwarn com.zzhoujay.markdown.**

# Sardine
-keep class com.thegrizzlylabs.sardineandroid.** { *; }

# TVBus
-keep class com.tvbus.engine.** { *; }

# XunLei
-keep class com.xunlei.downloadlib.** { *; }

# Zxing QRCode ABI
-keep class com.google.zxing.* { *; }
-keep class com.google.zxing.common.BitMatrix { *; }
-keep class com.google.zxing.common.HybridBinarizer { *; }
-keep class com.google.zxing.qrcode.QRCodeReader { *; }
-keep class com.google.zxing.qrcode.QRCodeWriter { *; }
-keep class com.google.zxing.qrcode.decoder.ErrorCorrectionLevel { *; }

# libplayer.so resolves these ISO callbacks by literal JNI names.
-keep class com.fongmi.android.tv.player.iso.IsoSessionManager {
    public static long length(long);
    public static int readAt(long, long, java.nio.ByteBuffer, int);
    public static void close(long);
    public static void prepareTrackMetadata(long, int);
}

# JNI entry points are resolved by name at runtime.
-keepclasseswithmembernames class com.fongmi.android.tv.player.subtitle.AssNative { native <methods>; }

# 实验室落盘模型（注册表 registry.json＋包内 lab.json）：全是 Gson 反射
# 落盘、跨版本读回的表。成员被 R8 改名/剥离时，旧版写的文件新版按新名
# 读回——字段对不上轻则全空、重则类型冲突令整表被当坏文件改名清空。
# 整类保名保成员，落盘名再由 @SerializedName 钉死，双保险。
-keep class com.fongmi.android.tv.ui.lab.LabRegistry { *; }
-keep class com.fongmi.android.tv.ui.lab.LabRegistry$Svc { *; }
-keep class com.fongmi.android.tv.ui.lab.LabRegistry$Runtime { *; }
-keep class com.fongmi.android.tv.ui.lab.LabManifest { *; }
-keep class com.fongmi.android.tv.ui.lab.LabManifest$LabCommand { *; }
-keep class com.fongmi.android.tv.ui.lab.LabManifest$LabVar { *; }
-keep class com.fongmi.android.tv.ui.lab.LabManifest$LabOption { *; }
-keep class com.fongmi.android.tv.ui.lab.LabManifest$LabDownload { *; }
-keep class com.fongmi.android.tv.ui.lab.LabManifest$LabOutput { *; }
-keep class com.fongmi.android.tv.ui.lab.LabCustomCommands$CustomCommand { *; }
