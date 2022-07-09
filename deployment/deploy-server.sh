#!/usr/bin/env bash

gcloud compute scp ../server/docker-compose.yml root@fogcomputing-server:~/docker-compose.yml
gcloud compute ssh root@fogcomputing-server --command="cd ~ && docker compose pull && docker compose up -d --no-build"
