package org.wooque.bench;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

public class Bench extends AbstractVerticle {
    private JDBCClient db;

    @Override
    public void start() {
        db = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", "jdbc:postgresql:bench?user=bench&bench=secret")
                .put("driver_class", "org.postgresql.Driver")
                .put("max_pool_size", 128));

        vertx.createHttpServer().requestHandler(r -> db.getConnection(conn -> {
            final SQLConnection connection = conn.result();

            int coin = ThreadLocalRandom.current().nextInt(0, 4);
            String data;
            switch (coin) {
                case 0:
                    connection.query("SELECT txt FROM tst LIMIT 1", rs -> {
                        List rows = rs.result().getRows();
                        String resp;
                        if (rows.size() > 0) {
                            resp = rows.get(0).toString();
                        } else {
                            resp = new JsonObject().put("txt", "").toString();
                        }
                        connection.close();
                        r.response().end(resp);
                    });
                    break;
                case 1:
                    data = UUID.randomUUID().toString();
                    connection.updateWithParams("INSERT INTO tst(txt) VALUES (?)", new JsonArray().add(data), rs -> {
                        rs.result();
                        connection.close();
                        String resp = new JsonObject().put("txt", data).toString();
                        r.response().end(resp);
                    });
                    break;
                case 2:
                    data = UUID.randomUUID().toString();
                    connection.updateWithParams("UPDATE tst SET txt=? WHERE id=(SELECT id FROM tst LIMIT 1)", new JsonArray().add(data), rs -> {
                        rs.result();
                        connection.close();
                        String resp = new JsonObject().put("txt", data).toString();
                        r.response().end(resp);
                    });
                    break;
                case 3:
                    connection.query("DELETE FROM tst WHERE id=(SELECT id FROM tst LIMIT 1) RETURNING id", rs -> {
                        List rows = rs.result().getRows();
                        String resp;
                        if (rows.size() > 0) {
                            resp = rows.get(0).toString();
                        } else {
                            resp = new JsonObject().put("id", "").toString();
                        }
                        connection.close();
                        r.response().end(resp);
                    });
                    break;
                default:
                    data = new JsonObject().put("error", "Wrong coin").toString();
                    r.response().end(data);
            }
        })).listen(8080);
    }

    public static void main(String[] args) throws ClassNotFoundException {
        Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(128));
        vertx.deployVerticle(Bench.class.getName());
    }
}
