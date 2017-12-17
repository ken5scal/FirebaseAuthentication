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
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit


class EmailPasswordActivity : AppCompatActivity(), View.OnClickListener, OnCompleteListener<AuthResult> {
    companion object {
        private const val TAG = "emailActivity"
        fun intent(context: Context): Intent =
                Intent(context, EmailPasswordActivity::class.java)
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mResult: TextView
    private lateinit var mEmail: TextView
    private lateinit var mPassword: TextView
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var mSMSVerificationID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.email_password_activity)

        mAuth = FirebaseAuth.getInstance()

        mResult = findViewById(R.id.result)
        mEmail = findViewById(R.id.email)
        mPassword = findViewById(R.id.password)

        findViewById<Button>(R.id.register).setOnClickListener(this)
        findViewById<Button>(R.id.sign_in).setOnClickListener(this)
        findViewById<Button>(R.id.reset).setOnClickListener(this)
        findViewById<Button>(R.id.sign_out).setOnClickListener(this)
        findViewById<Button>(R.id.register_phone).setOnClickListener(this)
        findViewById<Button>(R.id.send_sms_based_2fa_code).setOnClickListener(this)
        findViewById<SignInButton>(R.id.google_button).setOnClickListener(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken()
                .requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential?) {
                Toast.makeText(this@EmailPasswordActivity, ": Verification Completed", Toast.LENGTH_SHORT).show()
            }

            override fun onVerificationFailed(p0: FirebaseException?) {
                Toast.makeText(this@EmailPasswordActivity, p0?.message, Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(p0: String?, p1: PhoneAuthProvider.ForceResendingToken?) {
                mSMSVerificationID = p0.toString()
            }

            override fun onCodeAutoRetrievalTimeOut(p0: String?) {
                mSMSVerificationID = p0.toString()
            }
        }

        val task = Tasks.call(HogeCallable()) // This calls call() inside the HogeCallable
        task.addOnCompleteListener { Log.d("hoge", "fugafuga") }
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
                    mAuth.currentUser?.let { updateResult(it) }
//                    Toast.makeText(this, "Verify your account from email.", Toast.LENGTH_LONG).show()
//                    signOut()
                }
                null -> {
                    Toast.makeText(this, "Something went wrong.", Toast.LENGTH_LONG).show()
                }
            }
        }

//        mAuth.signInWithEmailAndPassword(email, password)
//                .continueWithTask(mAuth.)
    }

    private fun registerPhoneNumber(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Needs to be E.164 format. eg. +81805541xxxx(Japan), [+][country code][subscribed number with area code]
                10,
                TimeUnit.SECONDS,
                this@EmailPasswordActivity,
                mCallbacks
        )
    }

    private fun sendSMSBased2FACode(code: String) {
        val credential = PhoneAuthProvider.getCredential(mSMSVerificationID, code)
        signInWithPhoneAuthCredential(credential)
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
                mAuth.currentUser?.let { updateResult(it) }

                // Link Email Provider account
                val emailCredential = EmailAuthProvider.getCredential("kengoscal@gmail.com", "111111")
                mAuth.currentUser?.linkWithCredential(emailCredential)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        mAuth.currentUser?.let { updateResult(it) }
                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }

                // Link Phone Provider Account
                val smsCredential = PhoneAuthProvider.getCredential(mSMSVerificationID, findViewById<TextView>(R.id.mfa_code).text.toString())
                mAuth.currentUser?.linkWithCredential(smsCredential)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        mAuth.currentUser?.let { updateResult(it) }
                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Log.w("GOOGLE", "signInWithCredential:failure", task.exception)
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
            } else {
                Log.w("Phone", "signInWithPhoneAuthCredential:failure", task.exception)
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
            R.id.register_phone -> registerPhoneNumber(findViewById<TextView>(R.id.phone).text.toString())
            R.id.send_sms_based_2fa_code -> sendSMSBased2FACode(findViewById<TextView>(R.id.mfa_code).text.toString())
            R.id.google_button -> startActivityForResult(mGoogleSignInClient.signInIntent, 1000)
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

class HogeCallable : Callable<String> {
    override fun call(): String {
        Log.d("HOgecallble", "getcalled")
        return "Call me maybe."
    }
}

class SeparateWays : Continuation<String, List<String>> {
    override fun then(task: Task<String>): List<String> {
        return ArrayList(task.result.split(" +"))
    }
}