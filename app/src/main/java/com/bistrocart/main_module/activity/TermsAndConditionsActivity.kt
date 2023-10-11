package com.bistrocart.main_module.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.bistrocart.R
import com.bistrocart.base.BaseActivity
import com.bistrocart.databinding.ActivityTermsAndConditionsBinding

class TermsAndConditionsActivity : BaseActivity() {
    lateinit var binding:ActivityTermsAndConditionsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =DataBindingUtil.setContentView(this,R.layout.activity_terms_and_conditions)
        initView()
    }

    private fun initView() {
        binding.imgBack.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        if (!isOnline()){
            return
        }
        binding.webView.loadUrl("http://43.205.76.8/api/help&support.html")

    }
}