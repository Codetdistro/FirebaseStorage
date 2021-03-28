package com.example.firebasestorage

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FirebaseAdapter(val list:ArrayList<DataClass>,val click:OnItemClickListener):RecyclerView.Adapter<FirebaseAdapter.myViewHolder>() {

    interface OnItemClickListener{
        fun onItemClick(item:String,pos:Int)
    }


    class myViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val img:ImageView = itemView.findViewById(R.id.data_image)
        val btn:Button = itemView.findViewById(R.id.btnDownload)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        return myViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycleritem,parent,false))
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        Glide.with(holder.itemView)
                .load(list.get(position).imageUrl)
                .into(holder.img)
        holder.btn.setOnClickListener {
            click.onItemClick(list.get(position).imageUrl!!,position)
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }
}