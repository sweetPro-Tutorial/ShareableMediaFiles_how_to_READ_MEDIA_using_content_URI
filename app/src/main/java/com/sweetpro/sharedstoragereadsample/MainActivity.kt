package com.sweetpro.sharedstoragereadsample

import android.Manifest
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    data class ImageItem(val contentUri: Uri, val name: String, val date: Int)
    val images = mutableListOf<ImageItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // main process:
        // 0. setup ui action: for handling button click action: read and show latest photo
        setupUiAction()
        // 1. setup environment: need access permission for shared storage
        setupPermission()
    }

    private fun setupUiAction() {
        val readAndShowButton = findViewById<Button>(R.id.button)
        readAndShowButton.setOnClickListener {
            // 2. make data: make image list using MediaStore APIs.
            makeImageList()
            // 3. process data: show latest photo from the image list
            showLatestPhoto()
        }
    }

    // prepare id for request. need unique vale
    val MY_REQUEST_READ_EXTERNAL_STORAGE = 20000
    private fun setupPermission() {
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)

        //  note: i've omitted a lot of code for simplicity
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                MY_REQUEST_READ_EXTERNAL_STORAGE)
    }

    private fun makeImageList() {
        // init list
        images.clear()

        // note: using MediaStore API to read shared storage is SQL-like...
        // sql example:
        // SELECT column1, column2 FROM table1 WHERE column2 >= 'value' ORDER BY ASC;
        // |<------ 2------------>||<---- 1-->||<----- 3 ------------>||<---- 4 --->|
        //      what data           from where    matching condition     how to sort

        // process:
        // prepare 4 kinds of params:
        //  1. from where: collection uri where Media data exists
        val targetCollectionUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        //  2. what data: field names of image information
        //     ID, NAME, DATE
        val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED
        )

        //  3. matching conditions: to fetch
        val selection = "${MediaStore.Images.Media.DATE_ADDED} >= ?"
        val selectionArg = arrayOf<String>(
                getDaysAgo(-7)  // 1 week ago
        )

        //  4. how to sort: ASC or DESC
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"  // for getting latest photo

        // fetch data using above params
        val query: Cursor? = applicationContext.contentResolver.query(
                targetCollectionUri,
                projection,
                selection,
                selectionArg,
                sortOrder
        )
        query?.use { cursor ->
            // prepare data index
            val idColumn   = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            // while(selected data exist)
            while(cursor.moveToNext()) {
                // read field from each fetched image info
                val id: Long     = cursor.getLong(idColumn)
                val name: String = cursor.getString(nameColumn)
                val date: Int    = cursor.getInt(dateColumn)

                // very important:
                //  Additional information 'content uri' for each media file is useful
                //  to access shareable media files(ex: images/video/audio)
                val contentUri = ContentUris.withAppendedId(targetCollectionUri, id)

                //  add to image list
                images.add(ImageItem(contentUri, name, date))
            }

            println("images=${images}")
        }
    }

    private fun showLatestPhoto() {
        if (images.size <= 0) return;
        val latestPhoto: ImageItem = images.get(0)

        // display content name: latest photo
        findViewById<TextView>(R.id.textView).text = latestPhoto.name
        // display latest photo
        findViewById<ImageView>(R.id.imageView).setImageURI(latestPhoto.contentUri)
    }
}