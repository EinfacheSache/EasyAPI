# EasyAPI

[![Java 17](https://img.shields.io/badge/Java-17-red?logo=openjdk)](https://openjdk.org/)
[![Build with Maven](https://img.shields.io/badge/Build-Maven-orange?logo=apachemaven)](https://maven.apache.org/)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE.md)

🌍 Available Languages: [English](README.md) | [Deutsch](README_DE.md)

---

# EasyAPI

**EasyAPI** is a lightweight and general-purpose Java library.  
It provides helpers for **async execution**, **logging**, **REST HTTP calls**, **file/YAML utilities**, **runtime metrics**, **version utilities**, a small **console command framework**, and **Minecraft utilities** (e.g., Mojang UUID lookup).

> Project setup: **Java 17**, **Maven**, **Apache-2.0** license  
> Maven coordinates: `de.cubeattack:api:1.0`

---

## 📦 Dependency

If you use **JitPack**, add this to your `pom.xml`:

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

## 📋 Table of Contents
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage Examples](#usage-examples)
- [Modules / Packages](#modules--packages)
- [Build from Source](#build-from-source)
- [Dependencies](#dependencies)
- [License](#license)

---

## 🚀 Features
- **AsyncExecutor** — lightweight scheduler with `safe()` wrapper for error handling.
- **Logging** — Logback/SLF4J bridge with custom converters.
- **REST utilities** — OkHttp wrapper with GET/POST/DELETE helpers.
- **Files & YAML** — read/write helpers.
- **Runtime metrics** — monitor memory/CPU/runtime usage.
- **Version utils** — compare versions and check updates.
- **Console commands** — minimal framework to register commands.
- **Minecraft** — Mojang UUID/name lookup utilities.

---

## ⚙ Requirements
- Java 17+
- Maven 3.9+

---

## 🔧 Installation

### Option A: Build locally
```bash
mvn clean install
```
Then depend on `de.cubeattack:api:1.0`.

### Option B: Use via JitPack
See the [Dependency](#-dependency) section above.

---

## 💻 Usage Examples

### Async tasks
```java
AsyncExecutor.getService().schedule(
    AsyncExecutor.safe(() -> {
        // async work here
    }),
    1, java.util.concurrent.TimeUnit.SECONDS
);
```

### REST requests
```java
RestAPIUtils http = new RestAPIUtils();
var res = http.request("GET", "https://example.com/api", null);
if (res != null && res.isSuccessful()) {
    System.out.println(res.body().string());
}
```

### Minecraft UUID lookup
```java
UUID id = MinecraftAPI.getUUID("Notch");
System.out.println(id);
```

---

## 📂 Modules / Packages
- `de.einfachesache.api` — core utilities
- `de.einfachesache.api.console` — console framework
- `de.einfachesache.api.logger` — logging
- `de.einfachesache.api.minecraft` — Minecraft utils
- `de.einfachesache.api.util` — misc utils (files, runtime, REST, etc.)
- `de.einfachesache.api.util.version` — version comparison

---

## 🏗 Build from Source
```bash
mvn clean package
# Output: target/api-1.0.jar
```

---

## 📦 Dependencies
Declared in `pom.xml`:
- `com.google.code.gson:gson`
- `ch.qos.logback:logback-classic` *(scope: provided)*
- `org.slf4j:slf4j-simple`
- `org.bspfsystems:yamlconfiguration`
- `com.squareup.okhttp3:okhttp` *(scope: provided)*
- `commons-io:commons-io` *(scope: provided)*


---

## 📜 License
Apache-2.0 — see [LICENSE.md](LICENSE.md)
