package com.foreverrafs.radiocore.fragment

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
import butterknife.ButterKnife
import butterknife.OnClick
import com.foreverrafs.radiocore.R


// Created by Emperor95 on 1/13/2019.

class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_about, container, false)

        ButterKnife.bind(this, rootView)

        return rootView
    }

    @OnClick(R.id.text_email, R.id.text_mobile, R.id.text_phone1)
    fun onContactInfoClicked(view: TextView) {
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

    @OnClick(R.id.image_whatsapp)
    fun onWhatsappIconClicked() {
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.type = "text/plain"
        whatsappIntent.setPackage("com.whatsapp")

        val packageUrl = "https://play.google.com/store/apps/details?id=" + context!!.packageName
        val message = "Install *Star FM*  App and listen to your favorite radio station online anywhere. Download it " +
                "from *" + packageUrl + "*"
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, message)

        try {
            startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
        }

    }
}
