package com.ken5scal.firebaseauthentication

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth



class EmailPasswordActivity : AppCompatActivity() {
    val TAG = "emailActivity"
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

    private fun registerAccount(email: String, password: String) {
        mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = mAuth!!.currentUser
                        Log.d(TAG, user.toString())
                        // updateUI(user);
                    } else {
                        Log.d(TAG, "createUserWithEmail:failed: ", task.exception)
                        // updateUI(null);
                    }
                }
    }
}