#!/usr/bin/env bash

# Assumption: Gcloud sdk is configured, logged in and default region, zone is set

# Create ssh keyfile if not exists
#if [ ! -f "${KEY_FILE:=id_ed25519}" ]; then
#  ssh-keygen -f $KEY_FILE -t ed25519 -C "$(whoami)" -N ""
#  gcloud compute project-info add-metadata \
#    --metadata ssh-keys="$(gcloud compute project-info describe \
#    --format="value(commonInstanceMetadata.items.filter(key:ssh-keys).firstof(value))")
#    $(whoami):$(cat $KEY_FILE.pub)"
#fi

# Create network
gcloud compute networks create fogcomputing-server-network

# Create server instance on Ubuntu 22.04
gcloud compute instances create fogcomputing-server \
  --tags=fogcomputing \
  --machine-type=e2-standard-2 \
  --boot-disk-size=30GB \
  --network=fogcomputing-server-network \
  --image-project=ubuntu-os-cloud --image-family=ubuntu-2204-lts

# Configure firewall and allow incoming ssh and port 5000
gcloud compute firewall-rules create fogcomputing-server-fw-allow-incoming \
  --allow tcp:22,tcp:5000 \
  --network=fogcomputing-server-network \
  --source-ranges 0.0.0.0/0

gcloud compute scp install-docker.sh root@fogcomputing-server:~/install-docker.sh
gcloud compute scp stop-container.sh root@fogcomputing-server:~/stop-container.sh

gcloud compute ssh root@fogcomputing-server --command="sh ~/install-docker.sh"
