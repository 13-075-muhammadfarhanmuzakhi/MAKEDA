package com.farhanrr.makeda

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.farhanrr.makeda.app.App
import com.farhanrr.makeda.picker.ImagePickerBridge
import java.io.File

class MainActivity : ComponentActivity() {

    // Salin foto pilihan ke penyimpanan internal app supaya tetap bisa dibuka setelah app ditutup
    private fun copyToInternal(uri: Uri): String {
        return try {
            val file = File(filesDir, "profile_${System.currentTimeMillis()}.jpg")
            contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            uri.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContextHolder.appContext = applicationContext
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        setContent {
            val pickMedia = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri: Uri? ->
                ImagePickerBridge.onResult(uri?.let { copyToInternal(it) })
            }
            ImagePickerBridge.launcher = {
                pickMedia.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
            App()
        }
    }
}