package com.bistrocart.main_module.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bistrocart.R
import com.bistrocart.base.BaseActivity
import com.bistrocart.databinding.ActivityHomeScreenBinding
import com.bistrocart.main_module.fragment.CartFragment
import com.bistrocart.main_module.fragment.HomeFragment
import com.bistrocart.main_module.fragment.LikeFragment
import com.bistrocart.main_module.fragment.MyProfileFragment
import com.bistrocart.utils.AppPref
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView


class HomeScreenActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var navController: NavController
    private lateinit var badge: BadgeDrawable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //badge
        val firstMenuItemId = binding.bottomNavigation.menu.getItem(2).itemId
        badge = binding.bottomNavigation.getOrCreateBadge(firstMenuItemId)

        navigateFragments()
        //navigationDrawer()
        initView()
    }

    private fun initView() {
        if (!isNotificationPermissionGranted()) {
            requestNotificationPermission()
        }

        if (intent != null) {
            Log.e("TAG", "initView:IsCall")
            var i = intent.getStringExtra("ProductActivity").toString()
            var id = intent.getStringExtra("id").toString()
            var title = intent.getStringExtra("title").toString()

            if (i.equals("1")) {
              /*  val bundle = Bundle()
                bundle.putString("FromProduct","true")
                bundle.putString("id", id)
                bundle.putString("title", title)
                loadFragment(CartFragment(), bundle)*/

             /*   val bundle = Bundle()
                bundle.putString("FromProduct","true")
                bundle.putString("id", id)
                bundle.putString("title", title)
                Navigation.findNavController(this, R.id.mainNavContainer1).navigate(R.id.cartFragment,bundle)
*/

                /*val navHostFragment = supportFragmentManager.findFragmentById(R.id.mainNavContainer1) as? NavHostFragment
                navHostFragment?.let { navHost ->
                    val navController = navHost.navController
                    val bottomNavigationView = binding.bottomNavigation
                    bottomNavigationView.setupWithNavController(navController)
                }*/

//                val navHostFragment = supportFragmentManager.findFragmentById(R.id.mainNavContainer1) as? NavHostFragment
//                navHostFragment?.let { navHost ->
//                    val navController = navHost.navController
                    //val bottomNavigationView = binding.bottomNavigation
                   // bottomNavigationView.setupWithNavController(navController)
                    val bundle = Bundle()
                    bundle.putString("FromProduct","true")
                    bundle.putString("id", id)
                    bundle.putString("title", title)
                    navController.navigate(R.id.cartFragment,bundle)
//                }
            }
        }
    }

    public fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun navigationDrawer() {
        //drawer header
        val inflater = LayoutInflater.from(this@HomeScreenActivity)
        val headerView = inflater.inflate(R.layout.nav_header_main, binding.navView, false)
        val tvName = headerView.findViewById<AppCompatTextView>(R.id.tvName)
        val tvEmail = headerView.findViewById<AppCompatTextView>(R.id.tvEmail)

        tvName.setText(appPref!!.getString(AppPref.NAME))
        tvEmail.setText(appPref!!.getString(AppPref.EMAIL))

        //  Log.e(TAG, "initView1: "+appPref.getString(Constants.USER_NAME) )
        //   tvName.setText(appPref.getString(Constants.USER_NAME))
        //   tvEmail.setText(appPref.getString(Constants.EMAIL))
         binding.navView.addHeaderView(headerView)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
               /* R.id.nav_profile -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    // gotoActivity(MainActivity::class.java, null, false)
                    true
                }*/
              /*  R.id.nav_notification -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    //   changeFragment(RequestListFragment().newInstance(null), false, false)
                    true
                }*/
               /* R.id.nav_transaction -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    //   gotoActivity(MyDriverAssignActivity::class.java, null, false)
                    true
                }*/
              /*  R.id.nav_cart -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    //   gotoActivity(MyVehicleActivity::class.java, null, false)
                    true
                }*/
                R.id.nav_order -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    gotoActivity(MyOrderListActivity::class.java, null, false)
                    true
                }
                R.id.nav_logout -> {
                  //  binding.drawerLayout.closeDrawer(GravityCompat.START)
                    logout(this)
                    true
                }
                else -> false
            }
        }

    }

    private fun navigateFragments() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.mainNavContainer1) as? NavHostFragment
        navHostFragment?.let { navHost ->
            navController = navHost.navController
            val bottomNavigationView = binding.bottomNavigation
            bottomNavigationView.setupWithNavController(navController)

        }
    }


    /*  private fun navigateFragments() {
          val navigationView: BottomNavigationView = findViewById(R.id.bottomNavigation)
          navigationView.setOnItemSelectedListener { menuItem ->
              when (menuItem.itemId) {
                  R.id.homeFragment -> {
                      loadFragment(HomeFragment())
                      true
                  }
                  R.id.likeFragment -> {
                      loadFragment(LikeFragment())
                      true
                  }
                  R.id.cartFragment -> {
                      loadFragment(CartFragment())
                      true
                  }
                  R.id.myProfileFragment -> {
                      loadFragment(MyProfileFragment())
                      true
                  }
                  else -> false
              }
          }
      }*/

    private fun loadFragment(fragment: Fragment, bundle: Bundle? = null) {
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainNavContainer1, fragment)
            .commit()
    }




    companion object fun getBadge(count:Int) {
       // getTotalCount { count ->
            if (count!=0) {
                badge.isVisible =true
                badge.number = count
                badge.setBadgeTextColor(getColor(R.color.black))
                badge.backgroundColor = getColor(R.color.yellow)
            }else{
                badge.isVisible =false
            }
       // }
    }

    override fun onStart() {
        super.onStart()
        getBadge()
    }
    fun getBadge() {
        getTotalCount { count ->
            if (count!=0) {
                badge.isVisible =true
                badge.number = count
                badge.setBadgeTextColor(getColor(R.color.black))
                badge.backgroundColor = getColor(R.color.yellow)
            }else{
                badge.isVisible =false
            }
        }
    }

    private fun logout(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_logout, null)
        builder.setView(dialogView)

        val btnCancel = dialogView.findViewById<AppCompatButton>(R.id.btnCancel)
        val btnLogout = dialogView.findViewById<AppCompatButton>(R.id.btnLogout)

        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnLogout.setOnClickListener {
            logout()
        }
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
    }
}