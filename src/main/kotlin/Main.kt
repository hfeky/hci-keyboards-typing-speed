import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.HighlightedTextPath
import util.TimestampFormatter

@ExperimentalComposeUiApi
fun main() = application {

    val windowState = rememberWindowState(placement = WindowPlacement.Fullscreen)
    val screenState = remember { mutableStateOf<ScreenState>(ScreenState.MainMenu) }
    val keyboardLayout = remember { mutableStateOf(KeyboardLayout.QWERTY) }

    Window(
        state = windowState,
        onCloseRequest = ::exitApplication,
        onPreviewKeyEvent = {
            if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) {
                exitApplication()
                return@Window true
            }

            if (screenState.value is ScreenState.TypingTest) {
                val state = screenState.value as ScreenState.TypingTest
                val nextIndex = state.currentIndex + 1

                if (it.type == KeyEventType.KeyDown) {
                    var pressedKey = when (it.key) {
                        Key.A -> 'a'
                        Key.B -> 'b'
                        Key.C -> 'c'
                        Key.D -> 'd'
                        Key.E -> 'e'
                        Key.F -> 'f'
                        Key.G -> 'g'
                        Key.H -> 'h'
                        Key.I -> 'i'
                        Key.J -> 'j'
                        Key.K -> 'k'
                        Key.L -> 'l'
                        Key.M -> 'm'
                        Key.N -> 'n'
                        Key.O -> 'o'
                        Key.P -> 'p'
                        Key.Q -> 'q'
                        Key.R -> 'r'
                        Key.S -> 's'
                        Key.T -> 't'
                        Key.U -> 'u'
                        Key.V -> 'v'
                        Key.W -> 'w'
                        Key.X -> 'x'
                        Key.Y -> 'y'
                        Key.Z -> 'z'
                        Key.Spacebar -> ' '
                        Key.Period -> '.'
                        Key.Comma -> ','
                        else -> '\u0000'
                    }

                    if (it.isShiftPressed) pressedKey = pressedKey.uppercaseChar()
                    if (state.paragraph[state.currentIndex] == pressedKey) {
                        if (nextIndex == state.paragraph.length) {
                            screenState.value = ScreenState.TestResult.create(
                                timeTakenMs = state.timeTakenMs,
                                totalWords = state.paragraph.split(" ").size + 1,
                                correctCharacters = state.paragraph.length,
                                incorrectCharacters = 6
                            )
                        } else {
                            screenState.value = state.copy(currentIndex = nextIndex)
                        }
                    } else {
                        screenState.value = state.copy(incorrectCharacters = state.incorrectCharacters + 1)
                    }
                }
                return@Window true
            }

            false
        },
        undecorated = true
    ) {
        MaterialTheme {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource("images/bg_default.jpg"),
                    contentDescription = "Window Background",
                    contentScale = ContentScale.FillBounds
                )
                when (screenState.value) {
                    ScreenState.MainMenu -> {
                        renderMainMenu(screenState, keyboardLayout)
                    }
                    is ScreenState.TypingTest -> {
                        renderTypingTest(screenState, keyboardLayout)
                    }
                    is ScreenState.TestResult -> {
                        renderTestResult(screenState, keyboardLayout)
                    }
                }
            }
        }
    }
}

