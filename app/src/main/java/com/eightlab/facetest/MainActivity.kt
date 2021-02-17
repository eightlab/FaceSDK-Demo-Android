package com.eightlab.facetest

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.eightlab.facesdk.Constants
import co.eightlab.facesdk.FaceActivity
import co.eightlab.facesdk.FaceSdk
import co.eightlab.facesdk.model.FaceMatchResponse
import com.eightlab.facetest.util.compressImage
import com.eightlab.facetest.util.getCaptureImageIntent
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send
import java.io.File


class MainActivity : AppCompatActivity() {

    private var pickedImageFilePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.startButton).setOnClickListener {
            //Check storage write permission
            permissionsBuilder(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA).build().send { result ->
                // Handle the result.
                if (result.allGranted()) {
                    // All the permissions are granted.
                    //Now start image picker
                    startImagePicker()
                } else
                    Toast.makeText(this, "Please provide permission to continue", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode === FaceActivity.REQUEST_FINAL_RESULT) {
            if (resultCode === Activity.RESULT_OK && data != null) {
                if (data.hasExtra(Constants.FINAL_RESULT)) {
                    val faceMatchResponse: FaceMatchResponse? = data.getParcelableExtra<FaceMatchResponse>(Constants.FINAL_RESULT)
                    // Received Face Verification results as an instance of `FaceMatchResponse` class
                    if (faceMatchResponse?.getSuccess() == true) {
                        // Handle success case here
                        Toast.makeText(this, "Face verification successful.", Toast.LENGTH_LONG).show()
                    } else {
                        // Handle failed case here
                        Toast.makeText(this, "Face verification failed.", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                //Backpressed clicked, cancelled by user
                if (BuildConfig.DEBUG) Log.v("TAG", "Request Cancelled: \nRequest code: $requestCode\nResult code : $resultCode")
            }
        } else if (requestCode == PICK_IMAGE_FILE_RESULT_CODE && resultCode === Activity.RESULT_OK) {
            //Image file picked
            var file: File? = File(pickedImageFilePath)
            if (file?.exists() == true) {
                //First compress the image to decrease the size
                compressImage(this, pickedImageFilePath) {
                    if (it)
                    //If file exist and valid, start face SDK
                        startFaceSdk(pickedImageFilePath!!)
                }

            } else
                Toast.makeText(this, "File isn't valid", Toast.LENGTH_SHORT).show()

        }
    }

    /**
     * Start Liveness
     */
    private fun startFaceSdk(path: String) {
        FaceSdk.Builder()
                .apiKey("paste_provided_license_key")
                .imageUri(path) //Pass face photo file path
                .voiceVerification(false) // True if voice verification is to be enabled
                .build()
                .start(this)
    }


    /**
     * Open image file picker
     */
    private fun startImagePicker() {
        startActivityForResult(getCaptureImageIntent(this) { filePath ->
            pickedImageFilePath = filePath
        }, PICK_IMAGE_FILE_RESULT_CODE)
    }


    companion object {

        const val PICK_IMAGE_FILE_RESULT_CODE = 111
    }

}