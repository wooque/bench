package org.wooque.bench;

import java.sql.*;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.AbstractVerticle;

public class Bench extends AbstractVerticle
{   

  @Override
  public void start() {
    vertx.createHttpServer().requestHandler(r -> {
      r.response().end("Hello");
    }).listen(8080);
  }
  
  public static void main( String[] args ) throws ClassNotFoundException
  {
    Class.forName("org.postgresql.Driver");
    Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(32));
    vertx.deployVerticle(Bench.class.getName());
  }
}
