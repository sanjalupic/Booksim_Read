package com.example.booksim_read.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.booksim_read.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding : ActivityLoginBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialg: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //init progress dialog, while show login user
        progressDialg = ProgressDialog(this)
        progressDialg.setTitle("Please wait")
        progressDialg.setCanceledOnTouchOutside(false)

        //handle click, not have account, goto register screen

        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        //handle click, begin login
        binding.loginBnt.setOnClickListener {
            /*Steps
           1) Input Data
           2) Validate Data
           3) Login - Firebase Auth
           4) Check user type - Firebase Auth
                If user - Move to user dashboard
                If admin - Move to admin dashboard
            */

            validateData()
        }
    }

    private var email = ""
    private var password = ""

    private fun validateData() {
        // 1) Input Data
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        //  2) Validate Data
        if( !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email format!", Toast.LENGTH_SHORT).show()
        }
        else if(password.isEmpty()){
            Toast.makeText(this, "Enter password!", Toast.LENGTH_SHORT).show()
        }
        else{
            loginUser()
        }
    }

    private fun loginUser() {
        // 3) Login - Firebase Auth

        //show progress
        progressDialg.setMessage("Logging In...")
        progressDialg.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //login sucess
                checkUser()
            }
            .addOnFailureListener { e->
                //failed login
                progressDialg.dismiss()
                Toast.makeText(this, "Login failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        /*4) Check user type - Firebase Auth
                If user - Move to user dashboard
                If admin - Move to admin dashboard*/
        progressDialg.setMessage("Checking User")

        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialg.dismiss()

                    //get user type e.g. user or admin
                    val userType = snapshot.child("userType").value
                    if(userType == "user"){
                        //its simple, open user dashboard
                        startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
                    }
                    else if(userType =="admin"){
                        startActivity(Intent(this@LoginActivity, DashboardAdminActivity::class.java))
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}