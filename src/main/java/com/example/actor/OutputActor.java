package com.example.actor;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.model.UrlAndPhrase;
import com.example.writer.StringWriter;
import akka.actor.AbstractActor;
import akka.actor.Props;


/**
 * OutputActor received write requests and writes to some output
 * @author yan
 *
 */
public class OutputActor extends AbstractActor {
  
  private final static Logger LOGGER = LoggerFactory.getLogger(OutputActor.class);

  static Props props(StringWriter writer) {
    return Props.create(OutputActor.class, () -> new OutputActor(writer));
  }
  
  private final StringWriter writer;
  private boolean open;


  public OutputActor(StringWriter writer) throws IOException {
    super();
    this.writer = writer;
    this.writer.open();
    this.open = true;
  }
  
  @Override
  public Receive createReceive() {
   return receiveBuilder().
       match(WriteRequest.class, wRequest -> {
         String lineToWrite = String.format("%s %s", wRequest.getUrl(), wRequest.getPhrase());
         LOGGER.info("Received Write Request. Going to write {} to output", lineToWrite);
         writer.write(lineToWrite);
       }).
       match(FinishWriting.class, fw -> {
         LOGGER.info("Received finish writing request");
         writer.close();
         this.open = false;
       }).
       match(StatusRequest.class, sr -> {
         sender().tell(new StatusResponse(this.open), self());
       }).build();
  }
  
  public static final class WriteRequest{
    private final UrlAndPhrase urlAndPhrase;

    public WriteRequest(UrlAndPhrase urlAndPhrase) {
      this.urlAndPhrase = urlAndPhrase;
    }
    
    public String getUrl() {
      return urlAndPhrase.getUrl();
    }
    
    public String getPhrase() {
      return urlAndPhrase.getPhrase();
    }
    
  }
  
  public static final class FinishWriting{}
  
  /*
   * Below are for testing
   */
  public static final class StatusRequest{}
  
  public static final class StatusResponse{
    public final boolean open;
    
    public StatusResponse(boolean open) {
      this.open = open;
    }
  }

}
