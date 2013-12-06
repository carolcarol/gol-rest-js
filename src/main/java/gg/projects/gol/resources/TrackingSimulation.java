package gg.projects.gol.resources;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.SseFeature;

import com.telenity.trail.rest.mocks.TrackingFeed;

@Path("simulation")
public class TrackingSimulation {

   public static enum Command {
      START {
         @Override
         public String execute() {
            return TrackingFeed.start() ? "STARTED" : "ALREADY RUNNING";
         }
      },
      STOP {
         @Override
         public String execute() {
            return TrackingFeed.stop() ? "STOPPED" : "NOT RUNNING";
         }
      };
      public abstract String execute();
   }

   @POST
   @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
   public String execute(@NotNull @FormParam("command") String command) {
      Command cmd = Command.valueOf(command);
      return cmd.execute();
   }

   @GET
   @Path("events")
   @Produces(SseFeature.SERVER_SENT_EVENTS)
   public EventOutput connect() {
      EventOutput output = new EventOutput();
      TrackingFeed.register(output);
      return output;
   }

}
