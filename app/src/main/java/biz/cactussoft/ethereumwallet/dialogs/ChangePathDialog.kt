package biz.cactussoft.ethereumwallet.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import biz.cactussoft.ethcore.HDWalletManager
import biz.cactussoft.ethcore.exceptions.IncorrectDerivationPathException
import biz.cactussoft.ethereumwallet.R
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

/**
 * Created by viktor.chukholskiy
 * 10/07/18.
 */


class ChangePathDialog : DialogFragment() {

	interface ChangePathListener {
		fun changePath(path: String)
	}

	private var accountObservable: Observable<CharSequence>? = null
	private var extIntObservable: Observable<CharSequence>? = null

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		isCancelable = false

		val view = LayoutInflater.from(context).inflate(R.layout.dialog_change_path, null)
		val etPurpose = view.findViewById<EditText>(R.id.et_purpose)
		val etCoin = view.findViewById<EditText>(R.id.et_coin)
		val etAccount = view.findViewById<EditText>(R.id.et_account)
		val etExtInt = view.findViewById<EditText>(R.id.et_external_internal)
		accountObservable = RxTextView.textChanges(etAccount)
		extIntObservable = RxTextView.textChanges(etExtInt)

		try {
			val indexes = HDWalletManager.convertPathToIntArray(arguments!!.getString(ARG_PATH, ""))
			etPurpose.setText(indexes[0].toString())
			etCoin.setText(indexes[1].toString())
			etAccount.setText(indexes[2].toString())
			etExtInt.setText(indexes[3].toString())
		} catch (e: IncorrectDerivationPathException) {
			Log.e(javaClass.simpleName, "Incorrect path")
		}

		return AlertDialog.Builder(activity!!)
				.setView(view)
				.setTitle(R.string.choose_path)
				.setPositiveButton(R.string.change, { _, _ ->
					if (activity != null && activity is ChangePathListener) {
						(activity as ChangePathListener).changePath(
								HDWalletManager.buildPath(
										Integer.valueOf(etPurpose.text.toString()),
										Integer.valueOf(etCoin.text.toString()),
										Integer.valueOf(etAccount.text.toString()),
										Integer.valueOf(etExtInt.text.toString())
								)
						)
					}
				})
				.setNegativeButton(R.string.cancel, { _, _ -> })
				.create()
	}

	override fun onResume() {
		super.onResume()
		val isSignInEnabled: Observable<Boolean> = Observable.combineLatest(
				accountObservable!!,
				extIntObservable!!,
				BiFunction { account, extInt -> account.isNotEmpty() && extInt.isNotEmpty() })
		isSignInEnabled.subscribe({ (this.dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = it })
	}

	companion object {
		private const val ARG_PATH = "derivationPath"

		fun newInstance(path: String): ChangePathDialog {
			return ChangePathDialog().apply {
				arguments = Bundle().apply {
					putString(ARG_PATH, path)
				}
			}
		}
	}
}