#!/usr/bin/env python2

import psycopg2

db = psycopg2.connect(host="127.0.0.1", database="bench", user="bench", password="bench")
cur = db.cursor()
cur.execute("CREATE TABLE IF NOT EXISTS tst (id SERIAL, txt TEXT)")
db.commit()
cur.close()
db.close()
