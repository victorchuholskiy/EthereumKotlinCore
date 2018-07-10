package biz.cactussoft.ethereumwallet.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import biz.cactussoft.ethereumwallet.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		btn_check_balance.setOnClickListener {
			startActivity(CheckBalanceActivity.newIntent(this)); }
		btn_check_hd_wallet.setOnClickListener {
			startActivity(MnemonicActivity.newIntent(this)); }
		btn_check_contract.setOnClickListener {
			startActivity(CheckContractActivity.newIntent(this)); }
	}
}