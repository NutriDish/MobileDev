package com.dicoding.nutridish.helper


import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ImageClassifierHelper(
    private val context: Context,
    private val modelName: String = "model.tflite",
    private val classifierListener: ClassifierListener?
) {
    private var interpreter: Interpreter? = null

    init {
        setupInterpreter()
    }

    private fun setupInterpreter() {
        try {
            val modelFile = context.assets.open(modelName)
            val modelBytes = modelFile.readBytes()
            val modelBuffer = ByteBuffer.allocateDirect(modelBytes.size).order(ByteOrder.nativeOrder())
            modelBuffer.put(modelBytes)
            interpreter = Interpreter(modelBuffer)
        } catch (e: Exception) {
            classifierListener?.onError("Failed to load model: ${e.message}")
        }
    }

    fun classifyStaticImage(imageUri: Uri) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                    .copy(Bitmap.Config.ARGB_8888, true)
            }?.let {
                Bitmap.createScaledBitmap(it, 128, 128, true)
            } ?: throw IllegalArgumentException("Failed to process image.")

            val inputBuffer = convertBitmapToByteBuffer(bitmap)
            val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 128, 128, 3), DataType.FLOAT32)

            interpreter?.run(inputBuffer, outputBuffer.buffer.rewind())
            classifierListener?.onResults(outputBuffer.floatArray, System.currentTimeMillis())

        } catch (e: Exception) {
            classifierListener?.onError("Classification error: ${e.message}")
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(4 * 128 * 128 * 3)
        buffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(128 * 128)
        bitmap.getPixels(intValues, 0, 128, 0, 0, 128, 128)
        for (pixel in intValues) {
            buffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f)
            buffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)
            buffer.putFloat((pixel and 0xFF) / 255.0f)
        }
        return buffer
    }

    interface ClassifierListener {
        fun onError(error: String)
        fun onResults(results: FloatArray, inferenceTime: Long)
    }
}
