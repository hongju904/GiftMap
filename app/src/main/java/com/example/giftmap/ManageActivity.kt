package com.example.giftmap

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class ManageActivity : AppCompatActivity() {
    private var uri: Uri? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var imageList: ArrayList<String>

    private val storageRef = FirebaseStorage.getInstance().getReference("images")
//    val database = Firebase.database

    private var isMultiSelect = false

    private lateinit var addbtn: Button
    private lateinit var editbtn: Button
//    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        addbtn = findViewById(R.id.button1)
        editbtn = findViewById(R.id.button2)

        // 기프티콘 업로드
        addbtn.setOnClickListener {
            if (isMultiSelect) {
                toggleMultiSelect()
            } else {
                val intent = Intent(this, UploadActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE)
            }
        }

        editbtn.setOnClickListener{
            toggleMultiSelect()
        }

        // 리사이클러뷰 이미지
        recyclerView = findViewById(R.id.listgift)
        recyclerView.layoutManager = LinearLayoutManager(this)

        imageList = ArrayList()
        imageAdapter = ImageAdapter(imageList)
        recyclerView.adapter = imageAdapter

        storageRef.listAll().addOnSuccessListener { listResult ->
            listResult.items.forEach { item ->
                item.downloadUrl.addOnSuccessListener { uri ->
                    imageList.add(uri.toString())
                    imageAdapter.notifyDataSetChanged()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch images", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            storageRef.listAll().addOnSuccessListener { listResult ->
                imageList.clear()
                listResult.items.forEach { item ->
                    item.downloadUrl.addOnSuccessListener { uri ->
                        imageList.add(uri.toString())
                        imageAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }


    private fun Uri.toFile(context: Context, file: File): File {
        context.contentResolver.openInputStream(this).use { input ->
            file.outputStream().use { output ->
                input?.copyTo(output)
            }
        }
        return file
    }

    private fun toggleMultiSelect() {
        isMultiSelect = !isMultiSelect
        if (isMultiSelect) {
            addbtn.text = "취소"
            editbtn.text = "삭제"
        } else {
            addbtn.text = "추가"
            editbtn.text = "편집"
        }
        imageAdapter.toggleMultiSelect()
    }

//    fun showProgressDialog(context: Context) {
//        progressDialog = ProgressDialog.show(context, "", "업로드 중입니다...", true)
//    }
//
//    fun hideProgressDialog() {
//        progressDialog?.dismiss()
//        progressDialog = null
//    }

    companion object {
        private const val REQUEST_CODE = 100
    }

}