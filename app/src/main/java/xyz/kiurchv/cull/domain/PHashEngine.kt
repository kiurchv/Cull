package xyz.kiurchv.cull.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import xyz.kiurchv.cull.data.model.Photo
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos
import kotlin.math.sqrt

@Singleton
class PHashEngine @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val HASH_SIZE = 32      // DCT computed on 32x32
        private const val DCT_SIZE = 8        // top-left 8x8 of DCT used
        private const val HAMMING_THRESHOLD = 10  // bits difference for "duplicate"
    }

    /**
     * Compute perceptual hash for a photo file.
     * Returns null if the image cannot be decoded.
     */
    fun computeHash(path: String): Long? = runCatching {
        val opts = BitmapFactory.Options().apply {
            inSampleSize = 4   // downsample for speed before resize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = BitmapFactory.decodeFile(path, opts) ?: return null
        val resized = Bitmap.createScaledBitmap(bitmap, HASH_SIZE, HASH_SIZE, true)
        bitmap.recycle()
        val gray = toGrayscalePixels(resized)
        resized.recycle()
        val dct = applyDct(gray)
        buildHash(dct)
    }.getOrNull()

    /**
     * Compute sharpness as variance of Laplacian (higher = sharper).
     */
    fun computeSharpness(path: String): Float = runCatching {
        val opts = BitmapFactory.Options().apply {
            inSampleSize = 2
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = BitmapFactory.decodeFile(path, opts) ?: return 0f
        val sharpness = laplacianVariance(bitmap)
        bitmap.recycle()
        sharpness
    }.getOrElse { 0f }

    fun hammingDistance(a: Long, b: Long): Int =
        java.lang.Long.bitCount(a xor b)

    fun areDuplicates(a: Long, b: Long): Boolean =
        hammingDistance(a, b) <= HAMMING_THRESHOLD

    // ---- Private helpers ----

    private fun toGrayscalePixels(bmp: Bitmap): FloatArray {
        val pixels = IntArray(HASH_SIZE * HASH_SIZE)
        bmp.getPixels(pixels, 0, HASH_SIZE, 0, 0, HASH_SIZE, HASH_SIZE)
        return FloatArray(pixels.size) { i ->
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            (0.299f * r + 0.587f * g + 0.114f * b)
        }
    }

    private fun applyDct(pixels: FloatArray): FloatArray {
        val n = HASH_SIZE
        val result = FloatArray(n * n)
        for (u in 0 until n) {
            for (v in 0 until n) {
                var sum = 0.0
                for (x in 0 until n) {
                    for (y in 0 until n) {
                        sum += pixels[x * n + y] *
                                cos((2 * x + 1) * u * Math.PI / (2 * n)) *
                                cos((2 * y + 1) * v * Math.PI / (2 * n))
                    }
                }
                val cu = if (u == 0) 1.0 / sqrt(2.0) else 1.0
                val cv = if (v == 0) 1.0 / sqrt(2.0) else 1.0
                result[u * n + v] = (sum * cu * cv * 2.0 / n).toFloat()
            }
        }
        return result
    }

    private fun buildHash(dct: FloatArray): Long {
        // Extract top-left DCT_SIZE x DCT_SIZE, skip [0,0] DC component
        val n = HASH_SIZE
        val vals = mutableListOf<Float>()
        for (u in 0 until DCT_SIZE) {
            for (v in 0 until DCT_SIZE) {
                if (u == 0 && v == 0) continue
                vals += dct[u * n + v]
            }
        }
        val mean = vals.average().toFloat()
        var hash = 0L
        vals.take(64).forEachIndexed { i, v ->
            if (v > mean) hash = hash or (1L shl i)
        }
        return hash
    }

    private fun laplacianVariance(bmp: Bitmap): Float {
        val w = bmp.width
        val h = bmp.height
        val pixels = IntArray(w * h)
        bmp.getPixels(pixels, 0, w, 0, 0, w, h)

        val gray = FloatArray(w * h) { i ->
            val p = pixels[i]
            0.299f * ((p shr 16) and 0xFF) +
                    0.587f * ((p shr 8) and 0xFF) +
                    0.114f * (p and 0xFF)
        }

        var sum = 0.0
        var sumSq = 0.0
        var count = 0
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val lap = -gray[(y - 1) * w + x] - gray[(y + 1) * w + x] -
                        gray[y * w + (x - 1)] - gray[y * w + (x + 1)] +
                        4 * gray[y * w + x]
                sum += lap
                sumSq += lap * lap
                count++
            }
        }
        val mean = sum / count
        return ((sumSq / count) - mean * mean).toFloat()
    }
}
