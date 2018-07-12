package biz.cactussoft.ethereumwallet.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import biz.cactussoft.ethcontracts.ERC223ContractManager
import biz.cactussoft.ethereumwallet.BuildConfig
import biz.cactussoft.ethereumwallet.R
import kotlinx.android.synthetic.main.activity_check_contract.*

/**
 * Created by viktor.chukholskiy
 * 09/07/18.
 */
class CheckContractActivity : BaseHomeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_check_contract)

		et_address.setText(DEF_COUNTRACT_ADDRESS)

		btn_get_contract.setOnClickListener {
			Thread(Runnable {
				try {
					val erc223Manager = ERC223ContractManager(BuildConfig.INFURA_NODE_URL, this.filesDir.absolutePath + "/ethereum", et_address.text.toString())
					this@CheckContractActivity.runOnUiThread({
						Toast.makeText(this, "${erc223Manager.getNameInfo()}: ${erc223Manager.getSymbolInfo()}", Toast.LENGTH_SHORT).show()
					})
				} catch (e : Exception) {
					this@CheckContractActivity.runOnUiThread({
						Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
					})
				}
			}).start()
		}
	}

	companion object {
		const val DEF_COUNTRACT_ADDRESS = "0xd26114cd6EE289AccF82350c8d8487fedB8A0C07" // OMG Token

		fun newIntent(context: Context): Intent {
			return Intent(context, CheckContractActivity::class.java)
		}
	}
}
