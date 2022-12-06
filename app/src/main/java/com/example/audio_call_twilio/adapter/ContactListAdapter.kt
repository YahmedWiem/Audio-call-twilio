package com.example.audio_call_twilio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.audio_call_twilio.databinding.ContactItemBinding
import com.example.audio_call_twilio.model.Contact

class ContactListAdapter(private val contactList: List<Contact>) :
    RecyclerView.Adapter<ContactViewHolder>() {

    private lateinit var binding: ContactItemBinding
    private lateinit var itemListener: onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun onItemClickListener(listener: onItemClickListener) {
        itemListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        binding = ContactItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding, itemListener)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contactList[position])
    }

    override fun getItemCount() = contactList.size
}

class ContactViewHolder(private val binding: ContactItemBinding, listener: ContactListAdapter.onItemClickListener): RecyclerView.ViewHolder(binding.root){
    fun bind(contact: Contact){
        binding.contact = contact
        binding.imageContact.setImageResource(contact.contactImage)
    }
    init {
        binding.contactName.setOnClickListener {
            listener.onItemClick(adapterPosition)
        }
    }

}
