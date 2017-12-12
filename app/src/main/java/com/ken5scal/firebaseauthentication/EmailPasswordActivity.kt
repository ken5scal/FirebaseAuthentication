package com.ken5scal.firebaseauthentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*

class EmailPasswordActivity : AppCompatActivity(), View.OnClickListener, OnCompleteListener<AuthResult> {

    companion object {
        private const val TAG = "emailActivity"
        fun intent(context: Context): Intent =
                Intent(context, EmailPasswordActivity::class.java)
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mRegister: Button
    private lateinit var mSignIn: Button
    private lateinit var mSignOut: Button
    private lateinit var mReset: Button
    private lateinit var mGoogle: SignInButton
    private lateinit var mResult: TextView
    private lateinit var mEmail: TextView
    private lateinit var mPassword: TextView
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.email_password_activity)

        mAuth = FirebaseAuth.getInstance()

        mResult = findViewById(R.id.result)

        mEmail = findViewById(R.id.email)
        mPassword = findViewById(R.id.password)

        mRegister = findViewById(R.id.register)
        mRegister.setOnClickListener(this)

        mSignIn = findViewById(R.id.sign_in)
        mSignIn.setOnClickListener(this)

        mReset = findViewById(R.id.reset)
        mReset.setOnClickListener(this)

        mSignOut = findViewById(R.id.sign_out)
        mSignOut.setOnClickListener(this)

        mGoogle = findViewById(R.id.google_button)
        mGoogle.setOnClickListener(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken()
                .requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000) {
            val task = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            if (task.isSuccess) {
                Log.d("GOOGLE", "signInWithCredential:Success")
                task.signInAccount?.let { firebaseAuthWithGoogle(it) }
            } else {
                Log.w("GOOGLE", "signInWithCredential:failure", null)
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    override fun onStart() {
        super.onStart()
        mAuth.currentUser?.let { updateResult(it) }
    }

    private fun registerAccount(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mAuth.currentUser?.sendEmailVerification()?.addOnCompleteListener {
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Email sent.", Toast.LENGTH_LONG).show()
                    }
                }
                mAuth.signOut()
            } else {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "neither of email nor password can be empty", Toast.LENGTH_LONG).show()
            return
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                return@addOnCompleteListener
            }

            when (mAuth.currentUser?.isEmailVerified) {
                true -> {
                    mAuth.currentUser?.let { updateResult(it) }
                }
                false -> {
                    Toast.makeText(this, "Verify your account from email.", Toast.LENGTH_LONG).show()
                    signOut()
                }
                null -> {
                    Toast.makeText(this, "Something went wrong.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun resetPassword(email: String) {
        mAuth.sendPasswordResetEmail(email, ActionCodeSettings.newBuilder().build())
    }

    private fun updateResult(user: FirebaseUser) {
        mResult.visibility = View.VISIBLE
        mResult.text = "email: " + user.email + "\n" +
                "providers: " + user.providers + "\n" +
                "display: " + user.displayName + "\n" +
                "phone: " + user.phoneNumber + "\n" +
                "email verified?:" + user.isEmailVerified
    }

    private fun signOut() {
        mResult.text = ""
        mResult.visibility = View.GONE
        mAuth.signOut()
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("firebase google: ", acct.id)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("GOOGLE", "signInWithCredential:Success")
                mAuth.currentUser?.let { updateResult(it) }
            } else {
                Log.w("GOOGLE", "signInWithCredential:failure", task.exception)
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.sign_out -> signOut()
            R.id.sign_in -> signInWithEmailAndPassword(mEmail.text.toString(), mPassword.text.toString())
            R.id.register -> registerAccount(mEmail.text.toString(), mPassword.text.toString())
            R.id.reset -> mAuth.currentUser?.let { resetPassword(it.email.toString()) }
            R.id.google_button ->
                startActivityForResult(mGoogleSignInClient.signInIntent, 1000)
        }
    }

    override fun onComplete(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            mAuth.currentUser?.let { updateResult(it) }
        } else {
            Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
        }
    }

}