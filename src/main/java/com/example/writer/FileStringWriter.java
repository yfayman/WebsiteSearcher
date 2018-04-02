package com.example.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileStringWriter implements StringWriter {
  
  private final File file;
  
  private BufferedWriter bWriter;
  
  public FileStringWriter(File file) {
    this.file = file;
  }
  
  @Override
  public void write(String str) throws IOException {   
    bWriter.write(str);
    bWriter.newLine();
  }

  @Override
  public void open() throws IOException {
    bWriter = new BufferedWriter(new FileWriter(file));
  }

  @Override
  public void close() throws IOException {
    bWriter.close();   
  }

}
