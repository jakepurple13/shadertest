import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

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

val defaultShaderText = """
uniform float uTime;
uniform vec3 uResolution;

const float formuparam2 = 1.0;
const float cloud = 0.1;
const float iterations = 5.0;
const float volsteps = 4.0;
const float zoom = 5.0;
const float tile = 0.85;

const float stepsize = 0.390;
const float speed2 = 0.10;
 
const float speedz = 0.0;
const float brightness = 0.15;
const float darkmatter = 0.600;
const float distfading = 0.560;
const float saturation = 0.900;
const float transverseSpeed = zoom*2.0;
 
float triangle(float x, float a)
{
    float output2 = 2.0*abs(  2.0*  ( (x/a) - floor( (x/a) + 0.5) ) ) - 1.0;
    return output2;
}

float field(in vec3 p) {
	
	float strength = 7. + .03 * log(1.e-6 + fract(sin(uTime) * 4373.11));
	float accum = 0.;
	float prev = 0.;
	float tw = 0.;
	
	for (int i = 0; i < 6; ++i) {
		float mag = dot(p, p);
		p = abs(p) / mag + vec3(-.5, -.8 + 0.1*sin(uTime*0.2 + 2.0), -1.1+0.3*cos(uTime*0.15));
		float w = exp(-float(i) / 7.);
		accum += w * exp(-strength * pow(abs(mag - prev), 2.3));
		tw += w;
		prev = mag;
	}
	return max(0., 5. * accum / tw - .7);
}

vec4 main( vec2 fragCoord )
{
    vec2 uv2 = 2. * fragCoord.xy / uResolution.xy - 1.;
	vec2 uvs = uv2 * uResolution.xy / max(uResolution.x, uResolution.y);

	float time2 = uTime;
           
    float speed = speed2;
    //speed = 0.005 * cos(time2*0.02 + 3.1415926/4.0);
    speed = 0.00005 * speedz * time2;
          
	//speed = 0.0;

    float formuparam = formuparam2;

	//get coords and direction
	vec2 uv = uvs;
		       
	//mouse rotation
	float a_xz = 0.9;
	float a_yz = -.6;
	float a_xy = 0.9 + uTime*0.04;
	
	mat2 rot_xz = mat2(cos(a_xz),sin(a_xz),-sin(a_xz),cos(a_xz));
	mat2 rot_yz = mat2(cos(a_yz),sin(a_yz),-sin(a_yz),cos(a_yz));	
	mat2 rot_xy = mat2(cos(a_xy),sin(a_xy),-sin(a_xy),cos(a_xy));
	
	float v2 =1.0;
	
	vec3 dir=vec3(uv*zoom,1.);
 
	vec3 from=vec3(0.0, 0.0,0.0);
                   
    // from.x -= 5.0*(mouse.x-0.5);
    // from.y -= 5.0*(mouse.y-0.5);
           
    from.x -= 5.0*(1.0-0.5);
    from.y -= 5.0*(1.0-0.5);
               
	vec3 forward = vec3(0.,0.,1.);
    
	from.x += transverseSpeed*(1.0)*cos(0.01*uTime) + 0.001*uTime;
		from.y += transverseSpeed*(1.0)*sin(0.01*uTime) +0.001*uTime;
	
	from.z += 0.003*uTime;
	
	dir.xy*=rot_xy;
	forward.xy *= rot_xy;

	dir.xz*=rot_xz;
	forward.xz *= rot_xz;
	
	dir.yz*= rot_yz;
	forward.yz *= rot_yz;
	 
	from.xy*=-rot_xy;
	from.xz*=rot_xz;
	from.yz*= rot_yz;
	 
	//zoom
	float zooom = (time2-3311.)*speed;
	from += forward* zooom;
	float sampleShift = mod( zooom, stepsize );
	 
	float zoffset = -sampleShift;
	sampleShift /= stepsize; // make from 0 to 1

	//volumetric rendering
	float s=0.24;
	float s3 = s + stepsize/2.0;
	vec3 v=vec3(0.);
	float t3 = 0.0;
	
	vec3 backCol2 = vec3(0.);
	for (int r=0; r<20; r++) {
		if (r > int(volsteps)) {break;}
		vec3 p2=from+(s+zoffset)*dir;// + vec3(0.,0.,zoffset);
		vec3 p3=(from+(s3+zoffset)*dir )* (1.9/zoom);// + vec3(0.,0.,zoffset);
		
		p2 = abs(vec3(tile)-mod(p2,vec3(tile*2.))); // tiling fold
		p3 = abs(vec3(tile)-mod(p3,vec3(tile*2.))); // tiling fold
		
		t3 = field(p3);
		
		float pa,a=pa=0.;
		for (int i=0; i<20; i++) {
			if (i > int(iterations)) {break;}
			p2=abs(p2)/dot(p2,p2)-formuparam; // the magic formula
			//p=abs(p)/max(dot(p,p),0.005)-formuparam; // another interesting way to reduce noise
			float D = abs(length(p2)-pa); // absolute sum of average change
			
			if (i > 2)
			{
			    a += i > 7 ? min( 12., D) : D;
			}
            pa=length(p2);
		}
		
		//float dm=max(0.,darkmatter-a*a*.001); //dark matter
		a*=a*a; // add contrast
		//if (r>3) fade*=1.-dm; // dark matter, don't render near
		// brightens stuff up a bit
		float s1 = s+zoffset;
		// need closed form expression for this, now that we shift samples
		float fade = pow(distfading,max(0.,float(r)-sampleShift));
		
		//t3 += fade;
		
		v+=fade;
        //backCol2 -= fade;

		// fade out samples as they approach the camera
		if( r == 0 )
			fade *= (1. - (sampleShift));
		// fade in samples as they approach from the distance
		if( r == int(volsteps)-1 )
			fade *= sampleShift;
		v+=vec3(s1,s1*s1,s1*s1*s1*s1)*a*brightness*fade; // coloring based on distance
		
		backCol2 += mix(.4, 1., v2) * vec3(1.8 * t3 * t3 * t3, 1.4 * t3 * t3, t3) * fade;

		s+=stepsize;
		s3 += stepsize;
    }
		       
	v=mix(vec3(length(v)),v,saturation); //color adjust
	
	vec4 forCol2 = vec4(v*.01,1.);
	
	backCol2 *= cloud;
	backCol2.b *= 1.8;
	backCol2.r *= 0.55;
	
	backCol2.b = 0.5*mix(backCol2.g, backCol2.b, 0.8);
	backCol2.g = -0.5;

	backCol2.bg = mix(backCol2.gb, backCol2.bg, 0.5*(cos(uTime*0.01) + 1.0));
	return forCol2 + vec4(backCol2, 1.0);
}

    """

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
