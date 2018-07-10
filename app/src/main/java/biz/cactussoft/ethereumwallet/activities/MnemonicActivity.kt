package biz.cactussoft.ethereumwallet.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import biz.cactussoft.ethcore.HDWalletManager
import biz.cactussoft.ethereumwallet.R
import biz.cactussoft.ethereumwallet.dialogs.ChangePathDialog
import kotlinx.android.synthetic.main.activity_mnemonic.*

class MnemonicActivity : AppCompatActivity(), ChangePathDialog.ChangePathListener {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_mnemonic)

		val supportActionBar = supportActionBar
		if (supportActionBar != null) {
			supportActionBar.setDisplayHomeAsUpEnabled(true)
			supportActionBar.setDisplayShowHomeEnabled(true)
			supportActionBar.setHomeButtonEnabled(true)
		}

		tv_path.text = defDerivationPath

		btn_generate_mnemonic.setOnClickListener {
			et_mnemonic.setText(HDWalletManager.generateMnemonic().joinToString(separator = " "))
		}
		cv_path.setOnClickListener {
			val dialog = ChangePathDialog.newInstance(tv_path.text.toString())
			dialog.show(supportFragmentManager, ChangePathDialog::class.java.simpleName) }
	}

	override fun changePath(path: String) {
		tv_path.text = path
	}

	companion object {
		const val defDerivationPath = "m/44'/60'/0'/0"

		fun newIntent(context: Context): Intent {
			return Intent(context, MnemonicActivity::class.java)
		}
	}
}
