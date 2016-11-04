workers = 16
bind = "0.0.0.0:8080"
keepalive = 120
errorlog = '-'
pidfile = 'gunicorn.pid'
worker_class = "meinheld.gmeinheld.MeinheldWorker"


def post_fork(server, worker):
    import meinheld.server
    meinheld.server.set_access_logger(None)
