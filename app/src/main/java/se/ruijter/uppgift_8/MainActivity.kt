package se.ruijter.uppgift_8

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import se.ruijter.uppgift_8.ui.theme.Uppgift8Theme

/**
 * Utgå från veckans exempelprojekt: https://github.com/billmartensson/IntroMLKit/tree/main
 * 1. Lägg till tre knappar som väljer tre olika bilder med text du lagt till i projektet.
 * 2. Visa texten från bilden på skärmen.
 * 3. Publicera på github och ange länk nedan.
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Uppgift8Theme {
                TextRecognizer(
                    context = baseContext,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun TextRecognizer(
    context: Context,
    modifier: Modifier = Modifier,
    textRecognizerUiState: TextRecognitionInImage = viewModel(),
) {
    val textRecognitionUiState by textRecognizerUiState.uiState.collectAsState()
    val composableScope = rememberCoroutineScope()

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = modifier
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = typography.titleLarge,
                modifier = Modifier.padding(32.dp)
            )
            Text(
                text = stringResource(id = R.string.app_description),
                color = Color.Blue,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp),
            )
            Text(
                text = if (textRecognitionUiState.textElements.isEmpty()) {
                    stringResource(id = R.string.empty_string)
                } else {
                    "${textRecognitionUiState.textElements}\n" +
                            "${textRecognitionUiState.elementConfidence}\n" +
                            stringResource(id = R.string.total_confidence) +
                            "${textRecognitionUiState.totalConfidence}\n"
                },
                textAlign = TextAlign.Center,
            )
            OutlinedButton(
                onClick = {
                    TextRecognitionInImage.resetState()
                },
                enabled = textRecognitionUiState.enableProcessing
            ) {
                Text(stringResource(id = R.string.reset_button))
            }
            Button(onClick = {
                    composableScope.launch {
                        TextRecognitionInImage.recognizeTextInImage(context.resources, R.drawable.allt)
                    }
                },
                enabled = textRecognitionUiState.enableProcessing
            ) {
                Text(stringResource(id = R.string.first_image_button))
            }
            Button(onClick = {
                    composableScope.launch {
                        TextRecognitionInImage.recognizeTextInImage(context.resources, R.drawable.choklad)
                    }
                },
                enabled = textRecognitionUiState.enableProcessing
            ) {
                Text(stringResource(id = R.string.second_image_button))
            }
            Button(onClick = {
                    composableScope.launch {
                        TextRecognitionInImage.recognizeTextInImage(context.resources, R.drawable.sned)
                    }
                },
                enabled = textRecognitionUiState.enableProcessing
            ) {
                Text(stringResource(id = R.string.third_image_button))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TextRecognizerPreview() {
    Uppgift8Theme {
        TextRecognizer(
            context = LocalContext.current,
            modifier = Modifier
        )
    }
}