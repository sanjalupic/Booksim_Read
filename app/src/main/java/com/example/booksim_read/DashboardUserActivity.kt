package com.example.booksim_read

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.booksim_read.databinding.ActivityDashboardUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardUserActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding : ActivityDashboardUserBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var viewPagerAdapter: ViewPagerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupWithPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        //handle click, loggout
        binding.logoutBtn.setOnClickListener{
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        //handle click, open profile
        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupWithPagerAdapter(viewPager: ViewPager){
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager,
        FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
        this
        )

        //init categories
        categoryArrayList = ArrayList()

        //load categories from db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list
                categoryArrayList.clear()

                //load some static categories e.g All, Most View, Most Download
                //Add data to models
                val modelAll = ModelCategory("01", "All",1, "")
                val modelMostViewed = ModelCategory("01", "Most Viewed",1, "")
                val modelMostDownloaded = ModelCategory("01", "Most Downloaded",1, "")

                //add to list
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                categoryArrayList.add(modelMostDownloaded)
                //add to viewPagerAdapter
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                        "${modelAll.uid}"
                    ), modelAll.category
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",
                        "${modelMostViewed.uid}"
                    ), modelMostViewed.category
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostDownloaded.id}",
                        "${modelMostDownloaded.category}",
                        "${modelMostDownloaded.uid}"
                    ), modelMostDownloaded.category
                )
                //refresh list
                viewPagerAdapter.notifyDataSetChanged()

                //load from firebase db
                for(ds in snapshot.children){
                    //get data in model
                    val model = ds.getValue(ModelCategory::class.java)
                    //add to list
                    categoryArrayList.add(model!!)
                    //add to viewPagerAdapter
                    viewPagerAdapter.addFragment(
                        BooksUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        ), model.category
                    )

                    //refresh list
                    viewPagerAdapter.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        //setup adapter to viewpager
        viewPager.adapter = viewPagerAdapter

    }

    class  ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context): FragmentPagerAdapter(fm, behavior){
        //holds list of fragments i.e. new instance of some fragment for each category
        private val fragmentsList: ArrayList<BooksUserFragment> = ArrayList()
        //list of titles of categories for tabs
        private val fragmentTitleList: ArrayList<String> = ArrayList()

        private val context: Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return fragmentsList.size
        }

        override fun getItem(position: Int): Fragment {
            return  fragmentsList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }

        public fun addFragment(fragment: BooksUserFragment, title: String){
            //add fragment that will be passed as parameter in fragmentList
            fragmentsList.add(fragment)
            //add title that will be passed as parameter
            fragmentTitleList.add(title)
        }
    }

    //this activity can be open with or without login, so profile button must be hidden when user is not logged in
    private fun checkUser(){
        //get current user
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            //not logged in, user can stay in user dashboard without login too
            binding.subTitleTv.text = "Not Logged In"

            //hidden profile, loggout
            binding.profileBtn.visibility = View.GONE
            binding.logoutBtn.visibility = View.GONE

        }
        else{
            //logged in, get and show user info
            val email = firebaseUser.email
            //set to textview of toolbar
            binding.subTitleTv.text = email

            //show profile, loggout
            binding.profileBtn.visibility = View.VISIBLE
            binding.logoutBtn.visibility = View.VISIBLE
        }
    }
}