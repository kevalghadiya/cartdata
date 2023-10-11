package com.bistrocart.main_module.fragment

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.fragment.findNavController
import com.bistrocart.R
import com.bistrocart.base.BaseFragment
import com.bistrocart.databinding.FragmentMyProfileBinding
import com.bistrocart.main_module.activity.MyOrderListActivity
import com.bistrocart.main_module.activity.MyProfileActivity
import com.bistrocart.main_module.activity.TermsAndConditionsActivity
import com.bistrocart.utils.AppPref

class MyProfileFragment : BaseFragment() {

    private lateinit var binding: FragmentMyProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MyFragment", "onViewCreated")
        binding.imgBack.setOnClickListener {

            findNavController().navigate(R.id.homeFragment)
        }

        binding.tvVersion.text ="App Version "+getVersionName(requireContext())

        initView()
    }

    private fun initView() {
       binding.llLogout.setOnClickListener(View.OnClickListener {
           logout(requireContext())
       })

       binding.llMyOrder.setOnClickListener(View.OnClickListener {
           gotoActivity(MyOrderListActivity::class.java,null,false)
       })
        binding.llMyProfile.setOnClickListener(View.OnClickListener {
            gotoActivity(MyProfileActivity::class.java,null,false)
        })
        binding.llTermsAndCon.setOnClickListener(View.OnClickListener {
            gotoActivity(TermsAndConditionsActivity::class.java,null,false)
        })
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

}