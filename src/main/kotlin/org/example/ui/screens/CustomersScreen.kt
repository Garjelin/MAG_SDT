package org.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.data.BookShopRepository
import org.example.model.Customer

/**
 * Экран покупателей
 * Отображает список всех покупателей магазина
 */
@Composable
fun CustomersScreen(repository: BookShopRepository) {
    var customers by remember { mutableStateOf(repository.getAllCustomers()) }
    var searchQuery by remember { mutableStateOf("") }

    // Обновление списка при изменении данных
    LaunchedEffect(Unit) {
        repository.onDataChanged = {
            customers = repository.getAllCustomers()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок
        Text(
            text = "👥 Покупатели",
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Строка поиска
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                customers = if (it.isEmpty()) {
                    repository.getAllCustomers()
                } else {
                    repository.searchCustomers(it)
                }
            },
            label = { Text("Поиск покупателей") },
            placeholder = { Text("Введите имя или email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        // Статистика
        Text(
            text = "Всего покупателей: ${customers.size}",
            style = MaterialTheme.typography.body1,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Список покупателей
        if (customers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет зарегистрированных покупателей",
                    style = MaterialTheme.typography.h6,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(customers) { customer ->
                    CustomerCard(customer, repository)
                }
            }
        }
    }

    // Кнопка добавления покупателя
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { /* TODO: Открыть диалог добавления покупателя */ },
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Добавить покупателя")
        }
    }
}

/**
 * Карточка покупателя
 */
@Composable
fun CustomerCard(customer: Customer, repository: BookShopRepository) {
    val orders = repository.getOrdersByCustomer(customer.id)
    val activeOrders = orders.count { it.isActive() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Имя
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Email
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = customer.email,
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
            }

            // Телефон (если есть)
            if (customer.phone.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = customer.phone,
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )
                }
            }

            // Статистика заказов
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Заказов: ${orders.size}",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )

                if (activeOrders > 0) {
                    Surface(
                        color = MaterialTheme.colors.secondary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Активных: $activeOrders",
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