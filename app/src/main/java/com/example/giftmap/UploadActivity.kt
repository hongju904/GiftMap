package com.example.giftmap

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class UploadActivity : AppCompatActivity() {
    private val CLOUD_VISION_API_KEY = "292aa42d54eaef3434f99730c64a47bc5cc2daf8"

    private var selectedImageUri: Uri? = null
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val databaseRef = FirebaseDatabase.getInstance().getReference("user_data/$uid")
    private var progressDialog: ProgressDialog? = null
    private lateinit var bitmap: Bitmap

    private lateinit var itemEditText: EditText
    private lateinit var storeEditText: EditText
    private lateinit var dateEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        openGallery()

        storeEditText = findViewById(R.id.store2)
        itemEditText = findViewById(R.id.item2)
        dateEditText = findViewById(R.id.date2)

        val doneButton: Button = findViewById(R.id.done)
        doneButton.setOnClickListener {
            val store: String = storeEditText.text.toString()
            val item: String = itemEditText.text.toString()
            val date: String = dateEditText.text.toString()

            if (selectedImageUri != null) {
                showProgressDialog(this)
                uploadImageTOFirebase(selectedImageUri!!, store, item, date)
            }
        }
    }

    private fun openGallery() {
        val uploadImage = Intent(Intent.ACTION_PICK)
        uploadImage.type = "image/*"
        startActivityForResult(uploadImage, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data?.data
            val giftimg: ImageView = findViewById(R.id.giftimg)
            giftimg.setImageURI(selectedImageUri)

            bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), selectedImageUri)
            performOCR(bitmap)
        }
    }

    private fun uploadImageTOFirebase(uri: Uri, store: String?, item: String?, date: String?) {
        val fileName = "${SimpleDateFormat("yyyymmdd_HHmmss").format(Date())}"

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val reviewImage = stream.toByteArray()
        val simage = byteArrayToBinaryString(reviewImage)

        val key = databaseRef.push().key ?: ""
        val result = hashMapOf(
            "store" to store,
            "item" to item,
            "date" to date,
            "simage" to simage,
            "filename" to fileName
        )
        databaseRef.child(key).setValue(result)
            .addOnSuccessListener {
                hideProgressDialog()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_LONG).show()
                hideProgressDialog()
                finish()
            }
    }

    fun performOCR(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        val options = FirebaseVisionCloudTextRecognizerOptions.Builder()
            .setLanguageHints(Arrays.asList("ko", "en"))
            .build()

        val textRecognizer = FirebaseVision.getInstance().getCloudTextRecognizer(options)

        textRecognizer.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                val resultText = firebaseVisionText.text
                Log.d("OCR", "OCR: ${resultText}")

                val dateRegex = Regex("""\d{4}\.\d{2}\.\d{2}|\d{4}년 \d{2}월 \d{2}일""")
                val dateMatch = dateRegex.find(resultText)
                val dateString = dateMatch?.value ?: ""
                dateEditText.setText(dateString)

                val lines = resultText.split("\n")
                val firstLine = lines.getOrNull(0) ?: ""
                val secondLine = lines.getOrNull(1) ?: ""

                storeEditText.setText(firstLine)
                itemEditText.setText(secondLine)

            }
            .addOnFailureListener { exception ->
                // OCR 처리 중 오류가 발생한 경우의 처리 코드를 여기에 작성합니다.
            }


    }


    fun byteArrayToBinaryString(b: ByteArray): String? {
        val sb = StringBuilder()
        for (i in b.indices) {
            sb.append(byteToBinaryString(b[i]))
        }
        return sb.toString()
    }
    fun byteToBinaryString(n: Byte): String? {
        val sb = java.lang.StringBuilder("00000000")
        for (bit in 0..7) {
            if (n.toInt() shr bit and 1 > 0) {
                sb.setCharAt(7 - bit, '1')
            }
        }
        return sb.toString()
    }

    fun showProgressDialog(context: Context) {
        progressDialog = ProgressDialog.show(context, "", "업로드 중입니다...", true)
    }
    fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }
    companion object {
        private const val REQUEST_CODE = 100
    }
}