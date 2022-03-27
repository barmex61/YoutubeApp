package com.fatih.youtube.view

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import com.fatih.youtube.R
import com.fatih.youtube.databinding.ActivityAccountBinding
import com.fatih.youtube.databinding.ActivityMainBinding
import com.fatih.youtube.model.Channels
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.text.DateFormat
import java.util.*

class AccountActivity : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth
    private lateinit var user:FirebaseUser
    private lateinit var storage:FirebaseStorage
    private lateinit var reference: StorageReference
    private lateinit var fireStore:FirebaseFirestore
    private lateinit var binding:ActivityAccountBinding
    private lateinit var dialog:Dialog
    private lateinit var profileImage:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        doInitialization()

    }
    private fun doInitialization(){
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
        }
        auth= FirebaseAuth.getInstance()
        auth.currentUser?.let {
            user=it
        }
        fireStore= FirebaseFirestore.getInstance()
        storage= FirebaseStorage.getInstance()
        reference=storage.reference
        getData()
        binding.textChannel.setOnClickListener { checkUserHaveChannel() }
    }
    private fun getData(){
        fireStore.collection("Users").document(user.uid).addSnapshotListener { value, error ->
            error?.let {
                Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
            }?:if (value!=null){
                value.let {
                    binding.channelNameText.text=it.get("userName").toString()
                    binding.emailText.text=it.get("email").toString()
                    profileImage=it.get("profileImage").toString()
                    try {
                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_account_circle_24).into(binding.profileImage)

                    }catch (e:Exception){
                        Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun checkUserHaveChannel(){
        fireStore.collection("Channels").document(user.uid).addSnapshotListener { value, error ->
            if(error!=null){
                Toast.makeText(this,error.message,Toast.LENGTH_SHORT).show()
            }else{
                if(value!=null&&value.exists()){
                    value.let {
                        val intent= Intent(this,MainActivity::class.java)
                        intent.putExtra("type","channel")
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                    }
                }else{
                    showDialog()
                }
            }

        }
    }
    private fun showDialog(){
        dialog= Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.channel_dialog)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        val channelName=dialog.findViewById<TextView>(R.id.inputChannelText)
        val descriptionText=dialog.findViewById<TextView>(R.id.descriptionText)
        val createText=dialog.findViewById<TextView>(R.id.createText)
        createText.setOnClickListener {
            val name=channelName.text.toString()
            val description=descriptionText.text.toString()
            if(name.isEmpty()&&description.isEmpty()){
                Toast.makeText(this,"Fill the blanks",Toast.LENGTH_SHORT).show()
            }else{
                createNewChannel(name,description)
            }
        }
        dialog.show()
    }
    private fun createNewChannel(name:String,description:String){
        val progressDialog=ProgressDialog(this)
        progressDialog.setTitle("New Channel")
        progressDialog.setMessage("Creating...")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        val date: Long = Timestamp.now().toDate().time
        println(date)
        val currentChannel=Channels(name,description,date,user.uid,profileImage)

        fireStore.collection("Channels").document(user.uid).set(currentChannel).addOnCompleteListener {
            if(it.isSuccessful){
                progressDialog.dismiss()
                dialog.dismiss()
                Toast.makeText(this,"Channel has been created",Toast.LENGTH_SHORT).show()
            }else{
                progressDialog.dismiss()
                dialog.dismiss()
                Toast.makeText(this,it.exception?.let { it-> it.message }?:"Something went wrong",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
    }
}