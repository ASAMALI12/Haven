package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.RealtimeAudioEngine
import com.example.data.SampleData
import com.example.model.*
import com.example.network.ServerSyncEngine
import com.example.ui.components.formatCoins
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class GalacticUiState(
    val currentScreen: AppScreen = AppScreen.ROOM_STAGE,
    val currentRoom: Room = SampleData.sampleRooms[0],
    val roomsList: List<Room> = SampleData.sampleRooms,
    val selectedCategory: String = "الكل",
    val selectedCountry: String = "الكل",
    val stageViewMode: StageViewMode = StageViewMode.CURVED_ARC,
    val arcRotationAngle: Float = 0f,
    val selectedSpeakerIndex: Int = 0,
    val isMyMicMuted: Boolean = false,
    val isMyHandRaised: Boolean = false,
    val isSpeakingNow: Boolean = false,
    val myVoiceModulator: VoiceModulator = VoiceModulator.NATURAL,
    val chatMessages: List<ChatMessage> = SampleData.initialChatMessages,
    val userProfile: UserProfile = SampleData.defaultUserProfile,
    val adminVault: AdminVaultData = SampleData.defaultAdminVault,
    val decorations: List<DecorationItem> = SampleData.sampleDecorations,
    val moments: List<MomentPost> = SampleData.sampleMoments,
    val conversations: List<DirectConversation> = SampleData.sampleConversations,
    val visitors: List<ProfileVisitor> = SampleData.sampleVisitors,
    val agencyData: AgencyData = SampleData.sampleAgencyData,
    val wallet: MultiCurrencyWallet = SampleData.sampleWallet,
    val notifications: List<SystemNotification> = SampleData.sampleNotifications,
    val treasureBox: TreasureBoxState = TreasureBoxState(),
    val isTreasureBoxOpen: Boolean = false,
    val quickReactions: List<QuickReaction> = emptyList(),
    val vipEntranceBanner: String? = null,
    val isAuthDialogOpen: Boolean = false,
    val activeGiftAnimation: ActiveGiftAnimation? = null,
    val isGiftSheetOpen: Boolean = false,
    val isRechargeSheetOpen: Boolean = false,
    val rechargePackages: List<RechargePackage> = SampleData.rechargePackagesList,
    val isVerticalRibbonExpanded: Boolean = false,
    val activeVerticalTool: String? = null, // "SOUNDBOARD", "VOICE_MOD", "VIP_RADAR", "GIFT_CANNON"
    val lastTriggeredSound: QuickSoundEffect? = null,
    val pkBattleState: PkBattle? = SampleData.sampleRooms[0].pkBattle,
    val wheelSpinning: Boolean = false,
    val wheelWinningIndex: Int? = null,
    val floatingMiniPlayerVisible: Boolean = false,
    val systemToastMessage: String? = null,
    val serverConnection: ServerConnectionInfo = SampleData.defaultServerConnection,
    val serverNodes: List<ServerNode> = SampleData.sampleServerNodes,
    val isServerStatusOpen: Boolean = false,
    val audioEngineSettings: AudioEngineSettings = SampleData.defaultAudioSettings,
    val isAudioSettingsOpen: Boolean = false,
    val isReportDialogOpen: Boolean = false,
    val reportTargetUserId: String = "",
    val reportTargetUserName: String = "",
    val moderationReports: List<ModerationReport> = emptyList(),
    val linkedAccounts: List<LinkedAccount> = SampleData.sampleLinkedAccounts,
    val isLinkedAccountsOpen: Boolean = false,
    val soundEffects: List<RoomSoundEffect> = SampleData.sampleSoundEffects,
    val isSoundboardOpen: Boolean = false,
    val topGifters: List<LeaderboardUser> = SampleData.sampleTopGiftersDaily,
    val topHosts: List<LeaderboardUser> = SampleData.sampleTopHostsDaily
)

enum class AppScreen {
    ROOM_STAGE,          // The 3D Curved Arc Live Voice Room
    EXPLORE,             // Browse rooms with horizontal/vertical discovery ribbons
    HOST_CENTER,         // Host statistics, H11/H12 progress, agency tasks
    AGENCY_DASHBOARD,    // لوحة تحكم وإدارة الوكالة ومتابعة المضيفين
    VIP_PROFILE,         // Profile, 3D Badges gallery, Gift wall, CP
    GAMES_ARENA,         // Mini-games, Lucky Wheel, AI Voice DJ
    ADMIN_VAULT,         // Secret Hidden Treasury & Currency Master Panel (Admin Only)
    DECORATION_STORE,    // متجر الزينة والمظاهر الملكية
    COMMUNITY_MOMENTS,   // لحظات ومحادثات المجتمع DMs
    VISITORS_SOCIAL,     // زوار البروفايل، المتطابقين، وطلبات الصداقة
    VISITORS_LIST,       // سجل زوار الملف الشخصي
    NOTIFICATIONS_CENTER,// مركز إشعارات النظام والعمليات
    LEADERBOARD          // لوحة الصدارة والشرف العالمية
}

class GalacticViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GalacticUiState())
    val uiState: StateFlow<GalacticUiState> = _uiState.asStateFlow()

    private val _stageSlots = MutableStateFlow<List<StageSlot>>(
        List(8) { index -> StageSlot(index = index) }
    )
    val stageSlots: StateFlow<List<StageSlot>> = _stageSlots.asStateFlow()

    private val _micRequests = MutableStateFlow<List<MicRequest>>(emptyList())
    val micRequests: StateFlow<List<MicRequest>> = _micRequests.asStateFlow()

    fun acceptMicRequest(request: MicRequest) {
        val emptySlot = _stageSlots.value.firstOrNull { it.user == null && !it.isLocked }
        if (emptySlot != null) {
            _stageSlots.value = _stageSlots.value.map { slot ->
                if (slot.index == emptySlot.index) slot.copy(user = request.user) else slot
            }
            _micRequests.value = _micRequests.value.filter { it.id != request.id }
        }
    }

    fun rejectMicRequest(requestId: String) {
        _micRequests.value = _micRequests.value.filter { it.id != requestId }
    }

    fun toggleSlotMute(slotIndex: Int) {
        _stageSlots.value = _stageSlots.value.map { slot ->
            if (slot.index == slotIndex) slot.copy(isMuted = !slot.isMuted) else slot
        }
    }

    fun removeUserFromStage(slotIndex: Int) {
        _stageSlots.value = _stageSlots.value.map { slot ->
            if (slot.index == slotIndex) slot.copy(user = null, isMuted = false) else slot
        }
    }

    fun toggleSlotLock(slotIndex: Int) {
        _stageSlots.value = _stageSlots.value.map { slot ->
            if (slot.index == slotIndex) slot.copy(isLocked = !slot.isLocked, user = null) else slot
        }
    }

    private val _userCoins = MutableStateFlow(1500) // Example initial coin balance
    val userCoins: StateFlow<Int> = _userCoins.asStateFlow()

    private val _activeGiftEvent = MutableStateFlow<SentGiftEvent?>(null)
    val activeGiftEvent: StateFlow<SentGiftEvent?> = _activeGiftEvent.asStateFlow()

    fun sendGift(gift: Gift, receiverName: String): Boolean {
        if (_userCoins.value >= gift.coinPrice) {
            _userCoins.value -= gift.coinPrice
            _activeGiftEvent.value = SentGiftEvent(gift = gift, senderName = "أنت", receiverName = receiverName)
            return true
        }
        return false
    }

    fun clearActiveGift() {
        _activeGiftEvent.value = null
    }

    private val serverSyncEngine = ServerSyncEngine(viewModelScope)
    private val realtimeAudioEngine = RealtimeAudioEngine(viewModelScope)

    init {
        startVoiceEnergySimulation()
        startPkBattleTimer()
        startLiveRoomAudienceSimulation()
        listenToEngineUpdates()
    }

    private fun listenToEngineUpdates() {
        viewModelScope.launch {
            serverSyncEngine.connectionInfo.collect { info ->
                _uiState.update { it.copy(serverConnection = info) }
            }
        }
        viewModelScope.launch {
            realtimeAudioEngine.audioSettings.collect { audio ->
                _uiState.update { it.copy(audioEngineSettings = audio) }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun setStageViewMode(mode: StageViewMode) {
        _uiState.update { it.copy(stageViewMode = mode) }
        showToast("تم التحويل إلى نمط: " + when(mode) {
            StageViewMode.CURVED_ARC -> "المسرح القوسي ثلاثي الأبعاد 🌌"
            StageViewMode.CLASSIC_GRID -> "شبكة المايكات الكلاسيكية 📱"
            StageViewMode.ORBIT_SPHERE -> "المدار الفضائي الدائري 🪐"
        })
    }

    fun updateArcRotation(deltaAngle: Float) {
        _uiState.update {
            val newAngle = (it.arcRotationAngle + deltaAngle).coerceIn(-120f, 120f)
            it.copy(arcRotationAngle = newAngle)
        }
    }

    fun selectSpeaker(index: Int) {
        _uiState.update { it.copy(selectedSpeakerIndex = index) }
    }

    fun toggleMyMic() {
        _uiState.update { current ->
            val newMute = !current.isMyMicMuted
            val updatedSpeakers = current.currentRoom.speakers.map { sp ->
                if (sp.isHost || sp.micIndex == 0) sp.copy(isMuted = newMute, isSpeaking = !newMute)
                else sp
            }
            current.copy(
                isMyMicMuted = newMute,
                isSpeakingNow = !newMute,
                currentRoom = current.currentRoom.copy(speakers = updatedSpeakers)
            )
        }
        showToast(if (_uiState.value.isMyMicMuted) "تم كتم المايكروفون 🔇" else "المايكروفون مفتوح الآن 🎙️")
    }

    fun toggleRaiseHand() {
        _uiState.update { current ->
            val newState = !current.isMyHandRaised
            current.copy(isMyHandRaised = newState)
        }
        showToast(if (_uiState.value.isMyHandRaised) "تم رفع اليد لطلب المايك ✋" else "تم إلغاء رفع اليد")
    }

    fun setVoiceModulator(modulator: VoiceModulator) {
        _uiState.update { it.copy(myVoiceModulator = modulator) }
        showToast("تم تفعيل مؤثر الصوت: " + when(modulator) {
            VoiceModulator.NATURAL -> "صوت طبيعي"
            VoiceModulator.COSMIC_ECHO -> "صدى مجري فضائي 🌌"
            VoiceModulator.CYBER_ROBOT -> "روبوت سايبر 🤖"
            VoiceModulator.DEEP_BROADCASTER -> "مذيع إذاعي فخم 🎙️"
            VoiceModulator.NEON_CHIPMUNK -> "صوت سنجاب نيون مرح 🐿️"
            VoiceModulator.GALAXY_RADIO -> "لاسلكي فضائي 📻"
        })
    }

    fun triggerSoundEffect(sound: QuickSoundEffect) {
        _uiState.update { it.copy(lastTriggeredSound = sound) }
        val soundName = when (sound) {
            QuickSoundEffect.APPLAUSE -> "تصفيق حار 👏"
            QuickSoundEffect.CHEER -> "هتاف الجمهور 🎉"
            QuickSoundEffect.LAUGHTER -> "ضحك جماعي 😂"
            QuickSoundEffect.DRUMROLL -> "قرع الطبول 🥁"
            QuickSoundEffect.FANFARE -> "موسيقى النصر الملكية 🎺"
            QuickSoundEffect.LASER_SHOT -> "ليزر فضائي ⚡"
            QuickSoundEffect.HEARTBEAT -> "نبضات قلب ❤️"
        }
        sendSystemMessage("تم تشغيل مؤثر صوتي: $soundName")
        showToast("مؤثر: $soundName")
    }

    fun toggleVerticalRibbon() {
        _uiState.update { it.copy(isVerticalRibbonExpanded = !it.isVerticalRibbonExpanded) }
    }

    fun setActiveVerticalTool(tool: String?) {
        _uiState.update {
            it.copy(
                activeVerticalTool = if (it.activeVerticalTool == tool) null else tool,
                isVerticalRibbonExpanded = true
            )
        }
    }

    fun openGiftSheet(open: Boolean) {
        _uiState.update { it.copy(isGiftSheetOpen = open) }
    }

    fun openRechargeSheet(open: Boolean) {
        _uiState.update { it.copy(isRechargeSheetOpen = open) }
    }

    fun rechargeCoins(pkg: RechargePackage, method: PaymentMethod = PaymentMethod.ZAIN_CASH, detail: String = "") {
        val totalAdded = pkg.coinsAmount + pkg.bonusCoins
        val priceText = if (method == PaymentMethod.ZAIN_CASH) "${formatCoins(pkg.priceIqd)} د.ع" else "$${pkg.priceUsd}"
        val newNotification = SystemNotification(
            id = "n_${System.currentTimeMillis()}",
            title = "🪙 تأكيد شراء وشحن كوينز",
            message = "تم شحن ${formatCoins(totalAdded)} كوينز بنجاح عبر ${method.titleAr} ($priceText) - $detail",
            type = "RECHARGE",
            timestamp = "الآن"
        )

        _uiState.update { current ->
            current.copy(
                userProfile = current.userProfile.copy(
                    coinsBalance = current.userProfile.coinsBalance + totalAdded
                ),
                wallet = current.wallet.copy(
                    coinsBalance = current.wallet.coinsBalance + totalAdded
                ),
                adminVault = current.adminVault.copy(
                    hiddenVaultUsd = current.adminVault.hiddenVaultUsd + pkg.priceUsd
                ),
                notifications = listOf(newNotification) + current.notifications,
                isRechargeSheetOpen = false
            )
        }
        showToast("✨ تم اعتماد وشحن ${formatCoins(totalAdded)} 🪙 بنجاح عبر ${method.titleAr} ($priceText)!")
    }

    fun sendGift(gift: Gift, receiverSpeaker: Speaker, combo: Int = 1) {
        val currentCoins = _uiState.value.userProfile.coinsBalance
        val totalCost = gift.coinPrice * combo
        if (currentCoins < totalCost) {
            showToast("عذراً، رصيدك من العملة الموحدة غير كافٍ! 🪙")
            openRechargeSheet(true)
            return
        }

        // Deduct from unified coins and add to speaker & host received coins
        _uiState.update { current ->
            val updatedCoins = current.userProfile.coinsBalance - totalCost
            val updatedGiftsReceived = receiverSpeaker.giftsReceivedCoins + totalCost
            val updatedSpeakers = current.currentRoom.speakers.map { sp ->
                if (sp.id == receiverSpeaker.id) sp.copy(giftsReceivedCoins = updatedGiftsReceived)
                else sp
            }

            // Update PK score if PK active
            val updatedPk = current.pkBattleState?.let { pk ->
                if (receiverSpeaker.isHost) pk.copy(team1Score = pk.team1Score + totalCost)
                else pk.copy(team2Score = pk.team2Score + totalCost)
            }

            val chatMsg = ChatMessage(
                id = "gift_${System.currentTimeMillis()}",
                senderName = current.userProfile.name,
                senderAvatar = current.userProfile.avatarEmoji,
                senderVip = current.userProfile.vipLevel,
                senderNoble = current.userProfile.nobleLevel,
                text = "${gift.name} x$combo إلى ${receiverSpeaker.name}! 🎁✨",
                type = MessageType.GIFT,
                giftIcon = gift.iconEmoji,
                giftSent = gift,
                giftCombo = combo,
                timestamp = "الآن"
            )

            current.copy(
                userProfile = current.userProfile.copy(coinsBalance = updatedCoins),
                currentRoom = current.currentRoom.copy(speakers = updatedSpeakers, pkBattle = updatedPk),
                pkBattleState = updatedPk,
                chatMessages = current.chatMessages + chatMsg,
                activeGiftAnimation = ActiveGiftAnimation(
                    gift = gift,
                    senderName = current.userProfile.name,
                    receiverName = receiverSpeaker.name,
                    comboCount = combo
                ),
                isGiftSheetOpen = false
            )
        }

        showToast("تم إرسال ${gift.name} بنجاح! 🚀 (-$totalCost 🪙)")

        // Auto dismiss gift animation after 4 seconds
        viewModelScope.launch {
            delay(4000)
            _uiState.update { it.copy(activeGiftAnimation = null) }
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val current = _uiState.value
        val newMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderName = current.userProfile.name,
            senderAvatar = current.userProfile.avatarEmoji,
            senderVip = current.userProfile.vipLevel,
            senderNoble = current.userProfile.nobleLevel,
            text = text.trim(),
            type = MessageType.TEXT,
            timestamp = "الآن"
        )
        _uiState.update { it.copy(chatMessages = it.chatMessages + newMsg) }
    }

    private fun sendSystemMessage(text: String) {
        val newMsg = ChatMessage(
            id = "sys_${System.currentTimeMillis()}",
            senderName = "النظام الذكي",
            senderAvatar = "🤖",
            text = text,
            isSystem = true,
            type = MessageType.SYSTEM,
            timestamp = "الآن"
        )
        _uiState.update { it.copy(chatMessages = it.chatMessages + newMsg) }
    }

    fun joinRoom(room: Room) {
        _uiState.update {
            it.copy(
                currentRoom = room,
                currentScreen = AppScreen.ROOM_STAGE,
                floatingMiniPlayerVisible = false
            )
        }
        showToast("انضممت إلى: ${room.title}")
    }

    fun leaveRoom() {
        _uiState.update {
            it.copy(
                floatingMiniPlayerVisible = true,
                currentScreen = AppScreen.EXPLORE
            )
        }
        showToast("تم تصغير البث إلى المشغل العائم 🎵")
    }

    fun closeFloatingMiniPlayer() {
        _uiState.update { it.copy(floatingMiniPlayerVisible = false) }
        showToast("تم مغادرة الغرفة الصوتية")
    }

    fun filterRooms(category: String, country: String) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                selectedCountry = country
            )
        }
    }

    fun toggleBadgeEquip(badgeId: String) {
        _uiState.update { current ->
            val updatedBadges = current.userProfile.badges.map { b ->
                if (b.id == badgeId) b.copy(isEquipped = !b.isEquipped)
                else b
            }
            current.copy(userProfile = current.userProfile.copy(badges = updatedBadges))
        }
        showToast("تم تحديث حالة الشارات في الملف الشخصي 🎖️")
    }

    fun claimMissionReward(missionId: String) {
        _uiState.update { current ->
            val mission = current.userProfile.hostStats.missions.find { it.id == missionId }
            if (mission != null && !mission.isCompleted) {
                val updatedMissions = current.userProfile.hostStats.missions.map { m ->
                    if (m.id == missionId) m.copy(isCompleted = true) else m
                }
                val newCoins = current.userProfile.coinsBalance + mission.rewardCoins
                val updatedHostStats = current.userProfile.hostStats.copy(
                    coinsBalance = current.userProfile.hostStats.coinsBalance + mission.rewardCoins,
                    missions = updatedMissions
                )
                current.copy(
                    userProfile = current.userProfile.copy(
                        coinsBalance = newCoins,
                        hostStats = updatedHostStats
                    )
                )
            } else current
        }
        showToast("مبروك! تم استلام مكافأة المهمة بنجاح إلى رصيدك الموحد 🪙🎉")
    }

    fun spinWheel() {
        if (_uiState.value.wheelSpinning) return
        viewModelScope.launch {
            _uiState.update { it.copy(wheelSpinning = true, wheelWinningIndex = null) }
            delay(2500)
            val wonIndex = Random.nextInt(0, 8)
            val rewardCoins = when (wonIndex) {
                0 -> 500
                1 -> 1500
                2 -> 3000
                3 -> 10000
                4 -> 50000
                5 -> 250
                6 -> 8000
                else -> 20000
            }
            _uiState.update { current ->
                current.copy(
                    wheelSpinning = false,
                    wheelWinningIndex = wonIndex,
                    userProfile = current.userProfile.copy(
                        coinsBalance = current.userProfile.coinsBalance + rewardCoins
                    )
                )
            }
            showToast("🎉 مبروك! ربحت $rewardCoins 🪙 من العملة الموحدة في عجلة الحظ المجرية!")
        }
    }

    fun openAuthDialog(open: Boolean) {
        _uiState.update { it.copy(isAuthDialogOpen = open) }
    }

    fun loginMember(nickname: String, phone: String) {
        _uiState.update { current ->
            current.copy(
                userProfile = current.userProfile.copy(
                    name = nickname,
                    nickname = nickname,
                    phoneNumber = phone,
                    role = UserRole.MEMBER,
                    isPhoneVerified = true,
                    isEmailVerified = false
                ),
                isAuthDialogOpen = false
            )
        }
        showToast("✅ تم تأكيد حسابك عبر WhatsApp بنجاح! مرحباً بك $nickname 🚀")
    }

    fun loginAdmin(adminPhone: String, adminEmail: String) {
        _uiState.update { current ->
            current.copy(
                userProfile = current.userProfile.copy(
                    name = "صَــعـب (المدير العام 🛡️)",
                    nickname = "المدير العام",
                    phoneNumber = adminPhone,
                    email = adminEmail,
                    role = UserRole.SUPER_ADMIN,
                    isPhoneVerified = true,
                    isEmailVerified = true,
                    vipLevel = 12
                ),
                adminVault = current.adminVault.copy(
                    adminPhone = adminPhone,
                    adminEmail = adminEmail
                ),
                isAuthDialogOpen = false,
                currentScreen = AppScreen.ADMIN_VAULT
            )
        }
        showToast("🛡️ تم التحقق المزدوج بنجاح (WhatsApp + Gmail)! تم تفعيل صلاحيات المدير والخزينة السرية 👑")
    }

    fun withdrawVaultFunds(amountUsd: Double, destination: String, method: String) {
        val currentVault = _uiState.value.adminVault
        if (amountUsd > currentVault.hiddenVaultUsd) {
            showToast("⚠️ المبلغ المطلوب يتجاوز الرصيد المتوفر في المحفظة المخفية!")
            return
        }

        val coinsEquivalent = (amountUsd / currentVault.coinExchangeRateUsd).toLong()
        val txId = "TX-${Random.nextInt(10000, 99999)}"
        val txTypeName = when (method) {
            "ZAIN_CASH" -> "سحب أرباح الخزينة (زين كاش العراق 📱)"
            "MASTERCARD" -> "سحب أرباح الخزينة (ماستر كارد Direct 💳)"
            "USDT" -> "سحب أرباح الخزينة (USDT TRC20 ⚡)"
            else -> "سحب نقدي بنكي (IBAN 🏛️)"
        }
        val newTx = HiddenVaultTransaction(
            id = txId,
            type = txTypeName,
            amountCoins = coinsEquivalent,
            amountUsd = amountUsd,
            destination = destination,
            timestamp = "الآن"
        )
        val newNotification = SystemNotification(
            id = "n_${System.currentTimeMillis()}",
            title = "💵 سحب أرباح للمدير ناجح",
            message = "تم تنفيذ سحب $${String.format("%,.2f", amountUsd)} بنجاح عبر $txTypeName إلى $destination",
            type = "TRANSFER",
            timestamp = "الآن"
        )

        _uiState.update { current ->
            current.copy(
                adminVault = current.adminVault.copy(
                    hiddenVaultUsd = current.adminVault.hiddenVaultUsd - amountUsd,
                    transactions = listOf(newTx) + current.adminVault.transactions
                ),
                notifications = listOf(newNotification) + current.notifications
            )
        }
        showToast("✅ تم تنفيذ سحب $${String.format("%,.2f", amountUsd)} بنجاح إلى $destination ⚡")
    }

    fun mintCoins(amount: Long, note: String) {
        val txId = "TX-${Random.nextInt(10000, 99999)}"
        val currentVault = _uiState.value.adminVault
        val usdValue = amount * currentVault.coinExchangeRateUsd
        val newTx = HiddenVaultTransaction(
            id = txId,
            type = "سك وضخ عملات جديدة 🪙",
            amountCoins = amount,
            amountUsd = usdValue,
            destination = "احتياطي المجتمع الموحد",
            timestamp = "الآن"
        )

        _uiState.update { current ->
            current.copy(
                adminVault = current.adminVault.copy(
                    platformReserveCoins = current.adminVault.platformReserveCoins + amount,
                    totalMintedCoins = current.adminVault.totalMintedCoins + amount,
                    transactions = listOf(newTx) + current.adminVault.transactions
                )
            )
        }
        showToast("✨ تم سك وضخ ${formatCoins(amount)} 🪙 إلى احتياطي المنصة بنجاح!")
    }

    fun burnCoins(amount: Long, note: String) {
        val txId = "TX-${Random.nextInt(10000, 99999)}"
        val currentVault = _uiState.value.adminVault
        if (amount > currentVault.platformReserveCoins) {
            showToast("⚠️ كمية الحرق أكبر من الاحتياطي المتاح!")
            return
        }

        val usdValue = amount * currentVault.coinExchangeRateUsd
        val newTx = HiddenVaultTransaction(
            id = txId,
            type = "حرق عملات للتوازن المالي 🔥",
            amountCoins = amount,
            amountUsd = usdValue,
            destination = "محفظة الحرق (Black Hole)",
            timestamp = "الآن"
        )

        _uiState.update { current ->
            current.copy(
                adminVault = current.adminVault.copy(
                    platformReserveCoins = current.adminVault.platformReserveCoins - amount,
                    totalBurnedCoins = current.adminVault.totalBurnedCoins + amount,
                    transactions = listOf(newTx) + current.adminVault.transactions
                )
            )
        }
        showToast("🔥 تم حرق ${formatCoins(amount)} 🪙 بنجاح لضبط التضخم والتوازن المالي!")
    }

    fun directTransferToUser(targetUserId: String, amountCoins: Long) {
        val txId = "TX-${Random.nextInt(10000, 99999)}"
        val newTx = HiddenVaultTransaction(
            id = txId,
            type = "تحويل مباشر من المدير",
            amountCoins = amountCoins,
            amountUsd = amountCoins * _uiState.value.adminVault.coinExchangeRateUsd,
            destination = "المستخدم ID: $targetUserId",
            timestamp = "الآن"
        )

        _uiState.update { current ->
            val updatedUserCoins = if (current.userProfile.numericId == targetUserId) {
                current.userProfile.coinsBalance + amountCoins
            } else {
                current.userProfile.coinsBalance
            }

            current.copy(
                userProfile = current.userProfile.copy(coinsBalance = updatedUserCoins),
                adminVault = current.adminVault.copy(
                    platformReserveCoins = (current.adminVault.platformReserveCoins - amountCoins).coerceAtLeast(0),
                    transactions = listOf(newTx) + current.adminVault.transactions
                )
            )
        }
        showToast("👑 تم تحويل ${formatCoins(amountCoins)} 🪙 مباشرة إلى حساب العضو (ID: $targetUserId)")
    }

    fun buyDecoration(item: DecorationItem) {
        val currentCoins = _uiState.value.userProfile.coinsBalance
        if (currentCoins < item.priceCoins) {
            showToast("⚠️ رصيدك غير كافٍ لشراء هذا المظهر! اشحن كوينز أولاً 🪙")
            openRechargeSheet(true)
            return
        }

        _uiState.update { current ->
            val updatedDecorations = current.decorations.map { d ->
                if (d.id == item.id) d.copy(isPurchased = true, isEquipped = true)
                else if (d.type == item.type) d.copy(isEquipped = false)
                else d
            }
            current.copy(
                userProfile = current.userProfile.copy(
                    coinsBalance = current.userProfile.coinsBalance - item.priceCoins
                ),
                decorations = updatedDecorations
            )
        }
        showToast("✨ تم شراء وتفعيل ${item.name} بنجاح! (-${formatCoins(item.priceCoins)} 🪙)")
    }

    fun toggleEquipDecoration(item: DecorationItem) {
        _uiState.update { current ->
            val targetNewState = !item.isEquipped
            val updatedDecorations = current.decorations.map { d ->
                if (d.id == item.id) d.copy(isEquipped = targetNewState)
                else if (d.type == item.type && targetNewState) d.copy(isEquipped = false)
                else d
            }
            current.copy(decorations = updatedDecorations)
        }
        showToast(if (!item.isEquipped) "تم تفعيل ${item.name} ✓" else "تم إلغاء التفعيل")
    }

    fun sendQuickReaction(emoji: String) {
        val newReaction = QuickReaction(
            id = "react_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}",
            emoji = emoji,
            senderName = _uiState.value.userProfile.name,
            offsetX = (Random.nextFloat() * 80 - 40)
        )
        _uiState.update { it.copy(quickReactions = (it.quickReactions + newReaction).takeLast(15)) }
    }

    fun openTreasureDialog(open: Boolean) {
        _uiState.update { it.copy(isTreasureBoxOpen = open) }
    }

    fun chargeTreasureEnergy(amount: Int) {
        _uiState.update { current ->
            val newEnergy = (current.treasureBox.currentEnergy + amount).coerceAtMost(current.treasureBox.maxEnergy)
            current.copy(
                treasureBox = current.treasureBox.copy(currentEnergy = newEnergy)
            )
        }
        showToast("⚡ تم شحن صندوق الكنز (+${amount}⚡)")
    }

    fun openTreasureChest() {
        val currentBox = _uiState.value.treasureBox
        if (currentBox.currentEnergy < currentBox.maxEnergy) return

        val wonCoins = 50000L
        _uiState.update { current ->
            current.copy(
                userProfile = current.userProfile.copy(
                    coinsBalance = current.userProfile.coinsBalance + wonCoins
                ),
                treasureBox = current.treasureBox.copy(
                    currentEnergy = 10,
                    lastWinnerName = current.userProfile.name,
                    lastWonCoins = wonCoins
                ),
                isTreasureBoxOpen = false
            )
        }
        showToast("🎉 مبروك! فزت بجائزة الصندوق الكبرى ${formatCoins(wonCoins)} 🪙 تم إضافتها لمحفظتك!")
    }

    fun supportPkTeam(teamIndex: Int, coins: Long) {
        val currentCoins = _uiState.value.userProfile.coinsBalance
        if (currentCoins < coins) {
            showToast("رصيدك غير كافٍ لدعم التحدي! 🪙")
            openRechargeSheet(true)
            return
        }

        _uiState.update { current ->
            val pk = current.pkBattleState ?: return@update current
            val updatedPk = if (teamIndex == 1) {
                pk.copy(team1Score = pk.team1Score + coins)
            } else {
                pk.copy(team2Score = pk.team2Score + coins)
            }
            current.copy(
                userProfile = current.userProfile.copy(coinsBalance = currentCoins - coins),
                pkBattleState = updatedPk
            )
        }
        showToast("⚔️ تم دعم الفريق ${if (teamIndex == 1) "الأزرق" else "الأحمر"} بـ ${formatCoins(coins)} 🪙!")
    }

    fun triggerVipEntrance(vipName: String) {
        _uiState.update { it.copy(vipEntranceBanner = vipName) }
        viewModelScope.launch {
            delay(4500)
            _uiState.update { it.copy(vipEntranceBanner = null) }
        }
    }

    fun likeMoment(id: String) {
        _uiState.update { current ->
            val updatedMoments = current.moments.map { m ->
                if (m.id == id) {
                    val isNowLiked = !m.isLiked
                    m.copy(
                        isLiked = isNowLiked,
                        likesCount = if (isNowLiked) m.likesCount + 1 else m.likesCount - 1
                    )
                } else m
            }
            current.copy(moments = updatedMoments)
        }
    }

    fun postMoment(content: String, tag: String) {
        val newMoment = MomentPost(
            id = "moment_${System.currentTimeMillis()}",
            authorName = _uiState.value.userProfile.name,
            authorAvatar = _uiState.value.userProfile.avatarEmoji,
            authorVip = _uiState.value.userProfile.vipLevel,
            content = content,
            tag = tag,
            likesCount = 1,
            isLiked = true,
            commentsCount = 0,
            timeAgo = "الآن"
        )
        _uiState.update { it.copy(moments = listOf(newMoment) + it.moments) }
        showToast("🌟 تم نشر لحظتك في مجتمع المجرة بنجاح!")
    }

    fun recordProfileVisit(visitor: ProfileVisitor) {
        _uiState.update { current ->
            current.copy(visitors = listOf(visitor) + current.visitors.filter { it.userId != visitor.userId })
        }
    }

    fun withdrawHostBeans(amountBeans: Long, method: String) {
        val currentBeans = _uiState.value.wallet.hostBeansBalance
        if (currentBeans < amountBeans) {
            showToast("رصيد الفاصولياء غير كافٍ للسحب! ⚠️")
            return
        }
        val usdAmount = amountBeans * _uiState.value.wallet.exchangeRateBeansToUsd
        _uiState.update { current ->
            val updatedWallet = current.wallet.copy(
                hostBeansBalance = currentBeans - amountBeans,
                totalEarningsUsd = current.wallet.totalEarningsUsd - usdAmount,
                pendingWithdrawalUsd = usdAmount,
                lastWithdrawalMethod = method
            )
            val newNotification = SystemNotification(
                id = "n_${System.currentTimeMillis()}",
                title = "💵 تم تأكيد طلب سحب الأرباح",
                message = "تمت معالجة سحب ${amountBeans} فاصولياء ($${String.format("%.2f", usdAmount)}) بنجاح عبر $method.",
                type = "RECHARGE",
                timestamp = "الآن"
            )
            current.copy(
                wallet = updatedWallet,
                notifications = listOf(newNotification) + current.notifications
            )
        }
        showToast("✅ تم تحويل $${String.format("%.2f", usdAmount)} بنجاح إلى محفظة $method!")
    }

    fun convertDiamondsToCoins(diamonds: Long) {
        val currentDiamonds = _uiState.value.wallet.diamondsBalance
        if (currentDiamonds < diamonds) {
            showToast("رصيد المرجان/الألماس غير كافٍ للتحويل! 💎")
            return
        }
        val coinsMultiplier = 1.05 // 5% bonus for in-app conversion
        val generatedCoins = (diamonds * coinsMultiplier).toLong()

        _uiState.update { current ->
            val updatedWallet = current.wallet.copy(
                diamondsBalance = currentDiamonds - diamonds,
                coinsBalance = current.wallet.coinsBalance + generatedCoins
            )
            val updatedProfile = current.userProfile.copy(
                coinsBalance = current.userProfile.coinsBalance + generatedCoins
            )
            current.copy(wallet = updatedWallet, userProfile = updatedProfile)
        }
        showToast("🪙 تم تحويل $diamonds مرجان إلى ${formatCoins(generatedCoins)} كوينز بنجاح مع بونص 5%!")
    }

    fun claimAgencyBonus(bonusCoins: Long) {
        _uiState.update { current ->
            val updatedProfile = current.userProfile.copy(
                coinsBalance = current.userProfile.coinsBalance + bonusCoins
            )
            val updatedWallet = current.wallet.copy(
                coinsBalance = current.wallet.coinsBalance + bonusCoins
            )
            current.copy(userProfile = updatedProfile, wallet = updatedWallet)
        }
        showToast("👑 تم استلام عمولة ومكافأة الوكالة بنجاح (${formatCoins(bonusCoins)} كوينز)!")
    }

    fun markNotificationAsRead(id: String) {
        _uiState.update { current ->
            val updated = current.notifications.map {
                if (it.id == id) it.copy(isRead = true) else it
            }
            current.copy(notifications = updated)
        }
    }

    fun sendDirectMessage(conversationId: String, text: String) {
        val newMsg = PrivateMessage(
            id = "pm_${System.currentTimeMillis()}",
            senderName = _uiState.value.userProfile.name,
            senderAvatar = _uiState.value.userProfile.avatarEmoji,
            text = text,
            isFromMe = true,
            timestamp = "الآن"
        )

        _uiState.update { current ->
            val updatedConversations = current.conversations.map { conv ->
                if (conv.id == conversationId) {
                    conv.copy(
                        lastMessage = text,
                        lastTime = "الآن",
                        messages = conv.messages + newMsg
                    )
                } else conv
            }
            current.copy(conversations = updatedConversations)
        }
    }

    private fun startVoiceEnergySimulation() {
        viewModelScope.launch {
            while (true) {
                delay(200)
                _uiState.update { current ->
                    val updatedSpeakers = current.currentRoom.speakers.map { sp ->
                        if (!sp.isMuted && (sp.isSpeaking || Random.nextFloat() > 0.65f)) {
                            sp.copy(
                                isSpeaking = true,
                                voiceEnergy = (Random.nextFloat() * 0.7f + 0.3f)
                            )
                        } else {
                            sp.copy(
                                isSpeaking = false,
                                voiceEnergy = 0f
                            )
                        }
                    }
                    current.copy(currentRoom = current.currentRoom.copy(speakers = updatedSpeakers))
                }
            }
        }
    }

    private fun startPkBattleTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { current ->
                    val pk = current.pkBattleState ?: return@update current
                    if (pk.remainingSeconds > 0) {
                        current.copy(
                            pkBattleState = pk.copy(remainingSeconds = pk.remainingSeconds - 1)
                        )
                    } else {
                        current.copy(
                            pkBattleState = pk.copy(isActive = false)
                        )
                    }
                }
            }
        }
    }

    private fun startLiveRoomAudienceSimulation() {
        viewModelScope.launch {
            val randomSenders = listOf(
                Pair("الأمير فيصل", "🤴"),
                Pair("سلطانة الفضاء", "👑"),
                Pair("ساهر الليل", "🌙"),
                Pair("نجم المجرة", "⭐"),
                Pair("كابتن كروزر", "🚀"),
                Pair("زهرة البنفسج", "🌸")
            )
            val randomTexts = listOf(
                "أحلى روم وأقوى مضيفين في السيرفر 👑✨",
                "ما شاء الله على الصوت الملكي الفخم 🎙️🔥",
                "مساء الورد والفل على الجميع 🌹",
                "تحدي الـ PK حامي ولعوها يا شباب ⚡🏆",
                "صوت المايك واضح ونقي جداً 💫",
                "تحية خاصة لكل الموجودين على المايكات القوسية 🪐"
            )

            while (true) {
                delay(Random.nextLong(7000, 14000))
                val sender = randomSenders.random()
                val text = randomTexts.random()
                val newMsg = ChatMessage(
                    id = "auto_${System.currentTimeMillis()}",
                    senderName = sender.first,
                    senderAvatar = sender.second,
                    senderVip = Random.nextInt(1, 11),
                    text = text,
                    timestamp = "الآن"
                )
                _uiState.update { it.copy(chatMessages = (it.chatMessages + newMsg).takeLast(40)) }
            }
        }
    }

    private fun showToast(message: String) {
        _uiState.update { it.copy(systemToastMessage = message) }
        viewModelScope.launch {
            delay(2800)
            _uiState.update { if (it.systemToastMessage == message) it.copy(systemToastMessage = null) else it }
        }
    }

    // --- Server RTC & Connectivity Actions ---
    fun openServerStatus(open: Boolean) {
        _uiState.update { it.copy(isServerStatusOpen = open) }
    }

    fun selectServerNode(node: ServerNode) {
        serverSyncEngine.switchServerNode(node) {
            showToast("🌐 تم التحويل بنجاح إلى: ${node.name} (${node.pingMs} ms)")
        }
    }

    fun reconnectServer() {
        serverSyncEngine.forceReconnect {
            showToast("⚡ تم فحص وتحديث الاتصال بأعلى سرعة!")
        }
    }

    // --- Audio RTC & Studio Effects Actions ---
    fun openAudioSettings(open: Boolean) {
        _uiState.update { it.copy(isAudioSettingsOpen = open) }
    }

    fun toggleAudioMute(): Boolean {
        val muted = realtimeAudioEngine.toggleMute()
        toggleMyMic()
        return muted
    }

    fun toggleNoiseSuppression() {
        realtimeAudioEngine.toggleNoiseSuppression()
        val isEnabled = realtimeAudioEngine.audioSettings.value.noiseSuppression
        showToast(if (isEnabled) "تم تفعيل عزل الضوضاء الذكي (AI Noise-Cancel) 🎧" else "تم إيقاف عزل الضوضاء")
    }

    fun toggleEchoCancellation() {
        realtimeAudioEngine.toggleEchoCancellation()
        val isEnabled = realtimeAudioEngine.audioSettings.value.echoCancellation
        showToast(if (isEnabled) "تم تفعيل منع صدى الصوت (AEC) 🔇" else "تم إيقاف منع صدى الصوت")
    }

    fun setMicGain(gain: Int) {
        realtimeAudioEngine.setMicGain(gain)
    }

    fun setVoiceEffect(effect: String) {
        realtimeAudioEngine.setVoiceEffect(effect)
        showToast("🎙️ تم تفعيل مؤثر الصوت: $effect")
    }

    // --- Community Safety & Google Play Moderation Reporting ---
    fun openReportDialog(targetUserId: String, targetUserName: String) {
        _uiState.update {
            it.copy(
                isReportDialogOpen = true,
                reportTargetUserId = targetUserId,
                reportTargetUserName = targetUserName
            )
        }
    }

    fun closeReportDialog() {
        _uiState.update { it.copy(isReportDialogOpen = false) }
    }

    fun submitUserReport(reason: ReportReason, notes: String, alsoBlock: Boolean) {
        val targetId = _uiState.value.reportTargetUserId
        val targetName = _uiState.value.reportTargetUserName
        val newReport = ModerationReport(
            id = "rep_${System.currentTimeMillis()}",
            reportedUserId = targetId,
            reportedUserName = targetName,
            reporterUserId = _uiState.value.userProfile.id,
            reason = reason,
            notes = notes
        )
        val newNotif = SystemNotification(
            id = "n_${System.currentTimeMillis()}",
            title = "🛡️ تم استلام البلاغ وحماية الحساب",
            message = "تم توثيق بلاغك ضد $targetName (${reason.titleAr}) وسيتم اتخاذ الإجراء الرادع فورياً.",
            type = "SECURITY",
            timestamp = "الآن"
        )
        _uiState.update { current ->
            current.copy(
                moderationReports = listOf(newReport) + current.moderationReports,
                notifications = listOf(newNotif) + current.notifications,
                isReportDialogOpen = false
            )
        }
        showToast("🛡️ شكراً لمساهمتك في أمان المجتمع! تم إرسال البلاغ والحظر بنجاح.")
    }

    // --- Cloud Auth & Account Linking ---
    fun openLinkedAccounts(open: Boolean) {
        _uiState.update { it.copy(isLinkedAccountsOpen = open) }
    }

    fun linkAccount(provider: String, identifier: String) {
        _uiState.update { current ->
            val updated = current.linkedAccounts.map { acc ->
                if (acc.provider == provider) acc.copy(isLinked = true, identifier = identifier, linkedAt = "الآن")
                else acc
            }
            val newNotif = SystemNotification(
                id = "n_${System.currentTimeMillis()}",
                title = "☁️ تم ربط ومزامنة الحساب السحابي",
                message = "تم ربط $provider ($identifier) بنجاح وحفظ كامل الرصيد والمستوى VIP في السحابة.",
                type = "SECURITY",
                timestamp = "الآن"
            )
            current.copy(
                linkedAccounts = updated,
                notifications = listOf(newNotif) + current.notifications,
                isLinkedAccountsOpen = false
            )
        }
        showToast("☁️ تم ربط الحساب ($provider) بنجاح وحفظ بياناتك في السحابة!")
    }

    // --- Soundboard & Room Audio Effects ---
    fun openSoundboard(open: Boolean) {
        _uiState.update { it.copy(isSoundboardOpen = open) }
    }

    fun playSoundEffect(sound: RoomSoundEffect) {
        showToast("🎶 تم تشغيل مؤثر: ${sound.name}")
    }
}
