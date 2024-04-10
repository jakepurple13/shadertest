import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.layout.onGloballyPositioned
import org.jetbrains.skia.RuntimeShaderBuilder
import kotlin.math.round

/**
 * Interface to describe shaders supported by the [shaderBackground] Modifier.
 */
interface Shader {
    /** The name for this shader. */
    val name: String

    /** Contains the author name who created the shader. */
    val authorName: String

    /** Contains the url to the author reference. */
    val authorUrl: String

    /** Contains the url to the source of this shader. */
    val credit: String

    /** Contains the name of the license for this shader. */
    val license: String

    /** Contains the url to the license reference. */
    val licenseUrl: String

    /** Defaut time modifier for this shader */
    val speedModifier: Float
        get() = 0.5f

    /** Contains the sksl shader*/
    val sksl: String
}

fun Float.round(decimals: Int): Float {
    var multiplier = 1.0f
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

/**
 * Describes a platform independent runtime effect
 */
internal interface RuntimeEffect {

    /**
     * Indicates if the current platform is supported
     */
    val supported: Boolean

    /**
     * Defines if the effect is ready to be displayed
     */
    val ready: Boolean

    /**
     * Updates the uniforms for the shader, on changes of the size or time.
     */
    fun updateUniforms(time: Float, width: Float, height: Float)

    /**
     * Builds an updates ShaderBrush
     */
    fun build(): Brush?
}

class NonAndroidRuntimeEffect(shader: Shader) : RuntimeEffect {
    private val compositeRuntimeEffect = runCatching { org.jetbrains.skia.RuntimeEffect.makeForShader(shader.sksl) }
        .onFailure { it.printStackTrace() }
        .getOrNull()
    private val compositeShaderBuilder = compositeRuntimeEffect?.let { RuntimeShaderBuilder(it) }

    override val supported: Boolean = true
    override var ready: Boolean = false

    override fun updateUniforms(time: Float, width: Float, height: Float) {
        compositeShaderBuilder?.uniform("uResolution", width, height, width / height)
        compositeShaderBuilder?.uniform("uTime", time)
        ready = width > 0 && height > 0
    }

    override fun build(): Brush? {
        return compositeShaderBuilder?.makeShader()?.let { ShaderBrush(it) }
    }
}

internal fun buildEffect(shader: Shader): RuntimeEffect {
    return NonAndroidRuntimeEffect(shader)
}


/**
 * Draw's the shader as background via the [drawBehind] modifier.
 *
 * When running on Android 13 or newer (Tiramisu), usage of this API renders the shader.
 * On older Android devices, the provided [fallback] Brush is used instead.
 *
 * @param shader Shader to use to draw. [Shader] class. Example [com.mikepenz.hypnoticcanvas.shaders.GlossyGradients].
 * @param speed Adjusts how fast the shader is animated
 * @param fallback The fallback brush to draw on unsupported devices
 */
@Composable
fun Modifier.shaderBackground(
    shader: Shader,
    speed: Float = 1f,
    fallback: () -> Brush = {
        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
    }
): Modifier {
    val runtimeEffect = remember(shader) { buildEffect(shader) }
    var size: Size by remember { mutableStateOf(Size(-1f, -1f)) }

    if (runtimeEffect.supported) {
        var startMillis = remember(shader) { -1L }
        val speedModifier = shader.speedModifier

        val time by produceState(0f, speedModifier) {
            while (true) {
                withInfiniteAnimationFrameMillis {
                    if (startMillis < 0) startMillis = it
                    value = ((it - startMillis) / 16.6f) / 10f
                }
            }
        }

        runtimeEffect.updateUniforms((time * speed * speedModifier).round(3), size.width, size.height) // set uniforms for the shaders
    }

    return this then Modifier.onGloballyPositioned {
        size = Size(it.size.width.toFloat(), it.size.height.toFloat())
    }.drawBehind {
        if (runtimeEffect.ready) {
            runtimeEffect.build()?.let { drawRect(brush = it) }
        } else {
            drawRect(brush = fallback())
        }
    }
}