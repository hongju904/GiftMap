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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import androidx.lifecycle.Observer
import java.util.*
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class ManageActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter : ImageAdapter
    private val viewModel by lazy {ViewModelProvider(this).get(ListViewModel::class.java)}
    private lateinit var imageList: ArrayList<ItemData>

    private var isMultiSelect = false

    private lateinit var addbtn: Button
    private lateinit var editbtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        addbtn = findViewById(R.id.button1)
        editbtn = findViewById(R.id.button2)

        // 기프티콘 업로드, 삭제
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
        adapter = ImageAdapter(imageList)
        recyclerView.adapter = adapter

        observerData()
    }

    fun observerData(){
        viewModel.fetchData().observe(this, { data ->
            imageList.clear()
            imageList.addAll(data)
            adapter.setListData(data)
            adapter.notifyDataSetChanged()
        })
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
        adapter.toggleMultiSelect()
    }

    companion object {
        private const val REQUEST_CODE = 100
    }

}