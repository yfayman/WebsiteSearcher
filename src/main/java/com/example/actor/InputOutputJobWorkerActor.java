package com.example.actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.actor.InputOutputJobActor.GetUrlRequest;
import com.example.actor.OutputActor.WriteRequest;
import com.example.model.UrlAndPhrase;
import com.example.searcher.UrlSearcher;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

/**
 * This is the actor that handles the work. If a match is found, a messages is forwarded
 * to the output actor so it can write the data
 * @author yan
 *
 */
public class InputOutputJobWorkerActor extends AbstractActor {
  
  private final static Logger LOGGER = LoggerFactory.getLogger(InputOutputJobWorkerActor.class);

  static Props props(UrlSearcher searcher, ActorRef outputActor) {
    return Props.create(InputOutputJobWorkerActor.class,
        () -> new InputOutputJobWorkerActor(searcher, outputActor)).withDispatcher("my-pinned-dispatcher");
  }

  private final UrlSearcher searcher;
  private final ActorRef outputActor;


  public InputOutputJobWorkerActor(UrlSearcher searcher, ActorRef outputActor) {
    super();
    this.searcher = searcher;
    this.outputActor = outputActor;
  }



  @Override
  public Receive createReceive() {
    return receiveBuilder().
        match(Start.class, start -> {
          LOGGER.info("Worker {} received Start Message", self().path().toString());
          sender().tell(new GetUrlRequest(), self());
        }).
        match(GetUrlResponse.class, res -> {
          String name = self().path().toString();
          LOGGER.info("{} is going to look up {}", name, res.urlAndPhrase.getUrl());
          if(searcher.search(res.urlAndPhrase.getUrl(), res.urlAndPhrase.getPhrase())) {
            LOGGER.info("{} found a match for {}", name, res.urlAndPhrase.getUrl());
            outputActor.tell(new WriteRequest(res.urlAndPhrase), self());
          }          
          sender().tell(new GetUrlRequest(),self());
        }).build();
  }


  public static final class Start {}

  public static final class GetUrlResponse {
    private final UrlAndPhrase urlAndPhrase;

    public GetUrlResponse(UrlAndPhrase urlAndPhrase) {
      this.urlAndPhrase = urlAndPhrase;
    }

    @Override
    public int hashCode() {
     return urlAndPhrase.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      GetUrlResponse other = (GetUrlResponse) obj;
      if (urlAndPhrase == null) {
        if (other.urlAndPhrase != null)
          return false;
      } else if (!urlAndPhrase.equals(other.urlAndPhrase))
        return false;
      return true;
    }
    
    

  }

}
