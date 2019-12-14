package com.radiocore.news.data;

import com.radiocore.news.model.News;

import java.util.List;

public class NewsRepository {
    private static NewsRepository instance = null;
    private List<News> radioCoreNews = null;

    private NewsRepository() {

    }

    public static NewsRepository getInstance() {
        if (instance == null)
            instance = new NewsRepository();

        return instance;
    }

    public List<News> getRadioCoreNews() {
        return radioCoreNews;
    }

    public void setRadioCoreNews(List<News> radioCoreNews) {
        this.radioCoreNews = radioCoreNews;
    }
}
