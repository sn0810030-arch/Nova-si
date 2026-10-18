package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiClient
import com.example.ui.theme.NovaCosmicBg
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceCardBorder
import com.example.ui.theme.NovaSurfaceDark
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaWarning
import com.example.ui.viewmodel.NovaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToDevices: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentKey by viewModel.apiKey.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val temperature by viewModel.temperature.collectAsState()
    val ttsEnabled by viewModel.ttsEnabled.collectAsState()
    val testResult by viewModel.connectionTestResult.collectAsState()
    val isTesting by viewModel.isTestingConnection.collectAsState()
    val usedBytes by viewModel.memoryUsedBytes.collectAsState()
    val memories by viewModel.allMemories.collectAsState()
    val corrections by viewModel.allCorrections.collectAsState()

    var keyInput by remember(currentKey) { mutableStateOf(currentKey) }
    var tempSlider by remember(temperature) { mutableFloatStateOf(temperature) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val usedKb = usedBytes / 1024f
    val memoryProgress = (usedKb / (30f * 1024f)).coerceIn(0f, 1f)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaCosmicBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NovaTextPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NovaCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NovaCosmicBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: 30 MB Long-Term Memory & Mistake Rules
            SettingsCard(
                title = "30 MB Long-Term Memory",
                icon = Icons.Default.Psychology
            ) {
                Text(
                    text = "Persistent storage that survives app restarts, reboots, and updates. Remembers user preferences, project facts, and mistake correction rules.",
                    style = MaterialTheme.typography.bodySmall.copy(color = NovaTextSecondary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Storage Budget Used",
                        style = MaterialTheme.typography.bodySmall.copy(color = NovaTextPrimary, fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "${String.format("%.2f", usedKb)} KB / 30 MB",
                        style = MaterialTheme.typography.labelSmall.copy(color = NovaCyan, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { memoryProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = NovaCyan,
                    trackColor = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onNavigateToMemory,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = NovaCyan
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("open_memory_hub_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Manage Saved Facts & Correction Rules (${memories.size + corrections.size})")
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Section 2: Scheduled Tasks & Alarms
            SettingsCard(
                title = "Background Tasks & Reminders",
                icon = Icons.Default.Alarm
            ) {
                Text(
                    text = "Scheduled reminders and alarms set via voice commands using Android AlarmManager.",
                    style = MaterialTheme.typography.bodySmall.copy(color = NovaTextSecondary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onNavigateToTasks,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = NovaCyan
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("open_tasks_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("View Scheduled Tasks & Alarms")
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Section 3: Bluetooth & Smart Devices
            SettingsCard(
                title = "Bluetooth & Smart Devices",
                icon = Icons.Default.Headphones
            ) {
                Text(
                    text = "Manage Bluetooth audio routing to headphones/speakers, and control smart home devices.",
                    style = MaterialTheme.typography.bodySmall.copy(color = NovaTextSecondary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onNavigateToDevices,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = NovaCyan
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("open_devices_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Open Devices & Audio Control")
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Section 4: Gemini API Configuration
            SettingsCard(
                title = "Google Gemini AI Engine",
                icon = Icons.Default.Key
            ) {
                val effectiveKey = GeminiClient.getEffectiveApiKey(keyInput)
                val isKeyActive = effectiveKey != null

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isKeyActive) NovaSuccess else NovaWarning)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isKeyActive) "Engine Status: Live Cloud Active" else "Engine Status: Intelligent Offline Mode",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (isKeyActive) NovaSuccess else NovaWarning
                        )
                    )
                }

                Text(
                    text = "Enter your Google Gemini API key to activate live cloud responses. Leave empty to use Yesmin's offline responder.",
                    style = MaterialTheme.typography.bodySmall.copy(color = NovaTextSecondary)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("AIzaSy…") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaCyan,
                        unfocusedBorderColor = NovaSurfaceCardBorder,
                        focusedTextColor = NovaTextPrimary,
                        unfocusedTextColor = NovaTextPrimary,
                        focusedLabelColor = NovaCyan,
                        unfocusedLabelColor = NovaTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("api_key_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.saveApiKey(keyInput)
                            Toast.makeText(context, "API Key saved", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NovaCyan,
                            contentColor = NovaCosmicBg
                        ),
                        modifier = Modifier.weight(1f).testTag("save_api_key_button")
                    ) {
                        Text("Save Key")
                    }

                    Button(
                        onClick = { viewModel.testConnection() },
                        enabled = !isTesting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = NovaCyan
                        ),
                        modifier = Modifier.weight(1f).testTag("test_api_key_button")
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = NovaCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NetworkCheck,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Ping")
                            }
                        }
                    }
                }

                testResult?.let { result ->
                    Spacer(modifier = Modifier.height(10.dp))
                    val isSuccess = result.startsWith("Success")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSuccess) Color(0x2210B981) else Color(0x22EF4444))
                            .border(1.dp, if (isSuccess) NovaSuccess else NovaError, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isSuccess) NovaSuccess else NovaError
                            )
                        )
                    }
                }
            }

            // Section 5: Model Selection
            SettingsCard(
                title = "Model Selection",
                icon = Icons.Default.AutoAwesome
            ) {
                GeminiClient.AVAILABLE_MODELS.forEach { (modelId, displayName) ->
                    val isSelected = selectedModel == modelId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF1E293B) else Color.Transparent)
                            .clickable { viewModel.setModel(modelId) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.setModel(modelId) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = NovaCyan,
                                unselectedColor = NovaTextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = NovaTextPrimary
                                )
                            )
                            Text(
                                text = modelId,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NovaCyan,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // Section 6: Generation Tuning
            SettingsCard(
                title = "Creativity & Temperature",
                icon = Icons.Default.Tune
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Temperature", style = MaterialTheme.typography.bodyMedium.copy(color = NovaTextPrimary))
                    Text(String.format("%.1f", tempSlider), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = NovaCyan))
                }

                Slider(
                    value = tempSlider,
                    onValueChange = {
                        tempSlider = it
                        viewModel.setTemperature(it)
                    },
                    valueRange = 0.0f..1.0f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = NovaCyan,
                        activeTrackColor = NovaCyan,
                        inactiveTrackColor = NovaSurfaceCardBorder
                    ),
                    modifier = Modifier.testTag("temperature_slider")
                )
            }

            // Section 7: Voice Readout
            SettingsCard(
                title = "Voice Readout & TTS",
                icon = Icons.AutoMirrored.Filled.VolumeUp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Readout for Voice",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = NovaTextPrimary
                            )
                        )
                        Text(
                            text = "Speaks answers aloud with 3D avatar mouth sync",
                            style = MaterialTheme.typography.bodySmall.copy(color = NovaTextSecondary)
                        )
                    }

                    Switch(
                        checked = ttsEnabled,
                        onCheckedChange = { viewModel.setTemperature(if (it) 0.7f else 0.5f) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NovaCosmicBg,
                            checkedTrackColor = NovaCyan,
                            uncheckedThumbColor = NovaTextSecondary,
                            uncheckedTrackColor = NovaSurfaceCardBorder
                        )
                    )
                }
            }

            // Section 8: Data Management
            SettingsCard(
                title = "Data Management",
                icon = Icons.Default.DeleteForever
            ) {
                Button(
                    onClick = { showClearConfirm = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x33EF4444),
                        contentColor = NovaError
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("clear_all_data_button")
                ) {
                    Text("Clear All Chats & Reset Memory")
                }
            }

            // Section 9: About
            SettingsCard(
                title = "About Yesmin / Nova AI",
                icon = Icons.Default.Info
            ) {
                Text(
                    text = "Yesmin AI (Nova Engine) v2.0.0",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NovaTextPrimary
                    )
                )
                Text(
                    text = "Equipped with real-time 3D expressive avatar, 30MB persistent SQLite memory, self-correction learning, exact action execution, AlarmManager background tasks, and Bluetooth audio controls.",
                    style = MaterialTheme.typography.bodySmall.copy(color = NovaTextSecondary)
                )
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = NovaSurfaceCard,
            title = {
                Text(
                    text = "Reset All Data?",
                    color = NovaTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will wipe all conversation threads, 30MB long-term facts, and correction rules.",
                    color = NovaTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirm = false
                        Toast.makeText(context, "All data reset", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaError,
                        contentColor = Color.White
                    )
                ) {
                    Text("Reset All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = NovaTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NovaSurfaceDark)
            .border(1.dp, NovaSurfaceCardBorder, RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NovaCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NovaTextPrimary
                    )
                )
            }

            content()
        }
    }
}
