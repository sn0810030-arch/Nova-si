package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChatSessionEntity
import com.example.ui.components.getPersonaIcon
import com.example.ui.theme.NovaCosmicBg
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceCardBorder
import com.example.ui.theme.NovaSurfaceDark
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.viewmodel.NovaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    onSessionSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.filteredSessions.collectAsState()
    val activeSessionId by viewModel.currentSessionId.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    var sessionToRename by remember { mutableStateOf<ChatSessionEntity?>(null) }
    var renameText by remember { mutableStateOf("") }
    var sessionToDelete by remember { mutableStateOf<ChatSessionEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaCosmicBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Conversations",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NovaTextPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NovaCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NovaCosmicBg
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.createNewSession()
                    onSessionSelected()
                },
                containerColor = NovaCyan,
                contentColor = NovaCosmicBg,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_new_chat")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Conversation",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("search_sessions_input"),
                placeholder = { Text("Search chats…", color = NovaTextSecondary) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NovaCyan
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NovaCyan,
                    unfocusedBorderColor = NovaSurfaceCardBorder,
                    focusedContainerColor = NovaSurfaceDark,
                    unfocusedContainerColor = NovaSurfaceDark,
                    focusedTextColor = NovaTextPrimary,
                    unfocusedTextColor = NovaTextPrimary
                )
            )

            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (query.isBlank()) "No conversations yet" else "No matching chats found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = NovaTextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap + below to start a new chat with Nova AI",
                            style = MaterialTheme.typography.bodySmall.copy(color = NovaTextSecondary)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sessions, key = { it.id }) { session ->
                        val isSelected = session.id == activeSessionId
                        val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                        val formattedDate = dateFormat.format(Date(session.updatedAt))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0xFF1E293B) else NovaSurfaceDark)
                                .clickable {
                                    viewModel.selectSession(session.id)
                                    onSessionSelected()
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("session_item_${session.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) NovaCyan else Color(0xFF1F2937)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getPersonaIcon(session.persona),
                                    contentDescription = session.persona,
                                    tint = if (isSelected) NovaCosmicBg else NovaCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = session.title,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = NovaTextPrimary
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (session.isPinned) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.PushPin,
                                            contentDescription = "Pinned",
                                            tint = NovaCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = NovaTextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            // Actions
                            IconButton(
                                onClick = { viewModel.togglePinSession(session.id, session.isPinned) },
                                modifier = Modifier.size(32.dp).testTag("pin_session_${session.id}")
                            ) {
                                Icon(
                                    imageVector = if (session.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = "Pin session",
                                    tint = if (session.isPinned) NovaCyan else NovaTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    sessionToRename = session
                                    renameText = session.title
                                },
                                modifier = Modifier.size(32.dp).testTag("rename_session_${session.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Rename",
                                    tint = NovaTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = { sessionToDelete = session },
                                modifier = Modifier.size(32.dp).testTag("delete_session_${session.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = NovaError,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename Dialog
    sessionToRename?.let { target ->
        AlertDialog(
            onDismissRequest = { sessionToRename = null },
            containerColor = NovaSurfaceCard,
            title = {
                Text(
                    text = "Rename Conversation",
                    color = NovaTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaCyan,
                        unfocusedBorderColor = NovaSurfaceCardBorder,
                        focusedTextColor = NovaTextPrimary,
                        unfocusedTextColor = NovaTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("rename_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameSession(target.id, renameText)
                        sessionToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaCyan, contentColor = NovaCosmicBg),
                    modifier = Modifier.testTag("confirm_rename_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToRename = null }) {
                    Text("Cancel", color = NovaTextSecondary)
                }
            }
        )
    }

    // Delete Confirmation Dialog
    sessionToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            containerColor = NovaSurfaceCard,
            title = {
                Text(
                    text = "Delete Conversation?",
                    color = NovaTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete \"${target.title}\" and all its messages?",
                    color = NovaTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSession(target.id)
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaError, contentColor = Color.White),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Cancel", color = NovaTextSecondary)
                }
            }
        )
    }
}
