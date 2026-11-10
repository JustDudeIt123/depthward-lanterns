# Сборка JAR мода

Этот репозиторий содержит исходники отдельного мода, его ресурсы, лицензии
и файлы сборки JAR. Файлы всей сборки DepthWard сюда не включены.

Используйте Java 17 и `gradlew.bat build` (Windows) либо `sh gradlew build`.
Gradle wrapper требует доступ к сети. Точные зависимости перечислены в build.gradle.
Если используется flatDir `../mods`, поместите туда зависимости под ожидаемыми
именами. Сторонние JAR получайте с CurseForge или Modrinth. Собственные модули
клонируйте в соседние каталоги; Stamina и Lanterns собираются перед зависимыми
Ghost Villagers и Compat. Собранные зависимости также нужно поместить в ../mods.
Boss Scaling поддерживает Maven либо `-PlocalMods=<путь>`.

Исходники соответствуют локальным проектам пакета
DepthWard-CurseForge-2026-11-10.zip. Ресурсы пяти модулей побайтно сверены с JAR.
Boss Scaling компилировался локальным инструментом. Остальные четыре JAR
подготовлены с сохранением прежнего байткода и refmap; полная Gradle-пересборка
и соответствие полученного байткода этим исходникам пока не подтверждены.
Игровые и GameTest прогоны новых релизов не выполнялись.
Исторические проверки в README не подтверждают проверку этого релиза.

Лицензии сохранены. Boss Scaling — форк sixik под LGPL v3 с изменениями
justthekingg. У остальных модулей действуют собственные LICENSE.
Старые относительные ссылки README на общие документы относятся к исходной
структуре проекта:
https://github.com/JustDudeIt123/DepthWard/tree/f98d06252834e364e125473ac76f9444a46c1a0b

Другие модули:
- https://github.com/JustDudeIt123/depthward-stamina
- https://github.com/JustDudeIt123/depthward-lanterns
- https://github.com/JustDudeIt123/depthward-ghost-villagers
- https://github.com/JustDudeIt123/depthward-mod-compat
- https://github.com/JustDudeIt123/depthward-boss-scaling
