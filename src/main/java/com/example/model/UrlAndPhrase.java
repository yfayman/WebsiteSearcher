package com.example.model;

public class UrlAndPhrase {

  private final String url;
  private final String phrase;

  public UrlAndPhrase(String url, String phrase) {
    this.url = url;
    this.phrase = phrase;
  }

  public String getUrl() {
    return url;
  }

  public String getPhrase() {
    return phrase;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((phrase == null) ? 0 : phrase.hashCode());
    result = prime * result + ((url == null) ? 0 : url.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    UrlAndPhrase other = (UrlAndPhrase) obj;
    if (phrase == null) {
      if (other.phrase != null)
        return false;
    } else if (!phrase.equals(other.phrase))
      return false;
    if (url == null) {
      if (other.url != null)
        return false;
    } else if (!url.equals(other.url))
      return false;
    return true;
  }
  
  


}
