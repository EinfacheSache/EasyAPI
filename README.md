# EasyAPI

[![Java 21](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/)
[![Build with Maven](https://img.shields.io/badge/Build-Maven-orange?logo=apachemaven)](https://maven.apache.org/)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE.md)
[![JitPack](https://img.shields.io/jitpack/version/com.github.EinfacheSache/EasyAPI.svg?label=release&logo=apachemaven&color=blue)](https://jitpack.io/#EinfacheSache/EasyAPI)

🌍 Available Languages: [English](README.md) | [Deutsch](README.de.md)

---

## Dependency (via JitPack)

Add JitPack to your repositories:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add EasyAPI as a dependency:

```xml
<dependency>
    <groupId>com.github.EinfacheSache</groupId>
    <artifactId>EasyAPI</artifactId>
    <version>VERSION</version>
</dependency>
```

---

# Overview

**EasyAPI** is a lightweight, general-purpose Java library.  
It provides helpers for **async execution**, **logging** (Logback/SLF4J bridge with custom converters), **REST HTTP calls** (OkHttp), **file/YAML utilities**, **runtime metrics**, **version comparison/update helpers**, a **console command framework**, and **Minecraft utilities** (e.g., Mojang UUID lookup).

> Declared in project: **Java 21**, **Maven**, **Apache-2.0** license.  
> Maven coordinates: **`de.cubeattack:api:1.0`** (from `pom.xml`).

---

## Table of Contents
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage Examples](#usage-examples)
- [Modules / Packages](#modules--packages)
- [Build from Source](#build-from-source)
- [Dependencies](#dependencies)
- [License](#license)

---

## Features
- **AsyncExecutor** — simple scheduled executor & safe() wrapper for exception handling.
- **Logging** — Logback configuration (auto-selects config for Java 8/17), custom converters, SLF4J bridge.
- **REST utilities** — OkHttp client with GET/POST/DELETE helpers and error logging.
- **Files & YAML** — utilities for reading/writing configuration and resources.
- **Runtime metrics** — memory/CPU/runtime usage utils and a small stats container/manager.
- **Version utils** — semantic comparison helpers and update checks.
- **Console commands** — minimal framework for registering and executing console commands.
- **Minecraft utilities** — Mojang name→UUID lookup.

---

## Requirements
- **Java:** 21+
- **Build:** Maven 3.9+

---

## Installation

### Option A: Build & install locally
```bash
mvn -B -V clean install
# Then depend on: de.cubeattack:api:1.0
```

### Option B: Use as a dependency (via JitPack)
See [Dependency section](#dependency-via-jitpack).

---

## Usage Examples

### Async tasks
```java
AsyncExecutor.getService().schedule(
    AsyncExecutor.safe(() -> {
        // your async work
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

### Minecraft UUID by name
```java
UUID id = MinecraftAPI.getUUID("Notch");
System.out.println(id);
```

---

## Modules / Packages

- `de.einfachesache.api` — core (AsyncExecutor, ShutdownHook)
- `de.einfachesache.api.console` — simple console command system
- `de.einfachesache.api.logger` — LogManager + converters (logback configs in resources)
- `de.einfachesache.api.minecraft` — MinecraftAPI + stats
- `de.einfachesache.api.util` — File/Java/Localization/Log/REST/Runtime helpers
- `de.einfachesache.api.util.version` — ComparableVersion, VersionUtils

Resources:
- `src/main/resources/logback-8.xml`, `logback-17.xml`

---

## Build from Source
```bash
mvn -B -V clean package
# Output: target/EasyAPI-1.0-SNAPSHOT.jar
```

---

## Dependencies
Declared in `pom.xml`:
- `com.google.code.gson:gson`
- `ch.qos.logback:logback-classic` *(scope: provided)*
- `org.slf4j:slf4j-simple`
- `org.bspfsystems:yamlconfiguration`
- `com.squareup.okhttp3:okhttp` *(scope: provided)*
- `commons-io:commons-io` *(scope: provided)*

---

## License
Apache-2.0 — see [LICENSE.md](LICENSE.md).
