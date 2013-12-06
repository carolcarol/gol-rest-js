package gg.projects.gol.util;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;

import com.telenity.trail.rest.beans.Mark;

public class TrackingFeed {

   private static final ScheduledExecutorService executor = Executors
         .newSingleThreadScheduledExecutor();

   private static final SseBroadcaster broadcaster = new SseBroadcaster();

   private static final AtomicBoolean started = new AtomicBoolean(false);

   private static List<Mark> locations = MockLocations.createMockLocationReport(100, 0, 1L);

   public static boolean register(EventOutput output) {
      return broadcaster.add(output);
   }

   public static boolean start() {
      if (!started.compareAndSet(false, true)) {
         return false;
      }
      executor.submit(new EventBroadcaster());
      return true;
   }

   public static boolean stop() {
      return started.compareAndSet(true, false);
   }

   private static class EventBroadcaster implements Runnable {

      private EventBroadcaster() {
      }

      @Override
      public void run() {
         final Thread currentThread = Thread.currentThread();

         for (Mark mark : locations) {

            OutboundEvent trackingEvent = new OutboundEvent.Builder()
                  .mediaType(MediaType.APPLICATION_JSON_TYPE).data(mark).build();
            broadcaster.broadcast(trackingEvent);
            System.out.println("Broadcasted event: " + mark);
            try {
               if (started.get() && !currentThread.isInterrupted()) {
                  currentThread.sleep(500);
               } else {
                  currentThread.interrupt();
               }
            } catch (InterruptedException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
            
            if (started.get() && !currentThread.isInterrupted()) {
               executor.schedule(this, 500, TimeUnit.MILLISECONDS); // re-schedule
            } else {
               currentThread.interrupt();
            }
         }

      }

      private static void cleanup() {
         locations = null;
      }

   }
}
