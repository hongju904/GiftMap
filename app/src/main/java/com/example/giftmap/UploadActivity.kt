package com.example.giftmap

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date

class UploadActivity : AppCompatActivity() {
    private var selectedImageUri: Uri? = null
    val database = FirebaseDatabase.getInstance()

    val databaseRef = database.getReference("user_data")
    private var progressDialog: ProgressDialog? = null

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
            selectedImageUri = data.data
            val giftimg: ImageView = findViewById(R.id.giftimg)
            giftimg.setImageURI(selectedImageUri)
        }
    }

    private fun uploadImageTOFirebase(uri: Uri, store: String?, item: String?, date: String?) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val fileName = "IMAGE_${SimpleDateFormat("yyyymmdd_HHmmss").format(Date())}_.png"

        val imagesRef = storage.reference.child("images/").child(fileName)
        imagesRef.putFile(uri)
            .addOnSuccessListener {

                // firebase DB에 정보 업로드
                val key = databaseRef.push().key ?: ""
                val result = hashMapOf(
                    "image_url" to imagesRef.path,
                    "store" to store,
                    "item" to item,
                    "date" to date
                )
                databaseRef.child(key).setValue(result)
                    .addOnSuccessListener {
                        Toast.makeText(getApplicationContext(), "성공", Toast.LENGTH_LONG).show()
                        hideProgressDialog()
                        finish()
                    }
                    .addOnFailureListener {
                        // 데이터 전송 실패 시 처리할 내용
                        Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_LONG).show()
                        hideProgressDialog()
                        finish()
                    }

            }
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