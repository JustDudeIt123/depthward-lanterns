# Сборка Lanterns 1.3.3

Нужен JDK 17. Сборка: `gradlew.bat clean build` в Windows или `sh gradlew clean build` в Linux/macOS. Gradle wrapper, ForgeGradle и development-зависимости загружаются из сети; результат — build/libs.

Текущий build.gradle использует flatDir ../mods. В соседний с корнем проекта каталог mods поместите:

- curios-forge-5.14.1+1.20.1.jar
- VIS-RyoamicLights-forge-0.2.3+mc1.20.1.jar — официальный Forge-файл RyoamicLights переименуйте в это имя для development-сборки.

Сторонние JAR не включены. Проекты: https://www.curseforge.com/minecraft/mc-mods/curios и https://www.curseforge.com/minecraft/mc-mods/ryoamiclights. Обычная игровая установка не требует этих development-имён.

src/main — код/ресурсы, src/test — JUnit, src/gameTest — Forge GameTest. validation-tests содержит дополнительные standalone Java-проверки геометрии/фильтра изменений/контекста света. LanternOwnerLightTest требует игрового classpath и собранного JAR, остальные проверки геометрии/контекста можно компилировать вместе с соответствующими Java-классами без запуска Minecraft.

При подготовке 20 ноября 2026 полная новая Gradle-сборка не завершилась из-за тайм-аута загрузки Gradle 8.8. Прямая полная javac-проверка по локальному remapped API-кэшу остановилась на закрытом BlockEntitySupplier: кэш не применяет все Forge access transformers и не заменяет нормальную ForgeGradle-сборку. Успешная проверка отдельных изменённых классов не является подтверждением полной пересборки проекта. Standalone клиент/сервер без Compat ещё не запускался.
