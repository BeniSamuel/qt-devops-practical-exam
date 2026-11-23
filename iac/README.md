# Proxmox Terraform infrastructure

Template 9004 was confirmed through the Proxmox API as `opensuse-leap-16`: 30 GB `scsi0` on `local-lvm`, cloud-init on `ide2`, guest-agent flag enabled, CPU type `x86-64-v2-AES`, and `vmbr0` networking. The template's 2 cores and 2048 MB are overridden to 4 cores and 4096 MB, while its CPU type and serial console are retained. VMIDs 4201–4204 were free before the first apply.

The default `enabled_vms` creates **only `c2-docker-host`**. After that VM's OS and SSH access are verified, select all four names for the next plan. Do not use `-target` for the remaining VMs.

## Sensitive local inputs

- Set `TF_VAR_proxmox_api_token_secret` in the shell from the exam credential; never put it in a tracked file.
- The generated `devops` password is in ignored `iac/.env` as `TF_VAR_vm_password`. Load it into the process environment without printing it. The password and Terraform state must not be committed.
- The dedicated SSH key is in ignored `iac/id_ed25519_exam`; the public key is `iac/id_ed25519_exam.pub`. Provide the private key to examiners through a separate secure channel, not Git.
- Terraform state is local and contains the VM password. Keep `iac/terraform.tfstate*` private and backed up securely.

## First VM commands from PowerShell

Terraform 1.17.2 was installed using `winget install Hashicorp.Terraform`. Windows Application Control blocks the downloaded `bpg/proxmox` provider executable on this laptop, so the successful init, validate, plan, and apply used the official Linux Terraform container. The Docker engine must be running and able to reach `192.168.160.16:8006`.

```powershell
$env:TF_VAR_proxmox_api_token_secret = '<read securely from the exam credential>'
$env:TF_VAR_vm_password = (Get-Content iac/.env | Where-Object { $_ -like 'TF_VAR_vm_password=*' }) -replace '^TF_VAR_vm_password=', ''
$env:TF_VAR_ssh_public_key = (Get-Content iac/id_ed25519_exam.pub -Raw).Trim()
$iacPath = (Resolve-Path iac).Path
$dockerArgs = @('--rm', '--mount', "type=bind,source=$iacPath,target=/workspace", '-w', '/workspace', '-e', 'TF_VAR_proxmox_api_token_secret', '-e', 'TF_VAR_vm_password', '-e', 'TF_VAR_ssh_public_key', 'hashicorp/terraform:1.17.2')
docker run @dockerArgs init -backend=false -input=false
docker run @dockerArgs validate
docker run @dockerArgs plan -input=false -out first-vm.tfplan
docker run @dockerArgs apply -input=false first-vm.tfplan
```

Review the plan before applying. Terraform plan and state files are ignored because they contain secrets. The first run produced `Plan: 1 to add, 0 to change, 0 to destroy` and `Apply complete! Resources: 1 added, 0 changed, 0 destroyed` for VMID 4201.

## Verification and IP fallback

```powershell
docker run @dockerArgs output vm_ids
docker run @dockerArgs output vm_ips
docker run @dockerArgs output vm_guest_ips
docker run @dockerArgs output vm_macs
ssh -i iac/id_ed25519_exam devops@<docker-host-ip> hostname
ssh -i iac/id_ed25519_exam devops@<docker-host-ip> 'cat /etc/os-release'
ssh -i iac/id_ed25519_exam devops@<docker-host-ip> 'systemctl is-active qemu-guest-agent'
```

The first apply inherited Proxmox's `qemu64` CPU default and did not boot correctly. Terraform now sets the template's `x86-64-v2-AES` CPU type. The next clone booted and its QEMU guest agent confirmed `/etc/os-release` as openSUSE Leap 16.0, but DHCP did not assign `eth0` an IPv4 address. Terraform now sets static cloud-init addresses: Docker host `.204`, master `.205`, workers `.206` and `.207`, all under `192.168.160.0/24` with gateway `.1`. VM 4201 was replaced again with this setting. `vm_ips` reports the configured addresses from `vm_ips_static`; `vm_guest_ips` reports what the guest agent actually observes. Both now show `192.168.160.204` for the Docker host.

After the two-minute wait, `192.168.160.204` answered ping (2/2), TCP port 22 opened, and key-based SSH as `devops` succeeded. SSH confirmed hostname `c2-docker-host`, `eth0` address `192.168.160.204/24`, openSUSE Leap 16.0, and active `qemu-guest-agent`. Only VM 4201 exists so far.

After the first VM is fully verified, set `TF_VAR_enabled_vms` to a JSON array of all four names, review a new plan showing only three additions, and apply it. Confirm all four VM IDs, pool membership, disk sizes, hostnames, SSH access, and guest IPs before calling Task 1 complete.
