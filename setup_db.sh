#!/usr/bin/env bash
sudo -u postgres createdb bench
sudo -u postgres psql -c "CREATE USER bench WITH PASSWORD 'bench'"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE bench TO bench"
sudo -u postgres psql bench -c "CREATE TABLE IF NOT EXISTS tst (id SERIAL, txt TEXT)"
