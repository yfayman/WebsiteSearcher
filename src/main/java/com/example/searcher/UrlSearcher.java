package com.example.searcher;

import java.io.IOException;

public interface UrlSearcher {

  public boolean search(String url, String phrase) throws IOException;

}
