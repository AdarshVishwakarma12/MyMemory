package com.example.mymemory

import android.os.Build
import android.util.Log
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.mymemory.models.BoardSize
import com.example.mymemory.utils.EXTRA_BOARD_SIZE

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CreateActivity : AppCompatActivity() {

    private var numImagesRequired = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enables drawing under system bars (status bar, navigation bar)
        enableEdgeToEdge()

        setContentView(R.layout.activity_create)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Handle both new and old API levels
        var boardSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_BOARD_SIZE, BoardSize::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_BOARD_SIZE) as? BoardSize
        }

        Log.i("CreateActivity", "check me: $boardSize")

        numImagesRequired = boardSize?.getNumPairs() ?: BoardSize.EASY.getNumPairs()

        supportActionBar?.title = "Choose pics (0 / ${numImagesRequired})"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}