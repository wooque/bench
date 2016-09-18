package main

import (
	"database/sql"
	"encoding/json"
	_ "github.com/lib/pq"
	"github.com/satori/go.uuid"
	"math/rand"
	"net/http"
	"time"
)

func main() {
	db, _ := sql.Open("postgres", "dbname=bench user=bench password=bench sslmode=disable")
	db.SetMaxOpenConns(32)
    defer db.Close()
	rand.Seed(time.Now().UTC().UnixNano())

	http.HandleFunc("/bench", func(w http.ResponseWriter, req *http.Request) {
		var txt string
		coin := rand.Intn(10)
		if coin < 8 {
			err := db.QueryRow("SELECT txt FROM tst LIMIT 1").Scan(&txt)
			if err != nil {
				txt = ""
			}

		} else if coin < 9 {
			txt = uuid.NewV1().String()
			db.Exec("INSERT INTO tst(txt) VALUES ($1)", txt)

		} else {
			txt = uuid.NewV1().String()
			db.Exec("UPDATE tst SET txt=$1 WHERE id in (SELECT id FROM tst LIMIT 1)", txt)
		}
        json_resp, _ := json.Marshal(map[string]string{"txt": txt})
		w.Write(json_resp)
	})
	http.ListenAndServe(":8080", nil)
}
