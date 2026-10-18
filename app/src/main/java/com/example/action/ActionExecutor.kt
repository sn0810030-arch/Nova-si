package com.example.action

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.Settings
import com.example.receiver.ReminderBroadcastReceiver
import java.net.URLEncoder

object ActionExecutor {

    fun executeSendMessage(context: Context, action: ParsedAction.SendMessage): Result<String> {
        return try {
            when (action.app) {
                TargetApp.WHATSAPP -> {
                    // Try WhatsApp send intent
                    val encoded = URLEncoder.encode(action.exactMessage, "UTF-8")
                    val uri = Uri.parse("https://api.whatsapp.com/send?text=$encoded")
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.whatsapp")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                        Result.success("Opening WhatsApp to send exact message to ${action.recipient}.")
                    } else {
                        // Fallback to general share intent
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, action.exactMessage)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Send via").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        Result.success("WhatsApp not installed. Opened share sheet with exact message.")
                    }
                }
                TargetApp.SMS -> {
                    val smsUri = Uri.parse("smsto:")
                    val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                        putExtra("sms_body", action.exactMessage)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(smsIntent)
                    Result.success("Opening Messages app to send exact text to ${action.recipient}.")
                }
                TargetApp.EMAIL -> {
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_SUBJECT, "Message from Nova AI")
                        putExtra(Intent.EXTRA_TEXT, action.exactMessage)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(emailIntent)
                    Result.success("Opening Email app with exact draft for ${action.recipient}.")
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun executeSetAlarm(context: Context, action: ParsedAction.SetAlarm): Result<String> {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, action.hour)
                putExtra(AlarmClock.EXTRA_MINUTES, action.minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, action.label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            val amPm = if (action.hour >= 12) "PM" else "AM"
            val displayHour = if (action.hour % 12 == 0) 12 else action.hour % 12
            val displayMin = String.format("%02d", action.minute)
            Result.success("Alarm set for $displayHour:$displayMin $amPm.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun executeCreateCalendarEvent(context: Context, action: ParsedAction.CreateCalendarEvent): Result<String> {
        return try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, action.title)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, action.startEpochMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, action.endEpochMillis)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Result.success("Calendar event \"${action.title}\" ready for confirmation in your Calendar app.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun scheduleAlarmReminder(context: Context, taskId: String, title: String, triggerMillis: Long): Result<String> {
        return try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                putExtra("TASK_ID", taskId)
                putExtra("TASK_TITLE", title)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
            Result.success("Scheduled reminder for \"$title\".")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getConnectedAudioDevices(context: Context): List<String> {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = mutableListOf<String>()
        val deviceList = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (dev in deviceList) {
            when (dev.type) {
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> devices.add("Bluetooth Headphones / Speaker: ${dev.productName}")
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> devices.add("Bluetooth Headset: ${dev.productName}")
                AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> devices.add("Wired Headphones")
                AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> devices.add("Phone Speaker")
            }
        }
        return devices.distinct()
    }

    fun openBluetoothSettings(context: Context) {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
