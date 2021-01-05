
package me.kec.mp.bare;

import java.util.concurrent.SubmissionPublisher;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

import io.helidon.messaging.connectors.aq.AqMessage;
import io.helidon.messaging.connectors.jms.JmsMessage;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;

/**
 * A simple JAX-RS resource to greet you. Examples:
 * <p>
 * Get default greeting message:
 * curl -X GET http://localhost:8080/greet
 * <p>
 * The message is returned as a JSON object.
 */
@Path("/greet")
@ApplicationScoped
public class GreetResource {

    SubmissionPublisher<Message<String>> emitter = new SubmissionPublisher<>();
    private SseBroadcaster sseBroadcaster;

    @Incoming("from-aq")
    public void receive(AqMessage<String> msg) {
        String content = "id: " + msg.getProperty("tab_id") + " " + msg.getPayload();
        if (sseBroadcaster == null) {
            System.out.println("No SSE client subscribed yet: " + content);
            return;
        }
        sseBroadcaster.broadcast(new OutboundEvent.Builder().data(content).build());
    }

    @Outgoing("to-aq")
    public Publisher<Message<String>> registerPublisher() {
        return FlowAdapters.toPublisher(emitter);
    }

    @POST
    @Path("/send/{msg}")
    public void send(@PathParam("msg") String payload) {
        Message<String> msg = JmsMessage.builder(payload)
                .property("tab_id", -1)
                .build();
        emitter.submit(msg);
    }

    @GET
    @Path("sse")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void listenToEvents(@Context SseEventSink eventSink, @Context Sse sse) {
        if (sseBroadcaster == null) {
            sseBroadcaster = sse.newBroadcaster();
        }
        sseBroadcaster.register(eventSink);
    }
}
