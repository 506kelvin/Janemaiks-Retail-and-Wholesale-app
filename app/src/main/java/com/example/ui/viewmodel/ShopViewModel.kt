package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.RetrofitClient
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.RetailRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ShopViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RetailRepository

    // --- State Observables ---
    val products = MutableStateFlow<List<Product>>(emptyList())
    val sales = MutableStateFlow<List<Sale>>(emptyList())
    val requestedItems = MutableStateFlow<List<RequestedItem>>(emptyList())
    val chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())

    // --- UI States ---
    val loginRemembered = MutableStateFlow(true)
    val isLoggedIn = MutableStateFlow(false)
    val defaultProfitMarginPercent = MutableStateFlow(20) // e.g. 20%
    val shopName = MutableStateFlow("JaneMaiks Retail Shop")
    val isDarkMode = MutableStateFlow(false)

    // --- Active Sales Cart ---
    private val _cart = MutableStateFlow<Map<Product, Int>>(emptyMap())
    val cart: StateFlow<Map<Product, Int>> = _cart.asStateFlow()

    // --- Search Query State ---
    val searchQuery = MutableStateFlow("")
    val searchResults = MutableStateFlow<List<Product>>(emptyList())

    // --- Chatbot Assistant States ---
    val currentChatResponse = MutableStateFlow("")
    val isChatLoading = MutableStateFlow(false)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RetailRepository(database)

        // Observe Room streams of database rows
        viewModelScope.launch {
            repository.allProducts.collect { products.value = it }
        }
        viewModelScope.launch {
            repository.allSales.collect { sales.value = it }
        }
        viewModelScope.launch {
            repository.allRequestedItems.collect { requestedItems.value = it }
        }
        viewModelScope.launch {
            repository.chatMessages.collect { chatMessages.value = it }
        }

        // Initialize and Seed Settings / Sample Data
        viewModelScope.launch {
            repository.seedSampleProductsIfEmpty()

            // Load saved settings
            val savedLogin = repository.getSetting("is_logged_in")
            if (savedLogin == "true") {
                isLoggedIn.value = true
            }

            val savedShop = repository.getSetting("shop_name")
            if (savedShop != null) {
                shopName.value = savedShop
            }

            val savedMargin = repository.getSetting("default_margin")
            if (savedMargin != null) {
                defaultProfitMarginPercent.value = savedMargin.toIntOrNull() ?: 20
            }

            val savedDarkMode = repository.getSetting("dark_mode")
            if (savedDarkMode == "true") {
                isDarkMode.value = true
            }
        }

        // Observe Search query for realtime fuzzy suggestions
        viewModelScope.launch {
            searchQuery.collect { query ->
                searchResults.value = repository.searchProductsOffline(query)
            }
        }
    }

    // --- Authentication ---
    fun login(username: String, password: String): Boolean {
        if (username.trim().lowercase() == "janemaiks" && password == "admin123") {
            isLoggedIn.value = true
            if (loginRemembered.value) {
                viewModelScope.launch {
                    repository.saveSetting("is_logged_in", "true")
                }
            }
            return true
        }
        return false
    }

    fun logout() {
        isLoggedIn.value = false
        viewModelScope.launch {
            repository.saveSetting("is_logged_in", "false")
        }
    }

    // --- Settings Updates ---
    fun updateShopName(newName: String) {
        shopName.value = newName
        viewModelScope.launch {
            repository.saveSetting("shop_name", newName)
        }
    }

    fun updateDefaultMargin(marginPercent: Int) {
        defaultProfitMarginPercent.value = marginPercent
        viewModelScope.launch {
            repository.saveSetting("default_margin", marginPercent.toString())
        }
    }

    fun toggleDarkMode() {
        val newMode = !isDarkMode.value
        isDarkMode.value = newMode
        viewModelScope.launch {
            repository.saveSetting("dark_mode", newMode.toString())
        }
    }

    // --- Product Actions ---
    fun addOrUpdateProduct(product: Product) {
        viewModelScope.launch {
            repository.saveProduct(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    // --- Cart & Sales Actions ---
    fun addToCart(product: Product, quantity: Int = 1) {
        val currentMap = _cart.value.toMutableMap()
        val currentQty = currentMap[product] ?: 0
        currentMap[product] = currentQty + quantity
        _cart.value = currentMap
    }

    fun removeFromCart(product: Product) {
        val currentMap = _cart.value.toMutableMap()
        currentMap.remove(product)
        _cart.value = currentMap
    }

    fun decreaseCartQty(product: Product) {
        val currentMap = _cart.value.toMutableMap()
        val currentQty = currentMap[product] ?: 1
        if (currentQty <= 1) {
            currentMap.remove(product)
        } else {
            currentMap[product] = currentQty - 1
        }
        _cart.value = currentMap
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    fun checkoutCart() {
        val list = _cart.value.toList().map { Pair(it.first, it.second) }
        if (list.isNotEmpty()) {
            viewModelScope.launch {
                val success = repository.recordSale(list)
                if (success) {
                    _cart.value = emptyMap()
                }
            }
        }
    }

    // --- Customer Requested Items Tracker ---
    fun addRequestedItem(name: String, notes: String = "") {
        viewModelScope.launch {
            repository.trackRequestedItem(name, notes)
        }
    }

    fun deleteRequestedItem(item: RequestedItem) {
        viewModelScope.launch {
            repository.deleteRequestedItem(item)
        }
    }

    // --- Chatbot Actions ---
    fun sendMessageToAI(query: String) {
        if (query.trim().isEmpty()) return

        chatMessages.value = chatMessages.value + ChatMessage(sender = "USER", text = query)
        isChatLoading.value = true

        viewModelScope.launch {
            repository.insertChatMessage("USER", query)

            // Gather direct Context data
            val currentProducts = products.value
            val currentSales = sales.value
            val currentRequested = requestedItems.value

            val systemInstruction = buildSystemPrompt(currentProducts, currentSales, currentRequested)

            // Access API Key from BuildConfig Injection (from .env/Secrets gradle plugin)
            val apiKey = try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                "PLACEHOLDER_KEY"
            }

            if (apiKey == "MY_GEMINI_API_KEY" || apiKey == "PLACEHOLDER_KEY" || apiKey.isEmpty()) {
                val failMessage = "I am ready to help you manage *${shopName.value}*, but my active AI assistant key is missing. Please add your real **GEMINI_API_KEY** securely under the AI Studio Secrets Panel to get intelligent answers!"
                chatMessages.value = chatMessages.value + ChatMessage(sender = "ASSISTANT", text = failMessage)
                repository.insertChatMessage("ASSISTANT", failMessage)
                isChatLoading.value = false
                return@launch
            }

            try {
                withContext(Dispatchers.IO) {
                    val historyList = chatMessages.value.takeLast(10).map { msg ->
                        GeminiContent(parts = listOf(GeminiPart(text = "${msg.sender}: ${msg.text}")))
                    }

                    val request = GeminiRequest(
                        contents = historyList + GeminiContent(parts = listOf(GeminiPart(text = "USER: $query"))),
                        systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction)))
                    )

                    val response = RetrofitClient.service.generateContent(apiKey, request)
                    val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "Sorry, I could not understand that. Please try asking about prices, stock levels, or daily revenue."

                    chatMessages.value = chatMessages.value + ChatMessage(sender = "ASSISTANT", text = replyText)
                    repository.insertChatMessage("ASSISTANT", replyText)
                }
            } catch (e: Exception) {
                // Return offline fallback containing accurate search replies!
                val offlineReply = buildOfflineFuzzyFallback(query, currentProducts, currentSales)
                chatMessages.value = chatMessages.value + ChatMessage(sender = "ASSISTANT", text = offlineReply)
                repository.insertChatMessage("ASSISTANT", offlineReply)
            } finally {
                isChatLoading.value = false
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
            chatMessages.value = emptyList()
        }
    }

    private fun buildSystemPrompt(products: List<Product>, sales: List<Sale>, requested: List<RequestedItem>): String {
        val pContext = products.joinToString("\n") { p ->
            "- ${p.name} ($p.category): Price=${p.retailPrice}Ksh, Stock=${p.stockQty}, UnitCost=${p.calculatedUnitCost}Ksh, Box/DozenQty=${p.wholesalePackQty}, Box/DozenCost=${p.wholesaleCost}Ksh, WholeResalePrice=${p.wholesaleResalePrice}Ksh. Notes: ${p.notes}"
        }
        val sContext = sales.take(15).joinToString("\n") { s ->
            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(s.timestamp))
            "- Sale (Time=$time, Total=${s.totalAmount}Ksh, Profit=${s.profitAmount}Ksh, Items=[${s.itemsSummary}])"
        }
        val reqContext = requested.joinToString("\n") { r ->
            "- Missing: ${r.name} (times requested = ${r.timesRequested})"
        }
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        return """
            You are the smart shop AI helper of "${shopName.value}" in Nairobi, Kenya.
            You must be extremely responsive, clean, and write clear pricing numbers.
            Speak friendly and helpful. You can use English, Kiswahili (or Sheng) words appropriately (e.g., "Arimis iko punde!", "Profit yetu leo ni...").
            
            [DATE] $todayStr
            
            [INVENTORY DATABASE CONTEXT]
            $pContext
            
            [TODAY'S SALES HISTORY]
            $sContext
            
            [MISSING REQUESTED PRODUCTS BY CUSTOMERS]
            $reqContext
            
            CRITICAL RULES:
            1. Always answer about prices and stock quantities exactly matching values from the database above.
            2. For sales questions e.g. "how much did we sell today", compute sum of Today's Sale totals.
            3. If user searches an item with typos, use fuzzy approximation to suggest correct product listing details.
            4. Keep responses direct, elegant, fully formatted in markdown, and short (under 70 words).
        """.trimIndent()
    }

    private fun buildOfflineFuzzyFallback(query: String, products: List<Product>, sales: List<Sale>): String {
        val q = query.lowercase().trim()
        val matchedProducts = products.filter { repository.fuzzyMatch(q, it.name) || repository.fuzzyMatch(q, it.category) }

        if (matchedProducts.isNotEmpty()) {
            val sb = StringBuilder("Offline Mode: I found the following items inside **${shopName.value}** matching your request:\n\n")
            for (p in matchedProducts.take(4)) {
                val stockStatus = if (p.stockQty <= p.lowStockThreshold) "🚨 LOW INVENTORY: ${p.stockQty} left!" else "${p.stockQty} in stock"
                sb.append("- **${p.name}**: Price is **${p.retailPrice} Ksh** (${stockStatus}, category *${p.category}*, Supplier *${p.supplier}*)\n")
            }
            return sb.toString()
        }

        if (q.contains("sale") || q.contains("sold") || q.contains("today") || q.contains("pesa") || q.contains("mauzo")) {
            val totalSalesCount = sales.size
            val sumAmount = sales.sumOf { it.totalAmount }
            val sumProfit = sales.sumOf { it.profitAmount }
            return "Offline Mode Summary:\n- Recorded transactions: **$totalSalesCount**\n- Today Revenue: **$sumAmount Ksh**\n- Est. Profit: **$sumProfit Ksh**"
        }

        return "I could not find matching retail listings. Please check spelling or describe what products you are trying to verify."
    }
}
