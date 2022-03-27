package com.fatih.youtube.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.fatih.youtube.R
import com.fatih.youtube.databinding.ActivityMainBinding
import com.fatih.youtube.model.User
import com.fatih.youtube.util.GOOGLE_SIGN_IN
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Job



class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private var user:FirebaseUser?=null
    private lateinit var auth:FirebaseAuth
    private lateinit var googleSignInClient:GoogleSignInClient
    private lateinit var dialog: AlertDialog
    private lateinit var navController:NavController
    private var counter=0
    private lateinit var permissionLauncher:ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var onBackPressedJob:Job
    private var mBackPressed:Long=0
    private val timeInterval=2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        doInitialization()

    }

    private fun doInitialization(){
        setSupportActionBar(binding.toolbar)
        setStatusBarColor()
        auth= FirebaseAuth.getInstance()
        user=auth.currentUser
        setGoogleOptions()
        setBottomNavigationView()
        getData()
        showFragment()
        setCustomListeners()
        registerLauncher()

    }

    private fun setBottomNavigationView(){
        binding.bottomNavigationView.background=null
        binding.bottomNavigationView.menu.getItem(2).isEnabled=false
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController= navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)
    }

    private fun setCustomListeners() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.label!="ChannelDashboardFragment"){
                binding.appbar.visibility=View.VISIBLE
            }
        }
        binding.bottomNavigationView.menu.getItem(0).setOnMenuItemClickListener {
            navController.navigate(R.id.homeFragment)
            return@setOnMenuItemClickListener true
        }
        binding.fabButton.setOnClickListener {
            val dialog=Dialog(this)
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.upload_dialog)
            dialog.setCanceledOnTouchOutside(true)
            dialog.findViewById<LinearLayout>(R.id.uploadLayout).setOnClickListener {
                    checkPermissions()
            }
            dialog.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){

            R.id.notification->{Toast.makeText(this,"Notification",Toast.LENGTH_LONG).show()}
            R.id.search->{Toast.makeText(this,"Search",Toast.LENGTH_LONG).show()}
            R.id.account->{if(FirebaseAuth.getInstance().currentUser!=null){
                val intent=Intent(this,AccountActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)

            }else{
                binding.toolbar.menu.getItem(2).setIcon(R.drawable.ic_baseline_account_circle_24)
                showDialog()
            }

            }
            else->{ super.onOptionsItemSelected(item)}
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setGoogleOptions(){
        val gsc=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(
            getString(R.string.default_web_client_id2)).requestEmail().build()
        googleSignInClient=GoogleSignIn.getClient(this,gsc)
    }

    private fun showDialog(){

        val dialogBuilder=AlertDialog.Builder(this)
        dialogBuilder.setCancelable(true)
        val viewGroup:ViewGroup=findViewById(android.R.id.content)
        val view=LayoutInflater.from(applicationContext).inflate(R.layout.signing_dialog,viewGroup,false)
        dialogBuilder.setView(view)
        dialog=dialogBuilder.create()
        dialog.show()
        val textSignIn=view.findViewById<View>(R.id.text_google_sign)
        textSignIn.setOnClickListener {
            signIn()
        }

    }

    private fun signIn(){
        val intent=googleSignInClient.signInIntent
        startActivityForResult(intent, GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== GOOGLE_SIGN_IN){
            val task=GoogleSignIn.getSignedInAccountFromIntent(data)
            try {

                val account=task.getResult(ApiException::class.java)
                val credential=GoogleAuthProvider.getCredential(account.idToken,null)
                auth.signInWithCredential(credential).addOnCompleteListener {
                    if(task.isSuccessful){
                        val user=FirebaseAuth.getInstance().currentUser
                        if(user!=null){
                            val currentUser=User(account.displayName,account.email,account.photoUrl.toString(),user.uid,account.displayName?.lowercase())
                            FirebaseFirestore.getInstance().collection("Users").document(currentUser.uid).set(currentUser).addOnCompleteListener {
                                Toast.makeText(this,"Successfully logged in",Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                                getData()
                            }.addOnFailureListener {
                                Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
                            }
                        }
                    }else{
                        Toast.makeText(this,"Request failed !",Toast.LENGTH_SHORT).show()
                    }
                }
            }catch (e:Exception){
                Toast.makeText(this,e.message,Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getData(){
        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().uid.toString()).get().addOnCompleteListener {
                if(it.isSuccessful){
                    Picasso.get().load(it.result["profileImage"].toString()).resize(45,45)
                        .into(binding.fakeImage,object :Callback{
                            override fun onError(e: java.lang.Exception?) {
                                println("Error")
                            }
                            override fun onSuccess() {
                                val imageBitmap = (binding.fakeImage.drawable as BitmapDrawable).bitmap
                                val imageDrawable = RoundedBitmapDrawableFactory.create(resources, imageBitmap)
                                imageDrawable.isCircular = true
                                imageDrawable.cornerRadius = imageBitmap.width.coerceAtLeast(imageBitmap.height) / 2.0f
                                binding.fakeImage.setImageDrawable(imageDrawable)
                                binding.toolbar.menu.getItem(2).icon=binding.fakeImage.drawable
                            }
                        })
                }else{
                    Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showFragment(){
        val type=intent.getStringExtra("type")
        type?.let {
            when(it){
                "channel"->{
                    counter++
                    binding.appbar.visibility=View.GONE
                    navController.navigate(R.id.channelDashboardFragment)
                }
            }
        }
    }

    @SuppressLint("ShowToast")
    private fun checkPermissions(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Snackbar.make(binding.root,"Need Permission",Snackbar.LENGTH_SHORT).setAction("Give Permission"){
                    permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }else{
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }else{
            val intent=Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "video/*"
            activityResultLauncher.launch(Intent.createChooser(intent,"Select Video"))
        }
    }

    private fun registerLauncher() {
        permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()
        ) { result ->
            if (result == true) {
                val intent=Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "video/*"
                activityResultLauncher.launch(Intent.createChooser(intent,"Select Video"))
            } else {
                Toast.makeText(this@MainActivity, "Need Permission", Toast.LENGTH_SHORT).show()
            }
        }
        activityResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                 result.data?.let {
                     val data=it.data
                     val intent=Intent(this@MainActivity,PublishContentActivity::class.java)
                     intent.putExtra("type","video")
                     intent.data=data
                     startActivity(intent)
                 }

            }
        }
    }

    private fun setStatusBarColor(){
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor= Color.parseColor("#FFFFFF")

    }

    override fun onBackPressed() {
        if (mBackPressed + timeInterval > System.currentTimeMillis())
        { finishAffinity()
              }
        else { Toast.makeText(baseContext, "Tap back button in order to exit", Toast.LENGTH_SHORT).show() }
        mBackPressed = System.currentTimeMillis()
    }

    override fun onDestroy() {
        auth.signOut()
        onBackPressedJob.cancel()
        super.onDestroy()
    }
}