package com.example.actor;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.actor.InputOutputJobWorkerActor.GetUrlResponse;
import com.example.actor.OutputActor.FinishWriting;
import com.example.model.UrlAndPhrase;
import com.example.reader.UrlAndPhraseReader;
import com.example.searcher.UrlSearcher;
import com.example.writer.StringWriter;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

/**
 * This actor oversees a group of workers. The workers make requests 
 * for URLs. If there is more urls to check, a response will be sent.
 * 
 * If there are no more urls to check, the worker is shutdown and 
 * removed from activeWorkers 
 * @author yan
 *
 */
public class InputOutputJobActor extends AbstractActor {

  
  private final static Logger LOGGER = LoggerFactory.getLogger(InputOutputJobActor.class);

  public static Props props(UrlAndPhraseReader reader, StringWriter writer, UrlSearcher searcher) {
    return Props.create(InputOutputJobActor.class,
        () -> new InputOutputJobActor(reader, writer, searcher));
  }

  private final UrlAndPhraseReader reader;
  private final StringWriter writer;
  private final UrlSearcher searcher;
  private final static int MAX_WORKERS = 20;
  private Set<String> activeWorkers;

  /*
   * This actor will be passed to the worker actors. These actors will forward results to the output
   * actor
   */
  private final ActorRef outputActor;


  public InputOutputJobActor(UrlAndPhraseReader reader, StringWriter writer, UrlSearcher searcher) {
    super();
    this.reader = reader;
    this.writer = writer;
    this.searcher = searcher;
    outputActor = getContext().actorOf(OutputActor.props(this.writer));
  }



  private void initializeWorkers(int numberOfWorkers) {
    activeWorkers = new HashSet<>();
    for (int i = 0; i < numberOfWorkers; i++) {
      ActorRef worker =
          getContext().actorOf(InputOutputJobWorkerActor.props(searcher, outputActor));
      activeWorkers.add(worker.path().toString());

      // Start off the worker
      worker.tell(new InputOutputJobWorkerActor.Start(), self());
    }

  }



  @Override
  public Receive createReceive() {
    return receiveBuilder().
        match(Start.class, start -> {
          LOGGER.info("Received start request Initializing {} workers", MAX_WORKERS);
          initializeWorkers(MAX_WORKERS);
        }).
        match(GetUrlRequest.class, req -> {
          LOGGER.info("Got a URL request from a worker");
          Optional<UrlAndPhrase> urlAndPhraseOpt = reader.read();
          urlAndPhraseOpt.ifPresent(urlAndPhrase -> {
            sender().tell(new GetUrlResponse(urlAndPhrase), self());
          });
          
          if (!urlAndPhraseOpt.isPresent() && activeWorkers != null) {
            activeWorkers.remove(sender().path().toString());
            
            getContext().stop(sender());            
            if(activeWorkers.size() == 0) {
              outputActor.tell(new FinishWriting(), self());
              getContext().getSystem().terminate();
            }
            
          }
        }).match(GetWorkerPathsRequest.class, req -> {
          sender().tell(new GetWorkerPathsResponse(activeWorkers), self());
    }).build();
  }

  public static final class GetUrlRequest {}
  
  public static final class Start{}
  
  
  /*
   * Below are for testing
   */
  public static final class GetWorkerPathsRequest{}
  
  public static final class GetWorkerPathsResponse{
    final Set<String> workerPaths;

    public GetWorkerPathsResponse(Set<String> workerPaths) {
      this.workerPaths = workerPaths;
    }
    
  }


}
