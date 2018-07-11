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
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import kotlinx.android.synthetic.main.activity_hd_wallets.*


class HdWalletsActivity : BaseHomeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_hd_wallets)

		val ethManager = EthManager(BuildConfig.INFURA_NODE_URL, this.filesDir.absolutePath + "/ethereum")
		val adapter = HdWalletsAdapter(mutableListOf(), listener = {})

		rv_hd_wallets.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
		rv_hd_wallets.adapter = adapter
		rv_hd_wallets.addItemDecoration(HdWalletsItemDecoration(this))

		if (intent.extras != null) {
			Observable.create<List<HDWallet>> { emitter: ObservableEmitter<List<HDWallet>> ->
				try {
					val list = ethManager.getConsecutiveHDWallets(intent.extras.getStringArrayList(MNEMONIC), "", intent.extras.getString(PATH), 0, 10)
					emitter.onNext(list)
					emitter.onComplete()
				} catch (e: Exception) {
					emitter.onError(e)
				}
			}.subscribe({ adapter.setData(it) }, { Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show() })
		}
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
