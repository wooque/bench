from uuid import uuid1
from random import randint
from twisted.internet.defer import inlineCallbacks
from twisted.enterprise import adbapi
from cyclone.web import Application, RequestHandler


class BenchEndpoint(RequestHandler):
    def initialize(self, db=None):
        self.db = db

    @inlineCallbacks
    def get(self):
        coin = randint(0, 3)
        if coin == 0:
            result = yield self.db.runQuery("SELECT txt FROM tst LIMIT 1")
            if not result:
                resp = None
            else:
                resp = result[0][0]
            self.write(dict(txt=resp))

        elif coin == 1:
            txt = str(uuid1())
            yield self.db.runOperation("INSERT INTO tst(txt) VALUES (%s)", (txt,))
            self.write(dict(txt=txt))

        elif coin == 2:
            txt = str(uuid1())
            yield self.db.runOperation("UPDATE tst SET txt=%s WHERE id in (SELECT id FROM tst LIMIT 1)", (txt,))
            self.write(dict(txt=txt))

        else:
            result = yield self.db.runQuery("DELETE FROM tst WHERE id in (SELECT id FROM tst LIMIT 1) RETURNING id")
            if not result:
                resp = None
            else:
                resp = result[0][0]
            self.write(dict(id=resp))


class Bench(Application):
    def __init__(self):
        self.db_pool = adbapi.ConnectionPool("psycopg2", host="127.0.0.1", database="bench",
                                             user="bench", password="bench", cp_reconnect=True)

        handlers = [
            (r"/bench", BenchEndpoint, dict(db=self.db_pool)),
        ]
        Application.__init__(self, handlers, dict(log_function=lambda x: None, xheaders=True))

    def log_request(self, handler):
        pass

    def stopFactory(self):
        self.db_pool.close()
