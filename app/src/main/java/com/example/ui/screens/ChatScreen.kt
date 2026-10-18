package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.avatar.Avatar3DView
import com.example.avatar.AvatarState
import com.example.avatar.AvatarStateManager
import com.example.data.model.NovaPersona
import com.example.ui.components.ActionConfirmationDialog
import com.example.ui.components.ChatBubble
import com.example.ui.components.PromptStarterChips
import com.example.ui.components.ThinkingBubble
import com.example.ui.components.VoiceCommandDialog
import com.example.ui.components.getPersonaIcon
import com.example.ui.theme.NovaCosmicBg
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanGlow
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceCardBorder
import com.example.ui.theme.NovaSurfaceDark
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.viewmodel.NovaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: NovaViewModel,
    onNavigateToSessions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToDevices: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val messages by viewModel.currentMessages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val currentPersona by viewModel.currentPersona.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val autoReadVoice by viewModel.autoReadVoiceResponse.collectAsState()
    val lastVoiceCommand by viewModel.lastVoiceCommandText.collectAsState()
    val isVoiceListening by viewModel.isVoiceListening.collectAsState()
    val avatarState by viewModel.avatarState.collectAsState()
    val pendingAction by viewModel.pendingActionToConfirm.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showPersonaMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var isAvatarExpanded by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()

    // Speech Recognizer Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.isVoiceListening.value = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                viewModel.processVoiceCommand(spoken) { feedback ->
                    Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            AvatarStateManager.returnToIdle()
        }
    }

    fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Yesmin (Questions or Voice Commands)…")
        }
        try {
            viewModel.isVoiceListening.value = true
            AvatarStateManager.onUserStartedSpeaking()
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            viewModel.isVoiceListening.value = false
            AvatarStateManager.returnToIdle()
            showVoiceDialog = true
            Toast.makeText(context, "Voice command hub opened", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSpeechRecognition()
        } else {
            showVoiceDialog = true
            Toast.makeText(context, "Microphone permission required for voice", Toast.LENGTH_LONG).show()
        }
    }

    fun onMicClicked() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            startSpeechRecognition()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Auto scroll on new messages
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = NovaCosmicBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showPersonaMenu = true }
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .testTag("persona_dropdown_trigger")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(NovaCyan, NovaPurple))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getPersonaIcon(currentPersona.iconName),
                                contentDescription = currentPersona.title,
                                tint = NovaCosmicBg,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentSession?.title?.ifBlank { "Yesmin AI" } ?: "Yesmin AI",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NovaTextPrimary
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Persona",
                                    tint = NovaTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "${currentPersona.title} • 3D Avatar",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NovaCyan,
                                    fontSize = 10.sp
                                )
                            )
                        }

                        // Persona Dropdown
                        DropdownMenu(
                            expanded = showPersonaMenu,
                            onDismissRequest = { showPersonaMenu = false },
                            modifier = Modifier.background(NovaSurfaceCard)
                        ) {
                            NovaPersona.values().forEach { persona ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = persona.title,
                                                color = if (persona == currentPersona) NovaCyan else NovaTextPrimary,
                                                fontWeight = if (persona == currentPersona) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                text = persona.subtitle,
                                                style = MaterialTheme.typography.labelSmall.copy(color = NovaTextSecondary)
                                            )
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = getPersonaIcon(persona.iconName),
                                            contentDescription = null,
                                            tint = if (persona == currentPersona) NovaCyan else NovaTextSecondary
                                        )
                                    },
                                    onClick = {
                                        viewModel.setPersona(persona)
                                        showPersonaMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateToSessions,
                        modifier = Modifier.testTag("sessions_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Chat History",
                            tint = NovaCyan
                        )
                    }
                },
                actions = {
                    // Memory & Self-Correction Hub Button
                    IconButton(
                        onClick = onNavigateToMemory,
                        modifier = Modifier.testTag("memory_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "30MB Memory & Rules",
                            tint = NovaCyan
                        )
                    }

                    // Scheduled Tasks & Alarms Button
                    IconButton(
                        onClick = onNavigateToTasks,
                        modifier = Modifier.testTag("tasks_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Alarms & Reminders",
                            tint = NovaTextSecondary
                        )
                    }

                    // Settings & More
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = NovaTextSecondary
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMoreMenu = true },
                            modifier = Modifier.testTag("more_options_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = NovaTextSecondary
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            modifier = Modifier.background(NovaSurfaceCard)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Smart Devices & Bluetooth", color = NovaTextPrimary) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Headphones,
                                        contentDescription = null,
                                        tint = NovaCyan
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    onNavigateToDevices()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Voice Commands Hub", color = NovaTextPrimary) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = NovaCyan
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    showVoiceDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Clear Current Chat", color = NovaTextPrimary) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = null,
                                        tint = NovaTextSecondary
                                    )
                                },
                                onClick = {
                                    viewModel.clearCurrentChat()
                                    showMoreMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NovaCosmicBg)
            )
        },
        bottomBar = {
            // Chat & Voice Input Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NovaCosmicBg)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice Mic Button
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isVoiceListening) NovaCyan else Color(0xFF1E293B))
                            .border(1.dp, if (isVoiceListening) NovaCyanGlow else NovaSurfaceCardBorder, CircleShape)
                            .clickable { onMicClicked() }
                            .testTag("voice_command_mic_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Commands",
                            tint = if (isVoiceListening) NovaCosmicBg else NovaCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        placeholder = {
                            Text(
                                text = "Ask Yesmin or give voice command…",
                                color = NovaTextSecondary,
                                fontSize = 14.sp
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NovaCyan,
                            unfocusedBorderColor = NovaSurfaceCardBorder,
                            focusedContainerColor = NovaSurfaceDark,
                            unfocusedContainerColor = NovaSurfaceDark,
                            focusedTextColor = NovaTextPrimary,
                            unfocusedTextColor = NovaTextPrimary
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val canSend = inputText.isNotBlank() && !isGenerating

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    if (canSend) listOf(NovaCyan, NovaCyanGlow)
                                    else listOf(Color(0xFF1E293B), Color(0xFF1E293B))
                                )
                            )
                            .clickable(enabled = canSend || isGenerating) {
                                if (canSend) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                            }
                            .testTag("send_message_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isGenerating) Icons.Default.Stop else Icons.AutoMirrored.Filled.Send,
                            contentDescription = if (isGenerating) "Generating" else "Send Message",
                            tint = if (canSend) NovaCosmicBg else NovaTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // PROMINENT 3D YESMIN AVATAR ON HOME SCREEN
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Avatar3DView(
                    state = avatarState,
                    heightDp = if (isAvatarExpanded) 190 else 100,
                    modifier = Modifier.fillMaxWidth()
                )

                // Minimize/Maximize toggle button for 3D Avatar
                IconButton(
                    onClick = { isAvatarExpanded = !isAvatarExpanded },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(30.dp)
                        .testTag("toggle_avatar_size_button")
                ) {
                    Icon(
                        imageVector = if (isAvatarExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Avatar Size",
                        tint = NovaTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (messages.isEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Quick Action Presets
                        item {
                            Column {
                                Text(
                                    text = "Supported Voice & External Actions",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = NovaTextSecondary,
                                        letterSpacing = 0.5.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    items(listOf(
                                        "💬 WhatsApp 'Hi' to Rahul" to "Rahul ko WhatsApp par Hi message karo",
                                        "⏰ Set Alarm 6:30 AM" to "Set an alarm for 6:30 AM",
                                        "📅 Calendar Meeting" to "Schedule a meeting with Rahul on Monday at 3 PM",
                                        "🔔 Remind Tomorrow 8 AM" to "Tomorrow at 8 AM remind me to study",
                                        "🎧 Bluetooth Audio" to "Show my connected Bluetooth devices"
                                    )) { (label, prompt) ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(NovaSurfaceDark)
                                                .border(1.dp, NovaSurfaceCardBorder, RoundedCornerShape(12.dp))
                                                .clickable { viewModel.sendMessage(prompt) }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = NovaCyan,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Persona Selector Row
                        item {
                            Column {
                                Text(
                                    text = "Active Persona",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = NovaTextSecondary
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    items(NovaPersona.values()) { persona ->
                                        val isSelected = persona == currentPersona
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) Color(0xFF1E293B) else NovaSurfaceDark)
                                                .border(
                                                    1.dp,
                                                    if (isSelected) NovaCyan else NovaSurfaceCardBorder,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable { viewModel.setPersona(persona) }
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = getPersonaIcon(persona.iconName),
                                                contentDescription = null,
                                                tint = if (isSelected) NovaCyan else NovaTextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = persona.title,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) NovaCyan else NovaTextPrimary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Suggested Prompt Starter Chips
                        item {
                            PromptStarterChips(
                                persona = currentPersona,
                                onPromptSelected = { selectedPrompt ->
                                    viewModel.sendMessage(selectedPrompt)
                                }
                            )
                        }
                    }
                } else {
                    // Chat Messages List
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            ChatBubble(
                                message = msg,
                                onSpeak = { text -> viewModel.speakText(text) },
                                onRetry = if (msg.isError) {
                                    { viewModel.retryLastMessage() }
                                } else null
                            )
                        }

                        if (isGenerating) {
                            item {
                                ThinkingBubble()
                            }
                        }
                    }
                }
            }
        }
    }

    // Voice Command Dialog Hub
    if (showVoiceDialog) {
        VoiceCommandDialog(
            isListening = isVoiceListening,
            lastHeardText = lastVoiceCommand,
            autoSpeakResponse = autoReadVoice,
            onAutoSpeakChange = { viewModel.autoReadVoiceResponse.value = it },
            onStartListening = { onMicClicked() },
            onExecuteCommand = { cmd ->
                viewModel.processVoiceCommand(cmd) { feedback ->
                    Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = {
                showVoiceDialog = false
                AvatarStateManager.returnToIdle()
            }
        )
    }

    // Action Confirmation Preview Dialog (WhatsApp, SMS, Alarms, Reminders)
    pendingAction?.let { validation ->
        ActionConfirmationDialog(
            validationResult = validation,
            onConfirm = { confirmedAction ->
                viewModel.confirmAndExecuteAction(confirmedAction, app)
            },
            onDismiss = {
                viewModel.dismissPendingAction()
            }
        )
    }
}
