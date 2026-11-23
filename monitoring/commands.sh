#!/usr/bin/env bash
# Run one command at a time on the named VM, then save the screenshot with the filename above it.

# vm1=c2-docker-host (192.168.160.204)
# vm1-top.png
clear; hostname; top -bn1 | head -20
# vm1-df.png
clear; hostname; df -h
# vm1-free.png
clear; hostname; free -h
# vm1-uptime.png
clear; hostname; uptime
# vm1-ss.png
clear; hostname; sudo ss -tulpn
# vm1-ps.png
clear; hostname; ps aux --sort=-%cpu | head -20

# vm2=c2-k8s-master (192.168.160.205)
# vm2-top.png
clear; hostname; top -bn1 | head -20
# vm2-df.png
clear; hostname; df -h
# vm2-free.png
clear; hostname; free -h
# vm2-uptime.png
clear; hostname; uptime
# vm2-ss.png
clear; hostname; sudo ss -tulpn
# vm2-ps.png
clear; hostname; ps aux --sort=-%cpu | head -20

# vm3=c2-k8s-worker-1 (192.168.160.206)
# vm3-top.png
clear; hostname; top -bn1 | head -20
# vm3-df.png
clear; hostname; df -h
# vm3-free.png
clear; hostname; free -h
# vm3-uptime.png
clear; hostname; uptime
# vm3-ss.png
clear; hostname; sudo ss -tulpn
# vm3-ps.png
clear; hostname; ps aux --sort=-%cpu | head -20

# vm4=c2-k8s-worker-2 (192.168.160.207)
# vm4-top.png
clear; hostname; top -bn1 | head -20
# vm4-df.png
clear; hostname; df -h
# vm4-free.png
clear; hostname; free -h
# vm4-uptime.png
clear; hostname; uptime
# vm4-ss.png
clear; hostname; sudo ss -tulpn
# vm4-ps.png
clear; hostname; ps aux --sort=-%cpu | head -20

# Docker-only evidence on vm1
# docker-ps.png
clear; hostname; docker ps
# docker-stats.png
clear; hostname; docker stats --no-stream
# docker-volumes.png
clear; hostname; docker system df -v
# valkey-cache.png
clear; hostname; cd ~/qt-mydiary/docker && docker compose --env-file .env exec -T redis sh -lc 'redis-cli --no-auth-warning -a "$REDIS_PASSWORD" INFO keyspace; redis-cli --no-auth-warning -a "$REDIS_PASSWORD" INFO memory; redis-cli --no-auth-warning -a "$REDIS_PASSWORD" INFO stats' | grep -E '^(db[0-9]+:|used_memory_human:|keyspace_hits:|keyspace_misses:)'
# vm1-health.png
clear; hostname; curl -sS http://localhost/api/health; echo

# Kubernetes-only evidence on vm2
# k8s-nodes.png
clear; hostname; kubectl get nodes -o wide
# k8s-pods.png
clear; hostname; kubectl get pods -A -o wide
# k8s-health.png
clear; hostname; curl -sS http://192.168.160.205:30080/api/health; echo
