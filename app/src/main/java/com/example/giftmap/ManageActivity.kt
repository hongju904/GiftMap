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
    val database = Firebase.database

    private var isMultiSelect = false

    private lateinit var addbtn: Button
    private lateinit var editbtn: Button
    private var progressDialog: ProgressDialog? = null

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
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
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
            uri = data.data
            uri?.let {
                showProgressDialog(this)
                uploadImageTOFirebase(it)
            }
        }
    }

    private fun uploadImageTOFirebase(uri: Uri) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val fileName = "IMAGE_${SimpleDateFormat("yyyymmdd_HHmmss").format(Date())}_.png"

        val imagesRef = storage.reference.child("images/").child(fileName)
        imagesRef.putFile(uri)
            .addOnSuccessListener {
                // OCR 처리를 위한 이미지 파일 생성
                val imageFile = File(applicationContext.cacheDir, "temp_image.jpg")
                uri.toFile(applicationContext, imageFile)

                // OCR 실행
                val recognizedText = ocrImage(imageFile)

                // firebase DB에 OCR 결과 업로드
                val databaseRef = database.getReference("ocr_results")
                val key = databaseRef.push().key ?: ""
                val result = hashMapOf(
                    "image_url" to imagesRef.path,
                    "text" to recognizedText
                )
                databaseRef.child(key).setValue(result)

                // 성공적으로 업로드가 완료된 경우, 업데이트된 이미지 리스트를 로드
                storageRef.listAll().addOnSuccessListener { listResult ->
                    imageList.clear()
                    listResult.items.forEach { item ->
                        item.downloadUrl.addOnSuccessListener { uri ->
                            imageList.add(uri.toString())
                            imageAdapter.notifyDataSetChanged()
                        }
                    }
                }
                hideProgressDialog()
            }
    }

    private fun ocrImage(imageFile: File): String {
        val baseApi = TessBaseAPI()
        val datapath = "${applicationContext.filesDir}/tesseract/"
        val lang = arrayOf("kor", "eng")
        val trainedDataPaths = lang.map { "$it.traineddata" }

        for (trainedDataPath in trainedDataPaths) {
            // traineddata 파일의 경로
            val trainedDataFile = File("$datapath/tessdata/$trainedDataPath")

            // traineddata 파일이 존재하지 않는 경우, assets 폴더에서 복사해옴
            if (!trainedDataFile.exists()) {
                try {
                    val dir = File("$datapath/tessdata/")
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    val inputStream = applicationContext.assets.open(trainedDataPath)
                    val outputStream = FileOutputStream(trainedDataFile)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        baseApi.init(datapath, lang.joinToString("+"))
        baseApi.setImage(imageFile)
        val recognizedText = baseApi.utF8Text
        baseApi.end()

        Log.d("OCR Result", recognizedText) // OCR 결과를 Log로 출력

        return recognizedText
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