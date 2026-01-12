# Совместимость данных в PRO101 ПЛАНЕР

## Важно для разработчиков

При внесении изменений в систему хранения данных **ОБЯЗАТЕЛЬНО** соблюдайте правила совместимости, чтобы не потерять данные пользователей при обновлении приложения.

## Правила совместимости данных

### ❌ НЕЛЬЗЯ менять:
- `PREF_NAME` в классах хранения (`PlannerStorage.PREF_NAME` и т.д.)
- Имена полей в JSON (`"text"`, `"done"`, `"reminderOffsetMinutes"` и т.д.)
- Формат ключей хранения (`yyyy-MM-dd_HH` для задач)

### ✅ ДОСТУПНЫЕ изменения:
- Добавление новых полей в `PlannerEntry` и JSON
- Изменение логики приложения (не затрагивающее хранение)
- Добавление новых классов хранения
- Рефакторинг кода (без изменения сериализации)

## Добавление новых полей

```java
// В PlannerEntry.toJson():
if (newField != null) {
    jsonObject.put("newField", newField);
}

// В PlannerEntry.fromJson():
if (jsonObject.has("newField")) {
    entry.newField = jsonObject.optString("newField");
}
```

## Миграция данных

При структурных изменениях используйте систему версий:

```java
private void migrateData(int fromVersion) {
    if (fromVersion < 2) {
        // Миграция на версию 2
        // Пример: переименование поля
        migrateFieldName("oldField", "newField");
    }
}
```

## Тестирование совместимости

Перед выпуском обновления:
1. Создайте бэкап тестовых данных
2. Установите старую версию приложения
3. Создайте тестовые данные
4. Обновите до новой версии
5. Проверьте, что данные сохранились

## Система хранения

### PlannerStorage (основные задачи)
- **SharedPreferences**: `planner_prefs`
- **Ключ данных**: `entries`
- **Формат**: JSON с задачами по дням

### FolderStorage (папки документов)
- **SharedPreferences**: `folder_storage`
- **Формат**: JSON со структурой папок

### NoteStorage (заметки)
- **SharedPreferences**: `note_storage`

### DocumentStorage (документы)
- **SharedPreferences**: `documents_storage`

### TemplateTaskStorage (шаблонные задачи)
- **SharedPreferences**: `template_tasks`

### PlannerWidgetPrefs (настройки виджета)
- **SharedPreferences**: `widget_prefs`

## Контакты

При любых вопросах по совместимости данных обращайтесь к разработчику: killman.instruct@gmail.com