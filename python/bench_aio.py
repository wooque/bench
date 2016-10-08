from random import randint
import os
import asyncio
from aiohttp import web
import aiopg

trans = bytes.maketrans(bytearray(range(256)),
                        bytearray([ord(b'a') + b % 26 for b in range(256)]))

def randstr(size):
    return os.urandom(size).translate(trans).decode('ascii')


class Handler:

    def __init__(self):
        self.pool = None
        
        async def create_pool():
            dsn = 'host=127.0.0.1 dbname=bench user=bench password=bench'
            self.pool = await aiopg.create_pool(dsn, minsize=32, maxsize=32)
            
        loop = asyncio.get_event_loop()
        loop.run_until_complete(create_pool())

    async def handle(self, request):
        coin = randint(0, 9)
        async with self.pool.acquire() as conn:
            async with conn.cursor() as cur:
                if coin < 6:
                    await cur.execute("SELECT title, thumb, nc, nv FROM tst ORDER BY id DESC LIMIT 10")
                    result = []
                    async for row in cur:
                        result.append(dict(title=row[0], thumb=row[1], nc=row[2], nv=row[3]))
                    resp = dict(list=result)

                elif coin < 8:
                    title = randstr(140)
                    thumb = randstr(140)
                    nc = randint(0, 1000)
                    nv = randint(0, 5000)
                    await cur.execute("INSERT INTO tst(title, thumb, nc, nv) VALUES (%s, %s, %s, %s)", 
                                      (title, thumb, nc, nv,))
                    resp = dict(action="insert", title=title, thumb=thumb, nc=nc, nv=nv)

                else:
                    title = randstr(140)
                    thumb = randstr(140)
                    nc = randint(0, 1000)
                    nv = randint(0, 5000)
                    await cur.execute("UPDATE tst SET title=%s, thumb=%s, nc=%s, nv=%s "
                                      "WHERE id=(SELECT max(id) as m FROM tst LIMIT 1)", (title, thumb, nc, nv,))
                    resp = dict(action="update", title=title, thumb=thumb, nc=nc, nv=nv)
                          
        return web.json_response(resp)

handler = Handler()
app = web.Application()
app.router.add_route('GET', '/bench', handler.handle)
web.run_app(app)
