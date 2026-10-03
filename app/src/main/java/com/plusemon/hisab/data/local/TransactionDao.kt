package com.plusemon.hisab.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.plusemon.hisab.data.model.TransactionRecord
import com.plusemon.hisab.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY dateTimestamp DESC, id DESC")
    fun getAllTransactions(userId: String): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND dateTimestamp BETWEEN :startDate AND :endDate ORDER BY dateTimestamp DESC, id DESC")
    fun getTransactionsBetween(userId: String, startDate: Long, endDate: Long): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND accountId = :accountId ORDER BY dateTimestamp DESC")
    fun getTransactionsForAccount(userId: String, accountId: Long): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND categoryId = :categoryId ORDER BY dateTimestamp DESC")
    fun getTransactionsForCategory(userId: String, categoryId: Long): Flow<List<TransactionRecord>>

    @Query("SELECT COUNT(*) FROM transactions WHERE userId = :userId AND (accountId = :accountId OR toAccountId = :accountId)")
    suspend fun getTransactionCountForAccount(userId: String, accountId: Long): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE userId = :userId AND categoryId = :categoryId")
    suspend fun getTransactionCountForCategory(userId: String, categoryId: Long): Int

    @Query("SELECT * FROM transactions WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getTransactionById(id: Long, userId: String): TransactionRecord?

    @Query("SELECT * FROM transactions WHERE userId = :userId")
    suspend fun getAllTransactionsList(userId: String): List<TransactionRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionRecord>)

    @Update
    suspend fun updateTransaction(transaction: TransactionRecord)

    @Query("DELETE FROM transactions WHERE id = :id AND userId = :userId")
    suspend fun deleteTransactionById(id: Long, userId: String)

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteAllTransactionsForUser(userId: String)
}
