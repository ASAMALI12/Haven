package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Gift

// ==========================================
// 3. GIFT SELECTION BOTTOM SHEET (GiftBottomSheet.kt)
// ==========================================
@Composable
fun GiftBottomSheet(
    userCoins: Int,
    gifts: List<Gift>,
    onSendGift: (Gift) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("إرسال هدية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("الرصيد: 🪙 $userCoins", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFFFD700))
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.height(240.dp)
        ) {
            items(gifts) { gift ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { onSendGift(gift) }
                ) {
                    // Placeholder for Gift Icon
                    Icon(Icons.Default.CardGiftcard, contentDescription = gift.name, tint = Color(0xFFFF4081))
                    Text(gift.name, style = MaterialTheme.typography.bodySmall)
                    Text("🪙 ${gift.coinPrice}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}
