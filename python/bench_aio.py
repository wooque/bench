from uuid import uuid1
from random import randint
import asyncio
from aiohttp import web
import aiopg


class Handler:

    def __init__(self):
        self.pool = None
        
        async def create_pool():
            dsn = 'host=127.0.0.1 dbname=bench user=bench password=bench'
            self.pool = await aiopg.create_pool(dsn, minsize=32, maxsize=32)
            
        loop = asyncio.get_event_loop()
        loop.run_until_complete(create_pool())

    async def handle(self, request):
        coin = randint(0, 3)
        async with self.pool.acquire() as conn:
            async with conn.cursor() as cur:
                if coin == 0:
                    await cur.execute("SELECT txt FROM tst LIMIT 1")
                    result = []
                    async for row in cur:
                        result.append(row)
                      
                    if not result:
                        resp = None
                    else:
                        resp = result[0][0]
                    response = dict(txt=resp)

                elif coin == 1:
                    txt = str(uuid1())
                    await cur.execute("INSERT INTO tst(txt) VALUES (%s)", (txt,))
                    response = dict(txt=txt)

                elif coin == 2:
                    txt = str(uuid1())
                    await cur.execute("UPDATE tst SET txt=%s WHERE id in (SELECT id FROM tst LIMIT 1)", (txt,))
                    response = dict(txt=txt)

                else:
                    await cur.execute("DELETE FROM tst WHERE id in (SELECT id FROM tst LIMIT 1) RETURNING id")
                    result = []
                    async for row in cur:
                        result.append(row)
                      
                    if not result:
                        resp = None
                    else:
                        resp = result[0][0]
                    response = dict(id=resp)
                  
        return web.json_response(response)

handler = Handler()
app = web.Application()
app.router.add_route('GET', '/bench', handler.handle)
web.run_app(app)
