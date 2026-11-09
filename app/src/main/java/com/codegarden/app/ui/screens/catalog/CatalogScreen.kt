package com.codegarden.app.ui.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import com.codegarden.app.ui.components.DialogAction
import com.codegarden.app.ui.components.GardenDialog
import com.codegarden.app.ui.components.GardenIcons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codegarden.app.data.model.PlantCategory
import com.codegarden.app.data.model.PlantSpecies
import com.codegarden.app.ui.components.SpeciesImage
import com.codegarden.app.ui.theme.InkDark
import com.codegarden.app.ui.theme.InkSecondary
import com.codegarden.app.ui.theme.MintContainer
import com.codegarden.app.ui.theme.MintDeep
import com.codegarden.app.ui.theme.WarmBg
import com.codegarden.app.viewmodel.MainViewModel

/** Tab2 图鉴：内置 500 种常见盆栽，搜索 + 分类筛选，点开查看养护要点并可一键添加 */
@Composable
fun CatalogScreen(vm: MainViewModel, onAddSpecies: (Int) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<PlantCategory?>(null) }
    var detail by remember { mutableStateOf<PlantSpecies?>(null) }

    val filtered = remember(query, category, vm.catalog) {
        vm.catalog.filter { sp ->
            (category == null || sp.category == category) &&
                (query.isBlank() || sp.name.contains(query.trim(), ignoreCase = true))
        }
    }

    Column(Modifier.fillMaxSize()) {
        // 标题介绍 + 搜索框合并为一行
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 10.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text("图鉴", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = InkDark)
                Spacer(Modifier.height(2.dp))
                Text(
                    "内置 ${vm.catalog.size} 种常见盆栽",
                    fontSize = 11.sp, color = InkSecondary, maxLines = 1
                )
            }
            Spacer(Modifier.width(12.dp))
            CatalogSearchField(
                query = query,
                onQueryChange = { query = it },
                modifier = Modifier.width(158.dp)
            )
        }
        Spacer(Modifier.height(2.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp)
        ) {
            CategoryChip("全部", null, category) { category = it }
            PlantCategory.entries.forEach { cat ->
                CategoryChip("${cat.emoji} ${cat.label}", cat, category) { category = it }
            }
        }
        Text(
            "共 ${vm.catalog.size} 种 · 当前 ${filtered.size} 种",
            fontSize = 11.sp, color = InkSecondary,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 6.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 110.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.id }) { sp ->
                SpeciesCard(sp) { detail = sp }
            }
        }
    }

    detail?.let { sp ->
        SpeciesDetailDialog(sp, onDismiss = { detail = null }, onAdd = {
            detail = null
            onAddSpecies(sp.id)
        })
    }
}

@Composable
private fun CatalogSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Icon(GardenIcons.Search, null, tint = InkSecondary, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text("搜索植物", fontSize = 12.sp, color = InkSecondary)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(fontSize = 12.sp, color = InkDark),
                singleLine = true,
                cursorBrush = SolidColor(MintDeep),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CategoryChip(label: String, value: PlantCategory?, current: PlantCategory?, onClick: (PlantCategory?) -> Unit) {
    FilterChip(
        selected = current == value,
        onClick = { onClick(value) },
        label = { Text(label, fontSize = 12.sp) },
        shape = RoundedCornerShape(16.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MintContainer,
            selectedLabelColor = MintDeep
        )
    )
}

@Composable
private fun SpeciesCard(sp: PlantSpecies, onClick: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(Modifier.padding(12.dp)) {
            SpeciesImage(
                speciesId = sp.id,
                emoji = sp.emoji,
                fallbackSize = 40.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(WarmBg)
                    .padding(6.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(sp.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = InkDark, maxLines = 1)
            Text(sp.category.label, fontSize = 10.sp, color = InkSecondary)
            Spacer(Modifier.height(6.dp))
            Text("💧 约 ${sp.waterDays} 天 · 🌾 约 ${sp.fertilizeDays} 天", fontSize = 10.sp, color = MintDeep)
            Text("${sp.light} · ${sp.difficulty}", fontSize = 10.sp, color = InkSecondary, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun SpeciesDetailDialog(sp: PlantSpecies, onDismiss: () -> Unit, onAdd: () -> Unit) {
    GardenDialog(
        onDismiss = onDismiss,
        title = sp.name,
        icon = GardenIcons.Leaf,
        confirm = DialogAction(label = "＋ 养一盆", onClick = onAdd),
        dismiss = DialogAction(label = "关闭", onClick = onDismiss)
    ) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            SpeciesImage(
                speciesId = sp.id,
                emoji = sp.emoji,
                fallbackSize = 56.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(WarmBg)
                    .padding(8.dp)
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoTag(sp.category.label)
                InfoTag(sp.light)
                InfoTag("难度：${sp.difficulty}")
            }
            Spacer(Modifier.height(10.dp))
            Surface(shape = RoundedCornerShape(12.dp), color = MintContainer) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💧 浇水", fontSize = 11.sp, color = InkSecondary)
                        Text("约 ${sp.waterDays} 天/次", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MintDeep)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌾 施肥", fontSize = 11.sp, color = InkSecondary)
                        Text("约 ${sp.fertilizeDays} 天/次", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MintDeep)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(sp.description, fontSize = 12.sp, lineHeight = 19.sp, color = InkDark)
            Spacer(Modifier.height(6.dp))
            Text("养护间隔为通用参考值，添加后可按实际情况调整。", fontSize = 10.sp, color = InkSecondary)
        }
    }
}

@Composable
private fun InfoTag(text: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(text, fontSize = 10.sp, color = InkSecondary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}