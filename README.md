# IoT-Malicious-Traffic-Monitor

A network security project designed to monitor and capture suspicious or malicious traffic targeting IoT devices.
The system logs incoming requests and analyzes packet activity to help study potential attack patterns against IoT environments.

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
