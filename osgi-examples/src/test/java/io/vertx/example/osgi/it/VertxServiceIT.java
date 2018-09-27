package io.vertx.example.osgi.it;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.example.osgi.service.DataService;
import io.vertx.example.osgi.service.TestService;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import javax.inject.Inject;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class VertxServiceIT {

  @Inject
  private Vertx vertxService;

  @Inject
  private EventBus eventBus;

  @Inject
  private DataService data;

  @Inject
  private BundleContext context;

  @Configuration
  public Option[] config() {
    return options(

      systemPackages("sun.misc"),

      mavenBundle("io.vertx", "vertx-core").versionAsInProject(),

      mavenBundle("io.netty", "netty-common").versionAsInProject(),
      mavenBundle("io.netty", "netty-buffer").versionAsInProject(),
      mavenBundle("io.netty", "netty-transport").versionAsInProject(),
      mavenBundle("io.netty", "netty-handler").versionAsInProject(),
      mavenBundle("io.netty", "netty-codec").versionAsInProject(),
      mavenBundle("io.netty", "netty-handler-proxy").versionAsInProject(),
      mavenBundle("io.netty", "netty-codec-socks").versionAsInProject(),
      mavenBundle("io.netty", "netty-codec-http").versionAsInProject(),
      mavenBundle("io.netty", "netty-codec-http2").versionAsInProject(),
      mavenBundle("io.netty", "netty-codec-dns").versionAsInProject(),
      mavenBundle("io.netty", "netty-resolver").versionAsInProject(),
      mavenBundle("io.netty", "netty-resolver-dns").versionAsInProject(),

      mavenBundle("com.fasterxml.jackson.core", "jackson-core").versionAsInProject(),
      mavenBundle("com.fasterxml.jackson.core", "jackson-databind").versionAsInProject(),
      mavenBundle("com.fasterxml.jackson.core", "jackson-annotations").versionAsInProject(),

      mavenBundle("io.reactivex.rxjava2", "rxjava").versionAsInProject(),
      mavenBundle("io.reactivex", "rxjava").versionAsInProject(),
      mavenBundle("org.reactivestreams", "reactive-streams").versionAsInProject(),

      mavenBundle("io.vertx", "vertx-jwt").versionAsInProject(),
      mavenBundle("io.vertx", "vertx-web").versionAsInProject(),
      mavenBundle("io.vertx", "vertx-web-templ-handlebars").versionAsInProject(),
      mavenBundle("io.vertx", "vertx-web-templ-freemarker").versionAsInProject(),
      mavenBundle("com.github.jknack", "handlebars").versionAsInProject(),
      mavenBundle("org.freemarker", "freemarker").versionAsInProject(),
      mavenBundle("javax.servlet", "javax.servlet-api").version("3.0.1"),

      mavenBundle("io.vertx", "vertx-bridge-common").versionAsInProject(),
      mavenBundle("io.vertx", "vertx-web-client").versionAsInProject(),
      mavenBundle("io.vertx", "vertx-web-common").versionAsInProject(),
      mavenBundle("io.vertx", "vertx-rx-java2").versionAsInProject(),
      mavenBundle("io.vertx", "vertx-rx-java").versionAsInProject(),

      mavenBundle("io.vertx", "vertx-auth-common").versionAsInProject(),
      mavenBundle("io.vertx", "vertx-auth-jwt").versionAsInProject(),
      mavenBundle("io.vertx", "vertx-jdbc-client").versionAsInProject(),
      mavenBundle("io.vertx", "vertx-sql-common").versionAsInProject(),

      mavenBundle("io.vertx", "vertx-mail-client").versionAsInProject(),

      mavenBundle("com.zaxxer", "HikariCP").versionAsInProject(),
      mavenBundle("org.hsqldb", "hsqldb").versionAsInProject(),

      mavenBundle("io.vertx", "vertx-mongo-client").versionAsInProject(),
      mavenBundle("org.mongodb", "mongodb-driver-async").versionAsInProject(),

      mavenBundle("org.apache.felix", "org.apache.felix.ipojo").versionAsInProject(),
      mavenBundle("commons-io", "commons-io").versionAsInProject(),

      bundle("file:target/osgi-examples-" + System.getProperty("project.version", "3.5.3") + ".jar"),

      junitBundles(),

      cleanCaches()
    );
  }

  @Test
  public void testThatTheServiceIsAvailable() throws Exception {
    for (Bundle bundle : context.getBundles()) {
      System.out.println("[" + bundle.getBundleId() + "] - " + bundle.getSymbolicName() + " - " + bundle.getVersion() +
        " - " + bundle.getState());
    }

    Class<?> loadClass = context.getBundle(0).loadClass("org.hsqldb.jdbcDriver");
    System.out.println("Driver: " + loadClass);

    assertNotNull(vertxService);
    assertNotNull(eventBus);

    // Wait for verticle deployment
    await(() -> {
      System.out.println(vertxService.deploymentIDs());
      return vertxService.deploymentIDs().size() >= 2;
    });


    //==== Web Application

    // Check that the static assets has been published by the vert.x web app
    URL url = new URL("http://localhost:8081/assets/index.html");
    String page = IOUtils.toString(url.toURI(), "utf-8");
    assertNotNull(page);
    assertFalse(page.isEmpty());
    assertTrue(page.contains("Static web server"));

    url = new URL("http://localhost:8081/products");
    page = IOUtils.toString(url.toURI(), "utf-8");
    assertNotNull(page);
    assertFalse(page.isEmpty());
    assertTrue(page.contains("Egg Whisk"));

    //==== Http Server

    url = new URL("http://localhost:8080");
    page = IOUtils.toString(url.toURI(), "utf-8");
    assertNotNull(page);
    assertFalse(page.isEmpty());
    assertTrue(page.contains("Hello from OSGi"));

    // ==== Data service (JDBC)
    assertNotNull(data);
    List<String> results = new CopyOnWriteArrayList<>();
    data.retrieve(ar -> {
      if (ar.succeeded()) {
        results.addAll(ar.result());
      }
    });
    await(() -> results.size() != 0);
    assertThat(results.size(), is(1));
    System.out.println("JDBC test results: " + results);

    runInternalTests();
  }

  private void runInternalTests() throws InvalidSyntaxException {
    Collection<ServiceReference<TestService>> references = context.getServiceReferences(TestService.class, null);
    AtomicBoolean failure = new AtomicBoolean();
    references.forEach(ref -> {
      TestService service = context.getService(ref);
      System.out.println("Running internal test: " + service.getClass());
      try {
        service.run();
        System.out.println("Internal test: " + service.getClass() + " [OK]");
      } catch (Throwable e) {
        System.err.println("Test fail: " + service.getClass().getName() + " : " + e.getMessage());
        failure.set(true);
      } finally {
        context.ungetService(ref);
      }
    });

    if (failure.get()) {
      throw new AssertionError("Failing internal tests - check log");
    }
  }

  private static void await(Callable<Boolean> action) throws Exception {
    if (action.call()) {
      return;
    }

    int count = 0;
    while (count < 10) {
      Thread.sleep(1000);
      count++;
      if (action.call()) {
        return;
      }
    }

    throw new IllegalStateException("Timeout");
  }
}
