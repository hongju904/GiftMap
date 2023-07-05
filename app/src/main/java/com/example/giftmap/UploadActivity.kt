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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class UploadActivity : AppCompatActivity() {
    private var selectedImageUri: Uri? = null
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val databaseRef = FirebaseDatabase.getInstance().getReference("user_data/$uid")
    private var progressDialog: ProgressDialog? = null
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        openGallery()

        val doneButton: Button = findViewById(R.id.done)
        doneButton.setOnClickListener {
            val storeEditText: EditText = findViewById(R.id.store2)
            val store: String = storeEditText.text.toString()

            val itemEditText: EditText = findViewById(R.id.item2)
            val item: String = itemEditText.text.toString()

            val dateEditText: EditText = findViewById(R.id.date2)
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