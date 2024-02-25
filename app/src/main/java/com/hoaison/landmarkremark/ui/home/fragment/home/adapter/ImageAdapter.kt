package com.hoaison.landmarkremark.ui.home.fragment.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.hoaison.landmarkremark.R
import com.hoaison.landmarkremark.databinding.ItemImageAddBinding
import com.hoaison.landmarkremark.databinding.ItemImageBinding
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class ImageAdapter(val mContext: Context,
                   val mListener: OnEnvImageListener,
                   val mHasIcon: Boolean = true) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                       
    companion object {
        const val NO_IMAGE = "no_image"
        const val TAG = "ImageAdapter"
    }
    
    private val mDataList = mutableListOf<String>()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == R.layout.item_image_add) {
            val binding: ItemImageAddBinding = ItemImageAddBinding.inflate(
                LayoutInflater.from(mContext),
                parent, false)
            AddViewHolder(binding)
        } else {
            val binding: ItemImageBinding = ItemImageBinding.inflate(
                LayoutInflater.from(mContext),
                parent, false)
            ViewHolder(binding)
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val entry = mDataList[position]
        if (holder is ViewHolder) {
            holder.bind(entry, position)
        } else if (holder is AddViewHolder) {
            holder.itemBinding.root.setOnClickListener {
                mListener.onAddImage()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val data = mDataList.getOrNull(position)
        return if (data.isNullOrEmpty() || data == NO_IMAGE) R.layout.item_image_add else R.layout.item_image
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(data: List<String>) {
        mDataList.clear()
        mDataList.addAll(data)
        if (mHasIcon) mDataList.add(NO_IMAGE) // Add view

        notifyDataSetChanged()
    }


    @SuppressLint("NotifyDataSetChanged")
    fun addItems(data: List<String>) {
        Log.d(TAG, "addItems")
        mDataList.removeAll { it == NO_IMAGE }
        mDataList.addAll(data)
        if (mHasIcon) mDataList.add(NO_IMAGE) // Add view
        notifyDataSetChanged()
    }

    fun updateItems(currentImage: String?, newImageList: List<String>?) {
        Log.d(TAG, "updateItems")
        if (!currentImage.isNullOrEmpty() && !newImageList.isNullOrEmpty()) {
            val newDataList = mutableListOf<String>()
            mDataList.forEach {
                if (it != currentImage) {
                    newDataList.add(it)
                } else {
                    newDataList.addAll(newImageList)
                }
            }
            mDataList.clear()
            mDataList.addAll(newDataList)
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(val itemBinding: ItemImageBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        var mShowAnimLoading = MutableLiveData<Boolean>().apply { value = false }

        fun bind(data: String, position: Int) {
            if (data.isNotEmpty()) {
                itemBinding.imageView.let {
                    val size = mContext.resources.getDimensionPixelSize(R.dimen.env_image_size)
                    mShowAnimLoading.value = true
                    Picasso.get().load(data).resize(size, 0).into(it, object : Callback {
                        override fun onSuccess() {
                            mShowAnimLoading.value = false
                        }

                        override fun onError(e: java.lang.Exception) {
                            mShowAnimLoading.value = false
                        }
                    })
                }
            }
        }

    }

    inner class AddViewHolder(val itemBinding: ItemImageAddBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(data: String, position: Int) {

        }

    }


    interface OnEnvImageListener {
        fun onAddImage()
        fun onUpdateDataList()
    }

}