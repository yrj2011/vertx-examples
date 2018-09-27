package io.vertx.example.osgi.test;

import io.vertx.example.osgi.service.TestService;
import io.vertx.rxjava.core.Vertx;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import java.util.Objects;


@Component
@Provides
@Instantiate
public class RxJava1 implements TestService {


  @Override
  public void run() {
    System.out.println("Running " + this.getClass().getName());
    Vertx vertx = Vertx.vertx();
    Objects.requireNonNull(vertx, "Vert.x instance should not be null");
    vertx.close();
  }
}
