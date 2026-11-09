package com.codegarden.app.data.local

import android.content.Context
import com.codegarden.app.data.model.AppState
import com.codegarden.app.data.model.CareLog
import com.codegarden.app.data.model.CareType
import com.codegarden.app.data.model.MyPlant
import com.codegarden.app.data.model.Settings
import com.codegarden.app.data.model.TRASH_RETENTION_DAYS
import com.codegarden.app.data.model.TrashedPlant
import com.codegarden.app.data.model.UserProfile
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** 本地持久化：把 AppState 序列化为 JSON 存入 SharedPreferences，完全离线 */
class StateStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("code_garden_state", Context.MODE_PRIVATE)

    fun save(state: AppState) {
        prefs.edit().putString(KEY, toJson(state).toString()).apply()
    }

    fun load(): AppState? {
        val raw = prefs.getString(KEY, null) ?: return null
        return try {
            fromJson(JSONObject(raw))
        } catch (e: Exception) {
            null
        }
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    // ---------- 序列化 ----------

    private fun toJson(s: AppState): JSONObject = JSONObject().apply {
        put("user", s.user?.let { u ->
            JSONObject().apply {
                put("nickname", u.nickname)
                put("avatar", u.avatar)
            }
        } ?: JSONObject.NULL)
        put("plants", JSONArray().apply {
            s.plants.forEach { p -> put(plantToJson(p)) }
        })
        put("logs", JSONArray().apply {
            s.logs.forEach { l ->
                put(JSONObject().apply {
                    put("id", l.id)
                    put("plantId", l.plantId)
                    put("type", l.type.name)
                    put("date", l.date.toString())
                    put("epochMillis", l.epochMillis)
                    put("note", l.note)
                    put("amount", l.amount)
                    put("photoPath", l.photoPath ?: JSONObject.NULL)
                })
            }
        })
        put("settings", JSONObject().apply {
            put("dailyReminder", s.settings.dailyReminder)
            put("dailyReminderTime", s.settings.dailyReminderTime)
            put("overdueReminder", s.settings.overdueReminder)
            put("overdueReminderTime", s.settings.overdueReminderTime)
            put("darkMode", s.settings.darkMode)
            put("weatherCity", s.settings.weatherCity)
            put("weatherLat", s.settings.weatherLat)
            put("weatherLon", s.settings.weatherLon)
            put("weatherAuto", s.settings.weatherAuto)
            put("weatherAsked", s.settings.weatherAsked)
        })
        put("trash", JSONArray().apply {
            s.trash.forEach { t ->
                put(JSONObject().apply {
                    put("plant", plantToJson(t.plant))
                    put("deletedAt", t.deletedAt)
                    put("expiresAt", t.expiresAt)
                })
            }
        })
    }

    private fun plantToJson(p: MyPlant): JSONObject = JSONObject().apply {
        put("id", p.id)
        put("speciesId", p.speciesId ?: JSONObject.NULL)
        put("speciesName", p.speciesName)
        put("emoji", p.emoji)
        put("nickname", p.nickname)
        put("photoPath", p.photoPath ?: JSONObject.NULL)
        put("addedDate", p.addedDate.toString())
        put("lastWatered", p.lastWatered?.toString() ?: JSONObject.NULL)
        put("lastFertilized", p.lastFertilized?.toString() ?: JSONObject.NULL)
        put("waterDays", p.waterDays)
        put("fertilizeDays", p.fertilizeDays)
        put("note", p.note)
    }

    private fun plantFromJson(p: JSONObject): MyPlant = MyPlant(
        id = p.getString("id"),
        speciesId = if (p.isNull("speciesId")) null else p.optInt("speciesId"),
        speciesName = p.getString("speciesName"),
        emoji = p.getString("emoji"),
        nickname = p.optString("nickname", ""),
        photoPath = p.optStringOrNull("photoPath"),
        addedDate = LocalDate.parse(p.getString("addedDate")),
        lastWatered = p.optStringOrNull("lastWatered")?.let(LocalDate::parse),
        lastFertilized = p.optStringOrNull("lastFertilized")?.let(LocalDate::parse),
        waterDays = p.getInt("waterDays"),
        fertilizeDays = p.getInt("fertilizeDays"),
        note = p.optString("note", "")
    )

    private fun fromJson(o: JSONObject): AppState {
        val user = o.optJSONObject("user")?.let { u ->
            UserProfile(nickname = u.getString("nickname"), avatar = u.getString("avatar"))
        }
        val plants = o.getJSONArray("plants").let { arr ->
            (0 until arr.length()).map { i -> plantFromJson(arr.getJSONObject(i)) }
        }
        val logs = o.getJSONArray("logs").let { arr ->
            (0 until arr.length()).map { i ->
                val l = arr.getJSONObject(i)
                CareLog(
                    id = l.getString("id"),
                    plantId = l.getString("plantId"),
                    type = CareType.valueOf(l.getString("type")),
                    date = LocalDate.parse(l.getString("date")),
                    epochMillis = l.getLong("epochMillis"),
                    note = l.optString("note", ""),
                    amount = l.optString("amount", ""),
                    photoPath = l.optStringOrNull("photoPath")
                )
            }
        }
        val settings = o.getJSONObject("settings").let { st ->
            Settings(
                dailyReminder = st.getBoolean("dailyReminder"),
                dailyReminderTime = st.getString("dailyReminderTime"),
                overdueReminder = st.getBoolean("overdueReminder"),
                overdueReminderTime = st.getString("overdueReminderTime"),
                darkMode = st.optBoolean("darkMode", false),
                weatherCity = st.optString("weatherCity", ""),
                weatherLat = st.optDouble("weatherLat", 0.0),
                weatherLon = st.optDouble("weatherLon", 0.0),
                weatherAuto = st.optBoolean("weatherAuto", true),
                weatherAsked = st.optBoolean("weatherAsked", false)
            )
        }
        val trash = o.optJSONArray("trash")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                val t = arr.optJSONObject(i) ?: return@mapNotNull null
                val p = t.optJSONObject("plant") ?: return@mapNotNull null
                TrashedPlant(
                    plant = plantFromJson(p),
                    deletedAt = t.optLong("deletedAt", System.currentTimeMillis()),
                    expiresAt = t.optLong(
                        "expiresAt",
                        t.optLong("deletedAt", System.currentTimeMillis()) + TRASH_RETENTION_DAYS * 86_400_000L
                    )
                )
            }
        } ?: emptyList()
        return AppState(
            user = user,
            plants = plants,
            logs = logs,
            settings = settings,
            trash = trash
        )
    }

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (isNull(key) || !has(key)) null else optString(key).takeIf { it.isNotEmpty() && it != "null" }

    companion object {
        private const val KEY = "plant_care_state_v1"
    }
}