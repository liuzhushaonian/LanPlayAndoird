package com.app.legend.lan_play_android.adapters;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.legend.lan_play_android.R;
import com.app.legend.lan_play_android.bean.PreBean;
import com.app.legend.lan_play_android.interfaces.MoreClickListener;
import com.app.legend.lan_play_android.interfaces.PreItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class MainAdapter extends BaseAdapter<MainAdapter.ViewHolder> {


    List<PreBean> preBeanList=new ArrayList<>();

    private PreItemClickListener listener;

    private MoreClickListener moreClickListener;

    public void setMoreClickListener(MoreClickListener moreClickListener) {
        this.moreClickListener = moreClickListener;
    }

    public void setListener(PreItemClickListener listener) {
        this.listener = listener;
    }

    public void setPreBeanList(List<PreBean> preBeanList) {
        this.preBeanList = preBeanList;

        notifyDataSetChanged();
    }

    public void addBean(PreBean preBean){

        this.preBeanList.add(preBean);

        notifyDataSetChanged();

    }

    public void removeItem(PreBean preBean){

        if (this.preBeanList.contains(preBean)){
            this.preBeanList.remove(preBean);

            notifyDataSetChanged();
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.pre_item,parent,false);

        ViewHolder viewHolder=new ViewHolder(view);

        viewHolder.view.setOnClickListener(v -> {

            if (listener!=null && this.preBeanList!=null){

                int position=viewHolder.getAdapterPosition();

                PreBean bean=this.preBeanList.get(position);

                listener.clickItem(position,bean);

            }

        });

        viewHolder.more_menu.setOnClickListener(v -> {

            if (this.moreClickListener!=null){

                int position=viewHolder.getAdapterPosition();

                PreBean bean=this.preBeanList.get(position);

                moreClickListener.click(viewHolder.more_menu,position,bean);

            }

        });


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (this.preBeanList==null){
            return;
        }

        PreBean bean=this.preBeanList.get(position);

        holder.name.setText(bean.getName());
        holder.url.setText(bean.getUrl());

        if (bean.getSelect()>0){

            holder.icons.setBackgroundColor(holder.view.getResources().getColor(R.color.colorGreen));
            holder.more_menu.setClickable(false);


        }else {

            holder.icons.setBackgroundColor(holder.view.getResources().getColor(R.color.colorGrey));
            holder.more_menu.setClickable(true);

        }

//        if (bean)



    }

    @Override
    public int getItemCount() {

        if (this.preBeanList!=null){
            return this.preBeanList.size();
        }

        return super.getItemCount();
    }

    static class ViewHolder extends BaseAdapter.ViewHolder{


        TextView name,url;
        View view,icons;
        ImageView more_menu;

        public ViewHolder(View itemView) {
            super(itemView);

            this.view=itemView;
            this.name=itemView.findViewById(R.id.server_name);

            this.url=itemView.findViewById(R.id.server_ip);

            icons=itemView.findViewById(R.id.icons);

            this.more_menu=itemView.findViewById(R.id.more_menu);

        }
    }


    public PreBean getSelectBean(){

        for (int i=0;i<this.preBeanList.size();i++){

            PreBean bean=this.preBeanList.get(i);

            if (bean.getSelect()>0){
                return bean;
            }

        }

        return null;

    }
}
