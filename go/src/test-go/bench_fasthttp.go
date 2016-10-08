package main

import (
    "github.com/valyala/fasthttp"
    "encoding/json"
    "github.com/jackc/pgx"
    "math/rand"
    "strconv"
    "time"
)

const letterBytes = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

func RandString(n int) string {
    b := make([]byte, n)
    for i := range b {
        b[i] = letterBytes[rand.Intn(len(letterBytes))]
    }
    return string(b)
}

func main() {
    config := pgx.ConnConfig{Host: "127.0.0.1", Database: "bench", User: "bench", Password: "bench"}
    db, _ := pgx.NewConnPool(pgx.ConnPoolConfig{ConnConfig: config, MaxConnections: 32})
    defer db.Close()
    rand.Seed(time.Now().UTC().UnixNano())

    fasthttp.ListenAndServe(":8080", func(ctx *fasthttp.RequestCtx) {
        var json_resp []byte
        var title, thumb string
        var nc, nv int
        coin := rand.Intn(10)
        if coin < 6 {
            rows, err := db.Query("SELECT title, thumb, nc, nv FROM tst ORDER BY id LIMIT 10")
            var resp map[string][]map[string]string
            if err != nil {
                resp = map[string][]map[string]string{}
            } else {
                var data []map[string]string
                for rows.Next() {
                    rows.Scan(&title, &thumb, &nc, &nv)
                    data = append(data, map[string]string{"title": title, "thumb": thumb, 
                                                            "nc": strconv.Itoa(nc), "nv": strconv.Itoa(nv)})
                    
                }
                resp = map[string][]map[string]string{"list": data}
            }
            json_resp, _ = json.Marshal(resp)

        } else if coin < 8 {
            title = RandString(140)
            thumb = RandString(140)
            nc = rand.Intn(1000)
            nv = rand.Intn(5000)
            db.Exec("INSERT INTO tst(title, thumb, nc, nv) VALUES ($1, $2, $3, $4)", title, thumb, nc, nv)
            resp := map[string]string{"action": "insert", "title": title, "thumb": thumb, 
                                      "nc": strconv.Itoa(nc), "nv": strconv.Itoa(nv)}
            json_resp, _ = json.Marshal(resp)

        } else {
            title = RandString(140)
            thumb = RandString(140)
            nc = rand.Intn(1000)
            nv = rand.Intn(5000)
            db.Exec(`UPDATE tst SET title=$1, thumb=$2, nc=$3, nv=$4 
                    WHERE id=(SELECT max(id) FROM tst LIMIT 1)`, title, thumb, nc, nv)
            resp := map[string]string{"action": "update", "title": title, "thumb": thumb, 
                                      "nc": strconv.Itoa(nc), "nv": strconv.Itoa(nv)}
            json_resp, _ = json.Marshal(resp)
        }
        ctx.Write(json_resp)
    })
}
