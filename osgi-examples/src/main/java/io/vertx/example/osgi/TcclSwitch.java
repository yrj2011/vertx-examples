package io.vertx.example.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * The famous TCCL trick for OSGi.
 */
public class TcclSwitch {

  public static <T> T executeWithTCCLSwitch(Callable<T> action) throws Exception {
    final ClassLoader original = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(TcclSwitch.class.getClassLoader());
      return action.call();
    } finally {
      Thread.currentThread().setContextClassLoader(original);
    }
  }

  public static <T> T executeWithTCCLSwitch(Callable<T> action, BundleContext context, Class... classes) throws Exception {
    final ClassLoader original = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(new AllBundleClassLoader(context, classes));
      return action.call();
    } finally {
      Thread.currentThread().setContextClassLoader(original);
    }
  }

  public static void executeWithTCCLSwitch(Runnable action) {
    final ClassLoader original = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(TcclSwitch.class.getClassLoader());
      action.run();
    } finally {
      Thread.currentThread().setContextClassLoader(original);
    }
  }

  private static class AllBundleClassLoader extends ClassLoader {
    private final BundleContext context;


    private Set<ClassLoader> classloaders = new LinkedHashSet<>();

    public AllBundleClassLoader(BundleContext context, Class... classes) {
      this.context = context;
      classloaders.add(this.getClass().getClassLoader());
      Arrays.stream(classes).forEach(c -> classloaders.add(c.getClassLoader()));
      Arrays.stream(context.getBundles()).forEach(bundle -> classloaders.add(new ClassLoaderFromBundle(bundle)));
    }


    private Class<?> loadOrNull(String name, ClassLoader cl) {
      try {
        return cl.loadClass(name);
      } catch (ClassNotFoundException e) {
        return null;
      }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
      for (ClassLoader cl : classloaders) {
        Class<?> c = loadOrNull(name, cl);
        if (c != null) {
          return c;
        }
      }

      throw new ClassNotFoundException("Unable to load " + name);

    }

    @Override
    public URL getResource(String name) {
      for (ClassLoader cl : classloaders) {
        URL c = cl.getResource(name);
        if (c != null) {
          return c;
        }
      }
      return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
      Vector<URL> list = new Vector<>();
      for (ClassLoader cl : classloaders) {
        Enumeration<URL> c = cl.getResources(name);
        if (c != null) {
          while (c.hasMoreElements()) {
            URL url = c.nextElement();
            list.add(url);
          }
        }
      }
      return list.elements();
    }

  }

  private static class ClassLoaderFromBundle extends ClassLoader {

    private final Bundle bundle;

    public ClassLoaderFromBundle(Bundle bundle) {
      this.bundle = bundle;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
      return bundle.loadClass(name);
    }

    @Override
    public URL getResource(String name) {
      return bundle.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
      return bundle.getResources(name);
    }

    public String toString() {
      return "[classloader of " + bundle.getSymbolicName() + "]";
    }
  }
}
