variable "proxmox_endpoint" {
  description = "Proxmox API endpoint."
  type        = string
  default     = "https://192.168.160.16:8006/"
}

variable "proxmox_api_token_secret" {
  description = "API token secret. Supply only through TF_VAR_proxmox_api_token_secret."
  type        = string
  sensitive   = true
}

variable "node_name" {
  description = "Proxmox node hosting template 9004 and the clones."
  type        = string
  default     = "Server1"
}

variable "pool_id" {
  description = "Proxmox resource pool for the exam VMs."
  type        = string
  default     = "exam-c4"
}

variable "template_vmid" {
  description = "VMID of the openSUSE Leap 16 cloud-init template."
  type        = number
  default     = 9004
}

variable "datastore_id" {
  description = "Datastore for full clones, boot disks, and cloud-init disks."
  type        = string
  default     = "local-lvm"
}

variable "network_bridge" {
  description = "Proxmox bridge attached to the DHCP network."
  type        = string
  default     = "vmbr0"
}

variable "vm_names" {
  description = "All required VM names."
  type        = list(string)
  default = [
    "c2-docker-host",
    "c2-k8s-master",
    "c2-k8s-worker-1",
    "c2-k8s-worker-2",
  ]
}

variable "vm_ids" {
  description = "Reserved Proxmox VMIDs; confirmed free before the first apply."
  type        = map(number)
  default = {
    c2-docker-host  = 4201
    c2-k8s-master   = 4202
    c2-k8s-worker-1 = 4203
    c2-k8s-worker-2 = 4204
  }
}

variable "enabled_vms" {
  description = "Deploy only the Docker host first; add the three Kubernetes VMs after its OS and guest agent are checked."
  type        = set(string)
  default     = ["c2-docker-host"]
}

variable "cpu_cores" {
  description = "vCPU cores per VM."
  type        = number
  default     = 4
}

variable "memory_mb" {
  description = "Dedicated RAM per VM, in MB."
  type        = number
  default     = 4096
}

variable "disk_size_gb" {
  description = "Boot disk size in GB. Template disk is already 30 GB and must not be shrunk."
  type        = number
  default     = 30
  validation {
    condition     = var.disk_size_gb >= 30
    error_message = "disk_size_gb cannot be less than the template's 30 GB disk."
  }
}

variable "vm_user" {
  description = "Cloud-init SSH username."
  type        = string
  default     = "devops"
}

variable "ssh_public_key" {
  description = "SSH public key for the cloud-init user. Supply through TF_VAR_ssh_public_key."
  type        = string
  validation {
    condition     = startswith(var.ssh_public_key, "ssh-ed25519 ") || startswith(var.ssh_public_key, "ssh-rsa ")
    error_message = "ssh_public_key must be an OpenSSH public key."
  }
}

variable "vm_password" {
  description = "Cloud-init password. Supply only through TF_VAR_vm_password; Terraform state remains sensitive."
  type        = string
  sensitive   = true
  validation {
    condition     = length(var.vm_password) >= 12
    error_message = "vm_password must contain at least 12 characters."
  }
}
