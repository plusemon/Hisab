package com.plusemon.hisab.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.plusemon.hisab.data.model.Category
import com.plusemon.hisab.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE userId = :userId AND isArchived = 0 ORDER BY id ASC")
    fun getActiveCategories(userId: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId AND type = :type AND isArchived = 0 ORDER BY id ASC")
    fun getCategoriesByType(userId: String, type: TransactionType): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId AND type = :type AND isArchived = 0 ORDER BY id ASC")
    suspend fun getCategoriesByTypeSync(userId: String, type: TransactionType): List<Category>

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY id ASC")
    fun getAllCategories(userId: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY id ASC")
    suspend fun getAllCategoriesList(userId: String): List<Category>

    @Query("SELECT * FROM categories WHERE id = :categoryId AND userId = :userId LIMIT 1")
    suspend fun getCategoryById(categoryId: Long, userId: String): Category?

    @Query("SELECT COUNT(*) FROM categories WHERE userId = :userId")
    suspend fun getCategoryCount(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Update
    suspend fun updateCategory(category: Category)

    @Query("UPDATE categories SET isArchived = :isArchived WHERE id = :categoryId AND userId = :userId")
    suspend fun setArchived(categoryId: Long, userId: String, isArchived: Boolean)

    @Query("DELETE FROM categories WHERE id = :categoryId AND userId = :userId")
    suspend fun deleteCategory(categoryId: Long, userId: String)

    @Query("DELETE FROM categories WHERE userId = :userId")
    suspend fun deleteAllCategoriesForUser(userId: String)
}
