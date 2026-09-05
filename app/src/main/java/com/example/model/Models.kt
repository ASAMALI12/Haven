package com.example.model

enum class GiftCategory {
    POPULAR, EXCLUSIVE, SPECIAL, VIP
}

data class Gift(
    val id: String,
    val name: String,
    val coinPrice: Int,
    val iconUrl: String = "",
    val category: GiftCategory = GiftCategory.POPULAR
)

data class SentGiftEvent(
    val gift: Gift,
    val senderName: String,
    val receiverName: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class User(
    val id: String,
    val name: String,
    val avatar: String = "👤"
)

data class StageSlot(
    val index: Int,
    val user: User? = null,
    val isMuted: Boolean = false,
    val isLocked: Boolean = false
)

data class MicRequest(
    val id: String,
    val user: User,
    val requestedAt: Long = System.currentTimeMillis()
)
