package com.plusemon.hisab.domain.util

import com.plusemon.hisab.data.model.TransactionRecord
import com.plusemon.hisab.data.model.TransactionType
import com.plusemon.hisab.data.model.TransactionWithDetails
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporterImporter {

    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun exportToCsv(transactions: List<TransactionWithDetails>): String {
        val sb = StringBuilder()
        // Header
        sb.append("ID,Date,Type,Amount,Currency,Account,ToAccount,Category,Fee,Note\n")

        for (item in transactions) {
            val t = item.transaction
            val dateStr = sdf.format(Date(t.dateTimestamp))
            val typeStr = t.type.name
            val amountStr = String.format(Locale.US, "%.2f", t.amount)
            val currencyStr = item.account.currencyCode
            val accName = escapeCsv(item.account.name)
            val toAccName = escapeCsv(item.toAccount?.name ?: "")
            val catName = escapeCsv(item.category?.nameEn ?: "")
            val feeStr = String.format(Locale.US, "%.2f", t.fee)
            val noteStr = escapeCsv(t.note)

            sb.append("${t.id},$dateStr,$typeStr,$amountStr,$currencyStr,$accName,$toAccName,$catName,$feeStr,$noteStr\n")
        }

        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        var res = value.replace("\"", "\"\"")
        if (res.contains(",") || res.contains("\n") || res.contains("\"")) {
            res = "\"$res\""
        }
        return res
    }

    fun parseCsv(
        csvText: String,
        userId: String,
        defaultAccountId: Long
    ): List<TransactionRecord> {
        val results = mutableListOf<TransactionRecord>()
        val lines = csvText.lines()
        if (lines.isEmpty()) return results

        val startIndex = if (lines[0].contains("Amount", ignoreCase = true) || lines[0].contains("Date", ignoreCase = true)) 1 else 0

        for (i in startIndex until lines.size) {
            val line = lines[i].trim()
            if (line.isBlank()) continue

            try {
                val tokens = parseCsvLine(line)
                if (tokens.size >= 4) {
                    // Try parsing date
                    var timestamp = System.currentTimeMillis()
                    val dateToken = tokens.getOrNull(1) ?: ""
                    try {
                        val parsedDate = sdf.parse(dateToken)
                        if (parsedDate != null) timestamp = parsedDate.time
                    } catch (e: Exception) {
                        // try timestamp millis
                        dateToken.toLongOrNull()?.let { timestamp = it }
                    }

                    // Parse type
                    val typeStr = tokens.getOrNull(2) ?: "EXPENSE"
                    val type = when (typeStr.uppercase()) {
                        "INCOME" -> TransactionType.INCOME
                        "TRANSFER" -> TransactionType.TRANSFER
                        else -> TransactionType.EXPENSE
                    }

                    // Parse amount
                    val amountStr = tokens.getOrNull(3) ?: "0"
                    val amount = amountStr.replace(",", "").toDoubleOrNull() ?: 0.0

                    val note = tokens.getOrNull(9) ?: tokens.getOrNull(8) ?: tokens.getOrNull(7) ?: ""
                    val fee = tokens.getOrNull(8)?.toDoubleOrNull() ?: 0.0

                    if (amount > 0) {
                        results.add(
                            TransactionRecord(
                                userId = userId,
                                accountId = defaultAccountId,
                                amount = amount,
                                fee = fee,
                                type = type,
                                dateTimestamp = timestamp,
                                note = note
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // skip malformed line
            }
        }
        return results
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    result.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(ch)
            }
        }
        result.add(sb.toString().trim())
        return result
    }
}
