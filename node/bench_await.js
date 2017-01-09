var http = require('http');
var pg = require('pg').native;
var cluster = require('cluster')

if (cluster.isMaster) {
    for (let i = 0; i < 2; i++) {
        cluster.fork();
    }
    return;
}

var config = {
  user: 'bench',
  database: 'bench',
  password: 'bench',
  max: 16,
};

var pool = new pg.Pool(config);

function randInt(max) {
  return Math.floor(Math.random() * (max + 1));
}

function randStr(length) {
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for(var i=0; i < length; i++)
        text += possible.charAt(Math.floor(Math.random() * possible.length));

    return text;
}

function db_connect(pool) {
    return new Promise(function(resolve, reject) {
        pool.connect(function(err, client, done) {
            if (!err) {
                resolve([client, done]);
            } else {
                reject(err);
            }
        });
    });
}

async function db_query(pool, query, params) {
    params = params || [];
    var [client, done] = await db_connect(pool);
    return new Promise(function(resolve, reject) {
        client.query(query, params, function(err, result) {
            done();
            if (!err) {
                resolve(result);
            } else {
                reject(err);
            }
        });
    });
}

function response(resp, data) {
  resp.writeHead(200, {'Content-Type': 'application/json'});
  resp.end(JSON.stringify(data));
}

async function handler(req, res) {
  var coin = randInt(9);
  if (coin < 6) {
    var result = await db_query(pool, 'SELECT * FROM tst ORDER BY id DESC LIMIT 10')
    response(res, {list: result.rows});
  
  } else if (coin < 8) {
    var title = randStr(140);
    var thumb = randStr(140);
    var nc = randInt(1000);
    var nv = randInt(5000);
    var result = await db_query(pool, 'INSERT INTO tst(title, thumb, nc, nv) VALUES($1, $2, $3, $4)', 
                                [title, thumb, nc, nv]);
    response(res, {action: "insert", title: title, thumb: thumb, nc: nc, nv: nv});
  
  } else {
    var title = randStr(140);
    var thumb = randStr(140);
    var nc = randInt(1000);
    var nv = randInt(5000);
    var result = await db_query(pool, 'UPDATE tst SET title=$1, thumb=$2, nc=$3, nv=$4 WHERE id=(SELECT max(id) FROM tst)', 
                                [title, thumb, nc, nv]);
    response(res, {action: "update", title: title, thumb: thumb, nc: nc, nv: nv});
  }
}

var server = http.createServer(handler);
server.listen(8080);
