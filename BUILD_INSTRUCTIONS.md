# Инструкции по сборке исполняемого файла

## Требования
- JDK 21 или выше
- Gradle (встроен в проект через Gradle Wrapper)
- **Для Windows MSI**: WiX Toolset 3.11+ (см. ниже)

## ⚠️ ВАЖНО для создания MSI на Windows

Для создания MSI инсталлятора требуется **WiX Toolset**:

### Установка WiX Toolset:
1. Скачайте с: https://wixtoolset.org/releases/
2. Установите WiX Toolset 3.11 или выше
3. Перезапустите командную строку после установки

### Проверка установки:
```bash
light.exe -?
```
Если команда работает - WiX установлен правильно.

## Создание исполняемого файла

### ⭐ РЕКОМЕНДУЕТСЯ: Простой запуск (без инсталлятора)
```bash
# Просто запустить приложение
gradlew run
```
Это самый простой способ! Не требует создания инсталлятора.

### Для Windows (.msi):
```bash
# ТРЕБУЕТСЯ WiX Toolset!
gradlew packageMsi
```
Готовый файл: `build/compose/binaries/main/msi/BookShop-1.0.0.msi`

### Для Windows (распакованное приложение):
```bash
# Создать готовое к запуску приложение БЕЗ инсталлятора
gradlew createDistributable
```
Готовая папка: `build/compose/binaries/main/app/`
Запуск: `build/compose/binaries/main/app/BookShop/BookShop.exe`

### Для других платформ:
```bash
# macOS (.dmg)
./gradlew packageDmg

# Linux (.deb)
./gradlew packageDeb
```

## Быстрый запуск без установки
Для запуска приложения напрямую без создания инсталлятора:
```bash
gradlew run
```

## Возможные проблемы

1. **Ошибка "light.exe exited with 311 code"**
   - Причина: Не установлен WiX Toolset
   - Решение: Установите WiX Toolset 3.11+ или используйте `gradlew createDistributable`

2. **Ошибка JDK**: Убедитесь, что используется JDK 21 или выше

3. **Недостаточно памяти**: Увеличьте heap для Gradle в `gradle.properties`

4. **Отсутствует иконка**: Иконка не обязательна, сборка пройдет без неё

## ⚡ Быстрое решение без установки WiX

Если не хотите устанавливать WiX Toolset, используйте:

```bash
# Создать готовое приложение (БЕЗ инсталлятора)
gradlew createDistributable
```

Приложение будет в: `build/compose/binaries/main/app/BookShop/`
Можно скопировать эту папку куда угодно и запустить `BookShop.exe`

## Структура результата
После сборки в папке `build/compose/binaries/main/` будут:
- `app/` - папка с распакованным приложением
- `msi/` - Windows MSI инсталлятор
- `exe/` - Windows EXE инсталлятор (если поддерживается)

