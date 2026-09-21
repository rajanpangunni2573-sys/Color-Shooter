package com.example.game

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundEffects {
    private val scope = CoroutineScope(Dispatchers.Default)
    var isSfxEnabled: Boolean = true
    var isMusicEnabled: Boolean = true

    fun playPop() {
        if (!isSfxEnabled) return
        scope.launch {
            // High snappy chirp/pop from 600Hz to 1200Hz in 60ms
            generateChirp(startFreq = 500f, endFreq = 1100f, durationMs = 65, volume = 0.85f)
        }
    }

    fun playLaunch() {
        if (!isSfxEnabled) return
        scope.launch {
            // Whoosh sweep
            generateChirp(startFreq = 220f, endFreq = 440f, durationMs = 80, volume = 0.5f)
        }
    }

    fun playBomb() {
        if (!isSfxEnabled) return
        scope.launch {
            // Deep low rumble explosion
            generateNoiseBurst(durationMs = 220, baseFreq = 85f, volume = 0.95f)
        }
    }

    fun playRainbow() {
        if (!isSfxEnabled) return
        scope.launch {
            // Sparkling high arpeggio
            val notes = listOf(523.25f, 659.25f, 783.99f, 1046.50f)
            for (note in notes) {
                generateTone(freq = note, durationMs = 45, volume = 0.6f)
            }
        }
    }

    fun playOrphanDrop() {
        if (!isSfxEnabled) return
        scope.launch {
            generateChirp(startFreq = 700f, endFreq = 300f, durationMs = 90, volume = 0.7f)
        }
    }

    fun playVictory() {
        if (!isSfxEnabled) return
        scope.launch {
            val notes = listOf(523.25f, 659.25f, 783.99f, 1046.50f, 1318.51f)
            for (note in notes) {
                generateTone(freq = note, durationMs = 110, volume = 0.8f)
            }
        }
    }

    fun playDefeat() {
        if (!isSfxEnabled) return
        scope.launch {
            val notes = listOf(440f, 370f, 311f, 261f)
            for (note in notes) {
                generateTone(freq = note, durationMs = 130, volume = 0.75f)
            }
        }
    }

    fun playClick() {
        if (!isSfxEnabled) return
        scope.launch {
            generateTone(freq = 880f, durationMs = 25, volume = 0.4f)
        }
    }

    private fun generateTone(freq: Float, durationMs: Int, volume: Float) {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        if (numSamples <= 0) return
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val time = i.toDouble() / sampleRate
            val envelope = (1.0 - (i.toDouble() / numSamples)).toFloat() // linear decay
            val wave = sin(2.0 * Math.PI * freq * time).toFloat()
            samples[i] = (wave * envelope * volume * Short.MAX_VALUE).toInt().toShort()
        }
        playAudioPcm(samples, sampleRate)
    }

    private fun generateChirp(startFreq: Float, endFreq: Float, durationMs: Int, volume: Float) {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        if (numSamples <= 0) return
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            val time = i.toDouble() / sampleRate
            val envelope = (1f - progress * 0.7f)
            val wave = sin(2.0 * Math.PI * currentFreq * time).toFloat()
            samples[i] = (wave * envelope * volume * Short.MAX_VALUE).toInt().toShort()
        }
        playAudioPcm(samples, sampleRate)
    }

    private fun generateNoiseBurst(durationMs: Int, baseFreq: Float, volume: Float) {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        if (numSamples <= 0) return
        val samples = ShortArray(numSamples)
        val random = java.util.Random()

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val envelope = (1f - progress) * (1f - progress)
            val noise = (random.nextFloat() * 2f - 1f) * 0.4f
            val lowSine = sin(2.0 * Math.PI * baseFreq * (i.toDouble() / sampleRate)).toFloat() * 0.6f
            val sampleVal = (noise + lowSine) * envelope * volume
            samples[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playAudioPcm(samples, sampleRate)
    }

    private fun playAudioPcm(samples: ShortArray, sampleRate: Int) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()
            // Clean up after playback
            scope.launch {
                kotlinx.coroutines.delay((samples.size * 1000L / sampleRate) + 50)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // Audio output device busy or unavailable
        }
    }
}
