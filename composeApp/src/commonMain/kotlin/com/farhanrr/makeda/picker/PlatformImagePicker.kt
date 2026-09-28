package com.farhanrr.makeda.picker

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePicker(onImagePicked: (String?) -> Unit): () -> Unit