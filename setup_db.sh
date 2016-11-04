#!/usr/bin/env bash
sudo -u postgres createdb bench
sudo -u postgres psql -c "CREATE USER bench WITH PASSWORD 'bench'"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE bench TO bench"
PGPASSWORD=bench psql -h 127.0.0.1 -U bench bench -c "CREATE TABLE IF NOT EXISTS tst (id SERIAL, title TEXT, thumb TEXT, nc INTEGER, nv INTEGER)"
PGPASSWORD=bench psql -h 127.0.0.1 -U bench bench -c "CREATE INDEX ON tst(id desc)"
