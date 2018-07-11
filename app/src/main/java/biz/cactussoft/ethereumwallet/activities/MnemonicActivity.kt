package biz.cactussoft.ethereumwallet.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import biz.cactussoft.ethcore.HDWalletManager
import biz.cactussoft.ethereumwallet.R
import biz.cactussoft.ethereumwallet.dialogs.ChangePathDialog
import com.jakewharton.rxbinding2.widget.RxTextView
import kotlinx.android.synthetic.main.activity_mnemonic.*
import java.util.*
import kotlin.collections.ArrayList

class MnemonicActivity : BaseHomeActivity(), ChangePathDialog.ChangePathListener {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_mnemonic)

		tv_path.text = defDerivationPath
		RxTextView.textChanges(et_mnemonic).subscribe({ btn_generate_wallets.isEnabled = !it.isEmpty() })

		btn_generate_mnemonic.setOnClickListener {
			et_mnemonic.setText(HDWalletManager.generateMnemonic().joinToString(separator = " "))
		}

		cv_path.setOnClickListener {
			val dialog = ChangePathDialog.newInstance(tv_path.text.toString())
			dialog.show(supportFragmentManager, ChangePathDialog::class.java.simpleName) }

		btn_generate_wallets.setOnClickListener {
			startActivity(HdWalletsActivity.newIntent(this, ArrayList(mnemonicToList(et_mnemonic.text.toString())), tv_path.text.toString()))
		}
	}

	override fun changePath(path: String) {
		tv_path.text = path
	}

	companion object {
		const val defDerivationPath = "m/44'/60'/0'/0"

		fun newIntent(context: Context): Intent {
			return Intent(context, MnemonicActivity::class.java)
		}

		fun mnemonicToList(mnemonic: String): List<String> {
			return Arrays.asList(*mnemonic.toLowerCase().trim { it <= ' ' }.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
		}
	}
}
