package io.vertx.example.osgi.test;

import io.vertx.example.osgi.service.TestService;
import io.vertx.ext.jwt.JWT;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import java.util.Objects;

@Component
@Provides
@Instantiate
public class JWTAuth implements TestService {


  @Override
  public void run() throws Exception {
    JWT jwt = new JWT();
    Objects.requireNonNull(jwt);
  }
}
