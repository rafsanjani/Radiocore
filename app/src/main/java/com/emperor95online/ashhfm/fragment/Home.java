package com.emperor95online.ashhfm.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.emperor95online.ashhfm.NewsDetail;
import com.emperor95online.ashhfm.R;
import com.emperor95online.ashhfm.adapter.NewsAdapter;
import com.emperor95online.ashhfm.pojo.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;


// Created by Emperor95 on 1/13/2019.

public class Home extends Fragment implements View.OnClickListener {

    private ImageButton more_main, imBtn;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private ArrayList<News> newsList;
    private ArrayList<String> images;
    private NewsAdapter newsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        newsList = new ArrayList<>();
        images = new ArrayList<>();

        imBtn = view.findViewById(R.id.imBtn);
        more_main = view.findViewById(R.id.more_main);
        more_main.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
//
        recyclerView = view.findViewById(R.id.recyclerView);


        recyclerView.setLayoutManager(layoutManager);

        progressBar = view.findViewById(R.id.progressBar2);
        newsAdapter = new NewsAdapter(getActivity(), newsList);
        ScaleInAnimationAdapter adapter = new ScaleInAnimationAdapter(newsAdapter);
        adapter.setDuration(500);
        recyclerView.setAdapter(adapter);


        news = new ArrayList<>();
        images = new ArrayList<>();
        getData();

        //todo: make activity the activity implement this interface to keep oncreate clean enough
        newsAdapter.setOnNewsItemClickListener(new NewsAdapter.NewsItemClickListener() {
            @Override
            public void onNewItemClicked(News newsObject, Pair[] pairs, int position) {
                NewsAdapter.NewsHolder newsHolder = (NewsAdapter.NewsHolder) recyclerView.findViewHolderForAdapterPosition(position);

                String[] transitionNames = new String[]{
                        ViewCompat.getTransitionName(newsHolder.getImageImageView()),// newsHolder.getHeadlineTextView().getTransitionName(),
                        ViewCompat.getTransitionName(newsHolder.getHeadlineTextView())
                };

                Intent intent = new Intent(getContext(), NewsDetail.class);
                intent.putExtra("title", newsObject.getHeadline());
                intent.putExtra("content", newsObject.getContent());
                intent.putExtra("image", newsObject.getImage());
                intent.putExtra("date", newsObject.getDate());

                //also pass this for shared element transition
                intent.putExtra("transitions", transitionNames);

                ActivityOptions activityOptions = null;

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    activityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                            pairs);
                }

                startActivity(intent, activityOptions.toBundle());
            }
        });

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.more_main:
                showPopupMenu(imBtn);
                break;
        }
    }
    //todo: Rename this method and push it into a new file
    private void getData() {
        //final List<News> newsList = new ArrayList<>();
        if (getActivity() == null) {
            return;
        }

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());

//        String url = "http://www.ghananewsonline.com.gh/wp-json/wp/v2/posts?_embed&categories=13";
//        String url = "https://www.newsghana.com.gh/wp-json/wp/v2/posts?_embed&categories=29";
        String url = "https://www.newsghana.com.gh/wp-json/wp/v2/posts?_embed&categories=35";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jaa = new JSONArray(response);

                            for (int i = 0; i < jaa.length(); i++) {

                                JSONObject jsonObject = jaa.getJSONObject(i);
                                String title = jsonObject.getJSONObject("title").getString("rendered");
                                String content = jsonObject.getJSONObject("content").getString("rendered");

//                                String image = "https://pbs.twimg.com/profile_images/425274582581264384/X3QXBN8C.jpeg";
                                String image = "http://www.51allout.co.uk/wp-content/uploads/2012/02/Image-not-found.gif";

                                if (!jsonObject.getJSONObject("_embedded").isNull("wp:featuredmedia")) {
                                    image = jsonObject.getJSONObject("_embedded")
                                            .getJSONArray("wp:featuredmedia").getJSONObject(0)
                                            .getString("source_url");
                                }
                                String date = jsonObject.getString("date");


                                newsList.add(new News(title, date.substring(0, date.indexOf("T")), image/*images.get(i)*/, content));
                                //newsAdapter.notifyDataSetChanged();
                                // newsAdapter.notifyItemInserted(newsAdapter.getItemCount() + 1);


//                                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
//                                ClipData clip = ClipData.newPlainText("SBC", response);
//                                clipboard.setPrimaryClip(clip);
//                                Toast.makeText(getActivity(), "Bet Code copied ...", Toast.LENGTH_SHORT).show();

//                                break;
                            }
                            newsAdapter.notifyItemRangeChanged(0, newsList.size());

//                            Toast.makeText(getActivity(), "Posts: " + Integer.toString(jaa.length()) , Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }catch (JSONException e){
                            Toast.makeText(getActivity(), "JSON Exception" , Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "" + error, Toast.LENGTH_SHORT).show();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }
    private void showPopupMenu(final View view) {
=======
    private void showPopupMenu(final View view){
        // inflate menu
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.main_popup, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.staff:
                        if (getActivity() != null) {
                            getActivity()
                                    .getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                                    .replace(R.id.content, new Team())
                                    .commit();
                        }
                        return true;
                    case R.id.about_station:
                        return true;
                    case R.id.privacy_policy:
                        return true;
                }

                return false;
            }
        });
        popupMenu.show();
    }
}