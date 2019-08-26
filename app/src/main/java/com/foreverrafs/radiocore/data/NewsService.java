package com.foreverrafs.radiocore.data;

import com.foreverrafs.radiocore.model.News;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface NewsService {
    @GET("/news")
    Observable<List<News>> getAllNews();
}
