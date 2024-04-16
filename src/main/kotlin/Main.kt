import androidx.compose.animation.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.diffplug.spotless.cpp.ClangFormatStep
import com.wakaztahir.codeeditor.model.CodeLang
import com.wakaztahir.codeeditor.prettify.PrettifyParser
import com.wakaztahir.codeeditor.theme.CodeThemeType
import com.wakaztahir.codeeditor.utils.parseCodeAsAnnotatedString
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language
import org.jetbrains.skia.RuntimeShaderBuilder
import java.io.File
import java.util.*

const val MAIN_FUNCTION = "vec4 main( vec2 fragCoord )"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
@Preview
fun App() {
    var shaderText by remember { mutableStateOf(defaultShaderText) }
    var shaderText2 by remember { mutableStateOf(defaultShaderText) }

    // Step 1. Declare Language & Code
    val language = CodeLang.C

    // Step 2. Create Parser & Theme
    val parser = remember { PrettifyParser() } // try getting from LocalPrettifyParser.current
    val themeState by remember { mutableStateOf(CodeThemeType.Monokai) }
    val theme = remember(themeState) { themeState.theme }
    fun parse(text: String) = parseCodeAsAnnotatedString(
        parser = parser,
        theme = theme,
        lang = language,
        code = text
    )
    // Step 3. Parse Code For Highlighting
    var shaderText3 by remember { mutableStateOf(TextFieldValue(parse(defaultShaderText))) }
    var play by remember { mutableStateOf(true) }
    var speedModifier by remember { mutableFloatStateOf(.5f) }
    var includeDate by remember { mutableStateOf(false) }

    val shader by remember(shaderText, speedModifier) {
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
                override val speedModifier: Float
                    get() = speedModifier
                override val sksl: String
                    get() = shaderText

            }
        }
    }

    val hasMainFunction by remember {
        derivedStateOf { shaderText3.text.contains(MAIN_FUNCTION) }
    }

    LaunchedEffect(shaderText3) {
        shaderText = shaderText3.text
    }

    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    MaterialTheme(darkColorScheme()) {
        Surface {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        DrawerSheet(
                            includeDate = includeDate,
                            onDateChange = { includeDate = it },
                            speedModifier = speedModifier,
                            onSpeedModifierChange = { speedModifier = it }
                        )
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            navigationIcon = {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } }
                                ) { Icon(Icons.Default.Settings, null) }
                            },
                            title = { Text("Shader Maker") },
                            actions = {
                                val clipboard = LocalClipboardManager.current
                                IconButton(
                                    onClick = {
                                        clipboard.setText(
                                            buildAnnotatedString { append(shaderText3.text) }
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

                                if (false)
                                    Button(
                                        onClick = {
                                            /*val f = Formatter.builder()
                                                .name("test")
                                                .lineEndingsPolicy(LineEnding.MAC_CLASSIC.createPolicy())
                                                .encoding(Charset.defaultCharset())
                                                .rootDir(Path(""))
                                                .steps(
                                                    listOf<FormatterStep>(
                                                        ClangFormatStep.withVersion(ClangFormatStep.defaultVersion()).create()
                                                    )
                                                )
                                                .build()*/
                                            val file = File.createTempFile("make", "c")
                                            //file.writeText(shaderText3.text)
                                            //f.applyTo(file)
                                            val v = ClangFormatStep.withVersion(ClangFormatStep.defaultVersion())
                                                .create()
                                                .format(
                                                    shaderText3.text,
                                                    file//Formatter.NO_FILE_SENTINEL,
                                                )
                                            println(v)
                                            /*val c = file.readText()
                                            shaderText3 = shaderText3.copy(
                                                annotatedString = parse(c)
                                            )*/
                                            file.delete()
                                        }
                                    ) {
                                        Text("Format")
                                    }

                                Button(
                                    onClick = {
                                        shaderText2 = shaderText2
                                            .replace("iTime", "uTime")
                                            .replace("iResolution", "uResolution")
                                            .replace("iDate", "uDate")

                                        shaderText3 = shaderText3.copy(
                                            annotatedString = parse(
                                                shaderText3.text
                                                    .replace("iTime", "uTime")
                                                    .replace("iResolution", "uResolution")
                                                    .replace("iDate", "uDate")
                                            )
                                        )
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

                        //var mouseClick by remember { mutableStateOf(Offset.Zero) }
                        //var mouseClicked by remember { mutableStateOf(false) }

                        EmptyCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            cardBack = Modifier.shaderBackground(
                                shader,
                                playState = play,
                                customUniformUpdate = shaderUpdate(
                                    includeDate = includeDate,
                                    //mouseClick,
                                    //mouseClicked
                                )
                            )
                            /*.onDrag(
                                onDragStart = {
                                    mouseClicked = true
                                },
                                onDragEnd =  {
                                    mouseClicked = false
                                }
                            ) {
                                mouseClick = it
                                println(it)
                            }*/
                        )

                        var lineTops by remember { mutableStateOf(emptyArray<Float>()) }
                        val density = LocalDensity.current

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .animateContentSize()
                        ) {
                            BasicTextField(
                                modifier = Modifier
                                    .weight(2f)
                                    .padding(2.dp)
                                    .fillMaxWidth(),
                                value = shaderText3,
                                onValueChange = { shaderText3 = it.copy(annotatedString = parse(it.text)) },
                                onTextLayout = { result ->
                                    lineTops = Array(result.lineCount) { result.getLineTop(it) }
                                },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = Brush.sweepGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.onSurface,
                                        MaterialTheme.colorScheme.onSurface
                                    )
                                ),
                                decorationBox = {
                                    Row(
                                        modifier = Modifier.verticalScroll(rememberScrollState())
                                    ) {
                                        if (lineTops.isNotEmpty()) {
                                            Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                                                lineTops.forEachIndexed { index, top ->
                                                    Text(
                                                        text = (index + 1).toString(),
                                                        modifier = Modifier.offset(y = with(density) { top.toDp() }),
                                                        color = MaterialTheme.colorScheme.onBackground.copy(.3f)
                                                    )
                                                }
                                            }
                                        }
                                        it()
                                    }
                                }
                            )

                            //If the user does not have github stuff set up
                            /*BasicTextField(
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
                                    .weight(2f)
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
                            )*/

                            AnimatedVisibility(
                                !hasMainFunction,
                                modifier = Modifier.weight(.25f)
                            ) {
                                OutlinedCard {
                                    SelectionContainer {
                                        Text(
                                            "Cannot find:\n$MAIN_FUNCTION",
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(4.dp)
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(
                                currentIssue != null,
                                enter = slideInVertically { it } + fadeIn(),
                                exit = slideOutVertically { it } + fadeOut(),
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    currentIssue?.message?.let {
                                        SelectionContainer {
                                            Text(
                                                it,
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.padding(4.dp)
                                            )
                                        }
                                    }
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

fun shaderUpdate(
    includeDate: Boolean,
    //offset: Offset,
    //mouseClicked: Boolean
): (RuntimeShaderBuilder) -> Unit = {
    if (includeDate) {
        //An example for things to add
        val date = Date()
        //(year, month, day, time in seconds)
        it.uniform(
            "uDate",
            date.hours.toFloat(),
            date.minutes.toFloat(),
            date.seconds.toFloat(),
            date.day.toFloat()
        )
    }

    //TODO: Still working on this
    /*
uniform vec4      iMouse; // mouse pixel coords. xy: current (if MLB down), zw: click
 */
    /*it.uniform(
        "uMouse",
        offset.x,
        offset.y,
        if(mouseClicked) 1f else 0f,
        if(mouseClicked) 1f else 0f,
    )*/

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerSheet(
    includeDate: Boolean,
    onDateChange: (Boolean) -> Unit,
    speedModifier: Float,
    onSpeedModifierChange: (Float) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Options") }) },
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            ListItem(
                headlineContent = { Text("Include Date as uDate?") },
                trailingContent = {
                    Switch(
                        checked = includeDate,
                        onCheckedChange = onDateChange
                    )
                },
                modifier = Modifier.toggleable(
                    value = includeDate,
                    onValueChange = onDateChange
                )
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Speed Modifier") },
                trailingContent = {
                    IconButton(
                        onClick = { onSpeedModifierChange(.5f) }
                    ) { Icon(Icons.Default.LockReset, null) }
                },
                supportingContent = {
                    OutlinedTextField(
                        value = speedModifier.toString(),
                        onValueChange = { onSpeedModifierChange(it.toFloatOrNull() ?: 0.5f) },
                        singleLine = true,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            )
        }
    }
}

@Language("AGSL")
val defaultShaderText = """
uniform float uTime;
uniform vec3 uResolution;

//Is upside down?
// uv.y = -uv.y;
            
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
