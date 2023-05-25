package com.firebase.chat.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.firebase.chat.R
import com.firebase.chat.base.BaseViewModel
import com.firebase.chat.databinding.ActivityMainBinding
import com.firebase.chat.ui.viewmodel.ChatDetailViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mobisharnam.domain.model.firebasedb.NewUser
import com.mobisharnam.domain.util.AppConstant
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val viewModel : BaseViewModel by viewModel()
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private var currentDestination: NavDestination? = null
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.back).setOnClickListener {
            onCloseDrawer()
        }
        setUpDrawer()
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentDestination = destination
            when (destination.id) {
                R.id.chattingFragment -> {
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    binding.navView.visibility = View.VISIBLE
                    binding.appBarLayout.appbarMenu.visibility = View.VISIBLE
                    toggle.isDrawerIndicatorEnabled = true
                    setUpDrawer()
                }
                R.id.chatDetailFragment -> {
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    binding.navView.visibility = View.GONE
                    binding.appBarLayout.appbarMenu.visibility = View.GONE
                }

                else -> {
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    binding.navView.visibility = View.GONE
                    binding.appBarLayout.appbarMenu.visibility = View.GONE
                }
            }
        }
    }

    private fun setUpDrawer() {
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.appBarLayout.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        toggle.drawerArrowDrawable.color = Color.BLACK
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
        viewModel.getDataBaseReference().child(AppConstant.USER_TABLE).child(viewModel.getFireBaseAuth().uid.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(NewUser::class.java)?.let {
                    val name = it.userName
                    binding.navView.getHeaderView(0).findViewById<TextView>(R.id.tvUserName).text = name
                    val displayName = name.split(" ")
                    binding.navView.getHeaderView(0).findViewById<TextView>(R.id.ivUserImage).text = try {
                        displayName[0][0].toString() + displayName[1][0].toString()
                    }catch (e: Exception) {
                        e.printStackTrace()
                        name[0].toString()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_home -> {

            }
            R.id.menu_invitation -> {
                navController.navigate(R.id.invitationFragment)
            }
            R.id.menu_add_friend -> {
                navController.navigate(R.id.addUserFragment)
            }
            R.id.menu_notification -> {

            }
            R.id.action_logout -> {

            }
        }
        onCloseDrawer()
        return true
    }

    private fun onCloseDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }
}