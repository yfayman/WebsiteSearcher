package com.example.app;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import com.example.actor.InputOutputJobActor;
import com.example.reader.UrlAndPhraseFromFileReader;
import com.example.reader.UrlAndPhraseReader;
import com.example.searcher.JSoupSearcher;
import com.example.searcher.UrlSearcher;
import com.example.writer.FileStringWriter;
import com.example.writer.StringWriter;
import com.typesafe.config.ConfigFactory;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Inbox;

/**
 * This program is designed with a pull model in mind(workers finish their work
 * and then request more). This is to solve a potential back pressure issue if the 
 * file is very large.
 * @author yan
 *
 */
public class App {
  
  private final static String INPUT_FILE_NAME = "urls.txt";
  private final static String OUTPUT_FILE_NAME = "results.txt";

  public static void main(String[] args) throws TimeoutException, IOException {

    ActorSystem system = ActorSystem.create("mySystem", ConfigFactory.load().getConfig("configuration"));
    Inbox inbox = Inbox.create(system);
    
    File file = new File(INPUT_FILE_NAME);
    UrlAndPhraseReader reader = new UrlAndPhraseFromFileReader(file);
    File outputFile = new File(OUTPUT_FILE_NAME);
    
    if(outputFile.exists()) {
      outputFile.delete();
    }
    outputFile.createNewFile();
    
    StringWriter writer = new FileStringWriter(outputFile);
    UrlSearcher searcher = new JSoupSearcher();
    ActorRef mja = system.actorOf(InputOutputJobActor.props(reader, writer, searcher));
    inbox.send(mja, new InputOutputJobActor.Start());  
  }

}
