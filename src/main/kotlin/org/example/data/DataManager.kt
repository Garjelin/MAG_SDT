package org.example.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.model.*
import java.io.File

/**
 * Контейнер для всех данных приложения
 * @property catalog Каталог книг
 * @property customers Список покупателей
 * @property orders Список заказов
 * @property waitingList База ожидания
 * @property nextCustomerId Следующий ID для покупателя
 * @property nextOrderId Следующий ID для заказа
 */
@Serializable
data class AppData(
    val catalog: Catalog = Catalog(),
    val customers: MutableList<Customer> = mutableListOf(),
    val orders: MutableList<Order> = mutableListOf(),
    val waitingList: WaitingList = WaitingList(),
    var nextCustomerId: Int = 1,
    var nextOrderId: Int = 1
)

/**
 * Класс DataManager управляет сохранением и загрузкой данных приложения
 * Использует JSON формат для хранения данных в файлах
 */
class DataManager {

    // Настройка JSON с красивым форматированием
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // Папка для хранения данных
    private val dataDir = File(System.getProperty("user.home"), ".bookshop")

    // Файл с данными
    private val dataFile = File(dataDir, "data.json")

    // Файл резервной копии
    private val backupFile = File(dataDir, "data.backup.json")

    init {
        // Создаем папку для данных, если её нет
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }
    }

    /**
     * Загрузить все данные из файла
     * @return Загруженные данные или новый экземпляр AppData если файл не существует
     */
    fun loadData(): AppData {
        return try {
            if (dataFile.exists()) {
                val jsonString = dataFile.readText()
                val data = json.decodeFromString<AppData>(jsonString)
                println("✓ Данные успешно загружены из ${dataFile.absolutePath}")
                data
            } else {
                println("⚠ Файл данных не найден. Создаем новую базу данных.")
                createInitialData()
            }
        } catch (e: Exception) {
            println("✗ Ошибка при загрузке данных: ${e.message}")

            // Пытаемся загрузить из резервной копии
            if (backupFile.exists()) {
                try {
                    println("⚠ Попытка восстановления из резервной копии...")
                    val jsonString = backupFile.readText()
                    val data = json.decodeFromString<AppData>(jsonString)
                    println("✓ Данные восстановлены из резервной копии")
                    return data
                } catch (backupError: Exception) {
                    println("✗ Ошибка при восстановлении из резервной копии: ${backupError.message}")
                }
            }

            println("⚠ Создаем новую базу данных")
            createInitialData()
        }
    }

    /**
     * Сохранить все данные в файл
     * @param data Данные для сохранения
     * @return true если сохранение успешно
     */
    fun saveData(data: AppData): Boolean {
        return try {
            // Создаем резервную копию перед сохранением
            if (dataFile.exists()) {
                dataFile.copyTo(backupFile, overwrite = true)
            }

            // Сохраняем данные
            val jsonString = json.encodeToString(data)
            dataFile.writeText(jsonString)
            println("✓ Данные успешно сохранены в ${dataFile.absolutePath}")
            true
        } catch (e: Exception) {
            println("✗ Ошибка при сохранении данных: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Создать начальные тестовые данные
     * @return Новый экземпляр AppData с тестовыми данными
     */
    private fun createInitialData(): AppData {
        val appData = AppData()

        // Добавляем несколько тестовых книг
        appData.catalog.addBook(
            author = "Александр Пушкин",
            title = "Евгений Онегин",
            publisher = "АСТ",
            publicationYear = 2020,
            price = 350.0,
            quantity = 5
        )

        appData.catalog.addBook(
            author = "Лев Толстой",
            title = "Война и мир",
            publisher = "Эксмо",
            publicationYear = 2019,
            price = 850.0,
            quantity = 3
        )

        appData.catalog.addBook(
            author = "Федор Достоевский",
            title = "Преступление и наказание",
            publisher = "АСТ",
            publicationYear = 2021,
            price = 450.0,
            quantity = 0  // Нет в наличии
        )

        appData.catalog.addBook(
            author = "Михаил Булгаков",
            title = "Мастер и Маргарита",
            publisher = "Азбука",
            publicationYear = 2022,
            price = 550.0,
            quantity = 7
        )

        appData.catalog.addBook(
            author = "Антон Чехов",
            title = "Вишневый сад",
            publisher = "Эксмо",
            publicationYear = 2020,
            price = 300.0,
            quantity = 4
        )

        // Добавляем тестовых покупателей
        val customer1 = Customer(
            id = appData.nextCustomerId++,
            name = "Иванов Иван Иванович",
            email = "ivanov@example.com",
            phone = "+7 (900) 123-45-67"
        )
        appData.customers.add(customer1)

        val customer2 = Customer(
            id = appData.nextCustomerId++,
            name = "Петрова Мария Сергеевна",
            email = "petrova@example.com",
            phone = "+7 (900) 234-56-78"
        )
        appData.customers.add(customer2)

        val customer3 = Customer(
            id = appData.nextCustomerId++,
            name = "Сидоров Петр Александрович",
            email = "sidorov@example.com",
            phone = "+7 (900) 345-67-89"
        )
        appData.customers.add(customer3)

        println("✓ Созданы тестовые данные: ${appData.catalog.getTotalBooksCount()} книг, ${appData.customers.size} покупателей")

        return appData
    }

    /**
     * Экспортировать данные в указанный файл
     * @param exportFile Файл для экспорта
     * @param data Данные для экспорта
     * @return true если экспорт успешен
     */
    fun exportData(exportFile: File, data: AppData): Boolean {
        return try {
            val jsonString = json.encodeToString(data)
            exportFile.writeText(jsonString)
            println("✓ Данные экспортированы в ${exportFile.absolutePath}")
            true
        } catch (e: Exception) {
            println("✗ Ошибка при экспорте данных: ${e.message}")
            false
        }
    }

    /**
     * Импортировать данные из указанного файла
     * @param importFile Файл для импорта
     * @return Импортированные данные или null при ошибке
     */
    fun importData(importFile: File): AppData? {
        return try {
            if (!importFile.exists()) {
                println("✗ Файл ${importFile.absolutePath} не найден")
                return null
            }

            val jsonString = importFile.readText()
            val data = json.decodeFromString<AppData>(jsonString)
            println("✓ Данные импортированы из ${importFile.absolutePath}")
            data
        } catch (e: Exception) {
            println("✗ Ошибка при импорте данных: ${e.message}")
            null
        }
    }

    /**
     * Получить путь к файлу данных
     * @return Путь к файлу
     */
    fun getDataFilePath(): String {
        return dataFile.absolutePath
    }

    /**
     * Получить размер файла данных
     * @return Размер в байтах или 0 если файл не существует
     */
    fun getDataFileSize(): Long {
        return if (dataFile.exists()) dataFile.length() else 0L
    }

    /**
     * Проверить существование файла данных
     * @return true если файл существует
     */
    fun dataFileExists(): Boolean {
        return dataFile.exists()
    }

    /**
     * Удалить все данные (файл и резервную копию)
     * @return true если удаление успешно
     */
    fun deleteAllData(): Boolean {
        return try {
            var success = true
            if (dataFile.exists()) {
                success = success && dataFile.delete()
            }
            if (backupFile.exists()) {
                success = success && backupFile.delete()
            }
            println("✓ Все данные удалены")
            success
        } catch (e: Exception) {
            println("✗ Ошибка при удалении данных: ${e.message}")
            false
        }
    }

    /**
     * Создать резервную копию данных вручную
     * @param data Данные для копирования
     * @return true если создание резервной копии успешно
     */
    fun createManualBackup(data: AppData): Boolean {
        return try {
            val timestamp = System.currentTimeMillis()
            val manualBackupFile = File(dataDir, "data.backup.$timestamp.json")
            val jsonString = json.encodeToString(data)
            manualBackupFile.writeText(jsonString)
            println("✓ Создана резервная копия: ${manualBackupFile.name}")
            true
        } catch (e: Exception) {
            println("✗ Ошибка при создании резервной копии: ${e.message}")
            false
        }
    }
}