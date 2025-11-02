package org.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.data.BookShopRepository
import org.example.model.Book
import java.text.SimpleDateFormat
import java.util.*

/**
 * Экран статистики
 * Отображает аналитику и статистические данные
 */
@Composable
fun StatisticsScreen(repository: BookShopRepository) {
    val statistics = remember { repository.getStatistics() }
    val books = remember { repository.getAllBooks() }
    val orders = remember { repository.getAllOrders() }

    // Обновление при изменении данных
    var stats by remember { mutableStateOf(statistics) }

    LaunchedEffect(Unit) {
        repository.onDataChanged = {
            stats = repository.getStatistics()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "📊 Статистика",
                    style = MaterialTheme.typography.h4,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Общая статистика (4 карточки)
        item {
            OverallStatistics(stats)
        }

        // Статистика каталога
        item {
            CatalogStatistics(books, stats)
        }

        // Статистика заказов
        item {
            OrdersStatistics(orders, stats)
        }

        // Топ популярных книг
        item {
            PopularBooksStatistics(repository)
        }

        // График заказов по месяцам
        item {
            OrdersByMonthChart(orders)
        }
    }
}

/**
 * Общая статистика (4 главных показателя)
 */
@Composable
fun OverallStatistics(stats: Map<String, Any>) {
    Column {
        Text(
            text = "Общая статистика",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Книг",
                value = stats["totalBooks"].toString(),
                icon = Icons.Default.Menu,
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Покупателей",
                value = stats["totalCustomers"].toString(),
                icon = Icons.Default.Person,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Заказов",
                value = stats["totalOrders"].toString(),
                icon = Icons.Default.ShoppingCart,
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Ожидают",
                value = stats["waitingListSize"].toString(),
                icon = Icons.Default.Clear,
                color = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Карточка статистики
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = 4.dp,
        backgroundColor = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.caption,
                color = color
            )
        }
    }
}

/**
 * Статистика каталога
 */
@Composable
fun CatalogStatistics(books: List<Book>, stats: Map<String, Any>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📚 Каталог",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    StatRow("Всего книг:", stats["totalBooks"].toString())
                    StatRow("В наличии:", stats["totalQuantity"].toString() + " экз.")
                    StatRow("Общая стоимость:", "%.2f ₽".format(stats["totalValue"]))
                }

                Column(modifier = Modifier.weight(1f)) {
                    val availableBooks = books.count { it.isAvailable() }
                    val unavailableBooks = books.size - availableBooks
                    val avgPrice = if (books.isNotEmpty()) books.map { it.price }.average() else 0.0

                    StatRow("Доступно:", "$availableBooks книг")
                    StatRow("Отсутствует:", "$unavailableBooks книг")
                    StatRow("Средняя цена:", "%.2f ₽".format(avgPrice))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Прогресс бар наличия
            val availablePercentage = if (books.isNotEmpty()) {
                (books.count { it.isAvailable() }.toFloat() / books.size.toFloat())
            } else 0f

            Column {
                Text(
                    text = "Доступность книг",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = availablePercentage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF4CAF50),
                    backgroundColor = Color.LightGray
                )
                Text(
                    text = "${(availablePercentage * 100).toInt()}% книг в наличии",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Статистика заказов
 */
@Composable
fun OrdersStatistics(orders: List<org.example.model.Order>, stats: Map<String, Any>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📋 Заказы",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    StatRow("Всего заказов:", stats["totalOrders"].toString())
                    StatRow("Завершено:", stats["completedOrders"].toString())
                    StatRow("В ожидании:", stats["waitingOrders"].toString())
                }

                Column(modifier = Modifier.weight(1f)) {
                    StatRow("Активных:", stats["activeOrders"].toString())
                    val completionRate = if ((stats["totalOrders"] as Int) > 0) {
                        ((stats["completedOrders"] as Int).toFloat() / (stats["totalOrders"] as Int).toFloat() * 100).toInt()
                    } else 0
                    StatRow("Выполнение:", "$completionRate%")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Визуализация статусов
            val total = (stats["totalOrders"] as? Int) ?: 1
            val completed = (stats["completedOrders"] as? Int) ?: 0
            val waiting = (stats["waitingOrders"] as? Int) ?: 0
            val active = (stats["activeOrders"] as? Int) ?: 0

            Column {
                Text(
                    text = "Распределение по статусам",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))

                StatusBar("Завершено", completed, total, Color(0xFF4CAF50))
                Spacer(modifier = Modifier.height(4.dp))
                StatusBar("В ожидании", waiting, total, Color(0xFFFF9800))
                Spacer(modifier = Modifier.height(4.dp))
                StatusBar("Активных", active, total, Color(0xFF2196F3))
            }
        }
    }
}

