package com.example.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Shader
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ShaderBrush
import kotlin.random.Random

fun Modifier.noiseOverlay(strength: Float): Modifier = composed {
    if (strength <= 0f) return@composed this
    
    val noiseBrush = remember(strength) {
        val width = 128
        val height = 128
        val pixels = IntArray(width * height) {
            val alpha = (Random.nextFloat() * 255 * strength).toInt()
            Color.argb(alpha, 0, 0, 0)
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        val shader = android.graphics.BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        ShaderBrush(shader)
    }

    this.drawWithCache {
        onDrawWithContent {
            drawContent()
            drawRect(
                brush = noiseBrush,
                blendMode = BlendMode.SrcOver
            )
        }
    }
}
