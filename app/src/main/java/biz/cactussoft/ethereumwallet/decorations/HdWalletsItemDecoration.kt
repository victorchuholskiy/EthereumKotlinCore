package biz.cactussoft.ethereumwallet.decorations

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import biz.cactussoft.ethereumwallet.R

/**
 * Created by viktor.chukholskiy
 * 10/07/18.
 */
class HdWalletsItemDecoration constructor(context: Context) : RecyclerView.ItemDecoration() {

	private var horizontalMargin = 0
	private var verticalMargin = 0

	init {
		horizontalMargin = context.resources.getDimension(R.dimen.hd_wallet_horizontal_margin).toInt()
		verticalMargin = context.resources.getDimension(R.dimen.hd_wallet_vertical_margin).toInt()
	}

	override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
		val params = view.layoutParams as RecyclerView.LayoutParams
		outRect.set(horizontalMargin, verticalMargin, horizontalMargin, if (params.viewAdapterPosition == parent.adapter.itemCount - 1) verticalMargin else 0)
	}
}