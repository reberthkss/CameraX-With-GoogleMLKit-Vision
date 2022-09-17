package com.example.camerax

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.widget.Toast
import androidx.camera.lifecycle.ProcessCameraProvider
import android.util.Log
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCaptureException
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.PermissionChecker
import com.example.camerax.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.camerax.presentation.vm.MainViewModel
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.interfaces.Detector
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel>()

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
        setup()
    }

    private fun setup() {
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.smilingProbability.observe(this) { newSmilingProbability ->
            viewBinding.smileTemperature.alpha = newSmilingProbability
        }
        viewModel.semaphoreProbability.observe(this) { semaphoreProbability ->
            updateSemaphore(semaphoreProbability)

        }
    }

    private fun updateSemaphore(semaphoreProbability: Float) {
        viewBinding.semaphore10.alpha = if (semaphoreProbability < 0.1f) {
            0.5f
        } else {
            semaphoreProbability
        }
        viewBinding.semaphore20.alpha = if (semaphoreProbability < 0.2f) {
            0.5f
        } else {
            semaphoreProbability
        }
        viewBinding.semaphore30.alpha = if (semaphoreProbability < 0.3f) {
            0.5f
        } else {
            semaphoreProbability
        }
        viewBinding.semaphore40.alpha = if (semaphoreProbability < 0.4f) {
            0.5f
        } else {
            semaphoreProbability
        }
        viewBinding.semaphore50.alpha = if (semaphoreProbability < 0.5f) {
            0.5f
        } else {
            semaphoreProbability
        }
        viewBinding.semaphore60.alpha = if (semaphoreProbability < 0.6f) {
            0.5f
        } else {
            semaphoreProbability
        }
        viewBinding.semaphore70.alpha = if (semaphoreProbability < 0.7f) {
            0.5f
        } else {
            semaphoreProbability
        }
        viewBinding.semaphore80.alpha = if (semaphoreProbability < 0.8f) {
            0.5f
        } else {
            semaphoreProbability
        }
        viewBinding.semaphore90.alpha = if (semaphoreProbability < 0.9f) {
            0.5f
        } else {
            semaphoreProbability
        }
        viewBinding.semaphore100.alpha = if (semaphoreProbability < 0.98f) {
            0.5f
        } else {
            semaphoreProbability
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraController = LifecycleCameraController(this)

            val faceDetectionOptions = FaceDetectorOptions.Builder()
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()
            val faceDetector = FaceDetection.getClient(faceDetectionOptions)

            try {
                cameraController.unbind()
                val executor = ContextCompat.getMainExecutor(this)
                cameraController.setImageAnalysisAnalyzer(
                    executor,
                    MlKitAnalyzer(
                        listOf(faceDetector) as List<Detector<*>>,
                        COORDINATE_SYSTEM_VIEW_REFERENCED,
                        executor
                    ) { result: MlKitAnalyzer.Result? ->
                        result
                            ?.getValue(faceDetector)
                            ?.firstOrNull { it != null }
                            ?.smilingProbability
                            ?.let { smilingProbability ->
                                viewModel.setSmilingProbability(smilingProbability)
                            }
                    }
                )
                cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                cameraController.bindToLifecycle(this)
                viewBinding.viewFinder.controller = cameraController

            } catch(exc: Exception) {

            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

}
