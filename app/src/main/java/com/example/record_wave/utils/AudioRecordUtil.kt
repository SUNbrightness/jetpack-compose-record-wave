package com.example.record_wave.utils

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder

class AudioRecordUtil {
    private val audioSource = MediaRecorder.AudioSource.MIC
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var isRecording = false
    private var isPlaying = false

    private var playbackEnabled = false


    private val buffer = mutableListOf<Short>()

    // 获取缓冲的录制数据
    fun getBufferedData(): List<Short> {
        synchronized(buffer) {
            val data = buffer.toList()
            buffer.clear()
            return data
        }
    }


    @SuppressLint("MissingPermission")
    fun startRecording(playbackEnabled:Boolean= false,callback: ((ShortArray) -> Unit)?=null) {
        if (isRecording) {
            stopRecording()
        } else {
            this.playbackEnabled = playbackEnabled

            audioRecord = AudioRecord(
                audioSource,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            audioRecord?.startRecording()

            isRecording = true

            Thread {
                val data = ShortArray(bufferSize)

                while (isRecording) {
                    audioRecord?.read(data, 0, bufferSize)
                    if (playbackEnabled && isPlaying) {
                        audioTrack?.write(data, 0, bufferSize)
                    }
                    buffer.addAll(data.toList())
                    callback?.invoke(data)
                }

                audioRecord?.stop()
                audioRecord?.release()
                audioRecord = null

                isRecording = false
            }.start()
        }
    }

    fun stopRecording() {
        isRecording = false
    }

    fun togglePlayback(playbackEnabled: Boolean = false) {
        if(playbackEnabled && !isPlaying){
            if (audioTrack == null) {
                audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    audioFormat,
                    bufferSize,
                    AudioTrack.MODE_STREAM
                )
            }

            audioTrack?.play()

            isPlaying = true
        }else if(isPlaying && !playbackEnabled){
            stopPlayback()
        }
    }

    private fun stopPlayback() {
        audioTrack?.apply {
            stop()
            flush()
            isPlaying = false
        }
    }


}