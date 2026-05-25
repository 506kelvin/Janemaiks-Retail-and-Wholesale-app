package com.example.data.repository

import com.example.data.database.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

class RetailRepository(private val db: AppDatabase) {
    private val productDao = db.productDao()
    private val saleDao = db.saleDao()
    private val requestedItemDao = db.requestedItemDao()
    private val chatDao = db.chatDao()
    private val settingDao = db.settingDao()

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allSales: Flow<List<Sale>> = saleDao.getAllSales()
    val allRequestedItems: Flow<List<RequestedItem>> = requestedItemDao.getAllRequestedItems()
    val chatMessages: Flow<List<ChatMessage>> = chatDao.getAllMessages()

    suspend fun seedSampleProductsIfEmpty() {
        val snapshot = productDao.getAllProductsSnapshot()
        if (snapshot.isEmpty()) {
            val samples = listOf(
                Product(
                    name = "Arimis Petroleum Jelly 50ml",
                    category = "Cosmetics",
                    supplier = "Kapa Oil Refineries",
                    barcode = "890123456789",
                    wholesaleCost = 390.0,
                    wholesalePackQty = 12,
                    wholesaleResalePrice = 440.0,
                    retailPrice = 50.0,
                    calculatedUnitCost = 32.5,
                    profitMargin = 17.5,
                    stockQty = 35,
                    lowStockThreshold = 10,
                    notes = "Extremely popular for skin care in Kenya."
                ),
                Product(
                    name = "Kasuku Black Biro",
                    category = "Stationery",
                    supplier = "Kasuku Industries",
                    barcode = "890234567890",
                    wholesaleCost = 120.0,
                    wholesalePackQty = 12,
                    wholesaleResalePrice = 150.0,
                    retailPrice = 15.0,
                    calculatedUnitCost = 10.0,
                    profitMargin = 5.0,
                    stockQty = 4, // Trigger low stock!
                    lowStockThreshold = 8,
                    notes = "Standard blue/black writing pen."
                ),
                Product(
                    name = "White Handkerchief",
                    category = "Clothing",
                    supplier = "Mombasa Textiles Ltd",
                    barcode = "890345678901",
                    wholesaleCost = 180.0,
                    wholesalePackQty = 6,
                    wholesaleResalePrice = 210.0,
                    retailPrice = 50.0,
                    calculatedUnitCost = 30.0,
                    profitMargin = 20.0,
                    stockQty = 18,
                    lowStockThreshold = 5,
                    notes = "Pure cotton white handkerchiefs."
                ),
                Product(
                    name = "Colored Handkerchief",
                    category = "Clothing",
                    supplier = "Mombasa Textiles Ltd",
                    barcode = "",
                    wholesaleCost = 180.0,
                    wholesalePackQty = 6,
                    wholesaleResalePrice = 210.0,
                    retailPrice = 50.0,
                    calculatedUnitCost = 30.0,
                    profitMargin = 20.0,
                    stockQty = 12,
                    lowStockThreshold = 5,
                    notes = "Floral and checkered printed handkerchiefs."
                ),
                Product(
                    name = "Cooking Oil (Rina) 1L",
                    category = "Groceries",
                    supplier = "Bidco Africa",
                    barcode = "890456789012",
                    wholesaleCost = 1440.0,
                    wholesalePackQty = 6,
                    wholesaleResalePrice = 1550.0,
                    retailPrice = 280.0,
                    calculatedUnitCost = 240.0,
                    profitMargin = 40.0,
                    stockQty = 8,
                    lowStockThreshold = 3,
                    notes = "Rina double refined palm cooking oil."
                ),
                Product(
                    name = "Calculator Casio fx82MS",
                    category = "Stationery",
                    supplier = "Casio Kenya Office",
                    barcode = "890567890123",
                    wholesaleCost = 4800.0,
                    wholesalePackQty = 8,
                    wholesaleResalePrice = 5500.0,
                    retailPrice = 750.0,
                    calculatedUnitCost = 600.0,
                    profitMargin = 150.0,
                    stockQty = 15,
                    lowStockThreshold = 2,
                    notes = "Scientific calculator models requested by students."
                )
            )
            for (p in samples) {
                productDao.insertProduct(p)
            }
        }
    }

