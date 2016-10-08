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
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;


public class Bench {
    
    static private final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    static private String randomString(int len){
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++) {
            sb.append(Bench.chars.charAt(ThreadLocalRandom.current().nextInt(Bench.chars.length())));
        }
        return sb.toString();
    }
    
    private static class Article {
        public final String title;
        public final String thumb;
        public final int nc;
        public final int nv;
        public final String action;
        
        Article(String title, String thumb, int nc, int nv, String action) {
            this.title = title;
            this.thumb = thumb;
            this.nc = nc;
            this.nv = nv;
            this.action = action;
        }
    }

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
                        
                        if (coin < 6) {
                            stmt = conn.prepareStatement("SELECT title, thumb, nc, nv FROM tst ORDER BY id DESC LIMIT 10");
                            rs = stmt.executeQuery();
                            ArrayList<Article> arts = new ArrayList<>();
                            while (rs.next()) {
                                arts.add(new Article(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4), null));
                            }
                            conn.close();
                            
                            HashMap<String, ArrayList<Article>> resp = new HashMap<>();
                            resp.put("list", arts);

                            data = new Gson().toJson(resp);

                        } else if (coin < 8) {
                            String title = Bench.randomString(140);
                            String thumb = Bench.randomString(140);
                            int nc = ThreadLocalRandom.current().nextInt(0, 1000);
                            int nv = ThreadLocalRandom.current().nextInt(0, 5000);
                            stmt = conn.prepareStatement("INSERT INTO tst(title, thumb, nc, nv) VALUES (?, ?, ?, ?)");
                            stmt.setString(1, title);
                            stmt.setString(2, thumb);
                            stmt.setInt(3, nc);
                            stmt.setInt(4, nv);
                            stmt.executeUpdate();
                            conn.close();
                            
                            Article resp = new Article(title, thumb, nc, nv, "insert");
                            data = new Gson().toJson(resp);

                        } else {
                            String title = Bench.randomString(140);
                            String thumb = Bench.randomString(140);
                            int nc = ThreadLocalRandom.current().nextInt(0, 1000);
                            int nv = ThreadLocalRandom.current().nextInt(0, 5000);
                            stmt = conn.prepareStatement("UPDATE tst SET title=?, thumb=?, nc=?, nv=? " + 
                                                        "WHERE id=(SELECT max(id) FROM tst LIMIT 1)");
                            stmt.setString(1, title);
                            stmt.setString(2, thumb);
                            stmt.setInt(3, nc);
                            stmt.setInt(4, nv);
                            stmt.executeUpdate();
                            conn.close();
                            
                            Article resp = new Article(title, thumb, nc, nv, "update");
                            data = new Gson().toJson(resp);
                        }
                        
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        exchange.getResponseSender().send(data);
                    }
                })
                .setWorkerThreads(200)
                .build();
        server.start();
    }
}
