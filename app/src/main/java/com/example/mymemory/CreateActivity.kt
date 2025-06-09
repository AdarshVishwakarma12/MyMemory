package com.example.mymemory

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mymemory.models.BoardSize
import com.example.mymemory.utils.EXTRA_BOARD_SIZE
import com.example.mymemory.utils.requestPermission
import com.example.mymemory.utils.isPermissionGranted

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CreateActivity : AppCompatActivity() {

    companion object {
        const val TAG = "CreateActivity"
        private var READ_PHOTOS_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        private var READ_EXTERNAL_PHOTOS_CODE = 248
    }

    private lateinit var rvImagePicker: RecyclerView
    private lateinit var etGameName: EditText
    private lateinit var btnSave: Button
    private lateinit var adapter: ImagePickerAdapter

    private lateinit var createActivityForImageLauncher: ActivityResultLauncher<Intent>

    private var numImagesRequired = 1
    private var chosenImageUris = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enables drawing under system bars (status bar, navigation bar)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create)

        // Setting back button on status bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        createActivityForImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Handle result if needed
                Log.i(TAG, "got result back")
            }
        }

        // initializing late init
        rvImagePicker = findViewById(R.id.rvImagePicker)
        etGameName = findViewById(R.id.etGameName)
        btnSave = findViewById(R.id.btnSave)

        // Handle both new and old API levels
        var boardSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_BOARD_SIZE, BoardSize::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_BOARD_SIZE) as? BoardSize
        }
        Log.i("CreateActivity", "check me: $boardSize")
        // boardSize = (boardSize?.getNumPairs() ?: BoardSize.EASY) as BoardSize?
        if(boardSize == null) { boardSize = BoardSize.MEDIUM }

        Log.i("CreateActivity", "check me: $boardSize")

        numImagesRequired = boardSize!!.getNumPairs()
        supportActionBar?.title = "Choose pics (0 / ${numImagesRequired})"

        adapter = ImagePickerAdapter(this, chosenImageUris, boardSize, object: ImagePickerAdapter.ImageClickListener {
            override fun onPlaceHolderClick() {
                if(isPermissionGranted(this@CreateActivity, READ_PHOTOS_PERMISSION)) {
                    launchIntentForPhotos()
                } else {
                    requestPermission(this@CreateActivity, READ_PHOTOS_PERMISSION, READ_EXTERNAL_PHOTOS_CODE)
                }
            }
        })
        rvImagePicker.adapter = adapter
        rvImagePicker.setHasFixedSize(true)
        rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == READ_EXTERNAL_PHOTOS_CODE) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchIntentForPhotos()
            } else {
                Toast.makeText(this, "Permission Not Granted!", Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode != RESULT_OK || data == null) {
            Log.w(TAG, "didn't get data back from launched Activity")
            return
        }
        val selectedUri = data.data
        val clipData = data.clipData
        if(clipData != null) {
            Log.i(TAG, "clipData NumImages: ${clipData.itemCount}: $clipData")
            for(i in 0 until clipData.itemCount) {
                val clipItem = clipData.getItemAt(i)
                if(chosenImageUris.size < numImagesRequired) {
                    chosenImageUris.add(clipItem.uri)
                }
            }
        } else if(selectedUri != null) {
            Log.i(TAG, "data: $selectedUri")
            chosenImageUris.add(selectedUri)
        }

        adapter.notifyDataSetChanged()
        supportActionBar?.title = "Choose pics (${chosenImageUris.size} / ${numImagesRequired})"

    }

    private fun launchIntentForPhotos() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

        createActivityForImageLauncher.launch(Intent.createChooser(intent, "Choose Pics"))
    }
}