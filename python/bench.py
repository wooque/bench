from random import randint
import string
import os
from twisted.internet import reactor
from twisted.internet.defer import inlineCallbacks
from twisted.enterprise import adbapi
from cyclone.web import Application, RequestHandler

reactor.suggestThreadPoolSize(128)

letters = list(string.ascii_letters + ' ' + string.digits)
all_chars = string.maketrans('', '')
result_chars = ''.join(letters[b % len(letters)] for b in range(len(all_chars)))
trans = string.maketrans(all_chars, result_chars)

def randstr(size):
    return os.urandom(size).translate(trans)

class BenchEndpoint(RequestHandler):
    def initialize(self, db=None):
        self.db = db

    @inlineCallbacks
    def get(self):
        coin = randint(0, 9)
        if coin < 6:
            result = yield self.db.runQuery("SELECT title, thumb, nc, nv FROM tst ORDER BY id DESC LIMIT 10")
            data = result and [dict(title=r[0], thumb=r[1], nc=r[2], nv=r[3]) for r in result] or None
            resp = dict(list=data)

        elif coin < 8:
            title = randstr(140)
            thumb = randstr(140)
            nc = randint(0, 1000)
            nv = randint(0, 5000)
            yield self.db.runOperation("INSERT INTO tst(title, thumb, nc, nv) VALUES (%s, %s, %s, %s)", 
                                       (title, thumb, nc, nv,))
            resp = dict(action="insert", title=title, thumb=thumb, nc=nc, nv=nv)

        else:
            title = randstr(140)
            thumb = randstr(140)
            nc = randint(0, 1000)
            nv = randint(0, 5000)
            yield self.db.runOperation("UPDATE tst SET title=%s, thumb=%s, nc=%s, nv=%s "
                                       "WHERE id=(SELECT max(id) as m FROM tst LIMIT 1)",
                                       (title, thumb, nc, nv,))
            resp = dict(action="update", title=title, thumb=thumb, nc=nc, nv=nv)
        
        self.write(resp)


class Bench(Application):
    def __init__(self):
        self.db_pool = adbapi.ConnectionPool("psycopg2", host="127.0.0.1", database="bench",
                                             user="bench", password="bench", cp_max=32, cp_reconnect=True)

        handlers = [
            (r"/bench", BenchEndpoint, dict(db=self.db_pool)),
        ]
        Application.__init__(self, handlers, dict(xheaders=True))

    def log_request(self, handler):
        pass

    def stopFactory(self):
        self.db_pool.close()