@Composable
fun renderMainMenu(screenState: MutableState<ScreenState>, keyboardLayout: MutableState<KeyboardLayout>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.wrapContentHeight(),
            text = "Keyboards \uD83E\uDD37\u200D♂️\uD83E\uDD37\u200D♂️",
            textAlign = TextAlign.Center,
            fontSize = 60.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Row(
            modifier = Modifier.wrapContentHeight(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            KeyboardLayout.values().forEach { layout ->
                Row(
                    modifier = Modifier.wrapContentWidth().padding(30.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = layout == keyboardLayout.value,
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4C597D)),
                        onClick = {
                            keyboardLayout.value = layout
                        }
                    )
                    ClickableText(
                        text = AnnotatedString(layout.name),
                        style = TextStyle(fontSize = 24.sp),
                        onClick = {
                            keyboardLayout.value = layout
                        }
                    )
                }
            }
        }
        Image(
            modifier = Modifier.height(400.dp),
            contentDescription = "Keyboard Layout",
            painter = painterResource("images/${keyboardLayout.value.imageName}")
        )
        Button(
            modifier = Modifier.wrapContentHeight(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4C597D)),
            onClick = {
                screenState.value = ScreenState.TypingTest.create()
                val startTime = System.currentTimeMillis()
                CoroutineScope(Dispatchers.Default).launch {
                    while (screenState.value is ScreenState.TypingTest) {
                        delay(20L)
                        screenState.value = (screenState.value as ScreenState.TypingTest).copy(
                            timeTakenMs = System.currentTimeMillis() - startTime
                        )
                    }
                }
            }
        ) {
            Text(
                text = "Start Test",
                color = Color.White,
                fontSize = 40.sp
            )
        }
    }
}

@Composable
fun renderTypingTest(screenState: MutableState<ScreenState>, keyboardLayout: MutableState<KeyboardLayout>) {
    val state = screenState.value as ScreenState.TypingTest

    val text = state.paragraph
    val monospaceFont = FontFamily(Font("fonts/Inconsolata-Regular.ttf"))

    val currentIndex = state.currentIndex
    val nextIndex = state.currentIndex + 1

    val highlightedTextPath = remember { mutableStateOf(HighlightedTextPath()) }

    val greenColor = Color(0xFF499C54)
    val blueColor = Color.Blue

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(0.dp, 40.dp, 0.dp, 0.dp),
            text = "${keyboardLayout.value.name} Test",
            textAlign = TextAlign.Center,
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            modifier = Modifier.padding(0.dp, 20.dp, 0.dp, 0.dp),
            text = "Time: ${TimestampFormatter.format(state.timeTakenMs)}",
            textAlign = TextAlign.Center,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = monospaceFont
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(100.dp, 0.dp).drawBehind {
                    highlightedTextPath.value.apply {
                        drawPath(greenPath, style = Fill, color = greenColor.copy(alpha = 0.1f))
                        drawPath(bluePath, style = Fill, color = blueColor.copy(alpha = 0.1f))
                    }
                },
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = greenColor)) {
                        append(text.substring(0, currentIndex))
                    }
                    withStyle(style = SpanStyle(color = blueColor)) {
                        append(text.substring(currentIndex, nextIndex))
                    }
                    append(text.substring(nextIndex))
                },
                textAlign = TextAlign.Center,
                fontSize = 50.sp,
                fontFamily = monospaceFont,
                letterSpacing = 8.sp,
                onTextLayout = { layoutResult ->
                    highlightedTextPath.value = HighlightedTextPath(
                        greenPath = layoutResult.getBoxesPath(0, currentIndex),
                        bluePath = layoutResult.getBoxesPath(currentIndex, nextIndex)
                    )
                },
            )
        }
    }
}

@Composable
fun renderTestResult(screenState: MutableState<ScreenState>, keyboardLayout: MutableState<KeyboardLayout>) {
    val state = screenState.value as ScreenState.TestResult
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(0.dp, 40.dp, 0.dp, 0.dp),
            text = "${keyboardLayout.value.name} Test",
            textAlign = TextAlign.Center,
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(40.dp),
                text = "Time Taken: ${TimestampFormatter.format(state.timeTakenMs)}",
                textAlign = TextAlign.Center,
                fontSize = 40.sp
            )
            Text(
                modifier = Modifier.padding(40.dp),
                text = "Speed: ${state.wordsPerMinute} wpm",
                textAlign = TextAlign.Center,
                fontSize = 40.sp
            )
            Text(
                modifier = Modifier.padding(40.dp),
                text = "Accuracy: ${state.accuracy * 100}%",
                textAlign = TextAlign.Center,
                fontSize = 40.sp
            )
        }
    }
}
