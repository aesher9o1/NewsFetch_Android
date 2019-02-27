package demo.aesher.dsc.newsfetch.Interfaces;

import java.util.List;

import demo.aesher.dsc.newsfetch.Thread.NewsModel;

public interface AsyncNews {
    void processFinished(List<NewsModel> s, String body);

}
