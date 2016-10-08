#!/usr/bin/env bash
sudo -u postgres createdb bench
sudo -u postgres psql -c "CREATE USER bench WITH PASSWORD 'bench'"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE bench TO bench"
PG_PASSWORD=bench psql -U bench bench -c "CREATE TABLE IF NOT EXISTS tst (id SERIAL, title TEXT, thumb TEXT, nc INTEGER, nv INTEGER)"
PG_PASSWORD=bench psql -U bench bench -c "CREATE TABLE INDEX ON tst(id desc)"
