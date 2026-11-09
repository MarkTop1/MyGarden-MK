package com.codegarden.app.notify

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings as AndroidSettings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.codegarden.app.MainActivity
import com.codegarden.app.R
import com.codegarden.app.data.CareEngine
import com.codegarden.app.data.local.StateStore
import com.codegarden.app.data.model.Settings
import java.time.LocalDate
import java.util.Calendar

/** 本地通知：每日养护汇总 / 逾期预警。优先使用精确闹钟，像闹钟一样准点提醒 */
object Reminders {

    const val CHANNEL_ID = "garden_reminders"
    const val TYPE_DAILY = 1001
    const val TYPE_OVERDUE = 1002
    private const val NOTIFY_DAILY = 2001
    private const val NOTIFY_OVERDUE = 2002

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "养护提醒", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "每日浇水施肥提醒与逾期预警"
                enableVibration(true)
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun scheduleAll(context: Context, settings: Settings) {
        val am = context.getSystemService(AlarmManager::class.java)
        scheduleOne(am, context, TYPE_DAILY, settings.dailyReminder, settings.dailyReminderTime)
        scheduleOne(am, context, TYPE_OVERDUE, settings.overdueReminder, settings.overdueReminderTime)
    }

    fun cancelAll(context: Context) {
        val am = context.getSystemService(AlarmManager::class.java)
        am.cancel(pendingIntent(context, TYPE_DAILY))
        am.cancel(pendingIntent(context, TYPE_OVERDUE))
    }

    private fun scheduleOne(am: AlarmManager, context: Context, type: Int, enabled: Boolean, time: String) {
        val pi = pendingIntent(context, type)
        am.cancel(pi)
        if (!enabled) return
        val trigger = nextTrigger(time)
        val exact = canScheduleExact(context)
        try {
            if (exact) {
                // 精确闹钟：到点立即触发，Doze 休眠下也尽量准点
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
            }
        } catch (e: SecurityException) {
            // 精确闹钟权限被回收时降级，保证仍有提醒
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
        }
    }

    private fun pendingIntent(context: Context, type: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).putExtra("type", type)
        return PendingIntent.getBroadcast(
            context, type, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** 下一个 HH:mm 触发点（今天已过则顺延到明天） */
    private fun nextTrigger(time: String): Long {
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) cal.add(Calendar.DAY_OF_YEAR, 1)
        return cal.timeInMillis
    }

    /** 是否允许精确闹钟（Android 12+ 需要用户授权，其它版本默认允许） */
    fun canScheduleExact(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    /** 是否已获得通知权限 */
    fun canPostNotifications(context: Context): Boolean = canPost(context)

    private fun canPost(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    /** 跳系统设置：允许精确闹钟（Android 12+） */
    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val intent = Intent(
            AndroidSettings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }.onFailure {
            runCatching {
                context.startActivity(
                    Intent(AndroidSettings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    /** 跳系统设置：应用通知开关 */
    fun openNotificationSettings(context: Context) {
        val intent = Intent(AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(AndroidSettings.EXTRA_APP_PACKAGE, context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }.onFailure {
            runCatching {
                context.startActivity(
                    Intent(AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:${context.packageName}"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    private fun contentIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun post(context: Context, id: Int, title: String, body: String) {
        if (!canPost(context)) return
        createChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent(context))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(id, notification)
    }

    /** 按当前状态发一条汇总通知：每日提醒准点发，逾期预警有逾期才发 */
    fun postDueSummary(context: Context, type: Int) {
        val state = StateStore(context).load() ?: return
        if (state.plants.isEmpty()) return
        val summary = CareEngine.dueSummary(state.plants, LocalDate.now())
        when (type) {
            TYPE_DAILY -> {
                val parts = mutableListOf<String>()
                if (summary.needWater > 0) parts.add("${summary.needWater} 盆待浇水")
                if (summary.needFertilize > 0) parts.add("${summary.needFertilize} 盆待施肥")
                val body = if (parts.isEmpty()) {
                    "今天暂时没有需要养护的植物，去花园看看吧～"
                } else {
                    parts.joinToString("，") + "，去花园看看吧～"
                }
                post(context, NOTIFY_DAILY, "🌿 今日养护清单", body)
            }
            TYPE_OVERDUE -> {
                if (summary.overdue == 0) return
                post(
                    context, NOTIFY_OVERDUE, "🌵 逾期预警",
                    "有 ${summary.overdue} 盆植物已经逾期未养护，快救救它们！"
                )
            }
        }
    }
}

/** 闹钟触发：按当前状态发汇总提醒，再重调度到明天 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val state = StateStore(context).load() ?: return
        val type = intent.getIntExtra("type", Reminders.TYPE_DAILY)
        val enabled = when (type) {
            Reminders.TYPE_OVERDUE -> state.settings.overdueReminder
            else -> state.settings.dailyReminder
        }
        if (enabled) Reminders.postDueSummary(context, type)
        Reminders.scheduleAll(context, state.settings)
    }
}

/** 开机后按已保存的设置重调度提醒 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val state = StateStore(context).load() ?: return
            Reminders.createChannel(context)
            Reminders.scheduleAll(context, state.settings)
        }
    }
}