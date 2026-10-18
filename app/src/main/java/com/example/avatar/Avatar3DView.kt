package com.example.avatar

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaCosmicBg
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanGlow
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceCardBorder
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaWarning
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3D Vector mathematical representation for perspective projection.
 */
data class Point3D(val x: Float, val y: Float, val z: Float) {
    fun rotateY(angleRad: Float): Point3D {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Point3D(
            x = x * cosA + z * sinA,
            y = y,
            z = -x * sinA + z * cosA
        )
    }

    fun rotateX(angleRad: Float): Point3D {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Point3D(
            x = x,
            y = y * cosA - z * sinA,
            z = y * sinA + z * cosA
        )
    }

    fun project(viewportWidth: Float, viewportHeight: Float, fov: Float = 400f): Offset {
        val distance = 350f
        val factor = fov / (distance + z).coerceAtLeast(10f)
        return Offset(
            x = viewportWidth / 2f + x * factor,
            y = viewportHeight / 2f + y * factor
        )
    }
}

@Composable
fun Avatar3DView(
    state: AvatarState,
    modifier: Modifier = Modifier,
    heightDp: Int = 180
) {
    // Interactive Touch Rotation
    var userYaw by remember { mutableFloatStateOf(0f) }
    var userPitch by remember { mutableFloatStateOf(0f) }

    // Breathing Animation
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_anim")
    val breathCycle by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // Orbital Halo Rotation
    val haloAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo"
    )

    // Periodic Natural Eye Blink
    val blinkProgress by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )

    // Mouth Viseme / Phoneme Animation during speaking
    val mouthViseme by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(180, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "viseme"
    )

    // State-based head tilt / nod interpolation
    val targetHeadPitch = when (state) {
        AvatarState.THINKING -> -0.15f
        AvatarState.APOLOGETIC, AvatarState.SAD -> 0.14f
        AvatarState.LISTENING -> -0.05f
        AvatarState.TASK_SUCCESS, AvatarState.HAPPY -> 0.08f
        else -> 0f
    }
    val animatedHeadPitch by animateFloatAsState(targetValue = targetHeadPitch, label = "head_pitch")

    val targetHeadRoll = when (state) {
        AvatarState.CONFUSED -> 0.16f
        AvatarState.LISTENING -> 0.08f
        AvatarState.APOLOGETIC -> -0.08f
        else -> 0f
    }
    val animatedHeadRoll by animateFloatAsState(targetValue = targetHeadRoll, label = "head_roll")

    // State Color Aura
    val auraColor = when (state) {
        AvatarState.LISTENING -> NovaCyan
        AvatarState.THINKING -> NovaPurple
        AvatarState.SPEAKING -> NovaCyanGlow
        AvatarState.APOLOGETIC, AvatarState.SAD -> NovaWarning
        AvatarState.TASK_SUCCESS, AvatarState.HAPPY -> NovaSuccess
        else -> NovaCyan
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(NovaSurfaceCard)
            .border(1.dp, NovaSurfaceCardBorder, RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        userYaw += dragAmount.x * 0.008f
                        userPitch = (userPitch + dragAmount.y * 0.008f).coerceIn(-0.35f, 0.35f)
                    },
                    onDragEnd = {
                        // Smoothly relax back toward center
                        userYaw *= 0.5f
                        userPitch *= 0.5f
                    }
                )
            }
            .testTag("avatar_3d_container")
    ) {
        // Real-Time 3D Perspective Canvas
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw 3D Holographic Backdrop Grid & Atmospheric Aura
            drawAtmosphericAura(width, height, auraColor, breathCycle)

            // Combined Yaw and Pitch
            val totalYaw = userYaw + animatedHeadRoll * 0.5f
            val totalPitch = userPitch + animatedHeadPitch + breathCycle * 0.02f

            // 2. Render 3D Body (Shoulders and Torso with Breathing)
            draw3DTorso(width, height, totalYaw, totalPitch, breathCycle, auraColor)

            // 3. Render 3D Neck & Cranium
            draw3DHead(width, height, totalYaw, totalPitch)

            // 4. Render Expressive 3D Facial Geometry & Blend Shapes
            draw3DFace(
                width = width,
                height = height,
                yaw = totalYaw,
                pitch = totalPitch,
                state = state,
                mouthViseme = if (state == AvatarState.SPEAKING) mouthViseme else 0.1f,
                blink = blinkProgress,
                auraColor = auraColor
            )

            // 5. Render 3D Orbital Cybernetic Crown & Constellation Particles
            draw3DOrbitalHalo(width, height, haloAngle, totalYaw, auraColor)
        }

        // Top Status Badge
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x990B0F19))
                .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(auraColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "YESMIN 3D • ${state.displayName.uppercase()}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NovaTextPrimary,
                    letterSpacing = 0.5.sp
                )
            )
        }

        // State indicator hint at bottom right
        Text(
            text = "Drag to rotate 3D view",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = NovaTextSecondary
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
        )
    }
}

