#!/usr/bin/env bash

gcloud compute instances delete fogcomputing-server --quiet
gcloud compute firewall-rules delete fogcomputing-server-fw-allow-incoming --quiet
gcloud compute networks delete fogcomputing-server-network --quiet
