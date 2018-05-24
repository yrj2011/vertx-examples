package io.vertx.example.osgi.test;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.example.osgi.service.TestService;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks the behavior introduced in https://github.com/eclipse/vert.x/pull/2395/files.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@Component
@Provides
@Instantiate
public class CacheHydratation implements TestService {

  @Requires
  private Vertx vertx;

  @Override
  public void run() {
    System.out.println("Running " + this.getClass().getName());
    Buffer buff = vertx.fileSystem().readFileBlocking("hydratation/subdir/subfile.txt");
    assertEquals(buff.toString(), "Hello!\n");
    Set<String> names = vertx.fileSystem().readDirBlocking("hydratation/subdir").stream().map(path -> {
      int idx = path.lastIndexOf('/');
      return idx == -1 ? path : path.substring(idx + 1);
    }).collect(Collectors.toSet());
    assertEquals(names, new HashSet<>(Arrays.asList("subdir2", "subfile.txt")));
  }

  private void assertEquals(Object a, Object b) {
    if (a == null && b == null) {
      return;
    }

    if (a == null) {
      throw new IllegalArgumentException("expected " + b + " to be null");
    }

    if (b == null) {
      throw new IllegalArgumentException("expected " + a + " to be null");
    }

    if (!a.equals(b)) {
      throw new IllegalArgumentException("expected " + a + " to be equal to " + b);
    }
  }
}

