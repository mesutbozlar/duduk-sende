package com.armeaze.duduksende

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.PI
import kotlin.math.sin

/** Synthesised sound effects (no audio files needed) + vibration. */
object Sfx {
    private const val SR = 22050
    private var ctx: Context? = null
    private var store: Store? = null
    private val handler = Handler(Looper.getMainLooper())

    fun init(c: Context, s: Store) {
        ctx = c.applicationContext
        store = s
    }

    private fun note(freq: Double, ms: Int, vol: Double, vibHz: Double = 0.0, vibDepth: Double = 0.0, square: Boolean = false): ShortArray {
        val n = SR * ms / 1000
        val out = ShortArray(n)
        var phase = 0.0
        for (i in 0 until n) {
            val t = i.toDouble() / SR
            val f = freq + vibDepth * sin(2 * PI * vibHz * t)
            phase += 2 * PI * f / SR
            val edge = minOf(i, n - i).toDouble() / (SR * 0.01)
            val env = if (edge > 1.0) 1.0 else edge
            var s = sin(phase)
            if (square) s = if (s >= 0) 0.6 else -0.6
            out[i] = (s * env * vol * 32767.0).toInt().toShort()
        }
        return out
    }

    private val sCorrect: ShortArray by lazy { note(660.0, 90, 0.5) + note(880.0, 160, 0.5) }
    private val sWrong: ShortArray by lazy { note(196.0, 180, 0.5, square = true) + note(147.0, 280, 0.5, square = true) }
    private val sWhistle: ShortArray by lazy { note(2900.0, 750, 0.45, vibHz = 38.0, vibDepth = 170.0) }
    private val sWin: ShortArray by lazy { note(523.0, 120, 0.5) + note(659.0, 120, 0.5) + note(784.0, 120, 0.5) + note(1047.0, 300, 0.5) }

    private fun play(data: ShortArray) {
        if (store?.soundOn != true) return
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SR)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(data.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(data, 0, data.size)
            track.play()
            handler.postDelayed({ try { track.release() } catch (e: Exception) {} }, data.size * 1000L / SR + 300L)
        } catch (e: Exception) {
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrator(c: Context): Vibrator =
        if (Build.VERSION.SDK_INT >= 31) (c.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        else c.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private fun vibrate(pattern: LongArray) {
        if (store?.vibOn != true) return
        val c = ctx ?: return
        try {
            vibrator(c).vibrate(VibrationEffect.createWaveform(pattern, -1))
        } catch (e: Exception) {
        }
    }

    fun correct() { play(sCorrect); vibrate(longArrayOf(0, 40)) }
    fun wrong() { play(sWrong); vibrate(longArrayOf(0, 90, 70, 90)) }
    fun whistle() { play(sWhistle); vibrate(longArrayOf(0, 120)) }
    fun win() { play(sWin); vibrate(longArrayOf(0, 60, 60, 60, 60, 160)) }
}
