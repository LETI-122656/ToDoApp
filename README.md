# App README

## Project Structure

The main entry point into the application is `Application.java`. This class contains the `main()` method that start up 
the Spring Boot application.

The skeleton follows a *feature-based package structure*, organizing code by *functional units* rather than traditional 
architectural layers. It includes two feature packages: `base` and `examplefeature`.

* The `base` package contains classes meant for reuse across different features, either through composition or 
  inheritance. You can use them as-is, tweak them to your needs, or remove them.
* The `examplefeature` package is an example feature package that demonstrates the structure. It represents a 
  *self-contained unit of functionality*, including UI components, business logic, data access, and an integration test.
  Once you create your own features, *you'll remove this package*.

The `src/main/frontend` directory contains an empty theme called `default`, based on the Lumo theme. It is activated in
the `Application` class, using the `@Theme` annotation.

## Starting in Development Mode

To start the application in development mode, import it into your IDE and run the `Application` class. 
You can also start the application from the command line by running: 

```bash
./mvnw
```

## Building for Production

To build the application in production mode, run:

```bash
./mvnw -Pproduction package
```

To build a Docker image, run:

```bash
docker build -t my-application:latest .
```

If you use commercial components, pass the license key as a build secret:

```bash
docker build --secret id=proKey,src=$HOME/.vaadin/proKey .
```

## Getting Started

The [Getting Started](https://vaadin.com/docs/latest/getting-started) guide will quickly familiarize you with your new
App implementation. You'll learn how to set up your development environment, understand the project 
structure, and find resources to help you add muscles to your skeleton — transforming it into a fully-featured 
application.

## Features
- Lista de tarefas
- Conversão de moeda
- Gerar QR code para tarefas (requer setup do domínio raíz do projeto no ficheiro `application.properties`)
- Enviar tarefa por email (requer setup do email remetente no ficheiro `application.properties`)

## Pipeline de Build (CI/CD)

Este projeto possui um workflow de CI/CD configurado no GitHub Actions.  
Ele automatiza a criação de um ficheiro JAR executável da aplicação sempre que há um push na branch principal.

**Funcionalidades da pipeline:**
- Configura o ambiente Java (versão 21);
- Executa `mvn clean package` para gerar o JAR;
- Copia o JAR para a raiz do workspace (`app.jar`) para acesso direto no workflow;
- Publica o JAR como artefacto no GitHub Actions;

Nota: o app.jar não é commitado para a raiz do repositório, isto iria adicionar um ficheiro binário grande ao histórico do Git, o que não é recomendado. O JAR fica disponível apenas como artefacto do workflow no GitHub Actions, acessível na interface web na secção "Artifacts".

**Excerto do build.yml:**
```yaml
- name: Build with Maven
  run: mvn clean package

- name: Copy JAR to root
  run: cp target/*.jar ./app.jar

- name: Upload JAR
  uses: actions/upload-artifact@v4
  with:
    name: my-app-jar
    path: app.jar
```
