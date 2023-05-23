package technology.polygon.polygonid_android_sdk.presentation.common_items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import technology.polygon.polygonid_android_sdk.R

class CardAdapter(private val dataList: List<CardData>) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvMethod: TextView = itemView.findViewById(R.id.tv_method_name)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_description)

        init {
            itemView.setOnClickListener {
                dataList[adapterPosition].onClick.invoke()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val data = dataList[position]
        holder.tvTitle.text = data.title
        holder.tvMethod.text = data.methodName
        holder.tvDescription.text = data.description
    }

    override fun getItemCount() = dataList.size
}
