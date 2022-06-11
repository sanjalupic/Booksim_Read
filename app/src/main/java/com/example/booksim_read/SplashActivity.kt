package com.example.booksim_read

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed(Runnable {
            checkUser()
        }, 1000) //1 seconds
    }

    private fun checkUser() {
        //get current user, if logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            //user not logged in, goto main screen
            startActivity(Intent(this, MainActivity::class.java))
        }
        else{
            //user logged in, check user type and done in login screen
            val firebaseUser = firebaseAuth.currentUser!!

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        //get user type e.g. user or admin
                        val userType = snapshot.child("userType").value
                        if(userType == "user"){
                            //its simple, open user dashboard
                            startActivity(Intent(this@SplashActivity, DashboardUserActivity::class.java))
                        }
                        else if(userType =="admin"){
                            startActivity(Intent(this@SplashActivity, DashboardAdminActivity::class.java))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }

}