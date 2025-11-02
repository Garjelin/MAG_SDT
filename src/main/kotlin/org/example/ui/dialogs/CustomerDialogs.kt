package org.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.data.BookShopRepository
import org.example.model.Customer

/**
 * Диалог добавления нового покупателя
 */
@Composable
fun AddCustomerDialog(
    repository: BookShopRepository,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var emailErrorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Добавить покупателя",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Имя
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Имя *") },
                    placeholder = { Text("Иванов Иван Иванович") },
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (nameError) {
                    Text(
                        text = "Имя не может быть пустым",
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.caption
                    )
                }

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = false
                        emailErrorMessage = ""
                    },
                    label = { Text("Email *") },
                    placeholder = { Text("example@mail.com") },
                    isError = emailError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (emailError) {
                    Text(
                        text = emailErrorMessage,
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.caption
                    )
                }

                // Телефон (опционально)
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        // Разрешаем только цифры, пробелы, +, -, (, )
                        if (it.all { char -> char.isDigit() || char in " +-()" }) {
                            phone = it
                        }
                    },
                    label = { Text("Телефон") },
                    placeholder = { Text("+7 (900) 123-45-67") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "* Обязательные поля",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Валидация
                    var hasError = false

                    if (name.isBlank()) {
                        nameError = true
                        hasError = true
                    }

                    if (email.isBlank()) {
                        emailError = true
                        emailErrorMessage = "Email не может быть пустым"
                        hasError = true
                    } else if (!isValidEmail(email)) {
                        emailError = true
                        emailErrorMessage = "Некорректный формат email"
                        hasError = true
                    }

                    if (!hasError) {
                        // Добавляем покупателя
                        repository.addCustomer(
                            name = name.trim(),
                            email = email.trim(),
                            phone = phone.trim()
                        )
                        onSuccess()
                        onDismiss()
                    }
                }
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

/**
 * Диалог редактирования покупателя
 */
@Composable
fun EditCustomerDialog(
    customer: Customer,
    repository: BookShopRepository,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var name by remember { mutableStateOf(customer.name) }
    var email by remember { mutableStateOf(customer.email) }
    var phone by remember { mutableStateOf(customer.phone) }

    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var emailErrorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Редактировать покупателя",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ID покупателя
                Text(
                    text = "ID: ${customer.id}",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )

                // Имя
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Имя *") },
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (nameError) {
                    Text(
                        text = "Имя не может быть пустым",
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.caption
                    )
                }

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = false
                        emailErrorMessage = ""
                    },
                    label = { Text("Email *") },
                    isError = emailError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (emailError) {
                    Text(
                        text = emailErrorMessage,
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.caption
                    )
                }

                // Телефон
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() || char in " +-()" }) {
                            phone = it
                        }
                    },
                    label = { Text("Телефон") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Дата регистрации
                Text(
                    text = "Зарегистрирован: ${formatTimestamp(customer.registrationDate)}",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Валидация
                    var hasError = false

                    if (name.isBlank()) {
                        nameError = true
                        hasError = true
                    }

                    if (email.isBlank()) {
                        emailError = true
                        emailErrorMessage = "Email не может быть пустым"
                        hasError = true
                    } else if (!isValidEmail(email)) {
                        emailError = true
                        emailErrorMessage = "Некорректный формат email"
                        hasError = true
                    }

                    if (!hasError) {
                        // Обновляем покупателя
                        repository.updateCustomer(
                            customerId = customer.id,
                            name = name.trim(),
                            email = email.trim(),
                            phone = phone.trim()
                        )
                        onSuccess()
                        onDismiss()
                    }
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

/**
 * Диалог подтверждения удаления покупателя
 */
@Composable
fun DeleteCustomerDialog(
    customer: Customer,
    repository: BookShopRepository,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val orders = repository.getOrdersByCustomer(customer.id)
    val activeOrders = orders.count { it.isActive() }
    val waitingRecords = repository.getWaitingList().getRecordsForCustomer(customer.id)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Удалить покупателя?",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Вы действительно хотите удалить этого покупателя из базы?",
                    style = MaterialTheme.typography.body1
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Карточка покупателя
                Card(
                    backgroundColor = Color(0xFFFFF3E0),
                    elevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = customer.email,
                            style = MaterialTheme.typography.body2,
                            color = Color.Gray
                        )
                        if (customer.phone.isNotEmpty()) {
                            Text(
                                text = customer.phone,
                                style = MaterialTheme.typography.body2,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = "ID: ${customer.id}",
                            style = MaterialTheme.typography.caption,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Предупреждения
                if (activeOrders > 0 || waitingRecords.isNotEmpty()) {
                    Card(
                        backgroundColor = Color(0xFFFFEBEE),
                        elevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "⚠️ Важно:",
                                style = MaterialTheme.typography.subtitle2,
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold
                            )

                            if (activeOrders > 0) {
                                Text(
                                    text = "• Активных заказов: $activeOrders",
                                    style = MaterialTheme.typography.body2,
                                    color = Color(0xFFD32F2F)
                                )
                            }

                            if (orders.isNotEmpty()) {
                                Text(
                                    text = "• Всего заказов: ${orders.size}",
                                    style = MaterialTheme.typography.body2,
                                    color = Color.Gray
                                )
                            }

                            if (waitingRecords.isNotEmpty()) {
                                Text(
                                    text = "• В базе ожидания: ${waitingRecords.size}",
                                    style = MaterialTheme.typography.body2,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Заказы и записи в базе ожидания останутся в системе.",
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = "⚠️ Это действие нельзя отменить!",
                    style = MaterialTheme.typography.body2,
                    color = Color(0xFFF44336),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    repository.removeCustomer(customer.id)
                    onSuccess()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFF44336),
                    contentColor = Color.White
                )
            ) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

/**
 * Проверка корректности email
 */
private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return email.matches(emailRegex)
}

/**
 * Форматирование timestamp в дату
 */
private fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm")
    return format.format(date)
}