private fun DrawScope.drawAtmosphericAura(
    width: Float,
    height: Float,
    auraColor: Color,
    breath: Float
) {
    val auraRadius = (width * 0.35f + breath * 10f).coerceAtLeast(40f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                auraColor.copy(alpha = 0.22f),
                auraColor.copy(alpha = 0.05f),
                Color.Transparent
            ),
            center = Offset(width / 2f, height / 2f - 10f),
            radius = auraRadius
        )
    )
}

private fun DrawScope.draw3DTorso(
    width: Float,
    height: Float,
    yaw: Float,
    pitch: Float,
    breath: Float,
    accentColor: Color
) {
    val breathOffset = breath * 3f
    val leftShoulder = Point3D(-75f, 65f + breathOffset, 20f).rotateY(yaw * 0.6f).rotateX(pitch * 0.6f).project(width, height)
    val rightShoulder = Point3D(75f, 65f + breathOffset, 20f).rotateY(yaw * 0.6f).rotateX(pitch * 0.6f).project(width, height)
    val chestCenter = Point3D(0f, 75f + breathOffset, 30f).rotateY(yaw * 0.6f).rotateX(pitch * 0.6f).project(width, height)
    val neckBase = Point3D(0f, 38f + breathOffset * 0.5f, 15f).rotateY(yaw * 0.8f).rotateX(pitch * 0.8f).project(width, height)

    // Cyber suit shoulders
    val suitPath = Path().apply {
        moveTo(leftShoulder.x, leftShoulder.y)
        quadraticTo(chestCenter.x, chestCenter.y, rightShoulder.x, rightShoulder.y)
        lineTo(rightShoulder.x, height + 40f)
        lineTo(leftShoulder.x, height + 40f)
        close()
    }

    drawPath(
        path = suitPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A), NovaCosmicBg),
            startY = leftShoulder.y - 20f,
            endY = height
        )
    )

    // Collar / cyber armor accent line
    val collarPath = Path().apply {
        moveTo(leftShoulder.x + 20f, leftShoulder.y + 5f)
        lineTo(neckBase.x, neckBase.y + 15f)
        lineTo(rightShoulder.x - 20f, rightShoulder.y + 5f)
    }
    drawPath(
        path = collarPath,
        color = accentColor.copy(alpha = 0.8f),
        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.draw3DHead(width: Float, height: Float, yaw: Float, pitch: Float) {
    // 3D Cranium Base
    val headCenter = Point3D(0f, -22f, 0f).rotateY(yaw).rotateX(pitch).project(width, height)
    val headRadius = 48f

    // Stylized Futuristic Hair Silhouette (Back)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF1E1B4B), Color(0xFF0F172A)),
            center = headCenter,
            radius = headRadius * 1.25f
        ),
        radius = headRadius * 1.2f,
        center = Offset(headCenter.x, headCenter.y - 4f)
    )

    // 3D Smooth Face Contour
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFDFD3), // Smooth porcelain tone
                Color(0xFFF3C4B6),
                Color(0xFFDCA496)
            ),
            center = Offset(headCenter.x - yaw * 15f, headCenter.y - 10f),
            radius = headRadius
        ),
        radius = headRadius,
        center = headCenter
    )

    // Ear points projected in 3D
    val leftEar = Point3D(-46f, -20f, 0f).rotateY(yaw).rotateX(pitch).project(width, height)
    val rightEar = Point3D(46f, -20f, 0f).rotateY(yaw).rotateX(pitch).project(width, height)

    drawCircle(Color(0xFFEAA698), radius = 6f, center = leftEar)
    drawCircle(Color(0xFFEAA698), radius = 6f, center = rightEar)

    // Sleek front hair bangs with cyber highlights
    val hairPath = Path().apply {
        moveTo(headCenter.x - headRadius * 0.95f, headCenter.y - headRadius * 0.4f)
        quadraticTo(headCenter.x - headRadius * 0.3f, headCenter.y - headRadius * 1.1f, headCenter.x, headCenter.y - headRadius * 0.6f)
        quadraticTo(headCenter.x + headRadius * 0.4f, headCenter.y - headRadius * 1.1f, headCenter.x + headRadius * 0.95f, headCenter.y - headRadius * 0.4f)
        lineTo(headCenter.x + headRadius * 0.7f, headCenter.y - headRadius * 0.8f)
        lineTo(headCenter.x - headRadius * 0.7f, headCenter.y - headRadius * 0.8f)
        close()
    }
    drawPath(path = hairPath, color = Color(0xFF1E1B4B))
}

