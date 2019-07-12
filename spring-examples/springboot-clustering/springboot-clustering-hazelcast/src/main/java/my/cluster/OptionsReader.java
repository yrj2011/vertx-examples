package my.cluster;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

/**
 * 获取集群和部署配置类属性
 */
public class OptionsReader {
  private static final String VX_PREFIX = "vertx.";

  private static  ResourceLoader LOADER = ResourceLoader.getInstance();

  public static VertxOptions readOpts(final String name) throws Exception{
    final VertxOptions options = new VertxOptions();
    options.setEventLoopPoolSize(LOADER.getInt(VX_PREFIX+name+".pool.size.event.loop"));
    options.setWorkerPoolSize(LOADER.getInt(VX_PREFIX+name+".pool.size.worker"));
    options.setInternalBlockingPoolSize(LOADER.getInt(VX_PREFIX+name+".pool.size.internal.blocking"));

    options.setClustered(LOADER.getBoolean(VX_PREFIX+name+".cluster.enabled"));
    options.setClusterHost(LOADER.getString(VX_PREFIX+name+".cluster.host"));
    options.setClusterPort(LOADER.getInt(VX_PREFIX+name+".cluster.port"));

    options.setClusterPingInterval(LOADER.getLong(VX_PREFIX+name+".cluster.ping.interval"));
    options.setClusterPingReplyInterval(LOADER.getLong(VX_PREFIX+name+".cluster.ping.interval.reply"));

    options.setBlockedThreadCheckInterval(LOADER.getLong(VX_PREFIX+name+".blocked.thread.check.interval"));
    options.setMaxEventLoopExecuteTime(LOADER.getLong(VX_PREFIX+name+".execute.time.max.event.loop"));
    options.setMaxWorkerExecuteTime(LOADER.getLong(VX_PREFIX+name+".execute.time.max.worker"));

    options.setHAEnabled(LOADER.getBoolean(VX_PREFIX+name+".ha.enabled"));
    options.setHAGroup(LOADER.getString(VX_PREFIX+name+".ha.group"));
    options.setQuorumSize(LOADER.getInt(VX_PREFIX+name+".quorum.size"));
    options.setWarningExceptionTime(LOADER.getLong(VX_PREFIX+name+".warning.exception.time"));

    return options;
  }

  public static DeploymentOptions readOpts() throws Exception{
    final DeploymentOptions options = new DeploymentOptions();
    options.setConfig(new JsonObject()
      .put("http.port", LOADER.getInt("http.port"))
      .put("db_name", LOADER.getString("db_name"))
      .put("connection_string", LOADER.getString("connection_string")));
    return options;
  }
}
