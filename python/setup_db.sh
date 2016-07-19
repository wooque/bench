#!/usr/bin/env bash
sudo -u postgres createdb bench
sudo -u postgres psql -c "CREATE USER bench WITH PASSWORD 'bench'"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE bench TO bench"
./init_db.py
