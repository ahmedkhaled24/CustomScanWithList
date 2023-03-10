package com.example.customscanwithlist

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.SurfaceHolder
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

private const val TAG = "TAGMainActivity"
class MainActivity : AppCompatActivity() {

    private var arr: MutableList<DataScan> = ArrayList()
    private var barcodeDetector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var dataAdapter: DataAdapter
    var cameraStatus = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if(it) {
                recreate()
            } else {
                showPermissionReason()
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            initialiseDetectorsAndSources()
        }
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
                cameraSource!!.stop()
            }
        })

        barcodeDetector!!.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(applicationContext, "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() != 0) {
                    if (cameraStatus){
                        cameraStatus = false
                        Log.d(TAG, "receiveDetections: ${barcodes.valueAt(0).displayValue}")
                        setUpAdapter()
                        edt()
                    }
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
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            dataAdapter = DataAdapter(arr)
            recyclerView!!.adapter = dataAdapter
            alertDialog()
        }
    }

    @SuppressLint("MissingPermission")
    private fun alertDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Scanning...")
        builder.setPositiveButton("Resume") { dialog, _ ->
            cameraStatus = true
            dialog.dismiss()
        }
        builder.setNegativeButton("Close") { dialog, _ ->
            cameraStatus = false
            dialog.dismiss()
        }
        builder.show()
    }


    @SuppressLint("SetTextI18n")
    private fun edt(barcode: Long){
        val inputEditTextField = EditText(this)
        inputEditTextField.textSize = 25f
        inputEditTextField.gravity = Gravity.CENTER
        inputEditTextField.inputType = InputType.TYPE_CLASS_NUMBER
        val dialog = AlertDialog.Builder(this)
            .setTitle("Enter the quantity for (${barcode})")
            .setView(inputEditTextField)
            .setPositiveButton("Add") { _, _ ->
                val editTextInput = inputEditTextField.text.toString()
                if (editTextInput.toInt() != 0){
                    arr.add(DataScan(barcode, 1))
                }
            }
            .setNegativeButton("Remove", null).create()
        dialog.show()
    }


}
