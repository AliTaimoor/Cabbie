package com.cabbie.hat.cabbie.HistoryRecyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cabbie.hat.cabbie.R;

public class HistoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView rideId;
    public HistoryViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        rideId = (TextView) itemView.findViewById(R.id.rideId);
    }
    @Override
    public void onClick(View v) {
    }
}