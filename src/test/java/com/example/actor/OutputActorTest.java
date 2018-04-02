package com.example.actor;

import static akka.pattern.PatternsCS.ask;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.example.actor.OutputActor.FinishWriting;
import com.example.actor.OutputActor.StatusRequest;
import com.example.actor.OutputActor.StatusResponse;
import com.example.actor.OutputActor.WriteRequest;
import com.example.model.UrlAndPhrase;
import com.example.writer.StringWriter;
import com.typesafe.config.ConfigFactory;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;

public class OutputActorTest {

  private ActorSystem system;

  private StringWriter writer;



  @SuppressWarnings("unchecked")
  @Before
  public void setup() throws IOException {
    system = ActorSystem.create("test", ConfigFactory.load().getConfig("configuration"));
    writer = mock(StringWriter.class);

    doNothing().when(writer).write(anyString());

  }

  @After
  public void tearDown() {
    TestKit.shutdownActorSystem(system);
  }


  @Test
  public void writeRequestTest() {
    new TestKit(system) {
      {

        final ActorRef subj =
            system.actorOf(OutputActor.props(writer));
        final TestKit probe = new TestKit(system);
        subj.tell(new WriteRequest(new UrlAndPhrase("https://google.com", "cool")), probe.getRef());
        probe.expectNoMessage();
      }
    };
  }
  
  @Test
  public void finishWritingTest() {
    new TestKit(system) {
      {

        final ActorRef subj =
            system.actorOf(OutputActor.props(writer));
        final TestKit probe = new TestKit(system);
        
        CompletableFuture<Object> statusReqFuture = ask(subj,new StatusRequest(),1000).toCompletableFuture();
        statusReqFuture.thenApply( o ->  (StatusResponse)o).thenAccept(sr -> {
         Assert.assertTrue(sr.open);
        });
        
        subj.tell(new FinishWriting(), probe.getRef());
        probe.expectNoMessage();
        
        CompletableFuture<Object> statusReqFuture2 = ask(subj,new StatusRequest(),1000).toCompletableFuture();
        statusReqFuture2.thenApply( o ->  (StatusResponse)o).thenAccept(sr -> {
         Assert.assertFalse(sr.open);
        });
      }
    };
  }
  
  
}
