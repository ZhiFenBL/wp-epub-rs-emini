package us.wpdl.wprust

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loading)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loading)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Handler(Looper.getMainLooper()).postDelayed({
            navigateWithFade(MainActivity::class.java)
        }, 2500L)
    }

    private fun navigateWithFade(targetClass: Class<*>) {
        val intent = Intent(this, targetClass)
        val options = ActivityOptions.makeCustomAnimation(
            this,
            android.R.anim.fade_in,    // Fade in for target activity
            android.R.anim.fade_out    // Fade out for current activity
        )
        startActivity(intent, options.toBundle())
        finish() // Close the current activity
    }
}