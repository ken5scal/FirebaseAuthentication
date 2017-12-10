package com.ken5scal.firebaseauthentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth


class EmailPasswordActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "emailActivity"
        fun intent(context: Context): Intent =
                Intent(context, EmailPasswordActivity::class.java)
    }

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        val user = mAuth!!.currentUser
        Log.d(TAG, user.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        signOut()
    }

    private fun registerAccount(email: String, password: String) {
        mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "register by email/password:success")
                val user = mAuth!!.currentUser
                Log.d(TAG, user.toString())
                // updateUI(user);
            } else {
                Log.d(TAG, "register by email/password:failed: ", task.exception)
                // updateUI(null);
            }
        }
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "sign in by email/password: success")
                val user = mAuth!!.currentUser
                Log.d(TAG, user.toString())
                // updateUI(user);
            } else {
                Log.d(TAG, "sign in by email/password: failed: ", task.exception)
                // updateUI(null);
            }
        }
    }

    private fun signOut() {
        mAuth!!.signOut()
    }
}