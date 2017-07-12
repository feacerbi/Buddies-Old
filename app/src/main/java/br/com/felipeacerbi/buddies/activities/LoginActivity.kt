package br.com.felipeacerbi.buddies.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView

import android.content.Intent
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import android.widget.Toast
import br.com.felipeacerbi.buddies.R
import com.facebook.*
import com.facebook.login.LoginManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*

import java.lang.Exception
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

import com.facebook.login.LoginResult
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity(),
        OnCompleteListener<AuthResult>,
        GoogleApiClient.OnConnectionFailedListener,
        FacebookCallback<LoginResult> {

    companion object {
        val TAG = "LoginActivity"
        val RC_SIGN_IN = 100
    }

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val faceCallbackManager: CallbackManager by lazy {
        CallbackManager.Factory.create()
    }

    val faceLoginManager: LoginManager by lazy {
        LoginManager.getInstance()
    }

    val gso: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build()
    }

    val googleApiClient: GoogleApiClient by lazy {
        GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Set up the login form.
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                validate(this::attemptLogin)
                return@OnEditorActionListener true
            }
            false
        })

        email_sign_in_button.setOnClickListener { validate(this::attemptLogin) }
        email_register_button.setOnClickListener { validate(this::attemptRegister) }
        google_sign_in_button.setOnClickListener { signInWithGoogle() }

        face_sign_in_button.setReadPermissions("email", "public_profile")
        face_sign_in_button.registerCallback(faceCallbackManager, this)
    }

    override fun onError(error: FacebookException?) {
        Log.d(TAG, "Facebook error sign in")
    }

    override fun onCancel() {
        Log.d(TAG, "Facebook cancel sign in")
    }

    override fun onSuccess(result: LoginResult?) {
        Log.d(TAG, "Facebook success sign in " + result)
        showProgress(true)
        handleFacebookAccessToken(result?.accessToken)
    }

    override fun onStart() {
        super.onStart()
        if(isSignedIn()) {
            launchMainActivity()
        } else {
            if (email.text.isEmpty()) {
                email.requestFocus()
            } else {
                password.requestFocus()
            }

            password.setText("")
            faceLoginManager.logOut()
        }
    }

    private fun signInWithGoogle() {
        showProgress(true)
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        Log.d(TAG, "Starting Google sign in")
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Toast.makeText(this, "Fail to login", Toast.LENGTH_SHORT).show()
        Log.w(TAG, "Login failed " + result.errorMessage)
        showProgress(false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "Activity result code " + requestCode)

        faceCallbackManager.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleSignInResult(result)
        }
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken?) {
        if(accessToken != null) {
            val credential = FacebookAuthProvider.getCredential(accessToken.token)
            signInUsingCredential(credential)
        } else {
            Log.d(TAG, "Face token null")
        }
    }

    private fun handleSignInResult(result: GoogleSignInResult) {
        if (result.isSuccess) {
            Log.d(TAG, "Login success")
            // Signed in successfully, show authenticated UI.
            val account = result.signInAccount
            if(account != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                signInUsingCredential(credential)
            } else {
                Log.w(TAG, "Login account null")
                showProgress(false)
            }
        } else {
            Log.w(TAG, "Google login is not success")
            // Signed out, show unauthenticated UI.
            showProgress(false)
        }
    }

    fun signInUsingCredential(credential: AuthCredential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this)
                { task ->
                    // Sign in success, update UI with the signed-in user's information
                    if (task.isSuccessful && isSignedIn()) {
                        launchMainActivity()
                    }

                    Log.w(TAG, "Firebase login was not succeeded")
                    // If sign in fails, display a message to the user.
                    showProgress(false)
                }
    }

    fun isSignedIn() = firebaseAuth.currentUser != null

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun validate(action: (String, String) -> Unit) {
        // Reset errors.
        email.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if(TextUtils.isEmpty(passwordStr)) {
            password.error = getString(R.string.error_field_required)
            focusView = password
            cancel = true
        } else if (!isPasswordValid(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            email.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        } else if (!isEmailValid(emailStr)) {
            email.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)

            action(emailStr, passwordStr)
        }
    }

    private fun attemptLogin(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this)
    }

    private fun attemptRegister(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this)
    }

    override fun onComplete(task: Task<AuthResult>) {
        var focusView: View? = null
        if(task.isSuccessful) {
            Log.d(TAG, "Login successful")
            if(firebaseAuth.currentUser != null) {
                launchMainActivity()
            } else {
                Toast.makeText(this, "Fail to login, user invalid", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Login user invalid " + task.result.toString())
                focusView = email
                showProgress(false)
            }
        } else if(task.exception != null) {
            try {
                throw task.exception!!
            } catch(e: FirebaseAuthUserCollisionException) {
                Toast.makeText(this, "Fail to login, user already registered", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Login failed " + e.message)
                focusView = email
            } catch(e: FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(this, "Fail to login, wrong password", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Login failed " + e.message)
                focusView = password
            } catch(e: Exception) {
                Toast.makeText(this, "Fail to login, unknown exception", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Login failed " + e.message)
                focusView = email
            }
            showProgress(false)
        } else {
            Toast.makeText(this, "Fail to login, unknown error", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "Login failed " + task.result.toString())
            focusView = email
            showProgress(false)
        }
        focusView?.requestFocus()
    }

    fun isEmailValid(email: String): Boolean {
        return email.contains("@")
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun launchMainActivity() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(mainActivityIntent)
        finish()
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        login_form.visibility = if (show) View.GONE else View.VISIBLE
        login_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_form.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        login_progress.visibility = if (show) View.VISIBLE else View.GONE
        login_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }

    // Just in case
    fun printFaceLoginHash() {
        try {
            val info = packageManager.getPackageInfo(
                    "br.com.felipeacerbi.buddies",
                    PackageManager.GET_SIGNATURES)
            Log.w(TAG, "Print face hash")
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d(TAG, "KeyHash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }
    }
}
