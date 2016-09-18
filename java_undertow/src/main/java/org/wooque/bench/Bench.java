package org.wooque.bench;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariDataSource;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


public class Bench {

    public static void main(final String[] args) {
        final HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://127.0.0.1:5432/bench");
        ds.setMaximumPoolSize(32);
        ds.setUsername("bench");
        ds.setPassword("bench");

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "0.0.0.0")
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) throws Exception {
                        if (exchange.isInIoThread()) {
                            exchange.dispatch(this);
                            return;
                        }
                        final Connection conn = ds.getConnection();
                        int coin = ThreadLocalRandom.current().nextInt(0, 10);
                        String data;
                        PreparedStatement stmt;
                        ResultSet rs;
                        if (coin < 8) {
                                stmt = conn.prepareStatement("SELECT txt FROM tst LIMIT 1");
                                rs = stmt.executeQuery();
                                if (rs.next()) {
                                    data = rs.getString(1);
                                } else {
                                    data = "";
                                }

                        } else if (coin < 7) {
                                data = UUID.randomUUID().toString();
                                stmt = conn.prepareStatement("INSERT INTO tst(txt) VALUES (?)");
                                stmt.setString(1, data);
                                stmt.executeUpdate();

                        } else {
                                data = UUID.randomUUID().toString();
                                stmt = conn.prepareStatement("UPDATE tst SET txt=? WHERE id=(SELECT id FROM tst LIMIT 1)");
                                stmt.setString(1, data);
                                stmt.executeUpdate();
                        }
                        HashMap<String, String> resp = new HashMap<>();
                        resp.put("txt", data);

                        conn.close();
                        data = new Gson().toJson(resp);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        exchange.getResponseSender().send(data);
                    }
                })
                .setWorkerThreads(200)
                .build();
        server.start();
    }
}
