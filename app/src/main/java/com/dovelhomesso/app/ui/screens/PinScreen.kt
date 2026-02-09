package com.dovelhomesso.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinScreen(
    title: String = "Inserisci PIN",
    onPinCorrect: () -> Unit,
    onPinSet: (String) -> Unit = {},
    isSettingPin: Boolean = false,
    validatePin: ((String) -> Boolean)? = null
) {
    var pinInput by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(if (isSettingPin) 0 else 1) } // 0: Set, 1: Verify/Confirm

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when {
                isSettingPin && step == 0 -> "Crea un nuovo PIN"
                isSettingPin && step == 1 -> "Conferma il PIN"
                else -> title
            },
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Dots indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            val inputToShow = if (isSettingPin && step == 1) confirmPinInput else pinInput
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < inputToShow.length) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Numeric Keypad
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL")
            )

            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    row.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(modifier = Modifier.size(80.dp))
                        } else {
                            Button(
                                onClick = {
                                    if (key == "DEL") {
                                        if (isSettingPin && step == 1) {
                                            if (confirmPinInput.isNotEmpty()) confirmPinInput = confirmPinInput.dropLast(1)
                                        } else {
                                            if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                        }
                                        error = ""
                                    } else {
                                        if (isSettingPin && step == 1) {
                                            if (confirmPinInput.length < 4) confirmPinInput += key
                                        } else {
                                            if (pinInput.length < 4) pinInput += key
                                        }
                                        error = ""
                                    }

                                    // Check logic
                                    val currentInput = if (isSettingPin && step == 1) confirmPinInput else pinInput
                                    
                                    if (currentInput.length == 4) {
                                        if (isSettingPin) {
                                            if (step == 0) {
                                                // Move to confirmation
                                                step = 1
                                                error = ""
                                            } else {
                                                // Check match
                                                if (pinInput == confirmPinInput) {
                                                    onPinSet(pinInput)
                                                } else {
                                                    error = "I PIN non corrispondono. Riprova."
                                                    confirmPinInput = ""
                                                    step = 0
                                                    pinInput = ""
                                                }
                                            }
                                        } else {
                                            // Unlock mode
                                            if (validatePin != null) {
                                                if (validatePin(currentInput)) {
                                                    onPinCorrect()
                                                } else {
                                                    error = "PIN Errato"
                                                    pinInput = ""
                                                }
                                            } else {
                                                onPinCorrect()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.size(80.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (key == "DEL") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = if (key == "DEL") MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text(
                                    text = if (key == "DEL") "⌫" else key,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
