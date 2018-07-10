package biz.cactussoft.ethereumwallet.decorations

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Created by viktor.chukholskiy
 * 10/07/18.
 */
class HdWalletsItemDecoration : RecyclerView.ItemDecoration() {

	override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
		outRect.set(24, 12, 24, 12)
	}
}