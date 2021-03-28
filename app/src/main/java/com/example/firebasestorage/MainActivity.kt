package com.example.firebasestorage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.downloader.*
import com.example.firebasestorage.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity(), FirebaseAdapter.OnItemClickListener {

    lateinit var binding: ActivityMainBinding
    val ImageBack = 1
    lateinit var storage: StorageReference
    lateinit var databaseReference: DatabaseReference
    var dataUri: ArrayList<Uri> = ArrayList()
    val list: ArrayList<DataClass> = ArrayList()
    var dataUrl: String = ""
    private val REQ_CODE = 100
    var positionItem = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        storage = FirebaseStorage.getInstance().getReference().child("ImageFolder")
        databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://localstorage-3b71a-default-rtdb.firebaseio.com/").child("Image")


        binding.choose.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(intent, ImageBack)
        }

        binding.upload.setOnClickListener {
            uploadImage()
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        loadDataInRecyclerView()

    }

    private fun loadDataInRecyclerView() {

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val model = data.getValue(DataClass::class.java)
                    list.add(model!!)
                    binding.recyclerView.adapter = FirebaseAdapter(list, this@MainActivity)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Data is Canceled", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun uploadImage() {
        if (dataUri.isNotEmpty()) {
            for (item in dataUri) {
                val imageName: StorageReference = storage.child("image" + item.lastPathSegment)
                imageName.putFile(item).addOnSuccessListener(OnSuccessListener {
                    imageName.getDownloadUrl().addOnSuccessListener(OnSuccessListener<Uri> { uri ->
                        val hashMap: HashMap<String, String> = HashMap()
                        hashMap.put("imageUrl", uri.toString())
                        databaseReference.push().setValue(hashMap)
                        Toast.makeText(
                            this,
                            "Now data is inserted in Realtime DataBase",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                })
            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ImageBack) {
            if (resultCode == RESULT_OK) {
                if (data!!.getClipData() != null) {
                    val clipCount: Int = data.getClipData()!!.getItemCount()
                    var currentImage = 0
                    while (currentImage < clipCount) {
                        val image: Uri = data.getClipData()!!.getItemAt(currentImage).uri
                        dataUri.add(image)
                        currentImage += 1
                    }
                } else {
                    Toast.makeText(this, "Please Select Multiple Image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onItemClick(item: String,position:Int) {
        dataUrl = item
        positionItem = position
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            //Permission Denied Request it
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQ_CODE)
        } else {

            startDownloding()
        }
        Toast.makeText(this, "You clicked on Download Button", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startDownloding()
                } else {
                    Toast.makeText(this, "Permission not Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startDownloding() {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val file = File(path,"Demo")
        val download = PRDownloader.download(dataUrl,file.absolutePath,"Demo.jpg")
            .build()
            .setOnStartOrResumeListener { object :OnStartOrResumeListener{ override fun onStartOrResume() {} } }
            .setOnPauseListener(object :OnPauseListener{ override fun onPause() {} })
            .setOnCancelListener(object :OnCancelListener{ override fun onCancel() {} })
            .setOnProgressListener(object:OnProgressListener{
                override fun onProgress(progress: Progress) {
                    var percent = (progress.currentBytes.toFloat() / progress.totalBytes.toFloat())*100.00
                    updateStatus(percent.toInt(),positionItem)
                } })
            .start(object:OnDownloadListener{ override fun onDownloadComplete() {}override fun onError(error: Error?) {} })

    }

    private fun updateStatus(percent: Int, positionItem: Int) {

        val view = binding.recyclerView.getChildAt(positionItem)
        val progressbar:ProgressBar = view.findViewById(R.id.progressbar)
        val imageSeenBtn:Button =view.findViewById(R.id.ImageSeen)
        val btn:Button = view.findViewById(R.id.btnDownload)
        progressbar.setProgress(percent)
        if (percent>=99){
            btn.setEnabled(false)
            imageSeenBtn.setEnabled(true)
        }else{
            imageSeenBtn.setEnabled(false)
        }



    }

}
