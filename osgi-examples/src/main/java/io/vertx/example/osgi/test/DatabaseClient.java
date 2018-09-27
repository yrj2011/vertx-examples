package io.vertx.example.osgi.test;

import io.vertx.core.Vertx;
import io.vertx.example.osgi.service.DataService;
import io.vertx.example.osgi.service.TestService;
import io.vertx.ext.sql.ResultSetType;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Component
@Provides
@Instantiate
public class DatabaseClient implements TestService {

  @Requires
  DataService db;

  @Override
  public void run() throws Exception {
    assertTrue(ResultSetType.values().length != 0);

    CountDownLatch latch = new CountDownLatch(1);
    List<String> result = new ArrayList<>();
    db.retrieve(ar -> {
      if (ar.succeeded()) {
        result.addAll(ar.result());
      }
      latch.countDown();
    });
    latch.await();
    assertIt(() -> {
      System.out.println(result);
      return result.size() == 1 && result.get(0).contains("Hello");
    });
  }
}
