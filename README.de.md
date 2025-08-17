# EasyAPI

[![Java 17](https://img.shields.io/badge/Java-17-red?logo=openjdk)](https://openjdk.org/)
[![Build with Maven](https://img.shields.io/badge/Build-Maven-orange?logo=apachemaven)](https://maven.apache.org/)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE.md)
[![JitPack](https://img.shields.io/jitpack/version/com.github.EinfacheSache/EasyAPI?label=release&logo=apachemaven&color=blue)](https://jitpack.io/#EinfacheSache/EasyAPI)

🌍 Available Languages: [English](README.md) | [Deutsch](README.de.md)

---

## 📦 Dependency (JitPack)

Füge folgendes zu deinem `pom.xml` hinzu, um EasyAPI über **JitPack** zu nutzen:

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

## Überblick

**EasyAPI** ist eine **leichte, allgemeine Java-Bibliothek**.  
Sie bietet Hilfen für:

- **Asynchrone Ausführung**
- **Logging** (Logback/SLF4J-Bridge mit Custom-Konvertern)
- **REST-HTTP** (OkHttp)
- **Datei & YAML-Utilities**
- **Laufzeit-Metriken**
- **Versions-Vergleich & Update-Helfer**
- **Kleines Konsolen-Command-Framework**
- **Minecraft-Utilities** (z. B. Mojang-UUID-Lookup)

> Deklariert im Projekt: **Java 17**, **Maven**, **Apache-2.0** Lizenz.  
> Maven-Koordinaten: **`de.cubeattack:easyapi:1.0`** (aus `pom.xml`).

---

## Inhaltsverzeichnis
- [Funktionen](#funktionen)
- [Voraussetzungen](#voraussetzungen)
- [Installation](#installation)
- [Beispiele](#beispiele)
- [Module / Packages](#module--packages)
- [Build aus Source](#build-aus-source)
- [Abhängigkeiten](#abhängigkeiten)
- [Lizenz](#lizenz)

---

## Funktionen

- **AsyncExecutor** — geplanter Executor & `safe()`-Wrapper zum Fehlerfangen
- **Logging** — Logback-Konfiguration (Java 8/17), Custom-Konverter, SLF4J-Bridge
- **REST-Utilities** — OkHttp-Client mit Timeout & GET/POST/DELETE-Helpern
- **Dateien & YAML** — Utils für Configs/Resourcen
- **Runtime-Metriken** — Speicher/CPU/Runtime-Utils, kleiner Stats-Manager
- **Versions-Utils** — Vergleich/Parsing & Update-Check
- **Konsolen-Commands** — Framework zum Registrieren/Ausführen
- **Minecraft** — Name→UUID-Lookup via Mojang

Alle Features befinden sich unter `src/main/java/de/einfachesache/api/*`.

---

## Voraussetzungen

- **Java:** 17+
- **Build:** Maven 3.9+

---

## Installation

### Variante A: Lokal bauen
```bash
mvn -B -V clean install
# Danach nutzbar als Dependency: de.cubeattack:easyapi:1.0
```

### Variante B: Über JitPack
(siehe [📦 Dependency (JitPack)](#-dependency-jitpack))

---

## Beispiele

### Async-Task
```java
AsyncExecutor.getService().schedule(
    AsyncExecutor.safe(() -> {
        // dein async Code
    }),
    1, java.util.concurrent.TimeUnit.SECONDS
);
```

### REST-Request
```java
RestAPIUtils http = new RestAPIUtils();
var res = http.request("GET", "https://example.com/api", null);
if (res != null && res.isSuccessful()) {
    System.out.println(res.body().string());
}
```

### Minecraft UUID per Name
```java
UUID id = MinecraftAPI.getUUID("Notch");
System.out.println(id);
```

---

## Module / Packages

- `de.einfachesache.api` — Core (AsyncExecutor, ShutdownHook)
- `de.einfachesache.api.console` — Konsolen-Command-System
- `de.einfachesache.api.logger` — LogManager + Konverter
- `de.einfachesache.api.minecraft` — MinecraftAPI + metric (Stats*)
- `de.einfachesache.api.util` — File/Java/Log/REST/Runtime-Utils
- `de.einfachesache.api.util.version` — VersionUtils, ComparableVersion

Ressourcen:
- `src/main/resources/logback-8.xml`
- `src/main/resources/logback-17.xml`

---

## Build aus Source
```bash
mvn -B -V clean package
# Output: target/easyapi-1.0.jar
```

---

## Abhängigkeiten

Aus `pom.xml`:

- `com.google.code.gson:gson:2.13.1`
- `ch.qos.logback:logback-classic:1.4.12` *(scope: provided)*
- `org.slf4j:slf4j-simple:2.0.13`
- `org.bspfsystems:yamlconfiguration:2.0.1`
- `com.squareup.okhttp3:okhttp:5.0.0-alpha.14` *(scope: provided)*
- `commons-io:commons-io:2.15.1` *(scope: provided)*
- `org.apache.maven.plugins:maven-compiler-plugin:3.12.1` *(scope: provided)*

---

## Lizenz
Apache-2.0 — siehe [LICENSE.md](LICENSE.md).
