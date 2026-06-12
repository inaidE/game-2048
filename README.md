# 2048 Android Game

2048 — Android-приложение, представляющее собой классическую игру.
Приложение разработано на Kotlin с использованием Jetpack Compose и Material 3.

## Возможности

* Управление плитками с помощью жестов (свайпов)
* Подсчёт текущих очков
* Плавные анимации появления и слияния плиток
* Адаптивная верстка под любые разрешения экранов

## Технологии

* Kotlin
* Jetpack Compose
* Material 3
* SharedPreferences
* Gradle Kotlin DSL

## Структура проекта

```text
app/
 └── src/
     └── main/
         ├── java/com/sfedu/game2048/
         │   └── MainActivity.kt
         └── res/
             ├── drawable/
             ├── mipmap-*/
             └── values/

```

## Запуск проекта

* Склонируйте репозиторий:
  ```git clone https://github.com/ваш-юзернейм/compose-2048.git```

* Откройте проект в Android Studio.

* Дождитесь синхронизации Gradle.

* Запустите приложение на эмуляторе или физическом Android-устройстве.

## Сборка через Gradle

Для debug-сборки можно выполнить:
```./gradlew assembleDebug```

Для Windows:
```gradlew.bat assembleDebug```

После сборки APK будет находиться в директории:
```app/build/outputs/apk/debug/```