package my.cluster;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClusterStarter {

  private static final Logger log = LogManager
    .getLogger(ClusterStarter.class);

  public static void main(String[] args) throws Exception {
    ResourceLoader.DEFAULT_CONFIG_FILE = args[0];
    System.out.println("DEFAULT_CONFIG_FILE:"+ResourceLoader.DEFAULT_CONFIG_FILE);

    try{
      RootVerticle.PORT = Integer.parseInt(args[1]);
    }catch (Exception e){
      e.printStackTrace();
    }
    /*
     * final VertxOptions options = OptionsReader.readOpts("VXWEB"); final
     * VertxFactory factory = new VertxFactoryImpl(); final ClusterManager
     * manager = new HazelcastClusterManager(new Config());
     * options.setClusterManager(manager); factory.clusteredVertx(options,
     * res -> { if(res.succeeded()){ final Vertx vertx = res.result(); final
     * DeploymentOptions opts = OptionsReader.readOpts();
     * vertx.deployVerticle("com.lenovo.verticle.RouterVerticle", opts);
     * vertx.deployVerticle("com.lenovo.verticle.PollVerticle", opts);
     * log.info("Deploye RouterVerticle with cluster ok!"); } });
     */
    final VertxOptions options = new VertxOptions();
    options.setClustered(true);
    final DeploymentOptions opts = OptionsReader.readOpts();
    Consumer<Vertx> runner = vertx -> {
      vertx.deployVerticle("my.cluster.RootVerticle",opts);
      vertx.deployVerticle("my.cluster.PollVerticle",opts);
    };
    if (options.isClustered()) {
      Vertx.clusteredVertx(
        options,
        result -> {
          if (result.succeeded()) {
            Vertx vertx = result.result();
            runner.accept(vertx);
            log.info("pushservice is running with cluster by hazelcast.");
          } else {
            log.error("cluster running with error: "
              + result.cause().getMessage());
          }
        });
    } else {
      Vertx vertx = Vertx.vertx(options);
      runner.accept(vertx);
    }
  }

}
