package com.eli.lightaccount;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.nio.channels.NonWritableChannelException;
import java.util.ArrayList;
import java.util.List;

public class RecyclerView_ItemAdapter extends RecyclerView.Adapter<RecyclerView_ItemAdapter.ViewHolder> {

    private List<ItemBean> mItemBeanList;

    public class ViewHolder extends RecyclerView.ViewHolder {

        View itemView;

        public TextView mTextViewType;
        public TextView mTextViewNote;
        public TextView mTextViewDate;
        public TextView mTextViewMoney;
        public TextView mTextViewId;
        public TextView mTextViewCategory;


        public ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            mTextViewType = itemView.findViewById(R.id.textView_type);
            mTextViewNote = itemView.findViewById(R.id.textView_Note);
            mTextViewDate = itemView.findViewById(R.id.textView_date);
            mTextViewMoney = itemView.findViewById(R.id.textView_money);
            mTextViewId = itemView.findViewById(R.id.textView_id);
            mTextViewCategory = itemView.findViewById(R.id.textView_category);

        }
    }

    public RecyclerView_ItemAdapter(List<ItemBean> itemBeanList) {
        mItemBeanList = itemBeanList;
    }

    /**
     * 为点击事件创建接口
     */
    public interface OnItemClickLitener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemBean itemBean = mItemBeanList.get(position);
        holder.mTextViewType.setText(itemBean.getItemType());
        holder.mTextViewNote.setText(itemBean.getItemNote());
        holder.mTextViewDate.setText(itemBean.getItemDate());
        holder.mTextViewMoney.setText(itemBean.getItemMoney());
        holder.mTextViewId.setText(itemBean.getItemId());
        holder.mTextViewCategory.setText(itemBean.getItemCategory());

        if (holder.mTextViewCategory.getText().equals("Payment")) {
            holder.mTextViewMoney.setTextColor(Color.RED);
        } else if (holder.mTextViewCategory.getText().equals("Income")) {
            holder.mTextViewMoney.setTextColor(Color.GREEN);
        }


        // 如果设置了回调，则设置点击事件
        if (mOnItemClickLitener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(holder.itemView, position);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemLongClick(holder.itemView, position);
                    return true;
                }
            });
        }

    }


    @Override
    public int getItemCount() {
        return mItemBeanList.size();
    }

}
