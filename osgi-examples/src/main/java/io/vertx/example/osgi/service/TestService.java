package io.vertx.example.osgi.service;

import java.util.function.Supplier;

/**
 * An interface used to run "test"
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface TestService {
  /**
   * Runs the test.
   *
   * @throws Exception thrown when the test failed
   */
  void run() throws Exception;


  default void assertIt(Supplier<Boolean> predicate) throws Exception {
    try {
      if (!predicate.get()) {
        throw new Exception("Test Failure");
      }
    } catch (Exception e) {
      throw new Exception("Test Failure", e);
    }
  }

  default void assertIt(Runnable block) throws Exception {
    try {
      block.run();
    } catch (Exception e) {
      throw new Exception("Test Failure", e);
    }
  }

  default void assertTrue(boolean res) throws Exception {
    if (!res) {
      throw new Exception("Test Failure");
    }
  }


}
