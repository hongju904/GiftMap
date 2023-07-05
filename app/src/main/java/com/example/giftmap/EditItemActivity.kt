package com.example.giftmap

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayInputStream

class EditItemActivity : AppCompatActivity() {

    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val databaseRef = FirebaseDatabase.getInstance().getReference("user_data/$uid")

    private lateinit var itemEditText: EditText
    private lateinit var storeEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var giftimg: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_item)

        storeEditText = findViewById(R.id.store2)
        itemEditText = findViewById(R.id.item2)
        dateEditText = findViewById(R.id.date2)
        giftimg = findViewById(R.id.giftimg)

        storeEditText.setText(intent.getStringExtra("storeId"))
        itemEditText.setText(intent.getStringExtra("itemId"))
        dateEditText.setText(intent.getStringExtra("dateId"))
        val keyId = intent.getStringExtra("keyId")?: ""

        databaseRef.child(keyId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val simageValue = dataSnapshot.child("simage").getValue(String::class.java)?:""
                val b = binaryStringToByteArray(simageValue)
                val `is` = ByteArrayInputStream(b)
                val reviewImage = Drawable.createFromStream(`is`, "reviewImage")
                giftimg.setImageDrawable(reviewImage)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 오류 처리
            }
        })

        val doneButton: Button = findViewById(R.id.done)
        doneButton.setOnClickListener {
            val reference = databaseRef.child(keyId)
            reference.child("store").setValue(storeEditText.text.toString())
            reference.child("item").setValue(itemEditText.text.toString())
            reference.child("date").setValue(dateEditText.text.toString())
            finish()
        }

    }

    fun binaryStringToByteArray(s: String): ByteArray? {
        val count = s.length / 8
        val b = ByteArray(count)
        for (i in 1 until count) {
            val t = s.substring((i - 1) * 8, i * 8)
            b[i - 1] = binaryStringToByte(t)
        }
        return b
    }

    fun binaryStringToByte(s: String): Byte {
        var ret: Byte = 0
        var total: Byte = 0
        for (i in 0..7) {
            ret = if (s[7 - i] == '1') (1 shl i).toByte() else 0
            total = (ret.toInt() or total.toInt()).toByte()
        }
        return total
    }
}
