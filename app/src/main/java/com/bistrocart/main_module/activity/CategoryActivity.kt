package com.bistrocart.main_module.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.bistrocart.main_module.adapter.CategoryListAdapter
import com.bistrocart.base.BaseActivity
import com.bistrocart.databinding.ActivityCategoryListBinding
import com.bistrocart.main_module.model.res.CategoryRes
import com.bistrocart.utils.AppLog
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class CategoryActivity : BaseActivity(), CategoryListAdapter.OnItemListener {
    private val TAG = "CategoryActivity"
    private lateinit var binding: ActivityCategoryListBinding
    private lateinit var categoryListAdapter: CategoryListAdapter
    private val categoryList = ArrayList<CategoryRes.Data>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryData()
        setCategoryAdapter()

        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.imgSearch.setOnClickListener {
            binding.edtSearch.requestFocus()
        }

    }

    private fun setCategoryAdapter() {
        binding.rvCategoryList.layoutManager = GridLayoutManager(this, 3)
        categoryListAdapter = CategoryListAdapter(categoryList,this)
        binding.rvCategoryList.adapter = categoryListAdapter
        binding.rvCategoryList.setHasFixedSize(true)
    }

 /*   private fun categoryData() {
        categoryList.clear()
     *//*   categoryList.apply {
            add(CategoryModel("Dairy", R.drawable.dairy_img))
            add(CategoryModel("Grains", R.drawable.grains_img))
            add(CategoryModel("Grocery", R.drawable.grocery_img))
            add(CategoryModel("Soap", R.drawable.soap_img))
            add(CategoryModel("Dairy", R.drawable.dairy_img))
            add(CategoryModel("Grains", R.drawable.grains_img))
            add(CategoryModel("Grocery", R.drawable.grocery_img))
            add(CategoryModel("Soap", R.drawable.soap_img))
        }*//*

    }*/
/*

    override fun onItemClicked(position: Int, id: ArrayList<CategoryRes.Data>) {
        var bundle =Bundle()
        bundle.putString("id",list.get(position).id.toString())
        bundle.putString("title",list.get(position).main_cat_name.toString())
        gotoActivity(ProductActivity::class.java,bundle,false)
    }
*/


    @SuppressLint("CheckResult")
    private fun categoryData() {
        categoryList.clear()
        if (!isOnline()) {
            return
        }

        showLoading("loading")
        BaseActivity.apiService?.getCategoryList()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { t: Response<CategoryRes> ->
                    AppLog.e(TAG, "CategoryRes: ₹t")
                    AppLog.e(TAG, "Category.body(): " + Gson().toJson(t.body()))
                    hideLoading()
                    if (isSuccess(t,t.message())) {
                     /*   var count = 0 // Counter variable to keep track of the number of items added
                        for (item in t.body()!!.data) {
                            categoryList.add(item) // Add the item to the list
                            count++ // Increment the counter
                            if (count == 6) {
                                break // Exit the loop after adding six items
                            }
                        }*/
                         categoryList.addAll(t.body()!!.data)
                        categoryListAdapter.notifyDataSetChanged()

                        //   showToast(t.body()?.message)
                    } else {
                        showToast(t.body()?.message)
                    }
                },
                { error ->
                    hideLoading()
                    onFailure(error)
                }
            )
    }

    override fun onItemClicked(position: Int, list: ArrayList<CategoryRes.Data>) {
        var bundle =Bundle()
        bundle.putString("id",list.get(position).id.toString())
        bundle.putString("title",list.get(position).main_cat_name.toString())
        gotoActivity(ProductActivity::class.java,bundle,false)
    }
}