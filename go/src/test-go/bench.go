package main

import (
	"database/sql"
	"encoding/json"
	"fmt"
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
		var resp map[string]string
		coin := rand.Intn(4)
		if coin == 0 {
			var res string
			err := db.QueryRow("SELECT txt FROM tst LIMIT 1").Scan(&res)
			if err != nil {
				res = ""
			}
			resp = map[string]string{"txt": res}

		} else if coin == 1 {
			txt := uuid.NewV1().String()
			db.Exec("INSERT INTO tst(txt) VALUES ($1)", txt)
			resp = map[string]string{"txt": txt}

		} else if coin == 2 {
			txt := uuid.NewV1().String()
			db.Exec("UPDATE tst SET txt=$1 WHERE id in (SELECT id FROM tst LIMIT 1)", txt)
			resp = map[string]string{"txt": txt}

		} else {
			var res string
			err := db.QueryRow("DELETE FROM tst WHERE id in (SELECT id FROM tst LIMIT 1) RETURNING id").Scan(&res)
			if err != nil {
				res = ""
			}
			resp = map[string]string{"id": res}
		}
		json_resp, _ := json.Marshal(resp)
		fmt.Fprintln(w, string(json_resp))
	})
	http.ListenAndServe(":8080", nil)
}