/**
 * Полоса статуса
 */
@Composable
fun StatusBar(label: String, value: Int, total: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.caption,
            modifier = Modifier.width(100.dp)
        )

        LinearProgressIndicator(
            progress = if (total > 0) value.toFloat() / total.toFloat() else 0f,
            modifier = Modifier
                .weight(1f)
                .height(6.dp),
            color = color,
            backgroundColor = Color.LightGray
        )

        Text(
            text = value.toString(),
            style = MaterialTheme.typography.caption,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(start = 8.dp).width(30.dp)
        )
    }
}

/**
 * Топ популярных книг
 */
@Composable
fun PopularBooksStatistics(repository: BookShopRepository) {
    val orders = repository.getAllOrders()
    val books = repository.getAllBooks()

    // Подсчет заказов по книгам
    val bookOrderCounts = orders.groupBy { it.bookId }
        .mapValues { it.value.size }
        .entries
        .sortedByDescending { it.value }
        .take(5)

    if (bookOrderCounts.isEmpty()) {
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🏆 Топ-5 популярных книг",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                bookOrderCounts.forEachIndexed { index, entry ->
                    val book = books.find { it.id == entry.key }
                    if (book != null) {
                        PopularBookItem(
                            rank = index + 1,
                            book = book,
                            orderCount = entry.value,
                            maxCount = bookOrderCounts.first().value
                        )
                    }
                }
            }
        }
    }
}

/**
 * Элемент популярной книги
 */
@Composable
fun PopularBookItem(rank: Int, book: Book, orderCount: Int, maxCount: Int) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700) // Золото
        2 -> Color(0xFFC0C0C0) // Серебро
        3 -> Color(0xFFCD7F32) // Бронза
        else -> Color.Gray
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ранг
        Surface(
            color = rankColor,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.size(32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.toString(),
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Информация о книге
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = book.author,
                style = MaterialTheme.typography.caption,
                color = Color.Gray
            )
        }

        // Количество заказов
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "$orderCount",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary
            )
            Text(
                text = "заказов",
                style = MaterialTheme.typography.caption,
                color = Color.Gray
            )
        }
    }
}

/**
 * График заказов по месяцам
 */
@Composable
fun OrdersByMonthChart(orders: List<org.example.model.Order>) {
    if (orders.isEmpty()) {
        return
    }

    // Группировка заказов по месяцам
    val dateFormat = SimpleDateFormat("MMM yyyy", Locale("ru"))
    val ordersByMonth = orders
        .groupBy {
            val date = Date(it.creationDate)
            dateFormat.format(date)
        }
        .entries
        .sortedBy {
            SimpleDateFormat("MMM yyyy", Locale("ru")).parse(it.key)?.time ?: 0
        }
        .takeLast(6) // Последние 6 месяцев

    if (ordersByMonth.isEmpty()) {
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📈 Заказы по месяцам",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Простой столбчатый график
            val maxValue = ordersByMonth.maxOfOrNull { it.value.size } ?: 1

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ordersByMonth.forEach { (month, monthOrders) ->
                    MonthBar(
                        month = month,
                        count = monthOrders.size,
                        maxCount = maxValue
                    )
                }
            }
        }
    }
}

/**
 * Столбец месяца
 */
@Composable
fun MonthBar(month: String, count: Int, maxCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = month,
            style = MaterialTheme.typography.caption,
            modifier = Modifier.width(80.dp)
        )

        LinearProgressIndicator(
            progress = count.toFloat() / maxCount.toFloat(),
            modifier = Modifier
                .weight(1f)
                .height(24.dp),
            color = MaterialTheme.colors.primary,
            backgroundColor = Color.LightGray
        )

        Text(
            text = count.toString(),
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp).width(30.dp)
        )
    }
}

/**
 * Строка статистики
 */
@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
            color = Color.Gray,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Bold
        )
    }
}