package biz.cactussoft.ethereumwallet.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import biz.cactussoft.ethcore.models.HDWallet
import biz.cactussoft.ethereumwallet.R
import kotlinx.android.synthetic.main.item_hd_wallet.view.*

/**
 * Created by viktor.chukholskiy
 * 10/07/18.
 */
class HdWalletsAdapter(private var items: MutableList<HDWallet>, private val listener: (HDWallet) -> Unit) : RecyclerView.Adapter<HdWalletsAdapter.ViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_hd_wallet, parent, false))

	override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position], listener)

	override fun getItemCount() = items.size

	fun setData(wallets: List<HDWallet>) {
		items.clear()
		addData(wallets)
	}

	fun addData(wallets: List<HDWallet>) {
		items.addAll(wallets)
		notifyDataSetChanged()
	}

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		fun bind(item: HDWallet, listener: (HDWallet) -> Unit) = with(itemView) {
			val text = "${context.getText(R.string.index)} ${item.index}: ${item.address}"
			itemView.tv_address.text = text
			setOnClickListener { listener(item) }
		}
	}
}