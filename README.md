# QT Global DevOps Practical Exam — MyDiary

The exam organizers confirmed that the brief's Go, MongoDB, Valkey, and Vite stack names are a typo. The supplied application in `app/` is MyDiary: Next.js 14, Spring Boot 3.5, PostgreSQL 16, and Redis 7. Its source README contains the application setup and test account details.

## Repository layout

- `iac/` — four Proxmox VMs, pending template inspection.
- `ansible/` — Kubernetes cluster automation, pending the first VM's verified OS.
- `docker/` — Compose deployment and application Dockerfiles.
- `k8s/` — Kubernetes application manifests.
- `app/` — supplied MyDiary source.
- `monitoring/` — VM and application evidence captured during deployment.

Deployment URLs, VM IPs, image tags, verification output, and screenshot descriptions will be added after the corresponding tasks run. SSH username will be documented here; passwords, private keys, API tokens, and the exam document must stay outside Git.
