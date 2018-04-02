package com.example.searcher;

import java.io.IOException;
//TODO seperate out searching and matching. Searching should return a string of all the text in the doc
public interface UrlSearcher {

  public boolean search(String url, String phrase) throws IOException;

}
