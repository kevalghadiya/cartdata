package com.bistrocart.login_module.activity

import android.os.Bundle
import com.bistrocart.base.BaseActivity
import com.bistrocart.databinding.ActivitySignInOrUpBinding

class Sign_in_or_up_Activity : BaseActivity() {

    private lateinit var binding: ActivitySignInOrUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInOrUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        login()
        signUp()
    }

    private fun login() {
        binding.btnLogin.setOnClickListener {
            gotoActivity(SignInActivity::class.java,null,false)
        }
    }

    private fun signUp(){
        binding.btnSignUp.setOnClickListener {
            gotoActivity(SignUpActivity::class.java,null,false)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}