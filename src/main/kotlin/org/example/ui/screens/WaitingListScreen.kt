package org.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.data.BookShopRepository
import org.example.model.WaitingListRecord

/**
 * Экран базы ожидания
 * Отображает список покупателей, ожидающих поступления книг
 */
@Composable
fun WaitingListScreen(repository: BookShopRepository) {
    var records by remember { mutableStateOf(repository.getWaitingList().getAllRecords()) }
    var showOnlyActive by remember { mutableStateOf(true) }

    // Обновление списка при изменении данных
    LaunchedEffect(Unit) {
        repository.onDataChanged = {
            records = repository.getWaitingList().getAllRecords()
        }
    }

    // Фильтрация записей
    val displayedRecords = if (showOnlyActive) {
        records.filter { !it.notified }
    } else {
        records
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок
        Text(
            text = "⏳ База ожидания",
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Информационная панель
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            backgroundColor = Color(0xFFF5F5F5)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "ℹ️ База ожидания",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Покупатели, ожидающие поступления книг. При добавлении книги в каталог они будут автоматически уведомлены.",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
            }
        }

        // Фильтры
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = showOnlyActive,
                onClick = { showOnlyActive = true },
                label = "Активные (${records.count { !it.notified }})"
            )
            FilterChip(
                selected = !showOnlyActive,
                onClick = { showOnlyActive = false },
                label = "Все (${records.size})"
            )
        }

        // Список записей
        if (displayedRecords.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.h3,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (records.isEmpty())
                            "База ожидания пуста"
                        else
                            "Нет активных записей",
                        style = MaterialTheme.typography.h6,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayedRecords.sortedByDescending { it.registrationDate }) { record ->
                    WaitingListRecordCard(record, repository)
                }
            }
        }
    }
}

/**
 * Карточка записи в базе ожидания
 */
@Composable
fun WaitingListRecordCard(record: WaitingListRecord, repository: BookShopRepository) {
    val customer = repository.getCustomerById(record.customerId)
    val book = repository.getBookById(record.bookId)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        backgroundColor = if (record.notified) Color(0xFFE8F5E9) else Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Запись #${record.recordId}",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )

                // Статус уведомления
                if (record.notified) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Уведомлен",
                            style = MaterialTheme.typography.caption,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Информация о книге
            Text(
                text = "Книга: ${book?.title ?: "Неизвестно"}",
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Medium
            )

            if (book != null) {
                Text(
                    text = "Автор: ${book.author}",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Информация о покупателе
            Text(
                text = "Покупатель: ${customer?.name ?: "Неизвестно"}",
                style = MaterialTheme.typography.body2
            )

            if (customer != null) {
                Text(
                    text = customer.email,
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
            }

            // Дата регистрации
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Зарегистрирован: ${record.getFormattedRegistrationDate()}",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )

                // Доступность книги
                if (book != null) {
                    Surface(
                        color = if (book.isAvailable()) Color(0xFF4CAF50) else Color(0xFFF44336),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (book.isAvailable()) "В наличии" else "Нет в наличии",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        }
    }
}