import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.intellij.lang.annotations.Language

@Composable
@Preview
fun App() {
    var shaderText by remember { mutableStateOf(defaultShaderText) }
    var shaderText2 by remember { mutableStateOf(defaultShaderText) }
    var realtime by remember { mutableStateOf(false) }

    LaunchedEffect(shaderText2, realtime) {
        if(realtime) {
            shaderText = shaderText2
        }
    }

    MaterialTheme(darkColors()) {
        Surface {
            Scaffold(
                bottomBar = {
                    BottomAppBar {
                        Text("Realtime Shader")
                        Switch(
                            realtime,
                            onCheckedChange = { realtime = it },
                        )

                        Button(
                            onClick = { shaderText = shaderText2 }
                        ) { Text("Submit") }
                    }
                }
            ) { padding ->
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    EmptyCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        cardBack = Modifier.shaderBackground(
                            remember(shaderText) {
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
                        )
                    )

                    TextField(
                        shaderText2,
                        onValueChange = { shaderText2 = it },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    color: Color = MaterialTheme.colors.surface,
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
}
