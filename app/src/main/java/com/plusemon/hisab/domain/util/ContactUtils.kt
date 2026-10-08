package com.plusemon.hisab.domain.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.plusemon.hisab.ui.theme.IncomeGreen

data class ContactPhoneOption(
    val rawNumber: String,
    val displayNumber: String,
    val typeLabelEn: String,
    val typeLabelBn: String
)

data class PickedContact(
    val name: String,
    val phones: List<ContactPhoneOption>
)

object ContactUtils {

    fun extractContact(context: Context, contactUri: Uri): PickedContact? {
        val contentResolver = context.contentResolver
        var contactName = ""
        var contactId: String? = null

        // 1. Fetch Contact ID and Display Name
        try {
            contentResolver.query(
                contactUri,
                arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME
                ),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    if (idIdx != -1) contactId = cursor.getString(idIdx)
                    if (nameIdx != -1) contactName = cursor.getString(nameIdx) ?: ""
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (contactId.isNullOrBlank()) {
            contactId = contactUri.lastPathSegment
        }

        val phones = mutableListOf<ContactPhoneOption>()
        val seenDigits = mutableSetOf<String>()

        fun addPhoneNumber(rawNumber: String?, type: Int, customLabel: String?) {
            if (rawNumber.isNullOrBlank()) return
            val clean = rawNumber.trim()
            val digitsOnly = clean.filter { it.isDigit() }
            if (digitsOnly.length < 3) return

            // Prevent duplicates using last 8 digits or full digits
            val comparisonKey = if (digitsOnly.length >= 8) digitsOnly.takeLast(8) else digitsOnly
            if (seenDigits.add(comparisonKey)) {
                val (labelEn, labelBn) = when (type) {
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> Pair("Mobile", "মোবাইল")
                    ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> Pair("Home", "বাসা")
                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> Pair("Work", "অফিস")
                    ContactsContract.CommonDataKinds.Phone.TYPE_MAIN -> Pair("Main", "প্রধান")
                    ContactsContract.CommonDataKinds.Phone.TYPE_OTHER -> Pair("Other", "অন্যান্য")
                    ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM -> {
                        val lbl = if (!customLabel.isNullOrBlank()) customLabel else "Other"
                        Pair(lbl, lbl)
                    }
                    else -> Pair("Mobile", "মোবাইল")
                }
                phones.add(
                    ContactPhoneOption(
                        rawNumber = clean,
                        displayNumber = clean,
                        typeLabelEn = labelEn,
                        typeLabelBn = labelBn
                    )
                )
            }
        }

        // Method A: Query Phone.CONTENT_URI with CONTACT_ID
        if (!contactId.isNullOrBlank()) {
            try {
                contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.LABEL,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                    ),
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                    arrayOf(contactId),
                    null
                )?.use { cursor ->
                    val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val typeIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                    val labelIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)
                    val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    while (cursor.moveToNext()) {
                        if (contactName.isBlank() && nameIdx != -1) {
                            contactName = cursor.getString(nameIdx) ?: ""
                        }
                        val num = if (numIdx != -1) cursor.getString(numIdx) else null
                        val type = if (typeIdx != -1) cursor.getInt(typeIdx) else -1
                        val lbl = if (labelIdx != -1) cursor.getString(labelIdx) else null
                        addPhoneNumber(num, type, lbl)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Method B: Query via contactUri's Data sub-directory (handles cases with scoped URI permissions)
        if (phones.isEmpty()) {
            try {
                val dataUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Data.CONTENT_DIRECTORY)
                contentResolver.query(
                    dataUri,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.LABEL,
                        ContactsContract.Contacts.DISPLAY_NAME
                    ),
                    "${ContactsContract.Data.MIMETYPE} = ?",
                    arrayOf(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE),
                    null
                )?.use { cursor ->
                    val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val typeIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                    val labelIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)
                    val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    while (cursor.moveToNext()) {
                        if (contactName.isBlank() && nameIdx != -1) {
                            contactName = cursor.getString(nameIdx) ?: ""
                        }
                        val num = if (numIdx != -1) cursor.getString(numIdx) else null
                        val type = if (typeIdx != -1) cursor.getInt(typeIdx) else -1
                        val lbl = if (labelIdx != -1) cursor.getString(labelIdx) else null
                        addPhoneNumber(num, type, lbl)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Method C: Direct contactUri query in case picked URI was already a Phone data row
        if (phones.isEmpty()) {
            try {
                contentResolver.query(
                    contactUri,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                    ),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val typeIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                        val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        if (contactName.isBlank() && nameIdx != -1) {
                            contactName = cursor.getString(nameIdx) ?: ""
                        }
                        val num = if (numIdx != -1) cursor.getString(numIdx) else null
                        val type = if (typeIdx != -1) cursor.getInt(typeIdx) else -1
                        addPhoneNumber(num, type, null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return PickedContact(
            name = contactName.trim(),
            phones = phones
        )
    }

    fun dialPhoneNumber(context: Context, phoneNumber: String, isBangla: Boolean = false) {
        val cleanNumber = phoneNumber.trim()
        if (cleanNumber.isBlank()) {
            Toast.makeText(
                context,
                if (isBangla) "কোনো ফোন নম্বর পাওয়া যায়নি" else "No phone number available",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${Uri.encode(cleanNumber)}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                if (isBangla) "কল করার কোনো অ্যাপ পাওয়া যায়নি" else "No phone dialer application found",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@Composable
fun SelectContactPhoneDialog(
    contactName: String,
    options: List<ContactPhoneOption>,
    isBangla: Boolean,
    onSelectPhone: (ContactPhoneOption) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("select_phone_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContactPhone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "মোবাইল নম্বর নির্বাচন করুন" else "Select Phone Number",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (contactName.isNotBlank()) {
                            Text(
                                text = contactName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isBangla)
                        "এই কন্টাক্টে একাধিক নম্বর রয়েছে। যে নম্বরটি এই হিসাবে যুক্ত করতে চান সেটি বেছে নিন:"
                    else
                        "This contact has multiple phone numbers. Select which number to use for this record:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Options List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEachIndexed { index, option ->
                        Surface(
                            onClick = { onSelectPhone(option) },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_option_$index")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(IncomeGreen.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = IncomeGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = option.displayNumber,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val label = if (isBangla) option.typeLabelBn else option.typeLabelEn
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isBangla) "বাতিল" else "Cancel")
                    }
                }
            }
        }
    }
}
