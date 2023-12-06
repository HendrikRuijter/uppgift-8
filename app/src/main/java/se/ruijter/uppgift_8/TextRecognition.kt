package se.ruijter.uppgift_8

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.content.ContextCompat.getString
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

/**
 * The view model state
 *
 * Business logic produces processing state to update the UI
 *
 * @property enableProcessing UI state to enable and disable buttons during processing
 * @property textElements A list of all elements (words)
 * @property elementConfidence A list of the confidence in each identified element
 * @property totalConfidence The product of all element confidences, overall confidence
 *
 */
data class ParsedTextInImage(
    val enableProcessing: Boolean = true,
    val textElements: List<String> = emptyList(),
    val elementConfidence: List<Double> = emptyList(),
    val totalConfidence: Double = 0.0,
)

/**
 * The text recognition processing singleton
 *
 * Uses the ML Kit text recognition model to find latin text in an image
 *
 * @property uiState Exposed to the UI to change state when the state is updated
 * @property recognizer The client to use the ML Kit text recognition model
 *
 */
object TextRecognitionInImage: ViewModel() {
    private val _uiState = MutableStateFlow(ParsedTextInImage())
    val uiState: StateFlow<ParsedTextInImage> = _uiState.asStateFlow()

    /**
     * Resets the UI state and enables or disables buttons
     *
     * @param enable Buttons enabled (true) or disabled during processing (false)
     * @return _uiState New uiState content to dispatch
     *
     */
    fun resetState(enable: Boolean = true) {
        _uiState.value = ParsedTextInImage(enableProcessing = enable)
    }

    private const val logTag: String = "TextRecognitionInImage"
    private lateinit var recognizer: TextRecognizer

    /**
     * Initializes the text recognition model client once
     *
     * @param context The composable context
     * @return Configure a text recognition client in singleton
     */
    private fun configure(context: Context) {
        val textRecognizerOptions = TextRecognizerOptions.Builder().build()
        recognizer = TextRecognition.getClient(textRecognizerOptions)
        Log.i(logTag, getString(context, R.string.configure_message))
    }

    /**
     * Processes an image registered as a drawable in the project
     *
     * @param context The composable context
     * @param imageId The resource id of a project image
     *
     */
    fun recognizeTextInImage(context: Context, imageId: Int) {
        resetState(enable = false)
        val imageDataResources: Resources = context.resources
        val selectedImage = BitmapFactory.decodeResource(imageDataResources, imageId)
        val image = InputImage.fromBitmap(selectedImage, 0)
        if (! TextRecognitionInImage::recognizer.isInitialized) {
            configure(context)
        }
        processImage(context, image)
    }

    /**
     * Use the configured text recognition client to process an image
     *
     * @param context The composable context
     * @param image The image to process
     *
     */
    private fun processImage(context: Context, image: InputImage) {
        recognizer.process(image)
            .addOnSuccessListener { texts ->
                parseTextRecognitionResult(context, texts)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                throw (e)
            }
    }

    /**
     * Round a double value to a fixed number of decimals
     *
     * @param decimals The number of decimals, default 2
     */
    private fun Double.round(decimals: Int = 2): Double =
        "%.${decimals}f".format(this).toDouble()

    /**
     * Parse the text recognition client texts identified according to
     * https://developers.google.com/ml-kit/vision/text-recognition/v2
     *
     * @param context The composable context
     * @param texts The text recognizer segments text into blocks, lines, elements and symbols
     * @return _uiState New uiState content to dispatch
     *
     */
    private fun parseTextRecognitionResult(context: Context, texts: Text) {
        val textContent = mutableListOf<String>()
        val textConfidence = mutableListOf<Double>()
        var blockConfidence = 0.0

        val blocks: List<Text.TextBlock> = texts.textBlocks
        if (blocks.isEmpty()) {
            val emptyMsg = getString(context, R.string.empty_message)
            Log.i(logTag, emptyMsg)
            _uiState.value = ParsedTextInImage(textElements = listOf(emptyMsg))
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
