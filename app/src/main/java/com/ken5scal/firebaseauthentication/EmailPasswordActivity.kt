package com.ken5scal.firebaseauthentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

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
    private lateinit var mResult: TextView
    private lateinit var mEmail: TextView
    private lateinit var mPassword: TextView

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

        mSignOut = findViewById(R.id.sign_out)
        mSignOut.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        mAuth.currentUser?.let { updateResult(it) }
    }

    private fun registerAccount(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this)
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "neither of email nor password can be empty", Toast.LENGTH_LONG).show()
            return
        }

        mAuth.signInWithEmailAndPassword(email, password)
//        mAuth.sendPasswordResetEmail()
//        mAuth.verifyPasswordResetCode()
//        mAuth.fetchProvidersForEmail()
//        mAuth.sendPasswordResetEmail()
//        mAuth.checkActionCode()
//        mAuth.currentUser.updateProfile()
//        mAuth.currentUser.updatePassword()
//        mAuth.currentUser.updateEmail()
//        mAuth.currentUser.phoneNumber()
//        mAuth.currentUser.
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

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.sign_out -> signOut()
            R.id.sign_in -> signInWithEmailAndPassword(mEmail.text.toString(), mPassword.text.toString())
            R.id.register -> registerAccount(mEmail.text.toString(), mPassword.text.toString())
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