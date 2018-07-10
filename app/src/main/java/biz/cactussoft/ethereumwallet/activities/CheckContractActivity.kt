package biz.cactussoft.ethereumwallet.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import biz.cactussoft.ethcontracts.ERC223ContractManager
import biz.cactussoft.ethereumwallet.BuildConfig
import biz.cactussoft.ethereumwallet.R
import kotlinx.android.synthetic.main.activity_check_contract.*

class CheckContractActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_check_contract)

		val supportActionBar = supportActionBar
		if (supportActionBar != null) {
			supportActionBar.setDisplayHomeAsUpEnabled(true)
			supportActionBar.setDisplayShowHomeEnabled(true)
			supportActionBar.setHomeButtonEnabled(true)
		}

		et_address.setText(defContractAddress)

		btn_get_contract.setOnClickListener {
			Thread(Runnable {
				try {
					val erc223Manager = ERC223ContractManager(BuildConfig.INFURA_NODE_URL, this.filesDir.absolutePath + "/ethereum", defContractAddress)
					val data = erc223Manager.getNameInfo() + ": " + erc223Manager.getSymbolInfo()
					this@CheckContractActivity.runOnUiThread({
						Toast.makeText(this, data, Toast.LENGTH_SHORT).show()
					})
				} catch (e : Exception) {
					this@CheckContractActivity.runOnUiThread({
						Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
					})
				}
			}).start()
		}
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (item.itemId == android.R.id.home) {
			onBackPressed()
			return true
		}
		return super.onOptionsItemSelected(item)
	}

	companion object {
		const val defContractAddress = "0xd26114cd6EE289AccF82350c8d8487fedB8A0C07" // OMG Token

		fun newIntent(context: Context): Intent {
			return Intent(context, CheckContractActivity::class.java)
		}
	}
}
