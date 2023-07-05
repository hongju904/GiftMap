package com.example.giftmap

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayInputStream

class ImageAdapter(private val items: ArrayList<ItemData>) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    private var isMultiSelect = false
    private val selectedItems = mutableListOf<String>()
    private var giftList = ArrayList<ItemData>()
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val databaseRef = FirebaseDatabase.getInstance().getReference("user_data/$uid")

    fun toggleMultiSelect() {
        isMultiSelect = !isMultiSelect
        if (isMultiSelect) {
            notifyDataSetChanged()
            selectedItems.clear()
        } else {
            deleteSelectedItems()
        }
    }

    private fun deleteSelectedItems() {
        // 선택된 이미지들에 대해 firebase storage와 DB에서 삭제
        selectedItems.forEach { filename ->
            databaseRef.orderByChild("filename").equalTo(filename).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        snapshot.ref.removeValue()
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("ImageAdapter", "onCancelled: ${databaseError.toException()}")
                }
            })

            // 선택한 이미지 리스트에서 삭제
            val itemToRemove = giftList.find { it.filename == filename }
            itemToRemove?.let {
                val index = giftList.indexOf(it)
                giftList.removeAt(index)
                items.removeAt(index) // items에서도 해당 이미지 제거
            }
        }

        // 선택된 아이템 초기화
        selectedItems.clear()
        isMultiSelect = false

        // 어댑터 갱신
        notifyDataSetChanged()
    }

    fun setListData(data: MutableList<ItemData>) {
        val uniqueItems = ArrayList<ItemData>()
        uniqueItems.addAll(data.distinct()) // 중복 아이템 제거
        giftList.clear()
        giftList.addAll(uniqueItems)
        items.clear()
        items.addAll(giftList)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.img)
        val store: TextView = itemView.findViewById(R.id.store)
        val item: TextView = itemView.findViewById(R.id.item)
        val date: TextView = itemView.findViewById(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.giftlist, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gift: ItemData = giftList[position]
        holder.store.text = gift.store
        holder.item.text = gift.item
        holder.date.text = gift.date

        val imageString = gift.simage
        val b = binaryStringToByteArray(imageString)
        val `is` = ByteArrayInputStream(b)
        val reviewImage = Drawable.createFromStream(`is`, "reviewImage")
        holder.imageView.setImageDrawable(reviewImage)

        if (isMultiSelect) {
            val isSelected = selectedItems.contains(gift.filename)
            holder.itemView.setBackgroundColor(
                if (isSelected) {
                    ContextCompat.getColor(holder.itemView.context, R.color.selectpink)
                } else {
                    Color.TRANSPARENT
                }
            )

            holder.itemView.setOnClickListener {
                if (isSelected) {
                    selectedItems.remove(gift.filename)
                } else {
                    selectedItems.add(gift.filename)
                }
                notifyItemChanged(position)
            }
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            holder.itemView.setOnClickListener(null)

            holder.itemView.setOnClickListener {
                val selectedItem = giftList[position]
                var keyId: String? = null

                databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (userSnapshot in dataSnapshot.children) {   // userSnapshot: 아이템 리스트
                            val item = userSnapshot.getValue(ItemData::class.java)
                            if (item?.filename == selectedItem.filename) {
                                keyId = userSnapshot.key
                            }
                        }
                        val intent = Intent(holder.itemView.context, EditItemActivity::class.java)
                        intent.putExtra("keyId", keyId)
                        intent.putExtra("storeId", selectedItem.store)
                        intent.putExtra("itemId", selectedItem.item)
                        intent.putExtra("dateId", selectedItem.date)
                        holder.itemView.context.startActivity(intent)
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        // 오류 처리
                    }
                })
            }
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
