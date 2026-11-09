package com.codegarden.app.data

import android.content.Context
import com.codegarden.app.data.local.StateStore
import com.codegarden.app.data.model.AppState
import com.codegarden.app.data.model.PlantSpecies

/** 数据仓库：本地持久化 + 内置图鉴，ViewModel 与通知共用 */
class PlantRepository(context: Context) {

    private val store = StateStore(context)
    val catalog: List<PlantSpecies> = PlantCatalog.load(context)

    fun load(): AppState? = store.load()
    fun save(state: AppState) = store.save(state)
    fun clear() = store.clear()

    fun speciesById(id: Int?): PlantSpecies? =
        if (id == null) null else catalog.firstOrNull { it.id == id }
}