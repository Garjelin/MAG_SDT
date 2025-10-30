package org.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.data.BookShopRepository
import org.example.model.Order
import org.example.model.OrderStatus

/**
 * Экран заказов
 * Отображает список всех заказов
 */
@Composable
fun OrdersScreen(repository: BookShopRepository) {
    var orders by remember { mutableStateOf(repository.getAllOrders()) }
    var filterStatus by remember { mutableStateOf<OrderStatus?>(null) }

    // Обновление списка при изменении данных
    LaunchedEffect(Unit) {
        repository.onDataChanged = {
            orders = repository.getAllOrders()
        }
    }

    // Фильтрация заказов
    val filteredOrders = if (filterStatus != null) {
        orders.filter { it.status == filterStatus }
    } else {
        orders
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок
        Text(
            text = "📋 Заказы",
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Фильтры
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterStatus == null,
                onClick = { filterStatus = null },
                label = "Все (${orders.size})"
            )
            FilterChip(
                selected = filterStatus == OrderStatus.COMPLETED,
                onClick = { filterStatus = OrderStatus.COMPLETED },
                label = "Завершены (${orders.count { it.status == OrderStatus.COMPLETED }})"
            )
            FilterChip(
                selected = filterStatus == OrderStatus.WAITING,
                onClick = { filterStatus = OrderStatus.WAITING },
                label = "В ожидании (${orders.count { it.status == OrderStatus.WAITING }})"
            )
        }

        // Список заказов
        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (orders.isEmpty()) "Нет заказов" else "Нет заказов с выбранным статусом",
                    style = MaterialTheme.typography.h6,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredOrders.sortedByDescending { it.creationDate }) { order ->
                    OrderCard(order, repository)
                }
            }
        }
    }

    // Кнопка создания заказа
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { /* TODO: Открыть диалог создания заказа */ },
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Создать заказ")
        }
    }
}

/**
 * Компонент фильтра-чипа
 */
@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    Surface(
        color = if (selected) MaterialTheme.colors.primary else Color.LightGray,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.height(32.dp)
    ) {
        TextButton(onClick = onClick) {
            Text(
                text = label,
                color = if (selected) Color.White else Color.DarkGray,
                style = MaterialTheme.typography.caption
            )
        }
    }
}

/**
 * Карточка заказа
 */
@Composable
fun OrderCard(order: Order, repository: BookShopRepository) {
    val customer = repository.getCustomerById(order.customerId)
    val book = repository.getBookById(order.bookId)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Заголовок с ID и статусом
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Заказ #${order.id}",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )

                // Статус
                Surface(
                    color = getStatusColor(order.status),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = order.getStatusString(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Информация о книге
            Text(
                text = "Книга: ${book?.title ?: "Неизвестно"}",
                style = MaterialTheme.typography.body1
            )

            // Информация о покупателе
            Text(
                text = "Покупатель: ${customer?.name ?: "Неизвестно"}",
                style = MaterialTheme.typography.body2,
                color = Color.Gray
            )

            // Количество и дата
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Количество: ${order.quantity}",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
                Text(
                    text = order.getFormattedCreationDate(),
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Получить цвет для статуса заказа
 */
fun getStatusColor(status: OrderStatus): Color {
    return when (status) {
        OrderStatus.CREATED -> Color(0xFF2196F3)      // Синий
        OrderStatus.WAITING -> Color(0xFFFF9800)      // Оранжевый
        OrderStatus.AVAILABLE -> Color(0xFF9C27B0)    // Фиолетовый
        OrderStatus.COMPLETED -> Color(0xFF4CAF50)    // Зеленый
        OrderStatus.CANCELLED -> Color(0xFFF44336)    // Красный
    }
}