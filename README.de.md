# EasyAPI

[![Java 21](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/)
[![Build with Maven](https://img.shields.io/badge/Build-Maven-orange?logo=apachemaven)](https://maven.apache.org/)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE.md)

🌍 Verfügbare Sprachen: [English](README.md) | [Deutsch](README.de.md)

---

# EasyAPI

**EasyAPI** ist eine leichte, allgemeine Java‑Bibliothek.  
Sie bietet Helfer für **asynchrone Ausführung**, **Logging**, **REST‑HTTP‑Aufrufe**, **Datei-/YAML‑Utilities**, **Laufzeitmetriken**, **Versions‑Utilities**, ein kleines **Konsolen‑Command‑Framework** sowie **Minecraft‑Utilities** (z. B. Mojang‑UUID‑Lookup).

> Projekt-Setup: **Java 21**, **Maven**, **Apache‑2.0**‑Lizenz  
> Maven‑Koordinaten: `de.cubeattack:api:1.0`

---

## 📦 Abhängigkeit
<a id="dependency"></a>

Wenn du **JitPack** verwendest, füge dies zu deiner `pom.xml` hinzu:

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
- [Funktionen](#features)
- [Voraussetzungen](#requirements)
- [Installation](#installation)
- [Anwendungsbeispiele](#usage-examples)
- [Module / Pakete](#modules--packages)
- [Aus dem Quellcode bauen](#build-from-source)
- [Abhängigkeiten](#dependencies)
- [Lizenz](#license)

---

<a id="features"></a>
## 🚀 Funktionen
- **AsyncExecutor** — leichter Scheduler mit `safe()`‑Wrapper für Fehlerbehandlung.
- **Logging** — Logback/SLF4J‑Bridge mit benutzerdefinierten Convertern.
- **REST‑Utilities** — OkHttp‑Wrapper mit GET/POST/DELETE‑Hilfsfunktionen.
- **Dateien & YAML** — Lese-/Schreib‑Helfer.
- **Laufzeitmetriken** — Speicher/CPU/Laufzeit überwachen.
- **Versions‑Utilities** — Versionen vergleichen und Updates prüfen.
- **Konsolen‑Commands** — minimales Framework zum Registrieren von Commands.
- **Minecraft** — Utilities für Mojang‑UUID/Name‑Lookup.

---

<a id="requirements"></a>
## ⚙ Voraussetzungen
- Java 21+
- Maven 3.9+

---

<a id="installation"></a>
## 🔧 Installation

### Option A: Lokal bauen
```bash
mvn clean install
```
Danach auf `de.cubeattack:api:1.0` als Abhängigkeit verweisen.

### Option B: Über JitPack nutzen
Siehe Abschnitt [Abhängigkeit](#dependency) oben.

---

<a id="usage-examples"></a>
## 💻 Anwendungsbeispiele

### Asynchrone Tasks
```java
AsyncExecutor.getService().schedule(
    AsyncExecutor.safe(() -> {
        // async work here
    }),
    1, java.util.concurrent.TimeUnit.SECONDS
);
```

### REST‑Requests
```java
RestAPIUtils http = new RestAPIUtils();
var res = http.request("GET", "https://example.com/api", null);
if (res != null && res.isSuccessful()) {
    System.out.println(res.body().string());
}
```

### Minecraft‑UUID‑Lookup
```java
UUID id = MinecraftAPI.getUUID("Notch");
System.out.println(id);
```

---

<a id="modules--packages"></a>
## 📂 Module / Pakete
- `de.einfachesache.api` — Core‑Utilities
- `de.einfachesache.api.console` — Konsolen‑Framework
- `de.einfachesache.api.logger` — Logging
- `de.einfachesache.api.minecraft` — Minecraft‑Utilities
- `de.einfachesache.api.util` — Sonstige Utilities (Dateien, Laufzeit, REST, etc.)
- `de.einfachesache.api.util.version` — Versionsvergleich

---

<a id="build-from-source"></a>
## 🏗 Aus dem Quellcode bauen
```bash
mvn clean package
# Ausgabe: target/EasyAPI-1.0-SNAPSHOT.jar
```

---

<a id="dependencies"></a>
## 📦 Abhängigkeiten
Deklariert in der `pom.xml`:
- `com.google.code.gson:gson`
- `ch.qos.logback:logback-classic` *(scope: provided)*
- `org.slf4j:slf4j-simple`
- `org.bspfsystems:yamlconfiguration`
- `com.squareup.okhttp3:okhttp` *(scope: provided)*
- `commons-io:commons-io` *(scope: provided)*

---

<a id="license"></a>
## 📜 Lizenz
Apache‑2.0 — siehe [LICENSE.md](LICENSE.md)
