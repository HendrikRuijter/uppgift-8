package se.ruijter.uppgift_8

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ParsedTextInImage(
    // UI state to enable and disable buttons during processing to avoid queueing
    val enableProcessing: Boolean = true,
    val textElements: List<String> = emptyList(),
    val elementConfidence: List<Double> = emptyList(),
    val totalConfidence: Double = 0.0,
)

object TextRecognitionInImage: ViewModel() {
    private val _uiState = MutableStateFlow(ParsedTextInImage())
    val uiState: StateFlow<ParsedTextInImage> = _uiState.asStateFlow()

    fun resetState(enable: Boolean = true) {
        _uiState.update { currentState -> currentState.copy(
            enableProcessing = enable,
            textElements = emptyList(),
            elementConfidence = emptyList(),
            totalConfidence = 0.0
        )}
    }

    private const val logTag: String = "TextRecognitionInImage"
    private lateinit var recognizer: TextRecognizer

    private fun configure() {
        val textRecognizerOptions = TextRecognizerOptions.Builder().build()
        recognizer = TextRecognition.getClient(textRecognizerOptions)
        Log.i(logTag, "Configure the recognizer client.")
    }

    fun recognizeTextInImage(imageDataResources: Resources, imageId: Int) {
        resetState(enable = false)
        val selectedImage = BitmapFactory.decodeResource(imageDataResources, imageId)
        val image = InputImage.fromBitmap(selectedImage, 0)
        if (! TextRecognitionInImage::recognizer.isInitialized) {
            configure()
        }
        processImage(image)
    }

    private fun processImage(image: InputImage) {
        recognizer.process(image)
            .addOnSuccessListener { texts ->
                parseTextRecognitionResult(texts)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                throw (e)
            }
    }

    private fun Double.round(decimals: Int = 2): Double =
        "%.${decimals}f".format(this).toDouble()

    private fun parseTextRecognitionResult(texts: Text) {
        val textContent = mutableListOf<String>()
        val textConfidence = mutableListOf<Double>()
        var blockConfidence = 0.0
        val emptyMsg = "No text found in image."
        val blocks: List<Text.TextBlock> = texts.textBlocks
        if (blocks.isEmpty()) {
            Log.i(logTag, emptyMsg)
        } else {
            for (block in blocks) {
                val lines: List<Text.Line> = block.lines
                for (line in lines) {
                    val elements: List<Text.Element> = line.elements
                    textContent.addAll(elements = elements.map {
                        it.text
                    })
                    textConfidence.addAll(elements = elements.map {
                        it.confidence.toDouble().round()
                    })
                    blockConfidence = elements.map {
                        it.confidence
                    }.fold(1.0) {total, value -> total * value}.toDouble()

                    // Log.i(logTag, parsedText.textElements.toString())
                    // Log.i(logTag, parsedText.elementConfidence.toString())
                }
            }
            _uiState.update { currentState -> currentState.copy(
                enableProcessing = true,
                textElements = textContent,
                elementConfidence = textConfidence,
                totalConfidence = blockConfidence.round(),
            ) }
        }
    }
}
