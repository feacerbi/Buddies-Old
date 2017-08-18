package br.com.felipeacerbi.buddies.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import br.com.felipeacerbi.buddies.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_fullscreen_photo.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenPhotoActivity : AppCompatActivity() {

    var mVisible: Boolean = false
    var photoPath: String? = null
    var photoMessage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_photo)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        handleIntent(intent)

        mVisible = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreen_photo.setOnClickListener { toggle() }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.

        show()
    }

    override fun onResume() {
        super.onResume()

        if(photoMessage != null) {
            post_message.text = photoMessage
            fullscreen_content_controls.visibility = View.VISIBLE
        } else {
            fullscreen_content_controls.visibility = View.GONE
        }

        if(photoPath != null) {
            Picasso.with(this)
                    .load(photoPath)
                    .error(R.drawable.no_phototn)
                    .placeholder(R.drawable.no_phototn)
                    .fit()
                    .centerCrop()
                    .into(fullscreen_photo)
        }
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreen_content_controls.visibility = View.GONE
        fullscreen_photo.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE

        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
//        mHideHandler.removeCallbacks(mShowPart2Runnable)
//        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        supportActionBar?.show()
        fullscreen_content_controls.visibility = View.VISIBLE
        // Show the system bar
        fullscreen_photo.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        mVisible = true

        // Schedule a runnable to display UI elements after a delay
//        mHideHandler.removeCallbacks(mHidePart2Runnable)
//        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun  handleIntent(intent: Intent?) {
        if(intent != null) {
            photoPath = intent.extras.getString(PHOTO_PATH)
            photoMessage = intent.extras.getString(PHOTO_MESSAGE)
            supportActionBar?.title = intent.extras.getString(TOOLBAR_TITLE)
        }
    }

    companion object {

        val PHOTO_PATH = "photo_path"
        val TOOLBAR_TITLE = "toolbar_title"
        val PHOTO_MESSAGE = "photo_message"
    }
}
