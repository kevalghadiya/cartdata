package com.bistrocart.main_module.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bistrocart.databinding.AdapterCategoryRowFileBinding
import com.bistrocart.main_module.model.res.CategoryRes
import com.bumptech.glide.Glide

class CategoryListAdapter(
    private var categoryList: ArrayList<CategoryRes.Data>,
    val onItemListener: OnItemListener,
) : RecyclerView.Adapter<CategoryListAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: AdapterCategoryRowFileBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {

        val binding = AdapterCategoryRowFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)

    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        with(holder) {
            with(categoryList[position]) {

                binding.txtCategoryName.text = categoryList.get(position).main_cat_name
              //  binding.imgCategory.setImageResource(this.categoryImage!!)

                Glide.with(holder.itemView)
                    .load(categoryList.get(position).main_cat_image)
                    .into(binding.imgCategory)

                binding.root.setOnClickListener {
                    onItemListener.onItemClicked(position,categoryList)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    interface OnItemListener {
        fun onItemClicked(position: Int, id: ArrayList<CategoryRes.Data>)
    }
}