package com.example.acremotecontrol

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val fanSpeeds = listOf("自动", "静音", "低", "中", "高")
private val modes = listOf("送风", "制热", "制冷", "除湿", "自动")

@Composable
fun RemoteControlScreen(
    imei: String
) {
    var isPowerOn by remember { mutableStateOf(true) }
    var temperature by remember { mutableStateOf(24f) }
    var selectedFanSpeed by remember { mutableStateOf(2) }
    var selectedMode by remember { mutableStateOf(2) }

    val scope = rememberCoroutineScope()
    val lastCommandTime = remember { mutableStateOf(0L) }

    // Poll AC state every 1 second
    LaunchedEffect(imei) {
        while (true) {
            val state = AcApiService.fetchState(imei)
            // 如果最近1.5秒内发过命令，跳过轮询更新，等服务器处理完
            if (state.success && System.currentTimeMillis() - lastCommandTime.value > 1500) {
                isPowerOn = state.switchStatus == 1
                selectedMode = state.runMode.coerceIn(0, 4)
                selectedFanSpeed = state.windSpeed.coerceIn(0, 4)
                temperature = state.tempSet.toFloat().coerceIn(16f, 30f)
            }
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // IMEI display
        Text(
            text = "IMEI: $imei",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Temperature display
        Text(
            text = "${temperature.toInt()}°C",
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPowerOn) {
                if (selectedMode == 1) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Power button
        Button(
            onClick = {
                val newState = !isPowerOn
                isPowerOn = newState
                lastCommandTime.value = System.currentTimeMillis()
                scope.launch {
                    AcApiService.sendCommand(
                        imei,
                        mapOf("switchStatus" to if (newState) 1 else 0)
                    )
                }
            },
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPowerOn) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isPowerOn) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text(
                text = "开关",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Temperature slider section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("16°C", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "温度设置",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("30°C", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Slider(
                    value = temperature,
                    onValueChange = { temperature = it },
                    onValueChangeFinished = {
                        lastCommandTime.value = System.currentTimeMillis()
                        scope.launch {
                            AcApiService.sendCommand(
                                imei,
                                mapOf("tempSet" to temperature.toInt())
                            )
                        }
                    },
                    valueRange = 16f..30f,
                    steps = 14,
                    enabled = isPowerOn,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Fan speed section
        Text(
            text = "风速",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            fanSpeeds.forEachIndexed { index, label ->
                FilterChip(
                    selected = selectedFanSpeed == index,
                    onClick = {
                        selectedFanSpeed = index
                        lastCommandTime.value = System.currentTimeMillis()
                        scope.launch {
                            AcApiService.sendCommand(imei, mapOf("windSpeed" to index))
                        }
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isPowerOn,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Mode section
        Text(
            text = "模式",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            modes.forEachIndexed { index, label ->
                FilterChip(
                    selected = selectedMode == index,
                    onClick = {
                        selectedMode = index
                        lastCommandTime.value = System.currentTimeMillis()
                        scope.launch {
                            AcApiService.sendCommand(imei, mapOf("runMode" to index))
                        }
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isPowerOn,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Status summary
        if (isPowerOn) {
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "当前状态",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${temperature.toInt()}°C · ${modes[selectedMode]} · ${fanSpeeds[selectedFanSpeed]}风",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
