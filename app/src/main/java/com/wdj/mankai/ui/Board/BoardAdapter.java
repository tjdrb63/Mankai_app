package com.wdj.mankai.ui.Board;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.wdj.mankai.R;
import com.wdj.mankai.data.BoardData;

import org.json.JSONException;

import java.util.ArrayList;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.ViewHolder> {

    private ArrayList<BoardData> mData=null;

    @Override
    public int getItemViewType(int position) {
        try {
            return mData.get(position).getViewType();
        } catch (JSONException e) {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ViewHolder(View itemView){
            super(itemView);
        }
        //    layout ID
        TextView snsName = itemView.findViewById(R.id.snsName);
        ImageView snsUserImage = itemView.findViewById(R.id.snsUserImage);
        TextView snsContent = itemView.findViewById(R.id.snsContent);
        ImageView snsMainImage = itemView.findViewById(R.id.snsMainImage);
    }

    BoardAdapter(ArrayList<BoardData> list){
        mData = list;
    }
    //    아이템뷰를 위한 뷰홀더 객체 생성하여 리턴

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = null;
//        멀티 뷰타입 분리
        if(viewType == 0)
            view = inflater.inflate(R.layout.board_normal_view,parent,false);
        else
            view = inflater.inflate(R.layout.board_image_view,parent,false);

        BoardAdapter.ViewHolder vh = new BoardAdapter.ViewHolder(view);

        return vh;
    }

    //    position에 해당하는 데이터를 뷰홀더 아이템에 표시
    @Override
    public void onBindViewHolder(BoardAdapter.ViewHolder holder, int position){
        BoardData snsdata = mData.get(position);
        try {
            holder.snsContent.setText(snsdata.getContent_text());
            holder.snsName.setText(snsdata.getName());
            Glide.with(holder.itemView.getContext())
                    .load(snsdata.getProfile())
                    .override(100, 100)
                    .into(holder.snsUserImage);

//            사진 있을경우 1
            Log.d("Board", position+" "+snsdata.getViewType());
            if(snsdata.getViewType() == 1) {
                Glide.with(holder.itemView.getContext())
                        .load(snsdata.getBoardImage())
                        .centerCrop()
                        .into(holder.snsMainImage);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public int getItemCount() {
        return mData.size();
    }

}
