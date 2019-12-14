package com.radiocore.app.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.radiocore.app.BuildConfig
import com.radiocore.app.R
import kotlinx.android.synthetic.main.fragment_about.*


// Created by Emperor95 on 1/13/2019.

class AboutFragment : Fragment(), View.OnClickListener {
    override fun onClick(clickedView: View?) {
        when (clickedView?.id) {
            R.id.tvEmail, R.id.tvMobile, R.id.tvPhone -> {
                val view = clickedView as TextView
                onContactInfoClicked(view)
            }

            R.id.imageWhatsapp -> onWhatsappIconClicked()
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvFooterText.text = getString(R.string.about_build_info, BuildConfig.VERSION_NAME)
        initClickListeneners()
    }

    private fun initClickListeneners() {
        tvEmail.setOnClickListener(this)
        tvMobile.setOnClickListener(this)
        tvPhone.setOnClickListener(this)
        imageWhatsapp.setOnClickListener(this)
    }

    private fun onContactInfoClicked(view: TextView) {
        val actionIntent: Intent
        val contact = view.text.toString()

        try {
            if (contact.contains(".com")) { //contact is an email
                actionIntent = Intent(Intent.ACTION_VIEW)
                actionIntent.data = Uri.parse("mailto:$contact")
            } else {
                actionIntent = Intent(Intent.ACTION_DIAL)
                actionIntent.data = Uri.fromParts("tel", contact, null)
            }

            startActivity(actionIntent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "Action not supported on this device", Toast.LENGTH_SHORT).show()
        }

    }

    private fun onWhatsappIconClicked() {
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.type = "text/plain"
        whatsappIntent.setPackage("com.whatsapp")

        val packageUrl = "https://play.google.com/store/apps/details?id=" + context!!.packageName
        val message = "Install ${getString(R.string.app_name)}  App and listen to your favorite radio station online anywhere. Download it " +
                "from *" + packageUrl + "*"
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, message)

        try {
            startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
        }

    }
}
