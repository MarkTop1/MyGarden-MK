package com.codegarden.app.data

import android.content.Context
import com.codegarden.app.data.model.PlantCategory
import com.codegarden.app.data.model.PlantSpecies
import org.json.JSONArray

/** 内置 500 种常见盆栽图鉴，从 assets/plants.json 加载，进程内缓存一次 */
object PlantCatalog {

    private var cached: List<PlantSpecies>? = null

    fun load(context: Context): List<PlantSpecies> {
        cached?.let { return it }
        val list = try {
            val json = context.assets.open("plants.json").bufferedReader().use { it.readText() }
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                PlantSpecies(
                    id = o.getInt("id"),
                    name = o.getString("name"),
                    category = PlantCategory.fromKey(o.getString("category")),
                    emoji = o.getString("emoji"),
                    waterDays = o.getInt("waterDays"),
                    fertilizeDays = o.getInt("fertilizeDays"),
                    light = o.getString("light"),
                    difficulty = o.getString("difficulty"),
                    description = o.getString("description")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
        cached = list
        return list
    }
}