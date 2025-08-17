# EasyAPI

[![Java 17](https://img.shields.io/badge/Java-17-red?logo=openjdk)](https://openjdk.org/)
[![Build with Maven](https://img.shields.io/badge/Build-Maven-orange?logo=apachemaven)](https://maven.apache.org/)
[![License: Apache-2.0](https://img.shields.io/badge/Lizenz-Apache_2.0-blue.svg)](LICENSE.md)

🌍 Verfügbare Sprachen: [English](README.md) | [Deutsch](README.de.md)

---

# EasyAPI

**EasyAPI** ist eine leichte, allgemeine Java-Bibliothek.  
Sie bietet Helfer für **asynchrone Ausführung**, **Logging**, **REST-HTTP-Aufrufe**, **Datei/YAML-Utilities**, **Laufzeit-Metriken**, **Versions-Utilities**, ein kleines **Konsolen-Command-Framework** und **Minecraft-Utilities** (z. B. Mojang-UUID-Lookup).

> Projektumgebung: **Java 17**, **Maven**, **Apache-2.0** Lizenz  
> Maven-Koordinaten: `de.cubeattack:api:1.0`

---

## 📦 Dependency

Wenn du **JitPack** nutzt, füge Folgendes zu deiner `pom.xml` hinzu:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.EinfacheSache</groupId>
        <artifactId>EasyAPI</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```

---

## 📋 Inhaltsverzeichnis
- [Funktionen](#funktionen)
- [Voraussetzungen](#voraussetzungen)
- [Installation](#installation)
- [Beispielnutzung](#beispielnutzung)
- [Module / Packages](#module--packages)
- [Build aus Quellcode](#build-aus-quellcode)
- [Abhängigkeiten](#abhängigkeiten)
- [Lizenz](#lizenz)

---

## 🚀 Funktionen
- **AsyncExecutor** — leichter Scheduler mit `safe()`-Wrapper für Fehlerbehandlung.
- **Logging** — Logback/SLF4J-Bridge mit Custom-Konvertern.
- **REST-Utilities** — OkHttp-Wrapper mit GET/POST/DELETE.
- **Dateien & YAML** — Lese/Schreib-Helfer.
- **Laufzeit-Metriken** — Speicher/CPU/Runtime-Überwachung.
- **Versions-Utils** — Versionsvergleich und Update-Prüfung.
- **Konsolen-Befehle** — minimales Framework zur Command-Registrierung.
- **Minecraft** — Mojang UUID/Name Lookup.

---

## ⚙ Voraussetzungen
- Java 17+
- Maven 3.9+

---

## 🔧 Installation

### Variante A: Lokal bauen
```bash
mvn clean install
```
Danach: `de.cubeattack:api:1.0` als Dependency einbinden.

### Variante B: Über JitPack
Siehe [Dependency](#-dependency).

---

## 💻 Beispielnutzung

### Async-Tasks
```java
AsyncExecutor.getService().schedule(
    AsyncExecutor.safe(() -> {
        // async Code hier
    }),
    1, java.util.concurrent.TimeUnit.SECONDS
);
```

### REST-Requests
```java
RestAPIUtils http = new RestAPIUtils();
var res = http.request("GET", "https://example.com/api", null);
if (res != null && res.isSuccessful()) {
    System.out.println(res.body().string());
}
```

### Minecraft UUID Lookup
```java
UUID id = MinecraftAPI.getUUID("Notch");
System.out.println(id);
```

---

## 📂 Module / Packages
- `de.einfachesache.api` — Core Utilities
- `de.einfachesache.api.console` — Konsolen-Framework
- `de.einfachesache.api.logger` — Logging
- `de.einfachesache.api.minecraft` — Minecraft-Utils
- `de.einfachesache.api.util` — Utils (Dateien, Runtime, REST, usw.)
- `de.einfachesache.api.util.version` — Versionsvergleich

---

## 🏗 Build aus Quellcode
```bash
mvn clean package
# Output: target/api-1.0.jar
```

---

## 📦 Abhängigkeiten
Declared in `pom.xml`:
- `com.google.code.gson:gson`
- `ch.qos.logback:logback-classic` *(scope: provided)*
- `org.slf4j:slf4j-simple`
- `org.bspfsystems:yamlconfiguration`
- `com.squareup.okhttp3:okhttp` *(scope: provided)*
- `commons-io:commons-io` *(scope: provided)*

---

## 📜 Lizenz
Apache-2.0 — siehe [LICENSE.md](LICENSE.md)
