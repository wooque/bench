from random import randint
import string
import os
import json

from twisted.internet import reactor
from twisted.internet.defer import inlineCallbacks, returnValue
from twisted.enterprise import adbapi
from klein import Klein

reactor.suggestThreadPoolSize(128)

letters = list(string.ascii_letters + ' ' + string.digits)
all_chars = string.maketrans('', '')
result_chars = ''.join(letters[b % len(letters)] for b in range(len(all_chars)))
trans = string.maketrans(all_chars, result_chars)


def randstr(size):
    return os.urandom(size).translate(trans)

db = adbapi.ConnectionPool("psycopg2", host="127.0.0.1", database="bench",
                           user="bench", password="bench", cp_max=32, cp_reconnect=True)
app = Klein()


@app.route("/bench")
@inlineCallbacks
def get(request):
    coin = randint(0, 9)
    if coin < 6:
        result = yield db.runQuery("SELECT title, thumb, nc, nv FROM tst ORDER BY id DESC LIMIT 10")
        data = result and [dict(title=r[0], thumb=r[1], nc=r[2], nv=r[3]) for r in result] or None
        resp = dict(list=data)

    elif coin < 8:
        title = randstr(140)
        thumb = randstr(140)
        nc = randint(0, 1000)
        nv = randint(0, 5000)
        yield db.runOperation("INSERT INTO tst(title, thumb, nc, nv) VALUES (%s, %s, %s, %s)",
                              (title, thumb, nc, nv,))
        resp = dict(action="insert", title=title, thumb=thumb, nc=nc, nv=nv)

    else:
        title = randstr(140)
        thumb = randstr(140)
        nc = randint(0, 1000)
        nv = randint(0, 5000)
        yield db.runOperation("UPDATE tst SET title=%s, thumb=%s, nc=%s, nv=%s "
                              "WHERE id=(SELECT max(id) as m FROM tst LIMIT 1)",
                              (title, thumb, nc, nv,))
        resp = dict(action="update", title=title, thumb=thumb, nc=nc, nv=nv)
    
    request.setHeader('Content-Type', 'application/json')
    returnValue(json.dumps(resp))

resource = app.resource

if __name__ == "__main__":
    app.run("localhost", 8080, logFile=open('/dev/null'))
