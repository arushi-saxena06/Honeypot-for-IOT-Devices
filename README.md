# Honeypot-for-IOT-Devices

This project uses an IoT honeypot, which mimics vulnerable IoT devices to detect malicious activities. It records the activities of attackers, such as IP addresses, request patterns, and payload, which are then displayed on a real-time monitoring dashboard.

## Tech Stack

Backend:Java Spring Boot
Frontend: Simple UI for monitoring captured activity
Build Tool: Maven

## Requirements

Java 21 (LTS)— Install a JDK 21 distribution (Eclipse Temurin / Adoptium, Oracle JDK, Azul, etc.).
  Set `JAVA_HOME` to the JDK installation path and add its `bin` directory to your `PATH`.

Maven 3.x to build the backend.

## Setup & Build

Quick check and build using PowerShell:

```powershell
# Confirm Java installation
java -version

# Confirm Maven installation
mvn -v

# From the repository root: build the backend
mvn -f .\backend\pom.xml clean package
```

## Features

* Captures incoming network traffic targeting IoT services
* Logs suspicious requests for analysis
* Provides a simple UI to monitor captured activity
* Helps study attack behavior in IoT environments

## Use Case

This project can be used in cybersecurity labs or learning environments to understand how malicious traffic interacts with IoT systems and to analyze potential attack patterns.
