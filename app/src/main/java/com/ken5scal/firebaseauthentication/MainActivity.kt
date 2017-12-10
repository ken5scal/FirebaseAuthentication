package com.ken5scal.firebaseauthentication

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val AUTH_UI_REQUEST = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            EmailPasswordActivity.intent(this).let { startActivity(it) }
//            val providers : List<AuthUI.IdpConfig> = listOf(
//                    AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
//                    AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
//                    AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
//            )
//
//            startActivityForResult(
//                    AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(),
//                    AUTH_UI_REQUEST
//            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTH_UI_REQUEST) {
            val user = FirebaseAuth.getInstance().currentUser
            Log.d("AuthUIResult -> Success", user.toString())
        }
    }
}
