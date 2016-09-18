package main

import (
	"github.com/valyala/fasthttp"
	"encoding/json"
	"github.com/jackc/pgx"
	"github.com/satori/go.uuid"
	"math/rand"
	"time"
)

func main() {
	config := pgx.ConnConfig{Host: "127.0.0.1", Database: "bench", User: "bench", Password: "bench"}
	db, _ := pgx.NewConnPool(pgx.ConnPoolConfig{ConnConfig: config, MaxConnections: 32})
	defer db.Close()
	rand.Seed(time.Now().UTC().UnixNano())

	fasthttp.ListenAndServe(":8080", func(ctx *fasthttp.RequestCtx) {
	    var txt string
		coin := rand.Intn(10)
		if coin < 8 {
			err := db.QueryRow("SELECT txt FROM tst LIMIT 1").Scan(&txt)
			if err != nil {
				txt = ""
			}

		} else if coin < 8 {
			txt = uuid.NewV1().String()
			db.Exec("INSERT INTO tst(txt) VALUES ($1)", txt)

		} else {
			txt = uuid.NewV1().String()
			db.Exec("UPDATE tst SET txt=$1 WHERE id in (SELECT id FROM tst LIMIT 1)", txt)
		}
		json_resp, _ := json.Marshal(map[string]string{"txt": txt})
		ctx.Write(json_resp)
	})
}
