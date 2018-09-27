package io.vertx.example.osgi.test;

import io.vertx.core.Vertx;
import io.vertx.example.osgi.service.TestService;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Provides
@Instantiate
public class FreeMarkerTemplate implements TestService {

  @Requires
  Vertx vertx;
  private WebClient client;

  @Override
  public void run() throws Exception {
    System.out.println("Running FreeMarker Tests");
    client = WebClient.create(vertx);
    try {
      CountDownLatch latch = new CountDownLatch(1);
      AtomicReference<String> result = new AtomicReference<>();
      client.getAbs("http://localhost:8081/templates/fm").as(BodyCodec.string()).send(resp -> {
        if (resp.succeeded()) {
          result.set(resp.result().body());
        }
        latch.countDown();
      });
      latch.await();

      assertTrue(result.get() != null);
      assertTrue( result.get().contains("Hello"));
      assertTrue(result.get().contains("clement"));
      assertTrue(result.get().contains("vert.x"));
    } finally {
      client.close();
    }


  }
}
