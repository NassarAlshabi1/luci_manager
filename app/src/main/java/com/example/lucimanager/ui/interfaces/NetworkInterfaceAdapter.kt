package com.example.lucimanager.ui.interfaces

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lucimanager.R
import com.example.lucimanager.databinding.ItemInterfaceBinding
import com.example.lucimanager.model.NetworkInterface

class NetworkInterfaceAdapter(
    private val onToggleClick: (String, Boolean) -> Unit
) : ListAdapter<NetworkInterface, NetworkInterfaceAdapter.InterfaceViewHolder>(InterfaceDiffCallback()) {

    private var loadingInterface: String? = null
    
    fun setLoadingInterface(interfaceName: String?) {
        val oldLoadingInterface = loadingInterface
        loadingInterface = interfaceName

        // Find and update the old and new loading items
        val items = currentList
        oldLoadingInterface?.let { name ->
            items.indexOfFirst { it.name == name }.takeIf { it != -1 }?.let { notifyItemChanged(it) }
        }
        loadingInterface?.let { name ->
            items.indexOfFirst { it.name == name }.takeIf { it != -1 }?.let { notifyItemChanged(it) }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterfaceViewHolder {
        val binding = ItemInterfaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InterfaceViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: InterfaceViewHolder, position: Int) {
        holder.bind(getItem(position), loadingInterface == getItem(position).name, onToggleClick)
    }
    
    class InterfaceViewHolder(
        private val binding: ItemInterfaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(
            networkInterface: NetworkInterface,
            isLoading: Boolean,
            onToggleClick: (String, Boolean) -> Unit
        ) {
            binding.interfaceName.text = networkInterface.displayName
            
            // Set status
            binding.statusText.text = if (networkInterface.isActive) {
                binding.root.context.getString(R.string.status_active)
            } else {
                binding.root.context.getString(R.string.status_inactive)
            }
            
            // Simple status indicator
            if (networkInterface.isActive) {
                binding.statusIndicator.setBackgroundColor(binding.root.context.getColor(R.color.md_theme_primary))
            } else {
                binding.statusIndicator.setBackgroundColor(binding.root.context.getColor(R.color.dark_gray))
            }
            
            // Set additional info if available
            val additionalInfo = buildString {
                networkInterface.ipAddress?.let { ip -> append(ip) }
                networkInterface.device?.let { device -> 
                    if (isNotEmpty()) append(" • ")
                    append(device)
                }
            }
            
            if (additionalInfo.isNotEmpty()) {
                binding.additionalInfo.text = additionalInfo
                binding.additionalInfo.visibility = View.VISIBLE
            } else {
                binding.additionalInfo.visibility = View.GONE
            }
            
            // Set interface icon
            binding.interfaceIcon.setImageResource(R.drawable.ic_network)
            
            // Configure toggle button
            if (isLoading) {
                binding.toggleButton.visibility = View.INVISIBLE
                binding.toggleProgress.visibility = View.VISIBLE
            } else {
                binding.toggleButton.visibility = View.VISIBLE
                binding.toggleProgress.visibility = View.GONE
                
                val toggleText = if (networkInterface.isActive) {
                    binding.root.context.getString(R.string.button_disable)
                } else {
                    binding.root.context.getString(R.string.button_enable)
                }
                binding.toggleButton.text = toggleText
                
                binding.toggleButton.setOnClickListener {
                    onToggleClick(networkInterface.name, !networkInterface.isActive)
                }
            }
        }
    }
}

class InterfaceDiffCallback : DiffUtil.ItemCallback<NetworkInterface>() {
    override fun areItemsTheSame(oldItem: NetworkInterface, newItem: NetworkInterface): Boolean {
        return oldItem.name == newItem.name
    }
    
    override fun areContentsTheSame(oldItem: NetworkInterface, newItem: NetworkInterface): Boolean {
        return oldItem == newItem
    }
}