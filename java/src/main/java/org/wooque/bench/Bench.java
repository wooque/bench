package org.wooque.bench;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

public class Bench extends AbstractVerticle {
    
    static private final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    static private String randomString(int len){
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++) {
            sb.append(Bench.chars.charAt(ThreadLocalRandom.current().nextInt(Bench.chars.length())));
        }
        return sb.toString();
    }
    
    private JDBCClient db;

    @Override
    public void start() {
        db = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", "jdbc:postgresql://127.0.0.1/bench?user=bench&password=bench")
                .put("driver_class", "org.postgresql.Driver")
                .put("max_pool_size", 32));

        vertx.createHttpServer().requestHandler(r -> db.getConnection(conn -> {
            final SQLConnection connection = conn.result();
            int coin = ThreadLocalRandom.current().nextInt(0, 10);
            
            if (coin < 6) {
                connection.query("SELECT title, thumb, nc, nv FROM tst ORDER BY id DESC LIMIT 10", rs -> {
                    List rows = rs.result().getRows();
                    connection.close();
                    String resp = new JsonObject().put("list", rows).toString();
                    r.response().end(resp);
                });
            
            } else if (coin < 8) {
                String title = Bench.randomString(140);
                String thumb = Bench.randomString(140);
                int nc = ThreadLocalRandom.current().nextInt(0, 1000);
                int nv = ThreadLocalRandom.current().nextInt(0, 5000);
                JsonArray data = new JsonArray().add(title).add(thumb).add(nc).add(nv);
                connection.updateWithParams("INSERT INTO tst(title, thumb, nc, nv) VALUES (?, ?, ?, ?)", data, rs -> {
                    rs.result();
                    connection.close();
                    String resp = new JsonObject().put("action", "insert").put("title", title)
                                                .put("thumb", thumb).put("nc", nc).put("nv", nv).toString();
                    r.response().end(resp);
                });
            
            } else {
                String title = Bench.randomString(140);
                String thumb = Bench.randomString(140);
                int nc = ThreadLocalRandom.current().nextInt(0, 1000);
                int nv = ThreadLocalRandom.current().nextInt(0, 5000);
                JsonArray data = new JsonArray().add(title).add(thumb).add(nc).add(nv);
                connection.updateWithParams("UPDATE tst SET title=?, thumb=?, nc=?, nv=? " + 
                                            "WHERE id=(SELECT max(id) FROM tst LIMIT 1)", data, rs -> {
                    rs.result();
                    connection.close();
                    String resp = new JsonObject().put("action", "update").put("title", title)
                                                .put("thumb", thumb).put("nc", nc).put("nv", nv).toString();
                    r.response().end(resp);
                });
            }
        })).listen(8080);
    }

    public static void main(String[] args) throws ClassNotFoundException {
        Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(128));
        vertx.deployVerticle(Bench.class.getName());
    }
}