    // --- Product CRUD ---
    suspend fun saveProduct(product: Product) {
        val calculatedCost = if (product.wholesalePackQty > 0) product.wholesaleCost / product.wholesalePackQty else product.wholesaleCost
        val margin = product.retailPrice - calculatedCost
        val normalizedProduct = product.copy(
            calculatedUnitCost = calculatedCost,
            profitMargin = margin
        )
        if (normalizedProduct.id == 0) {
            productDao.insertProduct(normalizedProduct)
        } else {
            productDao.updateProduct(normalizedProduct)
        }
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    suspend fun getProductById(id: Int) = productDao.getProductById(id)
    suspend fun getProductByBarcode(barcode: String) = productDao.getProductByBarcode(barcode)

    // --- Record Sales with Stock Deduction ---
    suspend fun recordSale(cartItems: List<Pair<Product, Int>>): Boolean {
        if (cartItems.isEmpty()) return false

        var totalAmount = 0.0
        var totalProfit = 0.0
        val summaryBuilder = StringBuilder()

        for (i in cartItems.indices) {
            val (product, qty) = cartItems[i]
            val itemSum = qty * product.retailPrice
            val itemCost = qty * product.calculatedUnitCost
            val itemProfit = itemSum - itemCost

            totalAmount += itemSum
            totalProfit += itemProfit

            summaryBuilder.append("$qty x ${product.name}")
            if (i < cartItems.size - 1) {
                summaryBuilder.append(", ")
            }

            // Deduct inventory
            val updatedProduct = product.copy(
                stockQty = (product.stockQty - qty).coerceAtLeast(0)
            )
            productDao.updateProduct(updatedProduct)
        }

        val sale = Sale(
            totalAmount = totalAmount,
            profitAmount = totalProfit,
            itemsSummary = summaryBuilder.toString()
        )
        saleDao.insertSale(sale)
        return true
    }

    suspend fun deleteSale(id: Long) {
        saleDao.deleteSale(id)
    }

    // --- Requested Items Tracker ---
    suspend fun trackRequestedItem(name: String, notes: String = "") {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return

        val existing = requestedItemDao.getRequestedItemByName(trimmedName)
        if (existing != null) {
            val updated = existing.copy(
                timesRequested = existing.timesRequested + 1,
                dateRequested = System.currentTimeMillis()
            )
            requestedItemDao.updateRequestedItem(updated)
        } else {
            val newItem = RequestedItem(
                name = trimmedName,
                notes = notes,
                timesRequested = 1
            )
            requestedItemDao.insertRequestedItem(newItem)
        }
    }

    suspend fun deleteRequestedItem(item: RequestedItem) {
        requestedItemDao.deleteRequestedItem(item)
    }

    // --- Settings Storage ---
    suspend fun saveSetting(key: String, value: String) {
        settingDao.saveSetting(ShopSetting(key, value))
    }

    suspend fun getSetting(key: String): String? {
        return settingDao.getSetting(key)?.value
    }

    // --- Chat Memory Storage ---
    suspend fun insertChatMessage(sender: String, text: String) {
        chatDao.insertMessage(ChatMessage(sender = sender, text = text))
    }

    suspend fun clearChatHistory() {
        chatDao.clearHistory()
    }

    // --- Fuzzy Matching & Search Engine (Offline-First, Typo-Tolerant) ---
    suspend fun searchProductsOffline(query: String): List<Product> {
        val list = productDao.getAllProductsSnapshot()
        val q = query.lowercase().trim()
        if (q.isEmpty()) return list

        return list.filter { product ->
            fuzzyMatch(q, product.name) ||
            fuzzyMatch(q, product.category) ||
            product.barcode.contains(q)
        }
    }

    fun fuzzyMatch(query: String, text: String): Boolean {
        val q = query.lowercase().trim()
        val t = text.lowercase().trim()
        if (q.isEmpty()) return true
        if (t.contains(q)) return true

        val queryTokens = q.split("\\s+".toRegex())
        val textTokens = t.split("\\s+".toRegex())

        return queryTokens.all { qToken ->
            textTokens.any { tToken ->
                tToken.contains(qToken) || qToken.contains(tToken) || levenshteinDistance(qToken, tToken) <= 2
            }
        }
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        if (s1 == s2) return 0
        if (s1.isEmpty()) return s2.length
        if (s2.isEmpty()) return s1.length

        val dp = IntArray(s2.length + 1) { it }
        for (i in 1..s1.length) {
            var prev = i - 1
            dp[0] = i
            for (j in 1..s2.length) {
                val temp = dp[j]
                if (s1[i - 1] == s2[j - 1]) {
                    dp[j] = prev
                } else {
                    dp[j] = minOf(dp[j] + 1, dp[j - 1] + 1, prev + 1)
                }
                prev = temp
            }
        }
        return dp[s2.length]
    }
}
