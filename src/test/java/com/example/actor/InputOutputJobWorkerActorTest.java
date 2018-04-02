package com.example.actor;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.example.actor.InputOutputJobActor.GetUrlRequest;
import com.example.actor.InputOutputJobWorkerActor.GetUrlResponse;
import com.example.actor.InputOutputJobWorkerActor.Start;
import com.example.actor.OutputActor.WriteRequest;
import com.example.model.UrlAndPhrase;
import com.example.reader.UrlAndPhraseReader;
import com.example.searcher.UrlSearcher;
import com.example.writer.StringWriter;
import com.typesafe.config.ConfigFactory;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;

public class InputOutputJobWorkerActorTest {

  private ActorSystem system;
  private UrlAndPhraseReader reader;
  private StringWriter writer;
  private UrlSearcher searcher;
  private ActorRef outputActor;


  @SuppressWarnings("unchecked")
  @Before
  public void setup() throws IOException {
    system = ActorSystem.create("test", ConfigFactory.load().getConfig("configuration"));
    writer = mock(StringWriter.class);
    searcher = mock(UrlSearcher.class);
    outputActor = system.actorOf(OutputActor.props(writer));

    doNothing().when(writer).write(anyString());
    when(searcher.search(anyString(), anyString())).thenReturn(true);
  }

  @After
  public void tearDown() {
    TestKit.shutdownActorSystem(system);
  }


  @Test
  public void startTest() {
    new TestKit(system) {
      {

        final ActorRef subj =
            system.actorOf(InputOutputJobWorkerActor.props(searcher, outputActor));
        final TestKit probe = new TestKit(system);
        subj.tell(new Start(), probe.getRef());
        probe.expectMsgAnyClassOf(GetUrlRequest.class);
      }
    };
  }
  
  @Test
  public void getUrlResponseTest() {
    new TestKit(system) {
      {
        final TestKit outputProbe = new TestKit(system);
        final ActorRef subj =
            system.actorOf(InputOutputJobWorkerActor.props(searcher, outputProbe.getRef()));
        final TestKit probe = new TestKit(system);
        subj.tell(new GetUrlResponse(new UrlAndPhrase("https://google.com", "cat")), probe.getRef());
        
        probe.expectMsgAnyClassOf(GetUrlRequest.class);
        outputProbe.expectMsgAnyClassOf(WriteRequest.class);
      }
    };
  }
}
