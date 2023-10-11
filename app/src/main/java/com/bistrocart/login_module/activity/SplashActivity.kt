package com.bistrocart.login_module.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import com.bistrocart.base.BaseActivity
import com.bistrocart.databinding.ActivitySplashBinding
import com.bistrocart.main_module.activity.HomeScreenActivity
import com.bistrocart.utils.AppPref

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        Handler().postDelayed({
            gotoNext()
        }, 2000)
    }

    private fun gotoNext() {
        if (appPref?.isLogin() == true) {
            gotoActivity(HomeScreenActivity::class.java, null, false)
            finish()
        }else{
            gotoActivity(Sign_in_or_up_Activity::class.java, null, false)
        }
    }
}