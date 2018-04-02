package com.example.writer;

import java.io.IOException;

public interface StringWriter {
  
  public void open() throws IOException;
  
  public void write(String str) throws IOException;

  public void close() throws IOException;
}
