package com.example.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import com.example.model.UrlAndPhrase;

public class UrlAndPhraseFromFileReader implements UrlAndPhraseReader {

  private final static List<String> WORDS = Arrays.asList("cat", "dog", "rat", "subway", "the", "horse");

  private BufferedReader br;
  
  private boolean fileCompleted = false;

  public UrlAndPhraseFromFileReader(File file) throws IOException {
    br = new BufferedReader(new FileReader(file));
    br.readLine(); // Skips the header row TODO - Make this more robust
  }

  @Override
  public Optional<UrlAndPhrase> read() throws IOException {
    if(fileCompleted) {
      return Optional.empty();
    }else {
      String line = br.readLine();
     
      if(line == null) {
        fileCompleted = true;
        br.close();
        return Optional.empty();
      }else{
        String[] tokens = line.split(",") ;
        return getUrlOpt(tokens[1]).map(url -> new UrlAndPhrase(url,getPhrase()));
      }
    }

  }
  
  private Optional<String> getUrlOpt(String token){
    return Optional.ofNullable(token).map(u -> {
      String url = token.replaceAll("\"", "");
      if(!url.startsWith("https")) {
        return "https://" + url;
      }else {
        return url;
      }
    });
  }
  
  private String getPhrase() {
    return  WORDS.get(new Random().nextInt(WORDS.size()));
  }


}
