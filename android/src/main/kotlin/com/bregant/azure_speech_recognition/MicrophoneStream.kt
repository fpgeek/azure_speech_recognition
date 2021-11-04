package com.bregant.azure_speech_recognition

import android.media.*
import com.microsoft.cognitiveservices.speech.audio.AudioStreamFormat
import com.microsoft.cognitiveservices.speech.audio.PullAudioInputStreamCallback
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MicrophoneStream(
    var SAMPLE_RATE: Int = 16000,
//    var thisFormat: AudioStreamFormat = AudioStreamFormat.getWaveFormatPCM(
//        16000.toLong(),
//        16.toShort(),
//        1.toShort()
//    ),
    var recorder: AudioRecord? = null,
    var recording: Boolean = false,
    var recordFilePath: String? = null
) : PullAudioInputStreamCallback() {
    var recordFileStream: FileOutputStream? = null
    var pcmFilePath: String? = null

    init {
        initMic();
    }

    //@Override
    override fun read(bytes: ByteArray): Int {
        val ret: Int = recorder!!.read(bytes, 0, bytes.size);
        if (this.recordFileStream != null) {
            try {
                this.recordFileStream!!.write(bytes);
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
        return ret;
    }


    override fun close() {
        saveRecordFile();
        this.recorder!!.release();
        this.recorder = null;
    }

    fun saveRecordFile() {
        if (this.recordFileStream != null) {
            this.recordFileStream!!.close();
            this.recordFileStream = null;
            PcmToWav.convert(File(this.pcmFilePath), File(this.recordFilePath), 1, SAMPLE_RATE, 16)
        }
    }

    fun initMic() {
        // Note: currently, the Speech SDK support 16 kHz sample rate, 16 bit samples, mono (single-channel) only.
        val af: AudioFormat = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
            .build();
        this.recorder = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            .setAudioFormat(af)
            .build();
        if (this.recording && this.recordFilePath != null) {
            this.pcmFilePath = this.recordFilePath!!.replace(".wav", ".pcm")
            this.recordFileStream = FileOutputStream(this.pcmFilePath);
        }

        this.recorder!!.startRecording();
    }

    private fun randomID(): String = List(16) {
        (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
    }.joinToString("")
}