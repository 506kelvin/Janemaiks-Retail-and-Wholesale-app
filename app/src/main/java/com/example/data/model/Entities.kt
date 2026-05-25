package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val supplier: String = "",
    val barcode: String = "",
    val wholesaleCost: Double = 0.0, // Cost to buy the whole pack
    val wholesalePackQty: Int = 1,   // Qty inside the wholesale pack
    val wholesaleResalePrice: Double = 0.0, // Selling price of the pack
    val retailPrice: Double = 0.0,    // Selling price of a single item
    val calculatedUnitCost: Double = 0.0, // Cost of a single item (wholesaleCost / wholesalePackQty)
    val profitMargin: Double = 0.0,       // Profit on a single item (retailPrice - calculatedUnitCost)
    val stockQty: Int = 0,
    val lowStockThreshold: Int = 5,
    val notes: String = ""
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: Double = 0.0,
    val profitAmount: Double = 0.0,
    val itemsSummary: String = "" // Summary like "6 x Kasuku Biro (90Ksh)"
)

@Entity(tableName = "requested_items")
data class RequestedItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dateRequested: Long = System.currentTimeMillis(),
    val timesRequested: Int = 1,
    val notes: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "USER" or "ASSISTANT"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "settings")
data class ShopSetting(
    @PrimaryKey val key: String,
    val value: String
)
