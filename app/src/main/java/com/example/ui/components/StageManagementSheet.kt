package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.MicRequest
import com.example.model.StageSlot

@Composable
fun StageManagementSheet(
    stageSlots: List<StageSlot>,
    micRequests: List<MicRequest>,
    onAcceptRequest: (MicRequest) -> Unit,
    onRejectRequest: (String) -> Unit,
    onToggleMute: (Int) -> Unit,
    onRemoveUser: (Int) -> Unit,
    onToggleLock: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("إدارة المنصة والمايكات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Requests Section
        Text("طلبات الصعود (${micRequests.size})", style = MaterialTheme.typography.bodyLarge)
        LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
            items(micRequests) { request ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(request.user.name)
                    Row {
                        IconButton(onClick = { onAcceptRequest(request) }) {
                            Icon(Icons.Default.Check, contentDescription = "قبول", tint = Color.Green)
                        }
                        IconButton(onClick = { onRejectRequest(request.id) }) {
                            Icon(Icons.Default.Close, contentDescription = "رفض", tint = Color.Red)
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Slots Section
        Text("المايكات الحالية", style = MaterialTheme.typography.bodyLarge)
        LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
            items(stageSlots) { slot ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مايك ${slot.index + 1}: ${slot.user?.name ?: if (slot.isLocked) "مقفل" else "خالي"}")
                    Row {
                        if (slot.user != null) {
                            IconButton(onClick = { onToggleMute(slot.index) }) {
                                Icon(
                                    if (slot.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "كتم"
                                )
                            }
                            IconButton(onClick = { onRemoveUser(slot.index) }) {
                                Icon(Icons.Default.PersonRemove, contentDescription = "إنزال")
                            }
                        }
                        IconButton(onClick = { onToggleLock(slot.index) }) {
                            Icon(
                                if (slot.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "قفل"
                            )
                        }
                    }
                }
            }
        }
    }
}
