package com.example.booksim_read

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.booksim_read.databinding.ActivityPdfDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream


class PdfDetailActivity : AppCompatActivity() {

    //view binding
    private lateinit var  binding: ActivityPdfDetailBinding

    private companion object {
        //TAG
        const val TAG = "BOOK_DETAILS_TAG"
    }

    //book id
    private  var bookId = ""

    //get from firebas
    private var bookTitle = ""
    private var bookUrl = ""

    private var isInMyFavorite = false

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get book id from intent
        bookId = intent.getStringExtra("bookId")!!

        //init progress bar
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser != null){
            //user is logged in, check if book is in fav or not
            checkIsFavourite()
        }

        //increment book view count, whenever this page starts
        MyApplication.incrementBookViewCount(bookId)

        loadBookDetails()

        //handle backbuton click, goback
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click, open pdf view activity
        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("bookId", bookId)
            startActivity(intent)
        }

        //handle click download book
        binding.downloadBookBtn.setOnClickListener {
            //first check WRITE_EXTERNAL_STORAGE permission, if granted download book
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "onCreate: STORAGE PERMISSION is already granted")
                downloadBook()
            }
            else{
                Log.d(TAG, "onCreate: STORAGE PERMISSION was not granted, LETS request it")
                requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        //handle click favourite book add/remove
        binding.favoriteBtn.setOnClickListener {
            //1) Click if user is logged in or not
            if(firebaseAuth.currentUser == null){
                //user not logged in, cant do favourite function
                Toast.makeText(this, "You 're not logged in.", Toast.LENGTH_SHORT).show()
            }
            else{
                //user is logged in, we can do favourite function
                if(isInMyFavorite){
                    //already in fav, remove
                    removeFromFavourite()
                }
                else{
                    //not in fav., add
                    addToFavourite()
                }
            }
        }
    }

    private val requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted: Boolean ->
        //check if granted or not
        if(isGranted){
            Log.d(TAG, "onCreate: STORAGE PERMISSION is already granted")
            downloadBook()
        }
        else{
            Log.d(TAG, "onCreate: STORAGE PERMISSION is denied")
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadBook(){
        Log.d(TAG, "downloadBook: Downloading Book")
        progressDialog.setMessage("Downloading Book")
        progressDialog.show()

        //load download from firebase storage  using url
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes->
                Log.d(TAG, "downloadBook: Book downloaded...")
                savedToDownloadsFolder(bytes)
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Log.d(TAG, "downloadBook: Failed to download book due to ${e.message}")
                Toast.makeText(this, "Failed to download book due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun savedToDownloadsFolder(bytes: ByteArray) {
        Log.d(TAG, "savedToDownloadsFolder: Saving downloaded book")

        val nameWithExtention = "$bookTitle.pdf"

        try {
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdirs() //create folder if not exists

            val filePath = downloadsFolder.path +"/"+ nameWithExtention

            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Toast.makeText(this, "Saved to Downloads Folder", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "savedToDownloadsFolder: Saved to Downloads Folder")
            progressDialog.dismiss()
            incrementedDownloadCount()
        }
        catch (e: Exception){
            Log.d(TAG, "savedToDownloadsFolder: Failed to save due to ${e.message}")
            Toast.makeText(this, "Failed to save due to ${e.message}", Toast.LENGTH_SHORT).show()
        }

    }

    private fun incrementedDownloadCount() {
        //increment downloads count to firebase db
        Log.d(TAG, "incrementedDownloadCount: ")

        //STEP 1) Get previous downloads count
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get downloads count
                    var downloadsCount = "${snapshot.child("downloadsCount").value}"
                    Log.d(TAG, "onDataChange: Current Downloads Count: $downloadsCount")

                    if (downloadsCount == "" || downloadsCount == "null") {
                        downloadsCount = "0"
                    }

                    //convert to long and increment 1
                    val newDownloadCount: Long = downloadsCount.toLong() + 1
                    Log.d(TAG, "onDataChange: New Downloads Count: $newDownloadCount")

                    //setup data to update to db
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["downloadsCount"] = newDownloadCount

                    //STEP 2) Update new incremented downloads count to db
                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "onDataChange: Downloads count incremented")
                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "onDataChange: Failed to increment due to ${e.message}")

                        }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadBookDetails() {
        //Books > bookId > Details
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get data
                        val categoryId = "${snapshot.child("categoryId").value}"
                        val description = "${snapshot.child("description").value}"
                        val downloadsCount = "${snapshot.child("downloadsCount").value}"
                        val timestamp = "${snapshot.child("timestamp").value}"
                        bookTitle = "${snapshot.child("title").value}"
                        val uid = "${snapshot.child("uid").value}"
                        bookUrl = "${snapshot.child("url").value}"
                        val viewsCount = "${snapshot.child("viewsCount").value}"

                        //format date
                        val date = MyApplication.formatTimeStamp(timestamp.toLong())

                        //load pdf category
                        MyApplication.loadCategory(categoryId, binding.categoryTv)

                        //load odf thumbnail, pages count
                        MyApplication.loadPdfFormUrlSinglePage("$bookUrl","$bookTitle", binding.pdfView, binding.progressBar, binding.pagesTv)

                        //load pdf size
                        MyApplication.loadPdfSize("$bookUrl","$bookTitle", binding.sizeTv)

                        //set data
                        binding.titleTv.text = bookTitle
                        binding.descriptionTv.text = description
                        binding.viewsTv.text = viewsCount
                        binding.downloadsTv.text = downloadsCount
                        binding.dateTv.text = date

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
    }

    private fun checkIsFavourite(){
        Log.d(TAG, "checkIsFavourite: Checking if book is in fav or not")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if (isInMyFavorite) {
                        //available in favourite
                        Log.d(TAG, "onDataChange: Available in favorite")
                        //set drawable filled favorite
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite_filled_white,
                            0,
                            0
                        )
                        binding.favoriteBtn.text = "Remove Favorite"
                    } else {
                        //not available in favorite
                        Log.d(TAG, "onDataChange: Not available in favorite")
                        //set drawable border favorite
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite_border_white,
                            0,
                            0
                        )
                        binding.favoriteBtn.text = "Add Favorite"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun addToFavourite(){
        Log.d(TAG, "addToFavourite: Adding to fav")
        val timestamp = System.currentTimeMillis()

        //setup data to add in db
        val hashMap = HashMap<String,Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        //save to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                //add to fav
                Log.d(TAG, "addToFavourite: Added to fav")
                Toast.makeText(this, "Added to favorite", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                //failed to add to fav
                Log.d(TAG, "addToFavourite: Failed to add to fav due to ${e.message}")
                Toast.makeText(this, "Failed to add to fav due to ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun removeFromFavourite(){
        Log.d(TAG, "removeFromFavourite: Removing from fav")

        //database ref
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "removeFromFavourite: Removed form fav")
                Toast.makeText(this, "Removed from favorite", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Log.d(TAG, "removeFromFavourite: Failed to remove from fav due to ${e.message}")
                Toast.makeText(this, "Failed to remove from fav due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}