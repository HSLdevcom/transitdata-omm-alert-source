#!/bin/bash
DOCKER_BUILDKIT=1 docker build \
  --secret id=github_token,env=GITHUB_TOKEN \
  --build-arg GITHUB_ACTOR="${GITHUB_ACTOR:-github-actions}" \
  -t hsldevcom/transitdata-omm-alert-source .
