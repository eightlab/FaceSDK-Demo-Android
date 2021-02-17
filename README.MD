# Face Vision Demo
This is a sample application from EightLab for face verification. 

Integration Steps
=================

1. Add it in your root build.gradle at the end of repositories
  ```gradle
    allprojects {
        repositories {
            maven {
            url "http://maven.eightsquare.co:8081/artifactory/libs-release-local"
            credentials {
                username <USERNAME_HERE>
                password <PASSWORD_HERE>
            }
        }
        }
    }
```
2. Add the dependency
```gradle
   dependencies {
         implementation 'co.eightlab:facevision:1.0.2-dev13'
   }
```

3. Start Face Verification by
```kotlin
 FaceSdk.Builder()
                .apiKey("license_key")
                .imageUri("uri_to_the_image_to_be_verified")
                .voiceVerification(false) // True if voice verification is to be enabled
                .build()
                .start(this)
```
4. Receive results in `onActivityResult`
```kotlin
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
            }
    }
```

  Parameters
  ----------
  * `apiKey` - Get valid licnese key from support team
  * `imageUri` - Uri to the image to match with live person
  * `voiceVerification` - Option to toogle voice verification
  
  Verification Results
  ----------
  Result Class `FaceMatchResponse`
  * To get success status, use `getSuccess()`
  * To get face matching confidence, use `getConfidence()`
  * To get the message from verification server, use `getMessage()`
  
Notes
=======

1. Migrate to AndroidX, if current application is in support library. 
   Use the link below to migrate.
     	https://developer.android.com/jetpack/androidx/migrate

2. Add the code below in project's gradle.properties to enable 
```gradle
        android.useAndroidX=true
        android.enableJetifier=true
```

3. Add below inside android block if Java 8 incompatible error
```gradle
compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
   }
```

4. To keep only some CPU architectures, for example armeabi-v7a and arm64-v8a, add the following inside android block
```gradle
android {
    ...
    ndk {
        // Tells Gradle to package the following ABIs into your application
        abiFilters 'armeabi-v7a', 'arm64-v8a'
    }
}
```
