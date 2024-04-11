import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.intellij.lang.annotations.Language

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
@Preview
fun App() {
    var shaderText by remember { mutableStateOf(defaultShaderText) }
    var shaderText2 by remember { mutableStateOf(defaultShaderText) }
    var realtime by remember { mutableStateOf(true) }

    var play by remember { mutableStateOf(true) }

    val shader by remember(shaderText) {
        derivedStateOf {
            object : Shader {
                override val name: String
                    get() = ""
                override val authorName: String
                    get() = ""
                override val authorUrl: String
                    get() = ""
                override val credit: String
                    get() = ""
                override val license: String
                    get() = ""
                override val licenseUrl: String
                    get() = ""
                override val sksl: String
                    get() = shaderText

            }
        }
    }

    LaunchedEffect(shaderText2, realtime) {
        if (realtime) {
            shaderText = shaderText2
        }
    }

    MaterialTheme(darkColorScheme()) {
        Surface {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Shader Maker") },
                        actions = {
                            val clipboard = LocalClipboardManager.current
                            IconButton(
                                onClick = {
                                    clipboard.setText(
                                        buildAnnotatedString { append(shaderText2) }
                                    )
                                }
                            ) { Icon(Icons.Default.CopyAll, null) }
                        }
                    )
                },
                bottomBar = {
                    BottomAppBar {
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Realtime Shader")
                                Switch(
                                    realtime,
                                    onCheckedChange = { realtime = it },
                                )
                            }

                            IconToggleButton(
                                play,
                                onCheckedChange = { play = it },
                            ) {
                                if (play) {
                                    Icon(Icons.Default.PlayArrow, null)
                                } else {
                                    Icon(Icons.Default.Pause, null)
                                }
                            }

                            Button(
                                onClick = { shaderText = shaderText2 }
                            ) { Text("Submit") }

                            Button(
                                onClick = {
                                    shaderText2 = shaderText2
                                        .replace("iTime", "uTime")
                                        .replace("iResolution", "uResolution")
                                }
                            ) { Text("Replace i with u") }
                        }
                    }
                }
            ) { padding ->
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    /*Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        EmptyCard(
                            modifier = Modifier.weight(1f),
                            cardBack = Modifier.shaderBackground(shader)
                        )

                        OutlinedCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            currentIssue?.stackTraceToString()?.let { Text(it) }
                        }
                    }*/

                    EmptyCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        cardBack = Modifier.shaderBackground(shader, playState = play)
                    )

                    var lineTops by remember { mutableStateOf(emptyArray<Float>()) }
                    val density = LocalDensity.current

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        BasicTextField(
                            shaderText2,
                            onValueChange = { shaderText2 = it },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = Brush.sweepGradient(
                                listOf(
                                    MaterialTheme.colorScheme.onSurface,
                                    MaterialTheme.colorScheme.onSurface
                                )
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(2.dp)
                                .fillMaxWidth(),
                            onTextLayout = { result ->
                                lineTops = Array(result.lineCount) { result.getLineTop(it) }
                            },
                            decorationBox = {
                                Row(
                                    modifier = Modifier.verticalScroll(rememberScrollState())
                                ) {
                                    if (lineTops.isNotEmpty()) {
                                        Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                                            lineTops.forEachIndexed { index, top ->
                                                Text(
                                                    modifier = Modifier.offset(y = with(density) { top.toDp() }),
                                                    text = (index + 1).toString()
                                                )
                                            }
                                        }
                                    }
                                    it()
                                }
                            }
                        )

                        OutlinedCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            currentIssue?.stackTraceToString()?.let {
                                Text(it, modifier = Modifier.padding(4.dp))
                            }
                        }
                    }

                    /*TextField(
                        shaderText2,
                        onValueChange = { shaderText2 = it },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )*/
                }
            }
        }
    }
}

@Composable
fun EmptyCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color),
    border: BorderStroke? = null,
    cardBack: Modifier? = null,
    content: @Composable () -> Unit = {},
) = Surface(
    shape = shape,
    color = color,
    contentColor = contentColor,
    border = border,
    modifier = modifier//.size(100.dp, 150.dp),
) {
    cardBack?.let {
        Box(modifier = it) { content() }
    } ?: Box { content() }
}

@Language("AGSL")
val defaultShaderText = """
uniform float uTime;
uniform vec3 uResolution;
            
vec4 main( vec2 fragCoord ) {
    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = fragCoord / uResolution.xy;

    // Time varying pixel color
    vec3 col = 0.5 + 0.5*cos(uTime+uv.xyx+vec3(0,2,4));

    // Output to screen
    return vec4(col,1.0);
}
"""

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }

    /*Window(
        onCloseRequest = {},
        transparent = false,
        alwaysOnTop = true
    ) {
        MaterialTheme(darkColorScheme()) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                currentIssue?.stackTraceToString()?.let { Text(it) }
            }
        }
    }*/
}
