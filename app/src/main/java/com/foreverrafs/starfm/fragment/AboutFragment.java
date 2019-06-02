package com.foreverrafs.starfm.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.foreverrafs.starfm.R;

import butterknife.ButterKnife;
import butterknife.OnClick;


// Created by Emperor95 on 1/13/2019.

public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @OnClick({R.id.text_email, R.id.text_mobile, R.id.text_phone1, R.id.text_phone2})
    public void onContactInfoClicked(TextView view) {
        TextView target = view;
        Intent actionIntent;
        String contact = target.getText().toString();

        try {
            if (contact.contains(".com")) { //contact is an email
                actionIntent = new Intent(Intent.ACTION_VIEW);
                actionIntent.setData(Uri.parse("mailto:" + contact));
            } else {
                actionIntent = new Intent(Intent.ACTION_DIAL);
                actionIntent.setData(Uri.fromParts("tel", contact, null));
            }

            startActivity(actionIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "Action not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.image_whatsapp)
    public void onWhatsappIconClicked() {
        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        whatsappIntent.setType("text/plain");
        whatsappIntent.setPackage("com.whatsapp");

        String packageUrl = "https://play.google.com/store/apps/details?id=" + getContext().getPackageName();
        String message = "Install *Star FM*  App and listen to your favorite radio station online anywhere. Download it " +
                "from *" + packageUrl + "*";
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(whatsappIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "WhatsApp is not installed", Toast.LENGTH_SHORT).show();
        }
    }
}
