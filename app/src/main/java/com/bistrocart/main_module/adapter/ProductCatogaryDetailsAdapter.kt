import android.content.ClipData.Item
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bistrocart.R
import com.bistrocart.databinding.AdapterProductCatogaryDetailsBinding
import com.bistrocart.main_module.model.res.CategoryMainSubRes
import com.bumptech.glide.Glide

class ProductCatogaryDetailsAdapter(
    private val itemList: ArrayList<CategoryMainSubRes.Data>,
    private val itemClickCallback: ItemClickCallback
) : RecyclerView.Adapter<ProductCatogaryDetailsAdapter.ViewHolder>() {
    private var lastClickedPosition = 0

    interface ItemClickCallback {
        fun onItemClick(item: CategoryMainSubRes.Data, type: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AdapterProductCatogaryDetailsBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder(private val binding: AdapterProductCatogaryDetailsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryMainSubRes.Data) {
            binding.llMain.setOnClickListener { itemClickCallback.onItemClick(item, 1) }
            binding.txtCategoryName.text =item.sub_cat_name

            Log.e("TAG", "bind123: "+item.sub_cat_name )

            Glide.with(itemView)
                .load(item.sub_cat_image)
                .into(binding.imgCategory)

            if (adapterPosition == lastClickedPosition) {
                binding.cardBG.setBackgroundResource(R.drawable.bg_select)
            } else {
                binding.cardBG.setBackgroundResource(R.drawable.bg_un_select)
            }

            binding.llMain.setOnClickListener(View.OnClickListener {
                lastClickedPosition = adapterPosition
                notifyDataSetChanged()
                itemClickCallback.onItemClick(item, 1)
            })
        }
    }
}
