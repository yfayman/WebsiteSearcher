package com.example.reader;

import java.io.IOException;
import java.util.Optional;
import com.example.model.UrlAndPhrase;

public interface UrlAndPhraseReader {

  public Optional<UrlAndPhrase> read() throws IOException;

}
