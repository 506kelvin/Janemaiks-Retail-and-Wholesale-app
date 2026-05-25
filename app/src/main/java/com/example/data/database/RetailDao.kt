package com.example.data.database

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products")
    suspend fun getAllProductsSnapshot(): List<Product>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    @Query("SELECT * FROM products WHERE barcode = :barcode")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("DELETE FROM products")
    suspend fun clearAllProducts()
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales")
    suspend fun getAllSalesSnapshot(): List<Sale>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSale(id: Long)
}

@Dao
interface RequestedItemDao {
    @Query("SELECT * FROM requested_items ORDER BY timesRequested DESC, dateRequested DESC")
    fun getAllRequestedItems(): Flow<List<RequestedItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequestedItem(item: RequestedItem)

    @Query("SELECT * FROM requested_items WHERE name = :name LIMIT 1")
    suspend fun getRequestedItemByName(name: String): RequestedItem?

    @Update
    suspend fun updateRequestedItem(item: RequestedItem)

    @Delete
    suspend fun deleteRequestedItem(item: RequestedItem)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): ShopSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: ShopSetting)
}
