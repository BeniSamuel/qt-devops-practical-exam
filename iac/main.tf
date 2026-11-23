locals {
  selected_vms = {
    for name in var.vm_names : name => var.vm_ids[name]
    if contains(var.enabled_vms, name)
  }
}

resource "proxmox_virtual_environment_vm" "exam" {
  for_each = local.selected_vms

  name      = each.key
  vm_id     = each.value
  node_name = var.node_name
  pool_id   = var.pool_id
  started   = true
  on_boot   = true
  tags      = ["exam-c4", "terraform"]

  clone {
    vm_id        = var.template_vmid
    full         = true
    datastore_id = var.datastore_id
  }

  cpu {
    cores   = var.cpu_cores
    sockets = 1
    type    = "x86-64-v2-AES"
  }

  memory {
    dedicated = var.memory_mb
    floating  = 0
  }

  disk {
    interface    = "scsi0"
    datastore_id = var.datastore_id
    size         = var.disk_size_gb
  }

  network_device {
    bridge = var.network_bridge
    model  = "virtio"
  }

  agent {
    enabled = true
    timeout = "1m"
  }

  initialization {
    datastore_id = var.datastore_id
    interface    = "ide2"

    ip_config {
      ipv4 {
        address = "dhcp"
      }
    }

    dns {
      servers = ["8.8.8.8", "8.8.4.4"]
    }

    user_account {
      username = var.vm_user
      keys     = [trimspace(var.ssh_public_key)]
      password = var.vm_password
    }
  }

  serial_device {}

  vga {
    type = "serial0"
  }
}
