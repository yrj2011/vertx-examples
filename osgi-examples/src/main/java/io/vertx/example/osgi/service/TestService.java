package io.vertx.example.osgi.service;

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

}
