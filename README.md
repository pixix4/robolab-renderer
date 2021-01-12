# robolab renderer

Rewrite of the robolab rendering egnine. 

## Features

- [x] Visualise planets
- [x] Planet editor
- [x] Animation support **new**
- [x] Multiplatform support (ui abstraction) **new** 

## Usage

### JavaFX Version

Build and execute app directly
```shell script
./gradlew jvmRun
```

or

Build cross platform jar and execute app manually
```shell script
./gradlew jvmJar
java -jar build/libs/robolab-jvm.jar
```

### Web Version

Build app and run dev server directly
```shell script
./gradlew jsClientRun
```

or

Build app und run dev server as separate process (speeds up development)
```shell script
./gradlew jsClientSync

cd web
npm install
node index.js
```
