package com.eightlab.facetest.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.eightlab.facetest.BuildConfig
import me.shaohui.advancedluban.Luban
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun getCaptureImageIntent(
    context: Context?,
    filePathCallback: (filePath: String) -> Unit
): Intent? {

    val imageFile = createImageFile(context)
    filePathCallback.invoke(imageFile.absolutePath)
    val photoURI: Uri? = context?.let {
        FileProvider.getUriForFile(
            it,
            BuildConfig.APPLICATION_ID + ".provider",
            imageFile
        )
    }

    val allIntents: MutableList<Intent> = ArrayList()
    val packageManager = context?.packageManager
    val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    val listCam = packageManager?.queryIntentActivities(captureIntent, 0)
    if (listCam != null) {
        for (res in listCam) {
            val intent = Intent(captureIntent)
            intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
            intent.setPackage(res.activityInfo.packageName)
            if (photoURI != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }
            allIntents.add(intent)
        }
    }
    var mainIntent: Intent? = allIntents[allIntents.size - 1]
    for (intent in allIntents) {
        if (intent.component!!.className == "com.android.documentsui.DocumentsActivity") {
            mainIntent = intent
            break
        }
    }
    allIntents.remove(mainIntent)
    val chooserIntent = Intent.createChooser(mainIntent, "Select source")
    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toTypedArray())
    return chooserIntent
}


private fun createImageFile(context: Context?): File {
    val getImage = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    return File(getImage?.path, "facetest_$timeStamp.jpg")
}

fun compressImage(
    context: Context?,
    filePath: String?,
    compressedCallback: (success: Boolean) -> Unit
) {
    if (filePath?.isNullOrEmpty() == true) {
        compressedCallback.invoke(false)
        return
    }
    Luban.compress(File(filePath), context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES))
        .setMaxSize(1000)                // limit the final image size（unit：Kb）
        .putGear(Luban.CUSTOM_GEAR)     // use CUSTOM GEAR compression mode
        .asObservable()
        .subscribe({
            //success
            it.renameTo(File(filePath))
            it.delete()
            compressedCallback.invoke(true)
        }, {
            //on error
            compressedCallback.invoke(false)
        }) {
            //on complete
        }

}
