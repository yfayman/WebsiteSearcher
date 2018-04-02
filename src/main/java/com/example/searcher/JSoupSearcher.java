package com.example.searcher;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSoupSearcher implements UrlSearcher {
  
  private final static Logger LOGGER = LoggerFactory.getLogger(JSoupSearcher.class);

  @Override
  public boolean search(String url, String phrase) throws IOException {
    try {
      Document doc = Jsoup.connect(url).get();
      LOGGER.info("Making a request to {} on {}", url, Thread.currentThread().getName());
      return doc!= null && doc.getElementsContainingOwnText(phrase).size() > 0;
    }catch(Exception e) {
      LOGGER.warn("Error {} hitting {}", e.getMessage(), url);
      return false;
    }
    
  }

}
