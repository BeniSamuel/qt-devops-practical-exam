locals {
  vm_ips = {
    for name, vm in proxmox_virtual_environment_vm.exam : name => var.vm_ips_static[name]
  }
}

output "vm_ips" {
  description = "Static IPv4 addresses configured for the selected VMs through cloud-init."
  value       = local.vm_ips
}

output "vm_guest_ips" {
  description = "IPv4 addresses actually reported by the QEMU guest agent, for verification."
  value = {
    for name, vm in proxmox_virtual_environment_vm.exam : name => try([
      for ip in flatten(vm.ipv4_addresses) : ip
      if !startswith(ip, "127.") && !startswith(ip, "169.254.")
    ][0], null)
  }
}

output "vm_macs" {
  description = "MAC addresses for network troubleshooting."
  value = {
    for name, vm in proxmox_virtual_environment_vm.exam : name => try(vm.network_device[0].mac_address, null)
  }
}

output "vm_ids" {
  description = "VMIDs created in this apply."
  value = {
    for name, vm in proxmox_virtual_environment_vm.exam : name => vm.vm_id
  }
}

output "c2_docker_host_ip" {
  value = try(local.vm_ips["c2-docker-host"], null)
}

output "c2_k8s_master_ip" {
  value = try(local.vm_ips["c2-k8s-master"], null)
}

output "c2_k8s_worker_1_ip" {
  value = try(local.vm_ips["c2-k8s-worker-1"], null)
}

output "c2_k8s_worker_2_ip" {
  value = try(local.vm_ips["c2-k8s-worker-2"], null)
}
