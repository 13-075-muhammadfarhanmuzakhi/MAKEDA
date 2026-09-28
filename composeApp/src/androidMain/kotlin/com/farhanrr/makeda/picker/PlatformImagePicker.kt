package com.farhanrr.makeda.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberImagePicker(onImagePicked: (String?) -> Unit): () -> Unit {
    return remember {
        {
            ImagePickerBridge.pick { result -> onImagePicked(result) }
        }
    }
}