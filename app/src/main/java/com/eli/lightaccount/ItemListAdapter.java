package com.eli.lightaccount;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;


public class ItemListAdapter extends BaseAdapter {

    private List<ItemBean> mList;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public ItemListAdapter(Context context, List<ItemBean> list) {
        mContext = context;
        mList = list;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) { //缓存中没有viewHolder时候
            viewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.list_item, parent, false);

            viewHolder.mTextViewType = convertView.findViewById(R.id.textView_type);
            viewHolder.mTextViewNote = convertView.findViewById(R.id.textView_Note);
            viewHolder.mTextViewDate = convertView.findViewById(R.id.textView_date);
            viewHolder.mTextViewMoney = convertView.findViewById(R.id.textView_money);
            viewHolder.mTextViewId = convertView.findViewById(R.id.textView_id);
            viewHolder.mTextViewCategory = convertView.findViewById(R.id.textView_category);

            convertView.setTag(viewHolder); //将viewHolder存储进convertView中
        } else { //不为空，直接取缓存
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ItemBean itemBean = mList.get(position);
        viewHolder.mTextViewType.setText(itemBean.getItemType());
        viewHolder.mTextViewNote.setText(itemBean.getItemNote());
        viewHolder.mTextViewDate.setText(itemBean.getItemDate());
        viewHolder.mTextViewMoney.setText(itemBean.getItemMoney());
        viewHolder.mTextViewId.setText(itemBean.getItemId());
        viewHolder.mTextViewCategory.setText(itemBean.getItemCategory());

        if (viewHolder.mTextViewCategory.getText().equals("Payment")) {
            viewHolder.mTextViewMoney.setTextColor(Color.RED);
        }
        else if (viewHolder.mTextViewCategory.getText().equals("Income")) {
            viewHolder.mTextViewMoney.setTextColor(Color.GREEN);
        }

        return convertView;
    }

    //使用ViewHolder 来缓存控件实例
    private static class ViewHolder {
        public TextView mTextViewType;
        public TextView mTextViewNote;
        public TextView mTextViewDate;
        public TextView mTextViewMoney;
        public TextView mTextViewId;
        public TextView mTextViewCategory;
    }
}
