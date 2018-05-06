package com.eli.lightaccount;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 *
 * 类型列表适配器
 *
 */


public class TypeListAdapter extends BaseAdapter {

    private List<TypeBean> mList;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    /**
     * 构造函数
     * @param context 传入上下文
     * @param list 传入的列表
     */
    public TypeListAdapter(Context context, List<TypeBean> list) {
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

    /**
     * 加载列表项，并用缓存节约资源
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) { //没有缓存时候
            viewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.type_item,parent, false);

            viewHolder.mTextViewName = convertView.findViewById(R.id.textView_type_name);
            viewHolder.mTextViewId = convertView.findViewById(R.id.textView_type_id);

            //将viewHolder装入convertView
            convertView.setTag(viewHolder);
        } else {
            //不为空，直接装入
            viewHolder = (ViewHolder) convertView.getTag();
        }
        TypeBean typeBean = mList.get(position);
        viewHolder.mTextViewName.setText(typeBean.getTypeName());
        viewHolder.mTextViewId.setText(typeBean.getTypeId());
        return convertView;
    }

    /**
     * 用于保存列表项目的ViewHoler类
     */
    private static class ViewHolder {

        public TextView mTextViewId;
        public TextView mTextViewName;
    }
}
