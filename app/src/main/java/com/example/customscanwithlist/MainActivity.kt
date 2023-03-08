package com.example.customscanwithlist

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

private const val TAG = "TAGMainActivity"
class MainActivity : AppCompatActivity() {

    private var arr: MutableList<Long> = ArrayList()
    private var size = 0
    private var barcodeDetector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var dataAdapter: DataAdapter
    var cameraStatus = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                Log.d(TAG, "ruvbvdd cameraStatus: $cameraStatus")
                Log.d(TAG, "ruvbvdd arr.size: ${arr.size}")
                Log.d(TAG, "ruvbvdd size: ${size}")
                if (cameraStatus) {

                    if (size != arr.size) {
                        setUpAdapter()
                        size = arr.size
                        alertDialog()
                    }
                }
                mainHandler.postDelayed(this, 1000)
            }
        })


        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if(it)
                recreate()
            else
                showPermissionReason()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED )
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        else
            initialiseDetectorsAndSources()
    }

    private fun initialiseDetectorsAndSources() {
        Toast.makeText(applicationContext, "Barcode scanner started", Toast.LENGTH_SHORT).show()
        barcodeDetector = BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()
        cameraSource = CameraSource.Builder(this, barcodeDetector!!)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1280, 1024)
            .setAutoFocusEnabled(true)
            .build()

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                        cameraSource!!.start(surfaceView.holder)

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }



            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                stopCamera()
            }
        })

        barcodeDetector!!.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(applicationContext, "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() != 0) {
                    Log.d(TAG, "receiveDetections: ${barcodes.valueAt(0).displayValue}")
                    arr.add(barcodes.valueAt(0).displayValue.toLong())
                    stopCamera()
                }
            }
        })
    }

    private fun showPermissionReason(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.dialog_camera_permission_header))
        builder.setMessage(getString(R.string.dialog_camera_permission_body))
        builder.setPositiveButton(getString(R.string.dialog_location_permission_positive)) { dialog, _ ->
            dialog.dismiss()
            finish()
        }

        builder.setNegativeButton(getString(R.string.dialog_location_permission_negative)) { dialog, _ ->
            dialog.dismiss()
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun setUpAdapter(){
        val layoutManager = LinearLayoutManager(this)
        dataAdapter = DataAdapter(arr)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.adapter = dataAdapter
    }

    @SuppressLint("MissingPermission")
    private fun alertDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Scanning...")

        builder.setPositiveButton("Resume") { dialog, _ ->
            startCamera()
            dialog.dismiss()
        }

        builder.setNegativeButton("Close") { dialog, _ ->
            stopCamera()
            dialog.dismiss()
        }

        builder.show()
    }

    private fun stopCamera() {
        cameraStatus = false
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            cameraSource!!.stop()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startCamera() {
        cameraStatus = true
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            cameraSource!!.start()
            cameraSource!!.start(surfaceView.holder)
        }
    }
}
