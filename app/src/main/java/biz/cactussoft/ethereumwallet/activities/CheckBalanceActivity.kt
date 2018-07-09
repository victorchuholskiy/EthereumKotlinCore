package biz.cactussoft.ethereumwallet.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import biz.cactussoft.ethcore.EthManager
import biz.cactussoft.ethereumwallet.R
import kotlinx.android.synthetic.main.activity_check_balance.*

class CheckBalanceActivity : AppCompatActivity() {

	private var ethManager = EthManager(infuraUrlNode, filesDir.absolutePath + "/ethereum")

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_check_balance)

		btn_get_balance.setOnClickListener {
			val balance = ethManager.getBalance(et_address.text.toString()).inEther(balanceDigits)
			tv_balance.text = balance.toString() }
	}

	companion object {
		const val infuraUrlNode = "https://mainnet.infura.io/5JTl6zJV6HgGi1ZrMLCp"
		const val balanceDigits = 5

		fun newIntent(context: Context): Intent {
			return Intent(context, CheckBalanceActivity::class.java)
		}
	}
}
