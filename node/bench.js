var http = require('http');
var pg = require('pg').native;

var config = {
  user: 'bench',
  database: 'bench',
  password: 'bench',
  max: 32,
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

function query(callback) {
  pool.connect(function(err, client, done) {
    client.query('SELECT * FROM tst ORDER BY id DESC LIMIT 10', [], function(err, result) {
      done();
      callback({list: result.rows});
    });
  });
}

function insert(callback) {
  pool.connect(function(err, client, done) {
    var title = randStr(140);
    var thumb = randStr(140);
    var nc = randInt(1000);
    var nv = randInt(5000);
    client.query('INSERT INTO tst(title, thumb, nc, nv) VALUES($1, $2, $3, $4)', 
        [title, thumb, nc, nv], 
        function(err, result) {
            done();
            callback({action: "insert", title: title, thumb: thumb, nc: nc, nv: nv});
        });
   });
}

function update(callback) {
  pool.connect(function(err, client, done) {
    var title = randStr(140);
    var thumb = randStr(140);
    var nc = randInt(1000);
    var nv = randInt(5000);
    client.query('UPDATE tst SET title=$1, thumb=$2, nc=$3, nv=$4 WHERE id=(SELECT max(id) FROM tst)', 
        [title, thumb, nc, nv], 
        function(err, result) {
            done();
            callback({action: "update", title: title, thumb: thumb, nc: nc, nv: nv});
        });
   });
}

function response(resp, data) {
  resp.writeHead(200, {'Content-Type': 'application/json'});
  resp.end(JSON.stringify(data));
}

function handler(req, res) {
  var coin = randInt(9);
  if (coin < 6) {
    query(function(data) { response(res, data); });
  } else if (coin < 8) {
    insert(function(data) { response(res, data); });
  } else {
    update(function(data) { response(res, data); });
  }
}

var server = http.createServer(handler);
server.listen(8080);
