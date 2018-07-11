package biz.cactussoft.ethereumwallet.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

/**
 * Created by viktor.chukholskiy
 * 11/07/18.
 */
abstract class BaseHomeActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val supportActionBar = supportActionBar
		if (supportActionBar != null) {
			supportActionBar.setDisplayHomeAsUpEnabled(true)
			supportActionBar.setDisplayShowHomeEnabled(true)
			supportActionBar.setHomeButtonEnabled(true)
		}
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (item.itemId == android.R.id.home) {
			onBackPressed()
			return true
		}
		return super.onOptionsItemSelected(item)
	}
}