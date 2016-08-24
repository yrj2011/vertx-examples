package io.vertx.example.osgi;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class loading SPI implementation for Vert.x
 */
public class VertxSpiHelper {

  private final static Logger LOGGER = Logger.getLogger("SPIHelper");


  public static <T> T lookup(Class<T> clazz, BundleContext bc, String hint) throws IOException {
    Objects.requireNonNull(clazz);
    Objects.requireNonNull(bc);

    Bundle vertxBundle = getVertxCoreBundle(bc);
    if (vertxBundle == null) {
      throw new RuntimeException("Cannot find the vertx-core bundle");
    }

    for (Bundle bundle : bc.getBundles()) {
      if (bundle.getBundleId() == 0) {
        // Skip system bundle.
        continue;
      }
      T className = loadAndInstantiate(clazz, hint, bundle);
      if (className != null) return className;
    }

    // Nothing found, try with system bundle
    Bundle system = bc.getBundle(0);
    return loadAndInstantiate(clazz, hint, system);
  }

  private static <T> T loadAndInstantiate(Class<T> clazz, String hint, Bundle bundle) throws IOException {
    URL url = bundle.getResource("META-INF/services/" + clazz.getName());
    if (url != null) {
      InputStream stream = url.openStream();
      List<String> content = IOUtils.readLines(stream, "UTF-8");
      String className = null;
      for (String line : content) {
        if (!line.startsWith("#") && !line.trim().isEmpty()) {
          className = line.trim();
        }
      }

      if (className == null) {
        throw new RuntimeException("Service file not formatted correctly " + url.toExternalForm());
      }

      if (hint != null) {
        if (className.toLowerCase().contains(hint.toLowerCase())) {
          return instantiate(bundle, className);
        }
      } else {
        return instantiate(bundle, className);
      }
    }
    return null;
  }

  private static <T> T instantiate(Bundle bundle, String className) {
    try {
      Class<T> clazz = (Class<T>) bundle.loadClass(className);
      return clazz.newInstance();
    } catch (ClassNotFoundException e) {
      LOGGER.severe("Cannot load class " + className + " from " + bundle.getSymbolicName());
    } catch (InstantiationException | IllegalAccessException e) {
      LOGGER.log(Level.SEVERE, "Cannot instantiate class " + className + " from " + bundle.getSymbolicName(), e);
    }
    return null;
  }

  private static Bundle getVertxCoreBundle(BundleContext context) {
    for (Bundle bundle : context.getBundles()) {
      if (bundle.getSymbolicName().toLowerCase().contains("io.vertx.core")) {
        return bundle;
      }
    }
    return null;
  }

}
