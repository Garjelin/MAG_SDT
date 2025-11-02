package org.example

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.example.data.BookShopRepository
import org.example.ui.RoleManager
import org.example.ui.UserRole
import org.example.ui.screens.*

/**
 * Перечисление экранов приложения
 */
enum class Screen(
    val title: String,
    val icon: ImageVector,
    val adminOnly: Boolean = false
) {
    CATALOG("Каталог", Icons.Default.Menu, adminOnly = false),
    CUSTOMERS("Покупатели", Icons.Default.Person, adminOnly = true),
    ORDERS("Заказы", Icons.Default.ShoppingCart, adminOnly = false),
    WAITING_LIST("База ожидания", Icons.Default.DateRange, adminOnly = true),
    STATISTICS("Статистика", Icons.Default.Build, adminOnly = true)
}

fun main() = application {
    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)

    Window(
        onCloseRequest = ::exitApplication,
        title = "Книжный магазин",
        state = windowState
    ) {
        MaterialTheme(
            colors = lightColors(
                primary = Color(0xFF1976D2),
                primaryVariant = Color(0xFF1565C0),
                secondary = Color(0xFFFFA726)
            )
        ) {
            BookShopApp()
        }
    }
}

@Composable
fun BookShopApp() {
    // Инициализация репозитория
    val repository = remember { BookShopRepository() }

    // Текущий выбранный экран
    var currentScreen by remember { mutableStateOf(Screen.CATALOG) }

    // Текущая роль пользователя
    val currentRole by RoleManager.currentRole

    // Показать диалог смены роли
    var showRoleDialog by remember { mutableStateOf(false) }

    // Главный layout с боковой панелью навигации
    Row(modifier = Modifier.fillMaxSize()) {
        // Боковая панель навигации
        NavigationRail(
            currentScreen = currentScreen,
            currentRole = currentRole,
            onScreenSelected = { screen ->
                currentScreen = screen
            },
            onRoleClick = { showRoleDialog = true }
        )

        // Основной контент
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFFAFAFA)
        ) {
            when (currentScreen) {
                Screen.CATALOG -> CatalogScreen(repository)
                Screen.CUSTOMERS -> {
                    if (currentRole.canAccessCustomerManagement()) {
                        CustomersScreen(repository)
                    } else {
                        AccessDeniedScreen("Покупатели", "управление покупателями")
                    }
                }
                Screen.ORDERS -> OrdersScreen(repository)
                Screen.WAITING_LIST -> {
                    if (currentRole.canAccessWaitingList()) {
                        WaitingListScreen(repository)
                    } else {
                        AccessDeniedScreen("База ожидания", "просмотр базы ожидания")
                    }
                }
                Screen.STATISTICS -> {
                    if (currentRole.canAccessStatistics()) {
                        StatisticsScreen(repository)
                    } else {
                        AccessDeniedScreen("Статистика", "просмотр статистики")
                    }
                }
            }
        }
    }

    // Диалог смены роли
    if (showRoleDialog) {
        RoleSelectionDialog(
            currentRole = currentRole,
            onDismiss = { showRoleDialog = false },
            onRoleSelected = { role ->
                RoleManager.setRole(role)
                // Переключаемся на каталог если текущий экран недоступен
                if (currentScreen.adminOnly && role != UserRole.ADMIN) {
                    currentScreen = Screen.CATALOG
                }
                showRoleDialog = false
            }
        )
    }
}

/**
 * Боковая панель навигации
 */
@Composable
fun NavigationRail(
    currentScreen: Screen,
    currentRole: UserRole,
    onScreenSelected: (Screen) -> Unit,
    onRoleClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(240.dp),
        color = MaterialTheme.colors.primary,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            // Заголовок приложения
            AppHeader()

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопки навигации
            Screen.values().forEach { screen ->
                // Показываем только доступные экраны
                val isAccessible = !screen.adminOnly || currentRole == UserRole.ADMIN

                if (isAccessible) {
                    NavigationButton(
                        screen = screen,
                        isSelected = currentScreen == screen,
                        onClick = { onScreenSelected(screen) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Переключатель роли
            RoleSwitcher(currentRole, onRoleClick)

            Spacer(modifier = Modifier.height(16.dp))

            // Информация внизу
            AppFooter()
        }
    }
}

/**
 * Заголовок приложения в боковой панели
 */
@Composable
fun AppHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Иконка магазина
        Surface(
            color = Color.White.copy(alpha = 0.2f),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                Icons.Default.Home,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Книжный",
            style = MaterialTheme.typography.h5,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "МАГАЗИН",
            style = MaterialTheme.typography.subtitle2,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Light
        )
    }
}

/**
 * Кнопка навигации
 */
@Composable
fun NavigationButton(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent,
        shape = MaterialTheme.shapes.medium
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    screen.icon,
                    contentDescription = screen.title,
                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = screen.title,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.body1,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

/**
 * Переключатель роли
 */
@Composable
fun RoleSwitcher(currentRole: UserRole, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Divider(
            color = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            text = "Текущая роль:",
            style = MaterialTheme.typography.caption,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Surface(
            color = Color.White.copy(alpha = 0.15f),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentRole.icon,
                            style = MaterialTheme.typography.h6
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentRole.displayName,
                            color = Color.White,
                            style = MaterialTheme.typography.body2,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Сменить роль",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Футер приложения в боковой панели
 */
@Composable
fun AppFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Divider(
            color = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Курсовой проект",
            style = MaterialTheme.typography.caption,
            color = Color.White.copy(alpha = 0.6f)
        )
        Text(
            text = "Версия 1.0",
            style = MaterialTheme.typography.caption,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

/**
 * Диалог выбора роли
 */
@Composable
fun RoleSelectionDialog(
    currentRole: UserRole,
    onDismiss: () -> Unit,
    onRoleSelected: (UserRole) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Выбор роли")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Выберите роль для работы с системой:",
                    style = MaterialTheme.typography.body2
                )

                Spacer(modifier = Modifier.height(8.dp))

                UserRole.values().forEach { role ->
                    RoleCard(
                        role = role,
                        isSelected = role == currentRole,
                        onClick = { onRoleSelected(role) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

/**
 * Карточка роли
 */
@Composable
fun RoleCard(role: UserRole, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        backgroundColor = if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else Color.White,
        elevation = if (isSelected) 4.dp else 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = role.icon,
                    style = MaterialTheme.typography.h5
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = role.displayName,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colors.primary else Color.Black
                    )
                    Text(
                        text = when (role) {
                            UserRole.ADMIN -> "Полный доступ ко всем функциям"
                            UserRole.CUSTOMER -> "Просмотр каталога и своих заказов"
                        },
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                }
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Выбрано",
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
        }
    }
}

/**
 * Экран "Доступ запрещен"
 */
@Composable
fun AccessDeniedScreen(screenName: String, action: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Доступ запрещен",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                backgroundColor = Color(0xFFFFF3E0),
                elevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔒 Раздел \"$screenName\" доступен только администраторам",
                        style = MaterialTheme.typography.body1,
                        color = Color(0xFFF57C00)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Для доступа к функции \"$action\" необходимо переключиться на роль Администратора",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Нажмите на текущую роль в левом меню для переключения",
                style = MaterialTheme.typography.caption,
                color = Color.Gray
            )
        }
    }
}