from random import randint
import string
import os
from psycopg2.pool import ThreadedConnectionPool
from bottle import Bottle, run

letters = list(string.ascii_letters + ' ' + string.digits)
all_chars = string.maketrans('', '')
result_chars = ''.join(letters[b % len(letters)] for b in range(len(all_chars)))
trans = string.maketrans(all_chars, result_chars)


def randstr(size):
    return os.urandom(size).translate(trans)

db = ThreadedConnectionPool(4, 4, host="127.0.0.1", database="bench", user="bench", password="bench")
app = Bottle()


@app.route('/bench')
def get():
    coin = randint(0, 9)
    conn = db.getconn()
    c = conn.cursor()
    if coin < 6:
        c.execute("SELECT title, thumb, nc, nv FROM tst ORDER BY id DESC LIMIT 10")
        result = c.fetchall()
        data = result and [dict(title=r[0], thumb=r[1], nc=r[2], nv=r[3]) for r in result] or None
        resp = dict(list=data)

    elif coin < 8:
        title = randstr(140)
        thumb = randstr(140)
        nc = randint(0, 1000)
        nv = randint(0, 5000)
        c.execute("INSERT INTO tst(title, thumb, nc, nv) VALUES (%s, %s, %s, %s)",
                  (title, thumb, nc, nv,))
        conn.commit()
        resp = dict(action="insert", title=title, thumb=thumb, nc=nc, nv=nv)

    else:
        title = randstr(140)
        thumb = randstr(140)
        nc = randint(0, 1000)
        nv = randint(0, 5000)
        c.execute("UPDATE tst SET title=%s, thumb=%s, nc=%s, nv=%s "
                  "WHERE id=(SELECT max(id) as m FROM tst LIMIT 1)",
                  (title, thumb, nc, nv,))
        conn.commit()
        resp = dict(action="update", title=title, thumb=thumb, nc=nc, nv=nv)
    c.close()
    db.putconn(conn)

    return resp

if __name__ == "__main__":
    run(port=8080, debug=False, quiet=True)
