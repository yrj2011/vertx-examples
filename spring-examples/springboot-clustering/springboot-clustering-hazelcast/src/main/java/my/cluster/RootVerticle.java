package my.cluster;

/**
 * 设置监听，请求路由，接收请求然后发送消息到PollVerticle
 */
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class RootVerticle extends AbstractVerticle {
  public static final Logger log = LogManager.getLogger(RootVerticle.class);
  public static  int PORT = 8080;

  public void start(Future<Void> future){

    final HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);

    router.route("/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.putHeader("content-type", "text/html");
      response.end("<h2>Hello from vertx cluster.</h2>");
    });

    //接收上报消息
    router.routeWithRegex("/pushservice/*.*/poll*").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      boolean hastype = routingContext.request().params().contains("type");
      if(hastype){
        //使用eventbus发送消息
        EventBus eb = vertx.eventBus();
        eb.publish("pushservice-poll", "datareport");
      }else{
        //
      }

      response.putHeader("content-type", "application/json");

      response.end(new JsonObject().put("code", 200).put("status", "ok").toString());
    });
    router.routeWithRegex("/pushservice/*.*/appfeedback*").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      String lpsst = routingContext.request().getParam("lpsst");
      System.out.println("lpsst get: " + lpsst);
      String body = routingContext.getBodyAsString();
      System.out.println(body);
      response.putHeader("content-type", "text/html");
      response.end("<h2>feedback ok!</h2>");

    });
    router.routeWithRegex("/pushservice/*.*/appinfo*").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.putHeader("content-type", "text/html");
      response.end("<h2>appinfo ok!</h2>");

    });


    server.requestHandler(router::accept);

    server.listen(config().getInteger("http.port", PORT), result -> {
      if(result.succeeded()){
        future.complete();
      }else{
        future.fail(result.cause());
      }
    });
    log.info("httpserver listenning on port : "+config().getInteger("http.port",PORT));
  }
}
