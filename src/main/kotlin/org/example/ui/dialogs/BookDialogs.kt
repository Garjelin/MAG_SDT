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
import org.example.model.Book

/**
 * Диалог добавления новой книги
 */
@Composable
fun AddBookDialog(
    repository: BookShopRepository,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var author by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    var authorError by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }
    var publisherError by remember { mutableStateOf(false) }
    var yearError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Добавить книгу",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Автор
                OutlinedTextField(
                    value = author,
                    onValueChange = {
                        author = it
                        authorError = false
                    },
                    label = { Text("Автор *") },
                    isError = authorError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Название
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = false
                    },
                    label = { Text("Название *") },
                    isError = titleError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Издательство
                OutlinedTextField(
                    value = publisher,
                    onValueChange = {
                        publisher = it
                        publisherError = false
                    },
                    label = { Text("Издательство *") },
                    isError = publisherError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Год издания
                OutlinedTextField(
                    value = year,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() } && it.length <= 4) {
                            year = it
                            yearError = false
                        }
                    },
                    label = { Text("Год издания *") },
                    placeholder = { Text("2024") },
                    isError = yearError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Цена
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            price = it
                            priceError = false
                        }
                    },
                    label = { Text("Цена (руб.) *") },
                    placeholder = { Text("500.00") },
                    isError = priceError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Количество
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() } && it.length <= 4) {
                            quantity = it
                            quantityError = false
                        }
                    },
                    label = { Text("Количество *") },
                    placeholder = { Text("10") },
                    isError = quantityError,
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

                    if (author.isBlank()) {
                        authorError = true
                        hasError = true
                    }
                    if (title.isBlank()) {
                        titleError = true
                        hasError = true
                    }
                    if (publisher.isBlank()) {
                        publisherError = true
                        hasError = true
                    }
                    if (year.isBlank() || year.toIntOrNull() == null || year.toInt() < 1000 || year.toInt() > 2100) {
                        yearError = true
                        hasError = true
                    }
                    if (price.isBlank() || price.toDoubleOrNull() == null || price.toDouble() <= 0) {
                        priceError = true
                        hasError = true
                    }
                    if (quantity.isBlank() || quantity.toIntOrNull() == null || quantity.toInt() < 0) {
                        quantityError = true
                        hasError = true
                    }

                    if (!hasError) {
                        // Добавляем книгу
                        repository.addBook(
                            author = author.trim(),
                            title = title.trim(),
                            publisher = publisher.trim(),
                            publicationYear = year.toInt(),
                            price = price.toDouble(),
                            quantity = quantity.toInt()
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
 * Диалог редактирования книги
 */
@Composable
fun EditBookDialog(
    book: Book,
    repository: BookShopRepository,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var author by remember { mutableStateOf(book.author) }
    var title by remember { mutableStateOf(book.title) }
    var publisher by remember { mutableStateOf(book.publisher) }
    var year by remember { mutableStateOf(book.publicationYear.toString()) }
    var price by remember { mutableStateOf(book.price.toString()) }
    var quantity by remember { mutableStateOf(book.quantity.toString()) }

    var authorError by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }
    var publisherError by remember { mutableStateOf(false) }
    var yearError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Редактировать книгу",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ID книги
                Text(
                    text = "ID: ${book.id}",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )

                // Автор
                OutlinedTextField(
                    value = author,
                    onValueChange = {
                        author = it
                        authorError = false
                    },
                    label = { Text("Автор *") },
                    isError = authorError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Название
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = false
                    },
                    label = { Text("Название *") },
                    isError = titleError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Издательство
                OutlinedTextField(
                    value = publisher,
                    onValueChange = {
                        publisher = it
                        publisherError = false
                    },
                    label = { Text("Издательство *") },
                    isError = publisherError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Год издания
                OutlinedTextField(
                    value = year,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() } && it.length <= 4) {
                            year = it
                            yearError = false
                        }
                    },
                    label = { Text("Год издания *") },
                    isError = yearError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Цена
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            price = it
                            priceError = false
                        }
                    },
                    label = { Text("Цена (руб.) *") },
                    isError = priceError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Количество
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() } && it.length <= 4) {
                            quantity = it
                            quantityError = false
                        }
                    },
                    label = { Text("Количество *") },
                    isError = quantityError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Валидация
                    var hasError = false

                    if (author.isBlank()) {
                        authorError = true
                        hasError = true
                    }
                    if (title.isBlank()) {
                        titleError = true
                        hasError = true
                    }
                    if (publisher.isBlank()) {
                        publisherError = true
                        hasError = true
                    }
                    if (year.isBlank() || year.toIntOrNull() == null || year.toInt() < 1000 || year.toInt() > 2100) {
                        yearError = true
                        hasError = true
                    }
                    if (price.isBlank() || price.toDoubleOrNull() == null || price.toDouble() <= 0) {
                        priceError = true
                        hasError = true
                    }
                    if (quantity.isBlank() || quantity.toIntOrNull() == null || quantity.toInt() < 0) {
                        quantityError = true
                        hasError = true
                    }

                    if (!hasError) {
                        // Обновляем книгу
                        book.author = author.trim()
                        book.title = title.trim()
                        book.publisher = publisher.trim()
                        book.publicationYear = year.toInt()
                        book.price = price.toDouble()

                        // Обновляем количество через репозиторий (для проверки базы ожидания)
                        val quantityDiff = quantity.toInt() - book.quantity
                        if (quantityDiff > 0) {
                            repository.updateBookQuantity(book.id, quantityDiff)
                        } else if (quantityDiff < 0) {
                            book.quantity = quantity.toInt()
                        }

                        repository.forceSave()
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
 * Диалог подтверждения удаления книги
 */
@Composable
fun DeleteBookDialog(
    book: Book,
    repository: BookShopRepository,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
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
                    text = "Удалить книгу?",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Вы действительно хотите удалить эту книгу из каталога?",
                    style = MaterialTheme.typography.body1
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    backgroundColor = Color(0xFFFFF3E0),
                    elevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Автор: ${book.author}",
                            style = MaterialTheme.typography.body2,
                            color = Color.Gray
                        )
                        Text(
                            text = "ID: ${book.id}",
                            style = MaterialTheme.typography.caption,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                    repository.removeBook(book.id)
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