package com.foreverrafs.starfm.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.foreverrafs.starfm.R;


// Created by Emperor95 on 1/13/2019.

public class AboutFragment extends Fragment implements View.OnClickListener {

    TextView textEmail, textMobile, textPhone1, textPhone2;
    ImageView whatsappImg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        textEmail = rootView.findViewById(R.id.text_email);
        textMobile = rootView.findViewById(R.id.text_mobile);
        textPhone1 = rootView.findViewById(R.id.text_phone1);
        textPhone2 = rootView.findViewById(R.id.text_phone2);
        whatsappImg = rootView.findViewById(R.id.image_whatsapp);

        textEmail.setOnClickListener(this);
        textMobile.setOnClickListener(this);
        textPhone1.setOnClickListener(this);
        textPhone2.setOnClickListener(this);
        whatsappImg.setOnClickListener(this);

        return rootView;
    }


//    @OnClick(R.id.text_email)
//    public void socialButtonClicked(View view) {
//        Log.i("TAG", view.toString());
//
//    }
//
//    public void onSocialContactClicked(View view) {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setData(Uri.parse("http://starfmatbonline.com/"));
//        startActivity(intent);
//    }
//
//    public void onPhoneClicked(View view) {
//        String phone = "+2330205573828";
//        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
//        startActivity(intent);
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_email:
            case R.id.text_mobile:
            case R.id.text_phone1:
            case R.id.text_phone2:
                TextView target = (TextView) v;
                Intent actionIntent;
                String contact = target.getText().toString();
                if (contact.contains(".com")) { //contact is an email
                    actionIntent = new Intent(Intent.ACTION_VIEW);
                    actionIntent.setData(Uri.parse("mailto:" + contact));
                } else {
                    actionIntent = new Intent(Intent.ACTION_DIAL);
                    actionIntent.setData(Uri.fromParts("tel", contact, null));
                }

                startActivity(actionIntent);

                break;

            case R.id.image_whatsapp:
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("text/plain");
                whatsappIntent.setPackage("com.whatsapp");

                String packageUrl = "https://play.google.com/store/apps/details?id=" + getContext().getPackageName();
                String message = "Install *Star FM*  App and listen to your favorite radio station online anywhere. Download it " +
                        "from *" + packageUrl + "*";
                whatsappIntent.putExtra(Intent.EXTRA_TEXT, message);

                try {
                    startActivity(whatsappIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getContext(), "WhatsApp is not installed", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
