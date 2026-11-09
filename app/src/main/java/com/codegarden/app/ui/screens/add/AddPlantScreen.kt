package com.codegarden.app.ui.screens.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.codegarden.app.ui.components.DialogAction
import com.codegarden.app.ui.components.GardenDialog
import com.codegarden.app.ui.components.GardenIcons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.codegarden.app.data.model.PlantSpecies
import com.codegarden.app.ui.components.PhotoPickerCard
import com.codegarden.app.ui.components.SpeciesImage
import com.codegarden.app.ui.theme.InkDark
import com.codegarden.app.ui.theme.InkSecondary
import com.codegarden.app.ui.theme.MintContainer
import com.codegarden.app.ui.theme.MintDeep
import com.codegarden.app.viewmodel.MainViewModel

/** 添加植物：图鉴手动选择 / 自定义品种 + 拍照存档（识别接口预留） */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantScreen(
    vm: MainViewModel,
    speciesId: Int?,
    onBack: () -> Unit,
    onSaved: (String) -> Unit
) {
    var customMode by rememberSaveable { mutableStateOf(speciesId == null) }
    var species by remember { mutableStateOf(vm.speciesById(speciesId)) }
    var showPicker by remember { mutableStateOf(false) }

    var customName by rememberSaveable { mutableStateOf("") }
    var customEmoji by rememberSaveable { mutableStateOf("🪴") }
    var customWater by rememberSaveable { mutableStateOf("7") }
    var customFertilize by rememberSaveable { mutableStateOf("30") }

    var nickname by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var photoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("添加植物", fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !customMode,
                    onClick = { customMode = false; error = null },
                    label = { Text("📖 从图鉴选择", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MintContainer, selectedLabelColor = MintDeep)
                )
                FilterChip(
                    selected = customMode,
                    onClick = { customMode = true; error = null },
                    label = { Text("✏️ 自定义品种", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MintContainer, selectedLabelColor = MintDeep)
                )
            }

            if (!customMode) {
                val sp = species
                if (sp != null) {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            SpeciesImage(sp.id, sp.emoji, Modifier.size(52.dp), fallbackSize = 30.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(sp.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = InkDark)
                                Text(
                                    "${sp.category.label} · 💧${sp.waterDays}天 / 🌾${sp.fertilizeDays}天",
                                    fontSize = 11.sp, color = InkSecondary
                                )
                            }
                            TextButton(onClick = { showPicker = true }) {
                                Text("更换", fontSize = 12.sp, color = MintDeep)
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showPicker = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("🔍 从 500 种盆栽中选择", fontSize = 14.sp, color = MintDeep)
                    }
                }
            } else {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("图鉴里没有？自己记录一盆", fontSize = 12.sp, color = InkSecondary)
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it.take(20); error = null },
                            label = { Text("品种名称") },
                            placeholder = { Text("例如：朋友送的小多肉") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintDeep, focusedLabelColor = MintDeep),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = customWater,
                                onValueChange = { customWater = it.filter(Char::isDigit).take(3) },
                                label = { Text("浇水间隔(天)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintDeep, focusedLabelColor = MintDeep),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = customFertilize,
                                onValueChange = { customFertilize = it.filter(Char::isDigit).take(3) },
                                label = { Text("施肥间隔(天)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintDeep, focusedLabelColor = MintDeep),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text("表情头像", fontSize = 11.sp, color = InkSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("🪴", "🌿", "🌵", "🌸", "🌷", "🌻", "🌱", "🌳").forEach { e ->
                                Text(
                                    e, fontSize = 22.sp,
                                    modifier = Modifier
                                        .clickable { customEmoji = e }
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }

            PhotoPickerCard(photoPath, species?.emoji ?: customEmoji) { photoPath = it }

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it.take(20) },
                label = { Text("给它起个名字（可选）") },
                placeholder = { Text("例如：办公室的小绿") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintDeep, focusedLabelColor = MintDeep),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(200) },
                label = { Text("备注（可选）") },
                placeholder = { Text("放在南阳台，怕晒…") },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintDeep, focusedLabelColor = MintDeep),
                modifier = Modifier.fillMaxWidth()
            )

            error?.let {
                Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val id = when {
                        !customMode && species != null ->
                            vm.addFromSpecies(species!!, nickname, photoPath, note)
                        customMode && customName.isNotBlank() -> vm.addCustom(
                            customName, customEmoji, nickname, photoPath,
                            customWater.toIntOrNull() ?: 7, customFertilize.toIntOrNull() ?: 30, note
                        )
                        else -> {
                            error = if (!customMode) "请先从图鉴选择一个品种" else "请填写品种名称"
                            null
                        }
                    }
                    if (id != null) onSaved(id)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintDeep)
            ) {
                Text("🌱 种进我的花园", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(20.dp))
        }
    }

    if (showPicker) {
        SpeciesPickerDialog(
            catalog = vm.catalog,
            onDismiss = { showPicker = false },
            onPick = { species = it; showPicker = false; error = null }
        )
    }
}

@Composable
private fun SpeciesPickerDialog(
    catalog: List<PlantSpecies>,
    onDismiss: () -> Unit,
    onPick: (PlantSpecies) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        catalog.filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }
    }
    GardenDialog(
        onDismiss = onDismiss,
        title = "选择品种",
        icon = GardenIcons.Search,
        dismiss = DialogAction(label = "取消", onClick = onDismiss)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("搜索植物名称", fontSize = 13.sp) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintDeep),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.heightIn(max = 360.dp)) {
            items(filtered, key = { it.id }) { sp ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(sp) }
                        .padding(vertical = 10.dp, horizontal = 4.dp)
                ) {
                    SpeciesImage(sp.id, sp.emoji, Modifier.size(36.dp), fallbackSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(sp.name, fontSize = 14.sp, color = InkDark, fontWeight = FontWeight.Medium)
                        Text(
                            "${sp.category.label} · 💧${sp.waterDays}天 / 🌾${sp.fertilizeDays}天",
                            fontSize = 10.sp, color = InkSecondary
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            }
        }
    }
}