#!/bin/bash
cat << 'INNER_EOF' >> app/src/main/java/com/example/ui/DashboardScreen.kt

@Composable
fun SecretDevDialog(onDismiss: () -> Unit, onSave: (Int, Float, Float) -> Unit) {
    var password by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var stepsInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var distInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var calInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var errorMsg by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { androidx.compose.material3.Text("Developer Console") },
        text = {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMsg = "" },
                    label = { androidx.compose.material3.Text("Password") },
                    isError = errorMsg.isNotEmpty(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword)
                )
                if (errorMsg.isNotEmpty()) {
                    androidx.compose.material3.Text(
                        errorMsg,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }
                if (password == "4921") {
                    androidx.compose.material3.OutlinedTextField(
                        value = stepsInput,
                        onValueChange = { stepsInput = it },
                        label = { androidx.compose.material3.Text("Custom Steps") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = distInput,
                        onValueChange = { distInput = it },
                        label = { androidx.compose.material3.Text("Custom Distance (km)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = calInput,
                        onValueChange = { calInput = it },
                        label = { androidx.compose.material3.Text("Custom Calories (kcal)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = {
                if (password != "4921") {
                    errorMsg = "Incorrect Password"
                } else {
                    val s = stepsInput.toIntOrNull() ?: 0
                    val d = distInput.toFloatOrNull() ?: 0f
                    val c = calInput.toFloatOrNull() ?: 0f
                    onSave(s, d, c)
                }
            }) {
                androidx.compose.material3.Text("Save")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                androidx.compose.material3.Text("Cancel")
            }
        },
        containerColor = androidx.compose.ui.graphics.Color(0xFFFDFDFD),
        titleContentColor = androidx.compose.ui.graphics.Color(0xFF1E1E1E),
        textContentColor = androidx.compose.ui.graphics.Color(0xFF1E1E1E)
    )
}
INNER_EOF
