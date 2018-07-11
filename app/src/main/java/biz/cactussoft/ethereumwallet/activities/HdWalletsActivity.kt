package biz.cactussoft.ethereumwallet.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.LinearLayout
import android.widget.Toast
import biz.cactussoft.ethcore.EthManager
import biz.cactussoft.ethcore.models.HDWallet
import biz.cactussoft.ethereumwallet.BuildConfig
import biz.cactussoft.ethereumwallet.R
import biz.cactussoft.ethereumwallet.adapters.HdWalletsAdapter
import biz.cactussoft.ethereumwallet.decorations.HdWalletsItemDecoration
import biz.cactussoft.ethereumwallet.listeners.InfiniteScrollListener
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_hd_wallets.*

/**
 * Created by viktor.chukholskiy
 * 11/07/18.
 */
class HdWalletsActivity : BaseHomeActivity() {

	private var ethManager: EthManager? = null
	private var adapter: HdWalletsAdapter = HdWalletsAdapter(mutableListOf(), listener = {})
	private val pageWalletsCount = 15

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_hd_wallets)

		ethManager = EthManager(BuildConfig.INFURA_NODE_URL, this.filesDir.absolutePath + "/ethereum")

		val layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
		rv_hd_wallets.layoutManager = layoutManager
		rv_hd_wallets.adapter = adapter
		rv_hd_wallets.addItemDecoration(HdWalletsItemDecoration(this))

		if (intent.extras != null) {
			val mnemonic = intent.extras.getStringArrayList(MNEMONIC)
			val path = intent.extras.getString(PATH)

			generateWallets(mnemonic, path, 0, pageWalletsCount, true)
			addScrollListener(mnemonic, path, layoutManager)

			srl_refresh.setOnRefreshListener {
				generateWallets(mnemonic, path, 0, pageWalletsCount, true)
				addScrollListener(mnemonic, path, layoutManager)
			}
		}
	}

	private fun generateWallets(mnemonic: ArrayList<String>, path: String, startIndex: Int, count: Int, clear: Boolean) {
		srl_refresh.isRefreshing = true
		Observable.create<List<HDWallet>> { emitter: ObservableEmitter<List<HDWallet>> ->
			try {
				val list = ethManager!!.getConsecutiveHDWallets(mnemonic, "", path, startIndex, count)
				emitter.onNext(list)
				emitter.onComplete()
			} catch (e: Exception) {
				emitter.onError(e)
			}
		}.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe({
					if (clear) adapter.setData(it) else adapter.addData(it)
					srl_refresh.isRefreshing = false
				}, {
					Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
					finish()
				})
	}

	private fun addScrollListener(mnemonic: ArrayList<String>, path: String, layoutManager: LinearLayoutManager) {
		rv_hd_wallets.addOnScrollListener(InfiniteScrollListener(func = {
			generateWallets(mnemonic, path, adapter.itemCount, pageWalletsCount, false)
		}, layoutManager = layoutManager))
	}

	companion object {
		private const val MNEMONIC = "mnemonic"
		private const val PATH = "path"

		fun newIntent(context: Context, mnemonic: ArrayList<String>, path: String): Intent {
			val intent = Intent(context, HdWalletsActivity::class.java)
			intent.putStringArrayListExtra(MNEMONIC, mnemonic)
			intent.putExtra(PATH, path)
			return intent
		}
	}
}
