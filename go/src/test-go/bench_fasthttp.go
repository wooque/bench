package main

import (
	"fmt"
	"strconv"
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
		var resp map[string]string
		coin := rand.Intn(10)
		if coin < 5 {
			var res string
			err := db.QueryRow("SELECT txt FROM tst LIMIT 1").Scan(&res)
			if err != nil {
				res = ""
			}
			resp = map[string]string{"txt": res}

		} else if coin < 7 {
			txt := uuid.NewV1().String()
			db.Exec("INSERT INTO tst(txt) VALUES ($1)", txt)
			resp = map[string]string{"txt": txt}

		} else if coin < 9 {
			txt := uuid.NewV1().String()
			db.Exec("UPDATE tst SET txt=$1 WHERE id in (SELECT id FROM tst LIMIT 1)", txt)
			resp = map[string]string{"txt": txt}

		} else {
			var res string
			var resint int
			err := db.QueryRow("DELETE FROM tst WHERE id in (SELECT id FROM tst LIMIT 1) RETURNING id").Scan(&resint)
			if err != nil {
				res = ""
			} else {
				res = strconv.Itoa(resint)
			}
			resp = map[string]string{"id": res}
		}
		json_resp, _ := json.Marshal(resp)
		fmt.Fprintln(ctx, string(json_resp))
	})
}
