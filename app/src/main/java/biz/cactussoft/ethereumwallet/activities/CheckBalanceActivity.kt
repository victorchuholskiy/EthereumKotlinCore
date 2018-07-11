package biz.cactussoft.ethereumwallet.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import biz.cactussoft.ethcore.EthManager
import biz.cactussoft.ethereumwallet.BuildConfig
import biz.cactussoft.ethereumwallet.R
import kotlinx.android.synthetic.main.activity_check_balance.*

/**
 * Created by viktor.chukholskiy
 * 09/07/18.
 */
class CheckBalanceActivity : BaseHomeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_check_balance)

		et_address.setText(DEF_WALLET_ADDRESS)

		val ethManager = EthManager(BuildConfig.INFURA_NODE_URL, this.filesDir.absolutePath + "/ethereum")

		btn_get_balance.setOnClickListener {
			Thread(Runnable {
				try {
					val balance = ethManager.getBalance(et_address.text.toString()).inEther(BALANCE_DIGITS)
					this@CheckBalanceActivity.runOnUiThread({
						Toast.makeText(this, getString(R.string.balance) + ": " + balance.toString(), Toast.LENGTH_SHORT).show()
					})
				} catch (e : Exception) {
					this@CheckBalanceActivity.runOnUiThread({
						Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
					})
				}
			}).start()
		}
	}

	companion object {
		const val BALANCE_DIGITS = 5
		const val DEF_WALLET_ADDRESS = "0x847ed5f2e5dde85ea2b685edab5f1f348fb140ed"

		fun newIntent(context: Context): Intent {
			return Intent(context, CheckBalanceActivity::class.java)
		}
	}
}