private fun DrawScope.draw3DFace(
    width: Float,
    height: Float,
    yaw: Float,
    pitch: Float,
    state: AvatarState,
    mouthViseme: Float,
    blink: Float,
    auraColor: Color
) {
    // 3D Anchor points for facial features
    val leftEye3D = Point3D(-16f, -25f, 38f).rotateY(yaw).rotateX(pitch).project(width, height)
    val rightEye3D = Point3D(16f, -25f, 38f).rotateY(yaw).rotateX(pitch).project(width, height)
    val mouth3D = Point3D(0f, 2f, 40f).rotateY(yaw).rotateX(pitch).project(width, height)
    val nose3D = Point3D(0f, -12f, 44f).rotateY(yaw).rotateX(pitch).project(width, height)

    // Nose
    drawCircle(Color(0xFFD69384), radius = 2f, center = nose3D)

    // Eyes: Eyelashes & Pupils
    val eyeWidth = 10f
    val isBlinking = blink > 0.96f
    val eyeHeight = if (isBlinking) 1f else when (state) {
        AvatarState.SURPRISED -> 11f
        AvatarState.HAPPY -> 5f
        AvatarState.SAD, AvatarState.APOLOGETIC -> 6f
        else -> 8f
    }

    listOf(leftEye3D, rightEye3D).forEach { eyeCenter ->
        if (isBlinking) {
            drawLine(
                color = Color(0xFF1E293B),
                start = Offset(eyeCenter.x - eyeWidth, eyeCenter.y),
                end = Offset(eyeCenter.x + eyeWidth, eyeCenter.y),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
        } else {
            // White sclera
            drawOval(
                color = Color.White,
                topLeft = Offset(eyeCenter.x - eyeWidth / 2f, eyeCenter.y - eyeHeight / 2f),
                size = androidx.compose.ui.geometry.Size(eyeWidth, eyeHeight)
            )

            // Iris & Pupil (Cyan Cyberpunk Glow)
            val pupilOffset = when (state) {
                AvatarState.THINKING -> Offset(0f, -2f)
                AvatarState.SAD, AvatarState.APOLOGETIC -> Offset(0f, 2f)
                else -> Offset(yaw * 3f, pitch * 2f)
            }
            drawCircle(
                color = NovaCyan,
                radius = 3.5f,
                center = eyeCenter + pupilOffset
            )
            drawCircle(
                color = Color(0xFF0F172A),
                radius = 2f,
                center = eyeCenter + pupilOffset
            )
            // Catchlight sparkle
            drawCircle(
                color = Color.White,
                radius = 1f,
                center = eyeCenter + pupilOffset + Offset(-1f, -1f)
            )
        }
    }

    // Eyebrows
    val browOffset = when (state) {
        AvatarState.SURPRISED -> -4f
        AvatarState.CONFUSED -> -2f
        AvatarState.THINKING -> -1f
        AvatarState.APOLOGETIC, AvatarState.SAD -> 2f
        else -> 0f
    }
    // Left eyebrow
    drawLine(
        color = Color(0xFF334155),
        start = Offset(leftEye3D.x - 10f, leftEye3D.y - 8f + browOffset),
        end = Offset(leftEye3D.x + 8f, leftEye3D.y - 7f + (if (state == AvatarState.APOLOGETIC) browOffset - 2f else browOffset)),
        strokeWidth = 2f,
        cap = StrokeCap.Round
    )
    // Right eyebrow
    drawLine(
        color = Color(0xFF334155),
        start = Offset(rightEye3D.x - 8f, rightEye3D.y - 7f + (if (state == AvatarState.APOLOGETIC) browOffset - 2f else browOffset)),
        end = Offset(rightEye3D.x + 10f, rightEye3D.y - 8f + browOffset),
        strokeWidth = 2f,
        cap = StrokeCap.Round
    )

    // Cheeks (Gentle blush)
    if (state == AvatarState.HAPPY || state == AvatarState.APOLOGETIC || state == AvatarState.TASK_SUCCESS) {
        drawCircle(
            color = Color(0x33FF6B6B),
            radius = 7f,
            center = Offset(leftEye3D.x - 4f, leftEye3D.y + 12f)
        )
        drawCircle(
            color = Color(0x33FF6B6B),
            radius = 7f,
            center = Offset(rightEye3D.x + 4f, rightEye3D.y + 12f)
        )
    }

    // Mouth with real-time phoneme / expression blend shape
    val mouthPath = Path()
    when (state) {
        AvatarState.SPEAKING -> {
            // Dynamic Phoneme opening
            val openH = (mouthViseme * 9f).coerceAtLeast(2f)
            mouthPath.moveTo(mouth3D.x - 10f, mouth3D.y)
            mouthPath.quadraticTo(mouth3D.x, mouth3D.y - openH * 0.5f, mouth3D.x + 10f, mouth3D.y)
            mouthPath.quadraticTo(mouth3D.x, mouth3D.y + openH, mouth3D.x - 10f, mouth3D.y)
            mouthPath.close()
            drawPath(path = mouthPath, color = Color(0xFFBE123C))
        }
        AvatarState.HAPPY, AvatarState.TASK_SUCCESS -> {
            // Warm smiling curve
            mouthPath.moveTo(mouth3D.x - 11f, mouth3D.y - 2f)
            mouthPath.quadraticTo(mouth3D.x, mouth3D.y + 6f, mouth3D.x + 11f, mouth3D.y - 2f)
            drawPath(path = mouthPath, color = Color(0xFFBE123C), style = Stroke(width = 2.5f, cap = StrokeCap.Round))
        }
        AvatarState.APOLOGETIC, AvatarState.SAD -> {
            // Slight apologetic curve
            mouthPath.moveTo(mouth3D.x - 8f, mouth3D.y + 2f)
            mouthPath.quadraticTo(mouth3D.x, mouth3D.y - 2f, mouth3D.x + 8f, mouth3D.y + 2f)
            drawPath(path = mouthPath, color = Color(0xFFBE123C), style = Stroke(width = 2.2f, cap = StrokeCap.Round))
        }
        AvatarState.SURPRISED -> {
            // Small round 'O'
            drawCircle(Color(0xFFBE123C), radius = 4f, center = mouth3D)
        }
        else -> {
            // Subtle pleasant rest line
            mouthPath.moveTo(mouth3D.x - 8f, mouth3D.y)
            mouthPath.quadraticTo(mouth3D.x, mouth3D.y + 2f, mouth3D.x + 8f, mouth3D.y)
            drawPath(path = mouthPath, color = Color(0xFFBE123C), style = Stroke(width = 2f, cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.draw3DOrbitalHalo(
    width: Float,
    height: Float,
    haloAngle: Float,
    yaw: Float,
    auraColor: Color
) {
    val haloRadius = 75f
    val particleCount = 8
    val baseAngleRad = Math.toRadians(haloAngle.toDouble()).toFloat()

    for (i in 0 until particleCount) {
        val angle = baseAngleRad + (i * (2 * Math.PI / particleCount).toFloat())
        val x = cos(angle) * haloRadius
        val z = sin(angle) * haloRadius
        val y = -45f + sin(angle * 2f) * 10f

        // 3D projection
        val p3D = Point3D(x, y, z).rotateY(yaw).project(width, height)

        // Depth-scaled sizing (larger in front, smaller in back)
        val depthScale = ((z + 100f) / 200f).coerceIn(0.4f, 1.2f)
        val particleRadius = 3.5f * depthScale
        val alpha = (depthScale * 0.9f).coerceIn(0.2f, 1f)

        drawCircle(
            color = if (i % 2 == 0) auraColor.copy(alpha = alpha) else NovaPurple.copy(alpha = alpha),
            radius = particleRadius,
            center = p3D
        )
    }
}
