package com.example.record_wave.utils

import android.content.Context
import android.widget.Toast
import java.io.File

object FileUtil {

    lateinit var externalFilesDir: File;

    lateinit var bikeSavePath: String;

    fun init(context:Context){
        externalFilesDir = context.getExternalFilesDir(null)!!

        val directory: File = File(externalFilesDir, "bick_test")
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                Toast.makeText(context, "Directory created", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to create directory", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Directory already exists", Toast.LENGTH_SHORT).show()
        }
        this.bikeSavePath = directory.path

    }
}