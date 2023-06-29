package com.example.giftmap

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import android.widget.ImageView
import android.widget.ListAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class ImageAdapter(private val items: ArrayList<ItemData>) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    private var isMultiSelect = false
    private val selectedItems = mutableListOf<String>()
    private var giftList = ArrayList<ItemData>()
    private val storageRef = FirebaseStorage.getInstance().reference
    private val databaseRef = Firebase.database.getReference("user_data")

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
        selectedItems.forEach { imageUrl ->
            // 파일 경로에서 파일 이름만 추출
            val filename = imageUrl.substringAfterLast("%2F")
                .substringBeforeLast("?")
            Log.d("ImageAdapter", filename)

            // firebase storage에서 해당 이미지 삭제
            storageRef.child("images/$filename").delete()

            // firebase DB에서 해당 데이터 삭제
            databaseRef.orderByChild("image_url").equalTo("/images/$filename").addListenerForSingleValueEvent(object :
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
            val index = selectedItems.indexOf(imageUrl)
            items.removeAt(index)
        }

        // 선택된 아이템 초기화
        selectedItems.clear()
        isMultiSelect = false

        // 어댑터 갱신
        notifyDataSetChanged()
    }

    fun setListData(data: MutableList<ItemData>) {
        giftList.clear()
        giftList.addAll(data)
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

        val imageUrl = gift.image_url
        Log.v(TAG, "image = " + storageRef.child(imageUrl))
        if (!imageUrl.isNullOrEmpty()) {
            val storageReference: StorageReference = storageRef.child(imageUrl)
            val uri: Uri = Uri.parse(storageReference.toString())
            Picasso.get().load(uri).into(holder.imageView)
        } else {
            // 이미지 URL이 비어 있거나 null인 경우 처리할 내용 추가
            // 예: 기본 이미지 설정 또는 에러 처리
        }
//        Glide.with(holder.imageView.context).load(storageRef.child(imageUrl)).into(holder.imageView)


        if (isMultiSelect) {
            val isSelected = selectedItems.contains(gift.image_url)
            holder.itemView.setBackgroundColor(
                if (isSelected) {
                    ContextCompat.getColor(holder.itemView.context, R.color.selectpink)
                } else {
                    Color.TRANSPARENT
                }
            )

            holder.itemView.setOnClickListener {
                if (isSelected) {
                    selectedItems.remove(gift.image_url)
                } else {
                    selectedItems.add(gift.image_url)
                }
                notifyItemChanged(position)
            }
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            holder.itemView.setOnClickListener(null)
        }
    }
}
