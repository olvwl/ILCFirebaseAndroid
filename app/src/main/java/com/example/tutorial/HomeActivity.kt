package com.example.tutorial

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    private var req = 1
    private var status = 11
    private lateinit var filepath : Uri
    private lateinit var mImage : ImageView
    private lateinit var mNama : EditText
    private lateinit var mStorage : StorageReference
    private lateinit var mDatabase : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        mNama = findViewById<EditText>(R.id.et_nama)
        mImage = findViewById<ImageView>(R.id.iv_image)

        mStorage = FirebaseStorage.getInstance().getReference("uploads/")
        mDatabase = FirebaseDatabase.getInstance().getReference("uploads/")

        iv_image.setOnClickListener {
            FileChooser()
        }

        btn_upload.setOnClickListener {
            UploadFile()
        }


    }

    private fun FileChooser() {
        var i = Intent()
        i.setType("image/*")
        i.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(i,req)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == req && resultCode == RESULT_OK && data != null) {
            status = 11
            filepath = data.data!!
            Picasso.get().load(filepath).into(mImage)
        } else {
            status = 12
            Toast.makeText(this, "No Selected Item", Toast.LENGTH_SHORT).show()
        }
    }

    private fun UploadFile() {
        if(status == 11){
            val file = mStorage.child("${System.currentTimeMillis()}.jpg")
            file.putFile(filepath)
                .addOnSuccessListener {
                    val files = Uploads(mNama.text.toString().trim(),it.uploadSessionUri.toString())
                    val uploadID  = mDatabase.push().key.toString()
                    mDatabase.child(uploadID).setValue(files);

                    Toast.makeText(this, "File Uploaded", Toast.LENGTH_SHORT).show()

                }.addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
        }else{
            Toast.makeText(this,"Choose Your File", Toast.LENGTH_SHORT).show()
        }
    }
}