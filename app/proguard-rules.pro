# Libraries in use (Retrofit, kotlinx.serialization, Room, Hilt, WorkManager, Firebase, OkHttp,
# Coil, Play Review) ship their own consumer R8 rules, so no project keep rules are needed yet.

# Keep line numbers so Play Console can deobfuscate stack traces with the uploaded mapping file.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# slf4j (via Readability4J) looks up an optional logger binding and falls back to a no-op logger.
-dontwarn org.slf4j.impl.StaticLoggerBinder
