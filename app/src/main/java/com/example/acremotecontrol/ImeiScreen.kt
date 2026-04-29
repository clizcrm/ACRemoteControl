package com.example.acremotecontrol

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ImeiScreen(
    savedImei: String?,
    onConfirm: (String) -> Unit
) {
    var imei by remember {
        mutableStateOf(savedImei ?: "")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "空调遥控器",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "请输入设备IMEI码",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = imei,
            onValueChange = { imei = it },
            label = { Text("IMEI码") },
            placeholder = { Text("862858012345678") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (imei.isNotBlank()) onConfirm(imei)
                }
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { if (imei.isNotBlank()) onConfirm(imei) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = imei.isNotBlank()
        ) {
            Text("确认", fontSize = 18.sp)
        }

        if (savedImei != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "上次保存的IMEI: $savedImei",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
