package com.example.actor;

import static akka.pattern.PatternsCS.ask;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.example.actor.InputOutputJobActor.GetUrlRequest;
import com.example.actor.InputOutputJobActor.GetWorkerPathsRequest;
import com.example.actor.InputOutputJobActor.GetWorkerPathsResponse;
import com.example.actor.InputOutputJobActor.Start;
import com.example.actor.InputOutputJobWorkerActor.GetUrlResponse;
import com.example.model.UrlAndPhrase;
import com.example.reader.UrlAndPhraseReader;
import com.example.searcher.UrlSearcher;
import com.example.writer.StringWriter;
import com.typesafe.config.ConfigFactory;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;

public class InputOutputJobActorTest {

  private  ActorSystem system;
  private  UrlAndPhraseReader reader;
  private  StringWriter writer;
  private  UrlSearcher searcher;

  private  UrlAndPhrase firstResponse = new UrlAndPhrase("https://www.google.com", "cat");
  private  UrlAndPhrase secondResponse = new UrlAndPhrase("https://www.yahoo.com", "dog");

  @SuppressWarnings("unchecked")
  @Before
  public  void setup() throws IOException {
    system = ActorSystem.create("test",ConfigFactory.load().getConfig("configuration"));
    reader = mock(UrlAndPhraseReader.class);
    writer = mock(StringWriter.class);
    searcher = mock(UrlSearcher.class);


    Optional<UrlAndPhrase> firstResponseOpt = Optional.of(firstResponse);
    Optional<UrlAndPhrase> secondResponseOpt = Optional.of(secondResponse);
    when(reader.read()).thenReturn(firstResponseOpt, secondResponseOpt, Optional.empty());

    doNothing().when(writer).write(anyString());

    when(searcher.search(anyString(), anyString())).thenReturn(true);


  }

  @After
  public  void tearDown() {
    TestKit.shutdownActorSystem(system);
  }

  @Test
  public void getUrlRequestTest() {
    new TestKit(system) {
      {
        final ActorRef subj = system.actorOf(InputOutputJobActor.props(reader, writer, searcher));

        final TestKit probe = new TestKit(system);
        subj.tell(new GetUrlRequest(), probe.getRef());
        probe.expectMsg(new GetUrlResponse(firstResponse));

        subj.tell(new GetUrlRequest(), probe.getRef());
        probe.expectMsg(new GetUrlResponse(secondResponse));

        subj.tell(new GetUrlRequest(), probe.getRef());
        probe.expectNoMessage();
      }
    };
  }

  @Test
  public void startTest() {
    new TestKit(system) {
      {

        final ActorRef subj = system.actorOf(InputOutputJobActor.props(reader, writer, searcher));
        final TestKit probe = new TestKit(system);
        subj.tell(new Start(), probe.getRef());
        probe.expectNoMessage();
        
        CompletableFuture<Object> workerReqFuture = ask(subj, new GetWorkerPathsRequest(), 1000).toCompletableFuture();
        workerReqFuture.thenAccept( res -> {
          Assert.assertTrue(res instanceof GetWorkerPathsResponse);
          GetWorkerPathsResponse response = (GetWorkerPathsResponse) res;
          Assert.assertFalse(response.workerPaths.isEmpty());
          Assert.assertEquals(20, response.workerPaths.size());
        });
        
        
      }
    };
  }

}
