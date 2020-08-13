package com.example.tutorial

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.ByteArrayOutputStream

class ProfileActivity : AppCompatActivity() {
    lateinit var filepath: Uri
    var req = 1
    lateinit var auth : FirebaseAuth
    lateinit var mImageView : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mImageView = findViewById(R.id.iv_profile)

        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser

        if (user != null){
            if(user.photoUrl != null){
                Picasso.get().load(user.photoUrl).into(mImageView)
            }else{
                Picasso.get().load("https://picsum.photos/seed/picsum/200/300").into(mImageView)
            }

            et_nama.setText(user.displayName)
            et_email.setText(user.email)

            if (user.isEmailVerified){
                iv_verif.visibility = View.VISIBLE
            }else {
                iv_unverif.visibility = View.VISIBLE
            }
            if(user.phoneNumber.isNullOrEmpty()){
                et_loc.setText("Masukkan Nomor Telepon Anda")
            }else {
                et_loc.setText(user.phoneNumber)
            }
        }
        iv_profile.setOnClickListener {
            fileChooser()
        }

        btn_save.setOnClickListener {
            var images = when{
                ::filepath.isInitialized -> filepath
                user?.photoUrl == null -> Uri.parse("https://picsum.photos/seed/picsum/200/300")
                else -> user.photoUrl
            }
            val nama  = et_nama.text.toString().trim()

            if (nama.isEmpty()){
                et_nama.error = "Masukkan Nama Anda"
                et_nama.requestFocus()
                return@setOnClickListener
            }

            UserProfileChangeRequest.Builder()
                .setDisplayName(nama)
                .setPhotoUri(images)
                .build().also {
                    user?.updateProfile(it)?.addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(this,"Profile Updated.", Toast.LENGTH_LONG).show()
                        }else {
                            Toast.makeText(this,"${it.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }


        }

        iv_unverif.setOnClickListener {
            user?.sendEmailVerification()?.addOnCompleteListener{
                if (it.isSuccessful){
                    Toast.makeText(this,"Verification Email has been sent.", Toast.LENGTH_SHORT).show()
                }else {
                    Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun fileChooser() {
        var i = Intent()
        i.setType("image/*")
        i.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(i,req)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == req && resultCode == Activity.RESULT_OK && data != null){
            filepath = data.data!!

            val imgBitmap = MediaStore.Images.Media.getBitmap(contentResolver,filepath)
            upload(imgBitmap)
        }
    }

    private fun upload(imgBitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        val ref = FirebaseStorage.getInstance().getReference("Uploads/").child("img/${FirebaseAuth.getInstance().currentUser?.uid}")

        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

        ref.putBytes(image)
            .addOnCompleteListener {
                ref.downloadUrl.addOnCompleteListener {
                    it.result?.let {
                        filepath = it
                        Toast.makeText(this,"File Uploaded",Toast.LENGTH_LONG).show()
                        iv_profile.setImageBitmap(imgBitmap)
                    }

                }
            }
            .addOnFailureListener {
                Toast.makeText(this,it.message,Toast.LENGTH_LONG).show()
            }
    }
}