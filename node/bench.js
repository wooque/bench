var http = require('http');
var pg = require('pg').native;
var uuid = require('node-uuid');

var config = {
  user: 'bench',
  database: 'bench',
  password: 'bench',
  max: 32,
};

var pool = new pg.Pool(config);

function randint(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function query(callback) {
  pool.connect(function(err, client, done) {
    client.query('SELECT txt FROM tst LIMIT 1', [], function(err, result) {
      done();
      callback(result.rows[0]);
    });
  });
}

function insert(callback) {
  pool.connect(function(err, client, done) {
    var data = uuid.v1();
    client.query('INSERT INTO tst(txt) VALUES($1)', [data], function(err, result) {
      done();
      callback({txt: data});
    });
  });
}

function update(callback) {
  pool.connect(function(err, client, done) {
    var data = uuid.v1();
    client.query('UPDATE tst SET txt=$1 WHERE id in (SELECT id FROM tst LIMIT 1)', [data], function(err, result) {
      done();
      callback({txt: data});
    });
  });
}

function response(resp, data) {
  resp.writeHead(200, {'Content-Type': 'application/json'});
  resp.end(JSON.stringify(data));
}

function handler(req, res) {
  var coin = randint(0, 9);
  if (coin < 8) {
    query(function(data) { response(res, data); });
  } else if (coin < 9) {
    insert(function(data) { response(res, data); });
  } else {
    update(function(data) { response(res, data); });
  }
}

var server = http.createServer(handler);
server.listen(8080);
