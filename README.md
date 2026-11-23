# QT Global DevOps Practical Exam — MyDiary

The exam organizers confirmed that the brief's Go, MongoDB, Valkey, and Vite stack names are a typo. The supplied application in `app/` is MyDiary: Next.js 14, Spring Boot 3.5, PostgreSQL 16, and Redis 7. Its source README contains the application setup and test account details.

## Repository layout

- `iac/` — Terraform configuration for four verified Proxmox VMs.
- `ansible/` — openSUSE Kubernetes automation, clean reset playbook, and cgroup fix.
- `docker/` — Compose deployment and application Dockerfiles.
- `k8s/` — Kubernetes application manifests.
- `app/` — supplied MyDiary source.
- `monitoring/` — VM and application evidence captured during deployment.

SSH user is `devops`; VM addresses are `192.168.160.204` through `.207`. Passwords, private keys, API tokens, and the exam document must stay outside Git.

## Infrastructure status

[Terraform instructions](iac/README.md) document the inspected template and verified static VM outputs. All four VMs are in pool `exam-c4` with 4 vCPU, 4 GB RAM, 30 GB disks, and openSUSE Leap 16.0. The Docker host is `.204`; master `.205`; workers `.206` and `.207`. The clean Ansible bootstrap made all three Kubernetes nodes Ready, but the current pod stability gate still needs resolution: kube-proxy/Flannel restarts remain high.

## Docker Compose deployment

```text
Browser -> NGINX :80 ---- / ------> Next.js :3000
                    \---- /api/ ---> Spring Boot :8080
                                      |-- PostgreSQL :5432 (named volume)
                                      \-- Redis :6379 (optional cache)
```

Application images pushed to Docker Hub:

| Service | Image | Verified remote digest |
| --- | --- | --- |
| Frontend | `benisamuel/mydiary-frontend:exam-c4-v1` | `sha256:e6aa9289d5adb8b61de0ea00c53fbe60784664ec7d6a725138bbadcb967f3a26` |
| Backend | `benisamuel/mydiary-backend:exam-c4-v1` | `sha256:3504ded2431a1c38213a4ffe5f7a438c091aa1ad215a3b9435974c45f5e1b670` |

`docker/docker-compose.yml` also uses official NGINX 1.27, PostgreSQL 16, and Redis 7 images. Only NGINX publishes a host port. PostgreSQL data is in the `postgres_data` named volume. Redis is a cache, so it needs no persistent volume; the API reads authoritative data from PostgreSQL when Redis is unavailable.

Copy `docker/.env.example` to `docker/.env`, generate independent random values for `POSTGRES_PASSWORD`, `REDIS_PASSWORD`, and `JWT_SECRET` (at least 32 characters), and keep that file out of Git. Environment variables in the shell override `docker/.env`; remove conflicting variables before deployment. On the Docker VM, place `docker/` in `~/qt-mydiary/docker/`, then run:

```bash
cd ~/qt-mydiary/docker
docker compose --env-file .env pull frontend backend
docker compose --env-file .env up -d --no-build
docker compose --env-file .env ps
curl -i http://localhost/api/health
```

The Docker VM's OS and Docker installation steps will be documented after the first VM is inspected. Access from another machine will be `http://<docker-host-ip>/`.

### Local verification on 2026-11-23

- `docker compose config --quiet`: passed; all five required services resolved.
- Multi-stage frontend and backend builds: passed. The frontend runs the Next.js standalone server, and the backend packages `diary-api.jar` with Java 21.
- `GET /`: HTTP 200. `GET /api/health`: HTTP 200 with `{"status":"ok","database":"up","cache":"up"}`.
- Registered and logged in a new user, created and listed a diary entry, confirmed a USER receives HTTP 403 at `/api/admin/users`, and confirmed the seeded ADMIN can access it.
- Stopped Redis: `/api/health` reported `database=up, cache=down`; login and diary entry creation still worked. Redis was restarted.
- Pulled both application images back from Docker Hub, recreated the full stack with `--no-build`, and confirmed the frontend, health endpoint, and seeded admin login still worked. PostgreSQL retained its data across container recreation.
- Recreated the backend while Redis was stopped. Backend and frontend health checks passed, and `/api/health` reported `database=up, cache=down`. Redis was restored afterward.

These checks ran on the laptop's local Compose stack.

### Docker VM deployment

On `c2-docker-host` (`192.168.160.204`), openSUSE Leap 16.0 installed Docker 29.7.2 and Compose 2.33.1 from the `docker` and `docker-compose` zypper packages. Docker is enabled, and `devops` is in the `docker` group. The VM pulled the versioned images from Docker Hub and started all five services. Because SELinux is enforcing on this host, the NGINX bind mount uses the `:Z` relabel option. External verification returned HTTP 200 for `http://192.168.160.204/` and `{"status":"ok","database":"up","cache":"up"}` from `/api/health`.
