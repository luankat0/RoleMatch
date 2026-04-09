package com.pentagono.rolematch.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun InputWithIcon(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        placeholder = {
            placeholder?.let { Text(it) }
        },
        leadingIcon = {
            Icon(icon, contentDescription = null)
        },
        modifier = modifier,
        singleLine = true
    )
}