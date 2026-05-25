package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.theme.StatusLowStock
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusDelivered
import com.example.ui.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.*

// --- LOGIN SCREEN ---
@Composable
fun LoginScreen(
    viewModel: ShopViewModel,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val rememberState by viewModel.loginRemembered.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Shop Logo Avatar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "Shop logo",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "JaneMaiks",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "RETAIL ASSISTANT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.tertiary,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Input fields
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        showError = false
                    },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        showError = false
                    },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Remember me toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberState,
                        onCheckedChange = { viewModel.loginRemembered.value = it }
                    )
                    Text(
                        text = "Remember persistent session",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (showError) {
                    Text(
                        text = "Invalid credentials. Try janemaiks / admin123",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Button(
                    onClick = {
                        if (viewModel.login(username, password)) {
                            onLoginSuccess()
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Access System", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- MAIN WRAPPER SCAFFOLD DESIGN WITH FLOATING BAR / SIDERAIL ACCENTS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationWrapper(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    viewModel: ShopViewModel,
    content: @Composable () -> Unit
) {
    val shopNameState by viewModel.shopName.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            if (currentRoute != "dashboard") {
                val screenTitle = when (currentRoute) {
                    "sales" -> "POS Sales Basket"
                    "products" -> "Retail Product Listings"
                    "inventory" -> "Stock Replenishment"
                    "requested_items" -> "Customer Miss Tracker"
                    "chatbot" -> "AI Retail Copilot"
                    "analytics" -> "Profits Analytics"
                    "settings" -> "System Configuration"
                    else -> shopNameState
                }
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = screenTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Kenyan General Retail Helper",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    },
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(start = 12.dp, end = 8.dp)
                        )
                    },
                    actions = {
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Logout",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val screens = listOf(
                    Triple("dashboard", Icons.Outlined.GridView, "Dashboard"),
                    Triple("sales", Icons.Outlined.ShoppingCart, "Sales/Cart"),
                    Triple("products", Icons.Outlined.Inventory2, "Products"),
                    Triple("chatbot", Icons.Outlined.SmartToy, "AI Helper"),
                    Triple("settings", Icons.Outlined.Settings, "Settings")
                )

                screens.forEach { (route, icon, label) ->
                    NavigationBarItem(
                        selected = currentRoute == route,
                        onClick = { onNavigate(route) },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            content()
        }
    }
}

// --- 1. DASHBOARD SCREEN (HIGH DENSITY THEME) ---
@Composable
fun DashboardScreen(
    viewModel: ShopViewModel,
    onNavigate: (String) -> Unit
) {
    val currentProducts by viewModel.products.collectAsStateWithLifecycle()
    val currentSales by viewModel.sales.collectAsStateWithLifecycle()
    val requestedItems by viewModel.requestedItems.collectAsStateWithLifecycle()

    // Dynamic stats computation
    val todayRevenue = remember(currentSales) {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        currentSales.filter { it.timestamp >= todayStart }.sumOf { it.totalAmount }
    }

    val lowStockCount = remember(currentProducts) {
        currentProducts.filter { it.stockQty <= it.lowStockThreshold }.size
    }

    val requestedCount = remember(requestedItems) {
        requestedItems.sumOf { it.timesRequested }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA)),
        verticalArrangement = Arrangement.Top
    ) {
        // 1. COBALT BLUE HEADER BOX
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0047AB), shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 28.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Title and Avatar Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "JaneMaiks Retail",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "ASSISTANT DASHBOARD",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 1.2.sp
                            )
                        }

                        // Orange letters Avatar JM
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF6F00)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "JM",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Glassmorphic Today's Revenue card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "TODAY'S REVENUE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ksh ${String.format("%,.2f", todayRevenue)}",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF388E3C))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (todayRevenue > 0) "+12.4% ↑" else "Active ✨",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. THREE-COLUMN STATS ROW
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Low Stock
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, shape = RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(16.dp))
                        .padding(top = 12.dp, bottom = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD32F2F).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$lowStockCount",
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "LOW STOCK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Pending
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, shape = RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(16.dp))
                        .padding(top = 12.dp, bottom = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFBC02D).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Wait",
                                color = Color(0xFFFBC02D),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "PENDING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Requests
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, shape = RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(16.dp))
                        .padding(top = 12.dp, bottom = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0047AB).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$requestedCount",
                                color = Color(0xFF0047AB),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "REQUESTS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // 3. QUICK ACTIONS HEADER WITH ORANGE PILL
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF6F00))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Quick Actions",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }
        }

        // 4. QUICK ACTIONS BUTTONS
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Record Sale button
                Card(
                    onClick = { onNavigate("sales") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E8B57)),
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Record Sale",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // AI Helper button
                Card(
                    onClick = { onNavigate("chatbot") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0047AB)),
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ai",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Shop Helper",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 5. LOW STOCK ALERTS HEADER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Low Stock Alerts",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Text(
                    text = "VIEW ALL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0047AB),
                    modifier = Modifier
                        .clickable { onNavigate("inventory") }
                        .padding(4.dp)
                )
            }
        }

        // 6. LOW STOCK ALERTS BODY
        item {
            val alerts = remember(currentProducts) {
                currentProducts.filter { it.stockQty <= it.lowStockThreshold }
            }

            if (alerts.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Text(
                        text = "Nice job! All product items have healthy stock levels.",
                        fontSize = 13.sp,
                        color = Color(0xFF388E3C),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column {
                        alerts.forEachIndexed { index, product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigate("inventory") }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Product graphic placeholder (colorful rounded thumb)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color(0xFFF1F5F9), shape = RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initials = remember(product.name) {
                                        product.name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
                                    }
                                    Text(
                                        text = initials,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                // Product Info
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Only ${product.stockQty} units remaining",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFD32F2F)
                                    )
                                }

                                // Pricing and tags
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "RETAIL",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = "Ksh ${product.retailPrice.toInt()}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }

                            if (index < alerts.lastIndex) {
                                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    colorAccent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colorAccent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = colorAccent, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ShortcutCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1)
            }
        }
    }
}

