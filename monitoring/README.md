# Task 5 monitoring screenshots

Run one command at a time from `commands.sh` on the specified VM. Every command begins with `clear; hostname;` so the real hostname is visible. Capture screenshots while both deployments and the Kubernetes cluster are running.

| VM | Hostname | IP |
| --- | --- | --- |
| vm1 | c2-docker-host | 192.168.160.204 |
| vm2 | c2-k8s-master | 192.168.160.205 |
| vm3 | c2-k8s-worker-1 | 192.168.160.206 |
| vm4 | c2-k8s-worker-2 | 192.168.160.207 |

The Redis command reads `REDIS_PASSWORD` inside the running container through Compose; it does not print or embed the password.

| Screenshot | Target | Command | What the captured image shows |
| --- | --- | --- | --- |
| `vm1-top.png` | vm1 | `clear; hostname; top -bn1 \| head -20` | Docker host CPU, memory, load averages, and highest-activity processes. |
| `vm1-df.png` | vm1 | `clear; hostname; df -h` | Docker host filesystem capacity, used space, and available space. |
| `vm1-free.png` | vm1 | `clear; hostname; free -h` | Docker host total, used, free, and available memory. |
| `vm1-uptime.png` | vm1 | `clear; hostname; uptime` | Docker host uptime and 1/5/15-minute load averages. |
| `vm1-ss.png` | vm1 | `clear; hostname; sudo ss -tulpn` | Docker host listening TCP/UDP ports and owning services. |
| `vm1-ps.png` | vm1 | `clear; hostname; ps aux --sort=-%cpu \| head -20` | Docker host processes sorted by CPU usage. |
| `vm2-top.png` | vm2 | `clear; hostname; top -bn1 \| head -20` | Control-plane CPU, memory, load averages, and highest-activity processes. |
| `vm2-df.png` | vm2 | `clear; hostname; df -h` | Control-plane filesystem capacity, used space, and available space. |
| `vm2-free.png` | vm2 | `clear; hostname; free -h` | Control-plane total, used, free, and available memory. |
| `vm2-uptime.png` | vm2 | `clear; hostname; uptime` | Control-plane uptime and 1/5/15-minute load averages. |
| `vm2-ss.png` | vm2 | `clear; hostname; sudo ss -tulpn` | Control-plane listening ports including Kubernetes services. |
| `vm2-ps.png` | vm2 | `clear; hostname; ps aux --sort=-%cpu \| head -20` | Control-plane processes sorted by CPU usage. |
| `vm3-top.png` | vm3 | `clear; hostname; top -bn1 \| head -20` | Worker 1 CPU, memory, load averages, and highest-activity processes. |
| `vm3-df.png` | vm3 | `clear; hostname; df -h` | Worker 1 filesystem capacity, used space, and available space. |
| `vm3-free.png` | vm3 | `clear; hostname; free -h` | Worker 1 total, used, free, and available memory. |
| `vm3-uptime.png` | vm3 | `clear; hostname; uptime` | Worker 1 uptime and 1/5/15-minute load averages. |
| `vm3-ss.png` | vm3 | `clear; hostname; sudo ss -tulpn` | Worker 1 listening ports and Kubernetes node services. |
| `vm3-ps.png` | vm3 | `clear; hostname; ps aux --sort=-%cpu \| head -20` | Worker 1 processes sorted by CPU usage. |
| `vm4-top.png` | vm4 | `clear; hostname; top -bn1 \| head -20` | Worker 2 CPU, memory, load averages, and highest-activity processes. |
| `vm4-df.png` | vm4 | `clear; hostname; df -h` | Worker 2 filesystem capacity, used space, and available space. |
| `vm4-free.png` | vm4 | `clear; hostname; free -h` | Worker 2 total, used, free, and available memory. |
| `vm4-uptime.png` | vm4 | `clear; hostname; uptime` | Worker 2 uptime and 1/5/15-minute load averages. |
| `vm4-ss.png` | vm4 | `clear; hostname; sudo ss -tulpn` | Worker 2 listening ports and Kubernetes node services. |
| `vm4-ps.png` | vm4 | `clear; hostname; ps aux --sort=-%cpu \| head -20` | Worker 2 processes sorted by CPU usage. |
| `docker-ps.png` | vm1 | `clear; hostname; docker ps` | Running Compose containers, images, ports, and health status. |
| `docker-stats.png` | vm1 | `clear; hostname; docker stats --no-stream` | Per-container CPU, memory, network I/O, and process counts. |
| `docker-volumes.png` | vm1 | `clear; hostname; docker system df -v` | Docker images, containers, and PostgreSQL named-volume disk usage. |
| `valkey-cache.png` | vm1 | See `commands.sh` | Authenticated Redis cache keys, memory use, keyspace hits, and keyspace misses on `c2-docker-host`. |
| `vm1-health.png` | vm1 | `clear; hostname; curl -sS http://localhost/api/health; echo` | Compose health JSON showing database and cache state. |
| `k8s-nodes.png` | vm2 | `clear; hostname; kubectl get nodes -o wide` | Kubernetes node readiness, roles, addresses, versions, and runtimes. |
| `k8s-pods.png` | vm2 | `clear; hostname; kubectl get pods -A -o wide` | Pod readiness, status, restarts, IPs, and node placement. |
| `k8s-health.png` | vm2 | `clear; hostname; curl -sS http://192.168.160.205:30080/api/health; echo` | Kubernetes application health JSON showing database and cache state. |
