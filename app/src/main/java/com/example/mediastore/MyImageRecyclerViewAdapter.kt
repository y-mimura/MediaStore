package com.example.mediastore

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.example.mediastore.databinding.LayoutImageBinding

class MyImageRecyclerViewAdapter : RecyclerView.Adapter<MyImageRecyclerViewAdapter.ViewHolder>() {

    private var items = emptyList<ImageItem>()

    fun setItems(items: List<ImageItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            LayoutImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: LayoutImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ImageItem) {
            binding.apply {
                image.setImageURI(item.uri)
                name.text = item.name
            }
        }
    }

}