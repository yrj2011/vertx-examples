package my.cluster;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

/**
 * 接收消息然后向mongo中写入一条记录
 */
public class PollVerticle extends AbstractVerticle {

  public static final Logger log = LogManager.getLogger(PollVerticle.class);
  public static int count = 1;

  @Override
  public void start() throws Exception {
    //final MongoClient mongo = MongoClient.createShared(vertx, new JsonObject().put("db_name", "lenovodb").put("connection_string", "mongodb://192.168.56.201:27017"));
    final MongoClient mongo = MongoClient.createShared(vertx, config());

    EventBus eb = vertx.eventBus();
    eb.consumer("pushservice-poll",message ->{
      String msg = (String)message.body();
      log.info("received message: "+msg);
      JsonObject user = new JsonObject().put("id", count)
        .put("name", "vertx"+(count++))
        .put("age", "333")
        .put("mobile", "no");
      //插入用户信息
      mongo.insert("users", user,lookup->{
        if(lookup.failed()){
          log.error("insert user error: "+lookup.cause());
          return;
        }
      });
    });
  }

}
