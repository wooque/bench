package org.wooque.bench;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

public class Bench extends AbstractVerticle
{ 

  public JDBCClient db;

  @Override
  public void start() {
    db = JDBCClient.createShared(vertx, new JsonObject()
          .put("url", "jdbc:postgresql:bench?user=bench&bench=secret")
          .put("driver_class", "org.postgresql.Driver")
          .put("max_pool_size", 32));
          
    vertx.createHttpServer().requestHandler(r -> {
      db.getConnection(conn -> {
        final SQLConnection connection = conn.result();

        connection.queryWithParams("select * from tst where id = ?", new JsonArray().add(120451), rs -> {
          for (JsonArray line : rs.result().getResults()) {
            r.response().end(line.encode());
          }

          connection.close();
        });
      });
    }).listen(8080);
  }
  
  public static void main( String[] args ) throws ClassNotFoundException
  {
    Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(32));
    vertx.deployVerticle(Bench.class.getName());
  }
}
