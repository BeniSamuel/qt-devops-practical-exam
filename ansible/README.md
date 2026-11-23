# Ansible cluster setup

WSL is unavailable on the Windows control laptop (`E_ACCESSDENIED`). Docker is the working control environment. Build the derived image once; it adds the SSH client to `cytopia/ansible:latest` (Ansible Core 2.21.5). The inventory uses the ignored key at `/workspace/iac/id_ed25519_exam`.

```powershell
$repo = (Resolve-Path .).Path
docker build -t qt-ansible:exam-c4 ./ansible
docker run --rm -it -v "${repo}:/workspace" -w /workspace/ansible qt-ansible:exam-c4 ansible-playbook -i inventory.ini site.yml
```

The playbook targets openSUSE Leap 16, disables swap, configures kernel modules and sysctl, stops firewalld, installs containerd and Kubernetes packages, initializes the master, joins both workers, and applies Flannel. It is safe to rerun after a partial run. Verify with:

```powershell
docker run --rm -it -v "${repo}:/workspace" -w /workspace/ansible qt-ansible:exam-c4 ansible -i inventory.ini master -m shell -a 'kubectl --kubeconfig=/etc/kubernetes/admin.conf get nodes -o wide'
```

`reset.yml` is the destructive clean-cluster playbook. It is kept separate from `site.yml` and removes kubeadm, CNI, etcd, containers, and sandboxes before restarting containerd:

```powershell
docker run --rm -it -e ANSIBLE_HOST_KEY_CHECKING=False -v "${repo}:/workspace" -w /workspace/ansible qt-ansible:exam-c4 ansible-playbook -i inventory.ini reset.yml
```

Containerd's runc options persist `SystemdCgroup = true` before kubelet is started. `site.yml` guards initialization with `/etc/kubernetes/admin.conf` and worker joins with `/etc/kubernetes/kubelet.conf`.

Root cause note: the original version-1 `/etc/containerd/config.toml` was converted by containerd at startup, so editing its `SystemdCgroup` line did not change the effective runtime. Diagnosis compared `containerd config dump` with kubelet's `cgroupDriver: systemd`; the playbook now checks the effective dump, backs up and regenerates legacy config only when needed, sets `SystemdCgroup = true`, uses pause image `registry.k8s.io/pause:3.10`, restarts containerd through a handler, and asserts the effective setting.