// --- 2. SALES SCREEN (POS CART & CHECKOUT) ---
@Composable
fun SalesScreen(
    viewModel: ShopViewModel
) {
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val cartState by viewModel.cart.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showReceiptDialog by remember { mutableStateOf(false) }
    var lastReceiptSummary by remember { mutableStateOf("") }
    var lastReceiptTotal by remember { mutableStateOf(0.0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search & Add Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text(
                text = "POINT OF SALE ASSISTANT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.searchQuery.value = it },
                label = { Text("Search Products (Instant typo-tolerant)...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Live suggestions dropdown/list matching search
            if (query.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Matching suggestions (${searchResults.size}):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        if (searchResults.isEmpty()) {
                            Text(
                                text = "No products found. Add this to requested items tracker?",
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.addRequestedItem(query, "Customer searched for in checkout POS")
                                        viewModel.searchQuery.value = ""
                                    }
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            searchResults.take(3).forEach { product ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.addToCart(product)
                                            viewModel.searchQuery.value = ""
                                        }
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = product.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "Category: ${product.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "${product.retailPrice} Ksh", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Divider()

        // Cart items List
        Box(modifier = Modifier.weight(1f)) {
            if (cartState.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cart is Empty",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Search or choose from catalog suggestions to add items",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartState.toList()) { (product, count) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${product.retailPrice} Ksh each",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.decreaseCartQty(product) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Decrease",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Text(
                                        text = count.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.widthIn(min = 20.dp),
                                        textAlign = TextAlign.Center
                                    )

                                    IconButton(
                                        onClick = { viewModel.addToCart(product, 1) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Increase",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "${count * product.retailPrice} Ksh",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )

                                    IconButton(onClick = { viewModel.removeFromCart(product) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusLowStock)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Checkout Bottom Drawer
        if (cartState.isNotEmpty()) {
            val totalAmount = cartState.toList().sumOf { it.first.retailPrice * it.second }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CART TOTAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${String.format("%,.1f", totalAmount)} Ksh",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Button(
                            onClick = {
                                val summary = cartState.toList().joinToString(", ") { "${it.second} x ${it.first.name}" }
                                lastReceiptSummary = summary
                                lastReceiptTotal = totalAmount
                                viewModel.checkoutCart()
                                showReceiptDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("checkout_button")
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Checkout Sale", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Receipt Dialog Popup
    if (showReceiptDialog) {
        AlertDialog(
            onDismissRequest = { showReceiptDialog = false },
            confirmButton = {
                Button(onClick = { showReceiptDialog = false }) {
                    Text("Done")
                }
            },
            icon = { Icon(Icons.Default.TaskAlt, contentDescription = null, tint = StatusDelivered, modifier = Modifier.size(48.dp)) },
            title = { Text("Sale Successful!", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "JaneMaiks Receipt",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Items checked out:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = lastReceiptSummary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "TOTAL RECIEVED",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "$lastReceiptTotal Ksh",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = StatusDelivered
                    )
                }
            }
        )
    }
}

// --- 3. PRODUCTS SCREEN (CATALOG OVERVIEW & EDIT/ADD) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ShopViewModel
) {
    val currentProducts by viewModel.products.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProductForEdit by remember { mutableStateOf<Product?>(null) }
    var catalogQuery by remember { mutableStateOf("") }

    val filteredList = remember(currentProducts, catalogQuery) {
        if (catalogQuery.trim().isEmpty()) {
            currentProducts
        } else {
            currentProducts.filter {
                it.name.contains(catalogQuery, ignoreCase = true) ||
                it.category.contains(catalogQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedProductForEdit = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PRODUCT CATALOGUE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = catalogQuery,
                        onValueChange = { catalogQuery = it },
                        label = { Text("Search products by name...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Products list
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No products found. Tap '+' to create a new product entry.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { product ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.clickable {
                                selectedProductForEdit = product
                                showAddDialog = true
                            }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = product.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Category: ${product.category}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = "Supplier: ${product.supplier.ifEmpty { "N/A" }}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${product.retailPrice} Ksh",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        val statusColor = if (product.stockQty <= product.lowStockThreshold) StatusLowStock else StatusDelivered
                                        Text(
                                            text = "${product.stockQty} items in stock",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor
                                        )
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Unit Cost: ${String.format("%.1f", product.calculatedUnitCost)} Ksh",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "Marg: ${String.format("%.1f", product.profitMargin)} Ksh",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Text(
                                        text = "Pack Qty: ${product.wholesalePackQty}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Product Dialog with Smart Price Calculator
    if (showAddDialog) {
        ProductFormDialog(
            product = selectedProductForEdit,
            onDismiss = { showAddDialog = false },
            onSave = { product ->
                viewModel.addOrUpdateProduct(product)
                showAddDialog = false
            },
            onDelete = { product ->
                viewModel.deleteProduct(product)
                showAddDialog = false
            },
            defaultMarginPercent = viewModel.defaultProfitMarginPercent.value
        )
    }
}

@Composable
fun ProductFormDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit,
    onDelete: (Product) -> Unit,
    defaultMarginPercent: Int
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Groceries") }
    var supplier by remember { mutableStateOf(product?.supplier ?: "") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }

    // Cost parameters
    var wholesaleCostStr by remember { mutableStateOf(product?.wholesaleCost?.toString() ?: "0") }
    var wholesalePackQtyStr by remember { mutableStateOf(product?.wholesalePackQty?.toString() ?: "1") }
    var wholesaleResalePriceStr by remember { mutableStateOf(product?.wholesaleResalePrice?.toString() ?: "0") }

    // Retail price
    var retailPriceStr by remember { mutableStateOf(product?.retailPrice?.toString() ?: "0") }

    // Inventory levels
    var stockQtyStr by remember { mutableStateOf(product?.stockQty?.toString() ?: "0") }
    var lowStockThresholdStr by remember { mutableStateOf(product?.lowStockThreshold?.toString() ?: "5") }
    var notes by remember { mutableStateOf(product?.notes ?: "") }

    // Calculations Flow
    val wholesaleCost = wholesaleCostStr.toDoubleOrNull() ?: 0.0
    val packQty = wholesalePackQtyStr.toIntOrNull() ?: 1
    val calculatedUnitCost = if (packQty > 0) wholesaleCost / packQty else wholesaleCost
    val currentRetailPrice = retailPriceStr.toDoubleOrNull() ?: 0.0
    val margin = currentRetailPrice - calculatedUnitCost

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add New Product" else "Edit Product Details", fontWeight = FontWeight.Bold) },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        val finalProduct = Product(
                            id = product?.id ?: 0,
                            name = name,
                            category = category,
                            supplier = supplier,
                            barcode = barcode,
                            wholesaleCost = wholesaleCost,
                            wholesalePackQty = packQty,
                            wholesaleResalePrice = wholesaleResalePriceStr.toDoubleOrNull() ?: 0.0,
                            retailPrice = currentRetailPrice,
                            calculatedUnitCost = calculatedUnitCost,
                            profitMargin = margin,
                            stockQty = stockQtyStr.toIntOrNull() ?: 0,
                            lowStockThreshold = lowStockThresholdStr.toIntOrNull() ?: 5,
                            notes = notes
                        )
                        onSave(finalProduct)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (product != null) {
                    TextButton(onClick = { onDelete(product) }) {
                        Text("Delete", color = StatusLowStock)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name*") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    // Category dropdown or suggestion string
                    var isExpanded by remember { mutableStateOf(false) }
                    val categories = listOf("Groceries", "Cosmetics", "Stationery", "Clothing", "Hardware")

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            trailingIcon = {
                                IconButton(onClick = { isExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = isExpanded,
                            onDismissRequest = { isExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        isExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = supplier,
                        onValueChange = { supplier = it },
                        label = { Text("Supplier Product Origin") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Divider()
                    Text("SMART COST CALCULATOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = wholesaleCostStr,
                            onValueChange = { wholesaleCostStr = it },
                            label = { Text("Pack Cost") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = wholesalePackQtyStr,
                            onValueChange = { wholesalePackQtyStr = it },
                            label = { Text("Pack Qty") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Auto-Calculated Unit Cost: ${String.format("%.2f", calculatedUnitCost)} Ksh",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val suggested = calculatedUnitCost * (1 + (defaultMarginPercent / 100.0))
                            Text(
                                text = "Suggested Retail (at +$defaultMarginPercent.0% MARG): ${String.format("%.1f", suggested)} Ksh",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    retailPriceStr = String.format("%.0f", suggested)
                                },
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Apply Suggestion", fontSize = 11.sp)
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = retailPriceStr,
                            onValueChange = { retailPriceStr = it },
                            label = { Text("Retail Sale Price*") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = wholesaleResalePriceStr,
                            onValueChange = { wholesaleResalePriceStr = it },
                            label = { Text("Resale Pack Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Text(
                        text = "Estimate Profit margin per item: ${String.format("%.2f", margin)} Ksh",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 13.sp
                    )
                }

                item {
                    Divider()
                    Text("INVENTORY LEVELS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = stockQtyStr,
                            onValueChange = { stockQtyStr = it },
                            label = { Text("Current Stock") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = lowStockThresholdStr,
                            onValueChange = { lowStockThresholdStr = it },
                            label = { Text("Low Stock Alert") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Product Notes") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
}

// --- 4. STOCK & INVENTORY MANAGER SCREEN ---
@Composable
fun InventoryScreen(
    viewModel: ShopViewModel
) {
    val currentProducts by viewModel.products.collectAsStateWithLifecycle()
    var targetStockProduct by remember { mutableStateOf<Product?>(null) }
    var adjustStockAmountStr by remember { mutableStateOf("1") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Stats
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STOCK MANAGER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Realtime replenishment and logs",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val totalInventoryItems = currentProducts.sumOf { it.stockQty }
                    Text(
                        text = "$totalInventoryItems total stock items",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Divider()

        if (currentProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No stock entries. Seed or create database items under Products tab.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentProducts) { product ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Supplier: ${product.supplier.ifEmpty { "Generic origin" }}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val isLow = product.stockQty <= product.lowStockThreshold
                                    val pillColor = if (isLow) StatusLowStock else StatusDelivered
                                    val statusText = if (isLow) "LOW STOCK ALERT" else "HEALTHY INVENTORY"

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(pillColor.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = statusText,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = pillColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Alert limit: <= ${product.lowStockThreshold}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "${product.stockQty} Qty",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        targetStockProduct = product
                                        adjustStockAmountStr = "1"
                                    },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                                ) {
                                    Icon(Icons.Default.PostAdd, contentDescription = "Add Stock", tint = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (targetStockProduct != null) {
        val prod = targetStockProduct!!
        AlertDialog(
            onDismissRequest = { targetStockProduct = null },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = adjustStockAmountStr.toIntOrNull() ?: 0
                        viewModel.addOrUpdateProduct(prod.copy(stockQty = (prod.stockQty + amt).coerceAtLeast(0)))
                        targetStockProduct = null
                    }
                ) {
                    Text("Apply Batch")
                }
            },
            dismissButton = {
                TextButton(onClick = { targetStockProduct = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Refill/Deduct Stock Levels", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Adjusting stock for: **${prod.name}**\nCurrent stock levels: **${prod.stockQty} items**")
                    OutlinedTextField(
                        value = adjustStockAmountStr,
                        onValueChange = { adjustStockAmountStr = it },
                        label = { Text("Adjustment Quantity (Use minus '-' to deduct)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

// --- 5. CUSTOMER REQUESTED MISSING ITEMS TRACKER ---
@Composable
fun RequestedItemsScreen(
    viewModel: ShopViewModel
) {
    val items by viewModel.requestedItems.collectAsStateWithLifecycle()
    var inputName by remember { mutableStateOf("") }
    var inputNotes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Input area
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CUSTOMER SPECIAL REQUESTS TRACKER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Log items customers asked for recently but were unavailable in current stock, tracking frequency count for restocking.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Requested Item Name (e.g. Casio fx82ES)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = inputNotes,
                        onValueChange = { inputNotes = it },
                        label = { Text("Request details/Notes") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            if (inputName.isNotEmpty()) {
                                viewModel.addRequestedItem(inputName, inputNotes)
                                inputName = ""
                                inputNotes = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }

        Divider()

        // List requests
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recorded custom item requests yet.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "FREQUENTLY REQUESTED MISSING PRODUCTS LIST",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                items(items) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (item.notes.isNotEmpty()) {
                                    Text(
                                        text = item.notes,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                val formattedDate = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(item.dateRequested))
                                Text(
                                    text = "Last request: $formattedDate",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${item.timesRequested}x",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 15.sp
                                    )
                                }

                                IconButton(onClick = { viewModel.addRequestedItem(item.name, item.notes) }) {
                                    Icon(Icons.Default.ArrowCircleUp, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                }

                                IconButton(onClick = { viewModel.deleteRequestedItem(item) }) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = "Delete", tint = StatusLowStock, modifier = Modifier.size(28.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 6. AI SHOP ASSISTANT CHATBOT SCREEN ---
@Composable
fun ChatbotScreen(
    viewModel: ShopViewModel
) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    var inputQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Welcome Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "JANE-MAIKS INTELLIGENT BOT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Direct Inventory & Sales queries in Swahili/English",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                IconButton(onClick = { viewModel.clearChat() }) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Clear Session", tint = StatusLowStock)
                }
            }
        }

        Divider()

        // Message lists
        Box(modifier = Modifier.weight(1f)) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Hello! I am your AI Retailing Assistant.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "I have live access to current inventory costs, remaining stock, recent sales numbers and customer demands. Ask me anything!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Shortcut Query chips
                    Text("TAP COMMON INQUIRIES BELOW:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val suggestionQueries = listOf(
                            "What items are low on stock?",
                            "How much have we sold today?",
                            "How much is white handkerchief?"
                        )
                        suggestionQueries.forEach { sug ->
                            Surface(
                                modifier = Modifier
                                    .clickable { viewModel.sendMessageToAI(sug) }
                                    .clip(RoundedCornerShape(8.dp)),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Text(text = sug, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { msg ->
                        val isUser = msg.sender == "USER"
                        val alignment = if (isUser) Alignment.End else Alignment.Start
                        val cardBg = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = alignment
                        ) {
                            Card(
                                modifier = Modifier.widthIn(max = 280.dp),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 0.dp,
                                    bottomEnd = if (isUser) 0.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(containerColor = cardBg)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
                            Text(text = time, fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        }
                    }

                    if (isLoading) {
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Text("AI Assistant is thinking...", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }

        Divider()

        // Input send box
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = { Text("Ask about products or daily sales...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = {
                        if (inputQuery.isNotEmpty()) {
                            viewModel.sendMessageToAI(inputQuery)
                            inputQuery = ""
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}

// --- 7. ANALYTICS REPORT SCREEN ---
@Composable
fun AnalyticsScreen(
    viewModel: ShopViewModel
) {
    val currentProducts by viewModel.products.collectAsStateWithLifecycle()
    val currentSales by viewModel.sales.collectAsStateWithLifecycle()

    val totalCostInvestmentValue = remember(currentProducts) {
        currentProducts.sumOf { it.stockQty * it.calculatedUnitCost }
    }

    val totalPotentialSalesValue = remember(currentProducts) {
        currentProducts.sumOf { it.stockQty * it.retailPrice }
    }

    val totalPotentialProfit = totalPotentialSalesValue - totalCostInvestmentValue

    val totalAccumulatedProfit = remember(currentSales) {
        currentSales.sumOf { it.profitAmount }
    }

    val categoryCounts = remember(currentProducts) {
        currentProducts.groupBy { it.category }.mapValues { it.value.size }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "RETAIL SALES & PROFITS REPORT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Valuation figures calculated across local system",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Stats grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AnalyticsValuationCard(
                    title = "Total Shop Investment Value (Cost Wholsale)",
                    valKsh = "${String.format("%,.1f", totalCostInvestmentValue)} Ksh",
                    sub = "Investment locked in stock items",
                    tintColor = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnalyticsValuationCard(
                        title = "Est. Total Gross Profit",
                        valKsh = "${String.format("%,.1f", totalAccumulatedProfit)} Ksh",
                        sub = "Realized profits in sales",
                        tintColor = StatusDelivered,
                        modifier = Modifier.weight(1f)
                    )
                    AnalyticsValuationCard(
                        title = "Unrealized Margin",
                        valKsh = "${String.format("%,.1f", totalPotentialProfit)} Ksh",
                        sub = "Expected future profit",
                        tintColor = StatusPending,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Category Breakdown Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "STOCK DISTRIBUTION BY CATEGORY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (categoryCounts.isEmpty()) {
                        Text("No stock items found.")
                    } else {
                        categoryCounts.forEach { (cat, count) ->
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = cat, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(text = "$count Products", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { count.toFloat() / currentProducts.size.coerceAtLeast(1) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

        // Transaction history log Heading
        item {
            Text(
                text = "RECENT COMPLETED TRANSACTIONS LOGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Transaction rows
        if (currentSales.isEmpty()) {
            item {
                Text(
                    text = "No completed sales recorded.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            items(currentSales.take(10)) { sale ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            val timeStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(sale.timestamp))
                            Text(text = "Transaction ID: #${sale.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Items: ${sale.itemsSummary}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text(text = "Timestamp: $timeStr", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+${sale.totalAmount} Ksh",
                                color = StatusDelivered,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Prof: +${String.format("%.1f", sale.profitAmount)} Ksh",
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsValuationCard(
    title: String,
    valKsh: String,
    sub: String,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = valKsh, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = tintColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = sub, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

// --- 8. SETTINGS SCREEN ---
@Composable
fun SettingsScreen(
    viewModel: ShopViewModel
) {
    val shopNameState by viewModel.shopName.collectAsStateWithLifecycle()
    val marginState by viewModel.defaultProfitMarginPercent.collectAsStateWithLifecycle()
    val isDarkState by viewModel.isDarkMode.collectAsStateWithLifecycle()

    var inputShopName by remember { mutableStateOf(shopNameState) }
    var inputMarginSub by remember { mutableStateOf(marginState.toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "APPLICATION CONFIGURATION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Configure parameters for calculations and displays",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Shop Name customization
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EDIT SHOP BUSINESS IDENTIFICATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = inputShopName,
                        onValueChange = { inputShopName = it },
                        label = { Text("Shop Display Title Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (inputShopName.isNotEmpty()) {
                                viewModel.updateShopName(inputShopName)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Apply Title Change")
                    }
                }
            }
        }

        // Automatic suggest calculations margin
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DEFAULT PROFIT MARGIN %",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = inputMarginSub,
                        onValueChange = { inputMarginSub = it },
                        label = { Text("Calculation Margin Percentage (e.g. 15, 20 or 25)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val marginInt = inputMarginSub.toIntOrNull() ?: 20
                            viewModel.updateDefaultMargin(marginInt)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Apply Default Margin")
                    }
                }
            }
        }

        // Toggle Screen colors mode
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DARK THEME MODE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Enhances battery life and protects eye comfort",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Switch(
                        checked = isDarkState,
                        onCheckedChange = { viewModel.toggleDarkMode() }
                    )
                }
            }
        }

        // Developer and credits card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "JaneMaiks Retail Assistant v1.0",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Designed for Jane and Michael's general family wholesale retail store near Nairobi. Built with full offline capability and local database persistence.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
