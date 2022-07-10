#!/usr/bin/env bash

gcloud compute ssh root@fogcomputing-server --command="docker compose stop"
