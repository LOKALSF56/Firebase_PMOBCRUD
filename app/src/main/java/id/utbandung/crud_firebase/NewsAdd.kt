package id.utbandung.crud_firebase

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.annotation.Nullable
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.HashMap
import java.util.Map



class NewsAdd : AppCompatActivity() {
    private var id:String?=""
    private var judul:String?=""
    private var deskripsi:String?=""
    private var image:String?=""
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var title : EditText
    private lateinit var desc : EditText
    private lateinit var imageView : ImageView
    private lateinit var saveNews : Button
    private lateinit var ChooseImage : Button
    private var imageUri : Uri? = null
    private lateinit var dbNews : FirebaseFirestore
    private lateinit var storage : FirebaseStorage
    private lateinit var progressDialog : ProgressDialog
    private fun openFileChooser(){
    val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)

    }
    private fun uploadImageToStorage(newsTitle: String, newsDesc: String){
        imageUri?.let{uri ->
            val storageRef =storage.reference.child("news_image"+System.currentTimeMillis() + ".jpg")
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()
                    savedData(newsTitle, newsDesc, imageUrl)
                }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this@NewsAdd,"Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun savedData(newsTitle: String,newsDesc: String,imageUri:String){
        val news = HashMap<String, Any>()
        news["title"] = newsTitle
        news["desc"] = newsDesc
        news["imageUrl"] = imageUri

        dbNews.collection( "news")
            .add(news)
            .addOnSuccessListener{
                progressDialog.dismiss()
                Toast.makeText( this@NewsAdd,"News added successfully", Toast.LENGTH_SHORT).show()
                title.setText("")
                desc.setText("")
                imageView.setImageResource(0)
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText( this@NewsAdd, "Error adding news: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.w( "NewsAdd", "Error adding document", e)
            }
    }
        override fun onActivityResult(requestCode: Int, resultCode: Int, data : Intent?){
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK &&  data !=null && data.data !=null) {
                imageUri = data.data
                imageView.setImageURI(imageUri)
                imageView.setImageURI(imageUri)
            }

        enableEdgeToEdge()
        setContentView(R.layout.activity_news_add)
        dbNews = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        title = findViewById(R.id.title)
        desc = findViewById(R.id.desc)
        imageView = findViewById(R.id.ImageView)
        saveNews = findViewById(R.id.btnAdd)
        ChooseImage = findViewById(R.id.btnChooseImage)

        progressDialog=ProgressDialog(this@NewsAdd).apply{
            setTitle("Pemuatan.....")
        }
        ChooseImage.setOnClickListener {
        openFileChooser()
        }
        saveNews.setOnClickListener {
            val newsTitle = title.text.toString().trim()
            val newsDesc = desc.text.toString().trim()
            if (newsTitle.isEmpty() || newsDesc.isEmpty()) {
                Toast.makeText( this@NewsAdd,"Title and Description cannot be empty" ,
                    Toast.LENGTH_SHORT).show()
            }
                return@setOnClickListener
            progressDialog.show()
            savedData(newsTitle,newsDesc,image?:"")
        }


                ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}