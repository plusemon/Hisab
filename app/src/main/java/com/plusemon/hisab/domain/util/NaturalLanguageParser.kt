package com.plusemon.hisab.domain.util

import com.plusemon.hisab.data.model.Category
import com.plusemon.hisab.data.model.TransactionType
import com.plusemon.hisab.data.model.UserAccount

data class ParsedQuickEntry(
    val amount: Double,
    val type: TransactionType,
    val matchedCategory: Category?,
    val matchedAccount: UserAccount?,
    val matchedToAccount: UserAccount? = null,
    val note: String,
    val originalText: String
)

object NaturalLanguageParser {

    private val banglaToEngDigits = mapOf(
        '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
        '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
    )

    private fun normalizeBanglaNumbers(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            sb.append(banglaToEngDigits[ch] ?: ch)
        }
        return sb.toString()
    }

    fun parse(
        rawText: String,
        availableAccounts: List<UserAccount>,
        availableCategories: List<Category>
    ): ParsedQuickEntry? {
        val text = rawText.trim()
        if (text.isBlank()) return null

        val normalized = normalizeBanglaNumbers(text)

        // Find numbers (amount)
        val numberRegex = Regex("""(?:\b|৳|\$)\s*(\d+(?:\.\d{1,2})?)\s*(?:tk|taka|bdt|টাকা|৳)?""", RegexOption.IGNORE_CASE)
        val numberMatch = numberRegex.find(normalized) ?: Regex("""\b(\d+(?:\.\d{1,2})?)\b""").find(normalized)
        
        val amount = numberMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: return null
        if (amount <= 0.0) return null

        val lowerText = normalized.lowercase()

        // Detect if Transfer
        val isTransfer = lowerText.contains("transfer") || lowerText.contains("স্থানান্তর") ||
                lowerText.contains("পাঠালাম") || (lowerText.contains("to") && (lowerText.contains("from") || lowerText.contains("থেকে")))

        // Detect if Income
        val isIncome = !isTransfer && (
                lowerText.contains("salary") || lowerText.contains("বেতন") ||
                lowerText.contains("income") || lowerText.contains("আয়") ||
                lowerText.contains("পেলাম") || lowerText.contains("বোনাস") ||
                lowerText.contains("bonus") || lowerText.contains("freelance") ||
                lowerText.contains("ফ্রিল্যান্সিং") || lowerText.contains("লাভ") ||
                lowerText.contains("profit") || lowerText.contains("interest") ||
                lowerText.contains("উপহার") || lowerText.contains("gift received")
        )

        val transactionType = when {
            isTransfer -> TransactionType.TRANSFER
            isIncome -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }

        // Match accounts
        var matchedAccount: UserAccount? = null
        var matchedToAccount: UserAccount? = null

        for (acc in availableAccounts) {
            val accNameLower = acc.name.lowercase()
            val accTypeBnLower = acc.type.labelBn.lowercase()
            val accTypeEnLower = acc.type.labelEn.lowercase()

            if (lowerText.contains(accNameLower) || lowerText.contains(accTypeBnLower) || lowerText.contains(accTypeEnLower)) {
                if (matchedAccount == null) {
                    matchedAccount = acc
                } else if (isTransfer && matchedToAccount == null && acc.id != matchedAccount.id) {
                    matchedToAccount = acc
                }
            }
        }

        // Default to first active account if not detected
        if (matchedAccount == null && availableAccounts.isNotEmpty()) {
            matchedAccount = availableAccounts.firstOrNull { it.type.name == "CASH" } ?: availableAccounts.first()
        }

        // If transfer and toAccount is null, pick a second account if available
        if (isTransfer && matchedToAccount == null && availableAccounts.size > 1) {
            matchedToAccount = availableAccounts.firstOrNull { it.id != matchedAccount?.id }
        }

        // Match category
        var matchedCategory: Category? = null
        val targetCategories = availableCategories.filter { it.type == transactionType }

        // Keyword mapping dictionary
        val keywordsMap = mapOf(
            "খাবার" to listOf("food", "lunch", "dinner", "breakfast", "খাবার", "ভাত", "চা", "নাস্তা", "biryani", "burger", "pizza", "coffee", "restaurant", "হোটেল"),
            "বাজার" to listOf("market", "grocery", "groceries", "বাজার", "শাক", "সবজি", "মাছ", "মাংস", "চাল", "ডাল", "supermarket", "shwapno", "unimart"),
            "যাতায়াত" to listOf("transport", "rickshaw", "রিকশা", "uber", "pathao", "bus", "বাস", "cng", "সিএনজি", "metro", "মেট্রোরেল", "train", "ট্রেন", "fuel", "petrol", "অকটেন", "গাড়ি"),
            "বিল" to listOf("bill", "bills", "বিল", "বিদ্যুৎ", "electric", "current", "wifi", "internet", "ওয়াইফাই", "gas", "গ্যাস", "water", "পানি", "recharge", "রিচার্জ"),
            "কেনাকাটা" to listOf("shopping", "কেনাকাটা", "cloth", "dress", "জামা", "কাপড়", "shoes", "জুতা", "daraz", "দারাজ", "বই", "book"),
            "চিকিৎসা" to listOf("health", "medicine", "ঔষধ", "ওষুধ", "doctor", "ডাক্তার", "hospital", "হাসপাতাল", "pharma", "ফার্মেসি", "টেস্ট"),
            "বিনোদন" to listOf("entertainment", "movie", "সিনেমা", "outing", "ঘুরতে", "tour", "ভ্রমণ", "game", "খেলা", "cinema"),
            "শিক্ষা" to listOf("education", "fee", "tuition", "টিউশন", "school", "college", "university", "স্কুল", "বইখাতা", "exam"),
            "বেতন" to listOf("salary", "বেতন", "job", "চাকরি", "monthly"),
            "ব্যবসা" to listOf("business", "ব্যবসা", "sale", "বিক্রি", "shop"),
            "ফ্রিল্যান্সিং" to listOf("freelance", "upwork", "fiverr", "ফ্রিল্যান্সিং", "client", "ক্লায়েন্ট")
        )

        for (cat in targetCategories) {
            val nameBn = cat.nameBn.lowercase()
            val nameEn = cat.nameEn.lowercase()
            if (lowerText.contains(nameBn) || lowerText.contains(nameEn)) {
                matchedCategory = cat
                break
            }

            // Check keywords map
            val synonyms = keywordsMap[cat.nameBn] ?: emptyList()
            if (synonyms.any { lowerText.contains(it.lowercase()) }) {
                matchedCategory = cat
                break
            }
        }

        if (matchedCategory == null && targetCategories.isNotEmpty() && !isTransfer) {
            matchedCategory = targetCategories.first()
        }

        // Clean note
        var cleanNote = text
        // Keep note readable
        if (cleanNote.length > 60) {
            cleanNote = cleanNote.take(60)
        }

        return ParsedQuickEntry(
            amount = amount,
            type = transactionType,
            matchedCategory = matchedCategory,
            matchedAccount = matchedAccount,
            matchedToAccount = matchedToAccount,
            note = cleanNote,
            originalText = text
        )
    }
}
