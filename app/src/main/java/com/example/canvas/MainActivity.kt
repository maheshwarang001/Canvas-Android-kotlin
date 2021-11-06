package com.example.canvas

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.loader.content.AsyncTaskLoader
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.brushsize_dialogue.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest


class MainActivity : AppCompatActivity() {

    private var colorimagebutton: ImageButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //default brush value
        drawingview_tv.setNewSizeBrush(10.0.toFloat())

        colorimagebutton = color_holder[1] as ImageButton
        colorimagebutton!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallete_pressed)
        )

        image_btn_brush.setOnClickListener {
            brushdailoguebtn()
        }

        //gallery permission
        gallery.setOnClickListener {
            if (tocheckpermission()) {

                //if permission granted pick image from background

                val pickimage = Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                startActivityForResult(pickimage, GALLERY)

            } else {
                requestStoragepermission()
            }
        }

        Undo.setOnClickListener {
            drawingview_tv.onUndo(Undo)
        }
        redo.setOnClickListener {
            drawingview_tv.onRedo(redo)
        }
        save.setOnClickListener {
            if (tocheckpermission()) {
                bitmapasync(viewtobitmap(drawingview_framelayout)).execute()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                try {
                    if (data!!.data != null) {
                        Log.d("image", "import image")
                        image_view_change.visibility = View.VISIBLE
                        image_view_change.setImageURI(data.data)
                    } else {
                        Toast.makeText(this, "Error in parsing the image", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun brushdailoguebtn() {
        val brushdialog = Dialog(this)
        brushdialog.setContentView(R.layout.brushsize_dialogue)
        brushdialog.setTitle("Brush Size: ")
        brushdialog.size_1.setOnClickListener {
            drawingview_tv.setNewSizeBrush(10.0.toFloat())
            brushdialog.dismiss()
        }
        brushdialog.size_2.setOnClickListener {
            drawingview_tv.setNewSizeBrush(30.0.toFloat())
            brushdialog.dismiss()
        }
        brushdialog.size_3.setOnClickListener {
            drawingview_tv.setNewSizeBrush(60.0.toFloat())
            brushdialog.dismiss()
        }

        brushdialog.show()
    }

    fun paintClicked(view: View) {
        if (view != colorimagebutton) {
            val imagebtn = view as ImageButton

            val colortag = imagebtn.tag.toString()
            drawingview_tv.setcolor(colortag)
            imagebtn.setImageDrawable(ContextCompat.getDrawable(this,
                R.drawable.pallete_pressed))

            colorimagebutton!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_shape))
            colorimagebutton = view
        }
    }

    //if user denies storage permission
    private fun requestStoragepermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())
        ) {
            Toast.makeText(this, "Permission required to import/export images",
                Toast.LENGTH_SHORT).show()
        }
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            WRITE_READ_EXTERNAL_STORAGE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_READ_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(applicationContext, "Permission Denied Granted", Toast.LENGTH_SHORT)
                .show()
        }
    }

    //boolean to check permission
    private fun tocheckpermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun viewtobitmap(view: View): Bitmap {
        val returnbitmap = Bitmap.createBitmap(view.height, view.width, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(returnbitmap)
        val drawabel = view.background
        if (drawabel != null) {
            drawabel.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)
        return returnbitmap

    }

    private inner class bitmapasync(val mBitmap: Bitmap) :
        AsyncTask<Any, Void, String>() {

        private lateinit var mProgressDialog: Dialog


        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }


        override fun doInBackground(vararg params: Any?): String {
            var result = ""
            if (mBitmap != null) {
                try {
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                    val filename =
                        File(externalCacheDir!!.absoluteFile.toString() + File.separator + "canvas_app_" + System.currentTimeMillis() / 1000 + ".png")
                    val fileOS = FileOutputStream(filename)
                    fileOS.write(bytes.toByteArray())
                    fileOS.close()
                    result = filename.absolutePath
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            cancelProgressDialog()
            if (!result!!.isEmpty()) {
                Toast.makeText(this@MainActivity,
                    "File saved successfully :$result",
                    Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@MainActivity,
                    "Failed, Something went wrong. Try again",
                    Toast.LENGTH_SHORT).show()
            }
            shareImage(Uri.parse(result))
            savetoGallery()
        }

        private fun shareImage(uri: Uri) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(
                Intent.createChooser(
                    intent, "Share"))
        }

        private fun showProgressDialog() {
            mProgressDialog = Dialog(this@MainActivity)
            mProgressDialog.setContentView(R.layout.customdialog)
            mProgressDialog.show()

        }

        private fun cancelProgressDialog() {
            mProgressDialog.dismiss()
        }

        private fun createFilename(filename: String): String {
            val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SimpleDateFormat("YYYYMMdd-HHmm.ssSSS")
            } else {
                TODO("VERSION.SDK_INT < N")
            }
            val dateString = formatter.format(Date()) + "_"

            return dateString + filename + ".jpg"
        }

        private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
            if (outputStream != null) {
                try {
                    mBitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    outputStream.close()
                } catch (e: Exception) {
                    Log.e("**Exception", "Could not write to stream")
                    e.printStackTrace()
                }
            }

        }


        private fun savetoGallery(): String {
            var result = ""

            var resolver = this@MainActivity.contentResolver

            val foldername =
                packageManager.getApplicationLabel(applicationInfo).toString().replace(" ", "")
            val filename = createFilename(foldername)
            val saveLocation = Environment.DIRECTORY_PICTURES + File.separator + foldername

            if (Build.VERSION.SDK_INT >= 29) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)

                // RELATIVE_PATH and IS_PENDING are introduced in API 29.
                values.put(MediaStore.Images.Media.RELATIVE_PATH, saveLocation)
                values.put(MediaStore.Images.Media.IS_PENDING, true)


                val uri: Uri? = resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                if (uri != null) {
                    //val outstream = resolver.openOutputStream(uri)

                    if (mBitmap != null) {
                        saveImageToStream(mBitmap, resolver.openOutputStream(uri))
                    }
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    this@MainActivity.contentResolver.update(uri, values, null, null)
                    result = uri.toString()
                }
            }
            return result
        }

    }

    companion object {
        private const val WRITE_READ_EXTERNAL_STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }
}
