package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.GalacticViewModel
import kotlinx.coroutines.delay

// ==========================================
// 1. LIVE ROOM UI EXTENSIONS (LiveRoomStageScreen.kt)
// ==========================================
@Composable
fun LiveRoomStageContent(
    viewModel: GalacticViewModel = viewModel(),
    onOpenStageManagement: () -> Unit,
    onOpenGiftSheet: () -> Unit
) {
    val activeGift by viewModel.activeGiftEvent.collectAsState()
    val userCoins by viewModel.userCoins.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Live Stage UI content
        Column(modifier = Modifier.fillMaxSize()) {
            // Stage Header & Mic Grid
            // ... (Existing Stage Code)
        }

        // Active Gift Animation Banner
        activeGift?.let { giftEvent ->
            LaunchedEffect(giftEvent) {
                delay(3000) // Display banner for 3 seconds
                viewModel.clearActiveGift()
            }
            
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xCC000000),
                    border = BorderStroke(1.dp, Color(0xFFFFD700)),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${giftEvent.senderName} أرسل ${giftEvent.gift.name} إلى ${giftEvent.receiverName} 🎁",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Action Ribbon / Quick Controls at Bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Button to open Stage/Mic Control Sheet
            IconButton(
                onClick = onOpenStageManagement,
                modifier = Modifier
                    .background(Color(0x88000000), CircleShape)
                    .padding(4.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = "إدارة المايكات", tint = Color.White)
            }

            // Button to open Gift Sheet
            Button(
                onClick = onOpenGiftSheet,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.CardGiftcard, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("إرسال هدية (🪙 $userCoins)")
            }
        }
    }
}
