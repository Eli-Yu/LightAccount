package com.eli.lightaccount;

import android.content.DialogInterface;
import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class QueryActivity extends AppCompatActivity {

    private List<ItemBean> mItemBeanList;
    private List<String> mTypeString;
    private List<TypeBean> mTypeList;
    private DataBaseHelper mDatabaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        mTypeList = new ArrayList<>();
        mTypeString = new ArrayList<>();
        mDatabaseHelper = new DataBaseHelper(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        mItemBeanList = (List<ItemBean>) getIntent().getSerializableExtra("item_list");
        RecyclerView recyclerView = findViewById(R.id.recycler_view_query);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //设置分隔线
        recyclerView.addItemDecoration(new DividerItemDecoration(this, 1));
        //设置增加或删除条目的动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        RecyclerView_ItemAdapter mAdapter = new RecyclerView_ItemAdapter(mItemBeanList);
        recyclerView.setAdapter(mAdapter);

        //设定点击和长按事件，点击修改删除，长按删除
        mAdapter.setOnItemClickLitener(new RecyclerView_ItemAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {

                final AlertDialog.Builder updateBuilder = new AlertDialog.Builder(QueryActivity.this);
                final LayoutInflater updateInflater = LayoutInflater.from(QueryActivity.this);
                View viewUpdate = updateInflater.inflate(R.layout.new_item_data, null);
                final RadioGroup category = viewUpdate.findViewById(R.id.radio_group);
                final Spinner type = viewUpdate.findViewById(R.id.spinner_type);
                final EditText money = viewUpdate.findViewById(R.id.editText_money);
                final EditText note = viewUpdate.findViewById(R.id.editText_note);
                final DatePicker date = viewUpdate.findViewById(R.id.datePicker_date);
                Button manageType = viewUpdate.findViewById(R.id.button_manageType);

                final ItemBean updateItem = mItemBeanList.get(position);

                //隐藏类别选项
                category.setVisibility(View.GONE);

                //建立Spinner的监听器，用来确定获取到的类型
                setTypeList(updateItem.getItemCategory());
                final ArrayAdapter<TypeBean> adapter;
                adapter = new ArrayAdapter<TypeBean>(QueryActivity.this, android.R.layout.simple_spinner_item, mTypeList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                type.setAdapter(adapter);

                //类型管理监听事件
                typeManage(manageType, type, updateItem.getItemCategory());

                //用列表项中的数据初始化控件
                type.setSelection(mTypeString.indexOf(updateItem.getItemType()));
                money.setText(updateItem.getItemMoney());
                note.setText(updateItem.getItemNote());
                String[] oldDate = updateItem.getItemDate().split("-");
                date.init(Integer.parseInt(oldDate[0]), Integer.parseInt(oldDate[1]) - 1, Integer.parseInt(oldDate[2]), null);

                //设定布局
                updateBuilder.setView(viewUpdate);

                updateBuilder.setTitle("修改记录");
                //设定确认事件
                updateBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!money.getText().toString().isEmpty() && Float.parseFloat(money.getText().toString()) > 0) {
                            updateItem.setItemType(type.getSelectedItem().toString());
                            updateItem.setItemMoney(money.getText().toString());
                            updateItem.setItemNote(note.getText().toString());
                            updateItem.setItemDate(date.getYear() + "-" + (date.getMonth() + 1) + "-" + date.getDayOfMonth());
                            mDatabaseHelper.updateItem(updateItem, updateItem.getItemCategory(), updateItem.getItemId());
                            //更新列表
                            mAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(QueryActivity.this, "金额不能为空且大于0", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                //取消不做任何事情
                updateBuilder.setNegativeButton("取消", null);
                //显示dialog
                updateBuilder.create().show();

            }

            @Override
            public void onItemLongClick(View view, int position) {

                final ItemBean itemDelete = mItemBeanList.get(position);
                Snackbar.make(view, "确认删除？", Snackbar.LENGTH_LONG).setAction("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDatabaseHelper.delete(itemDelete.getItemCategory(), itemDelete.getItemId());
                        mItemBeanList.remove(itemDelete);
                        //更新列表
                        mAdapter.notifyDataSetChanged();
                    }
                }).show();
            }
        });
    }



    /**
     * 根据账目类别设置类型列表
     *
     * @param table 表名（支出还是收入）
     */
    private void setTypeList(String table) {
        Cursor cursor = mDatabaseHelper.getAllTypeData("Type" + table);
        if (cursor != null) {
            mTypeList.clear();
            mTypeString.clear();
            while (cursor.moveToNext()) {
                TypeBean typeBean = new TypeBean();
                typeBean.setTypeName(cursor.getString(cursor.getColumnIndex("name")));
                typeBean.setTypeId(cursor.getString(cursor.getColumnIndex("id")));
                mTypeString.add(typeBean.getTypeName());
                mTypeList.add(typeBean);
            }
            cursor.close();
        }
    }


    private void typeManage(Button button, Spinner type, String category) {
        //类型管理监听事件
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder addBuilder = new AlertDialog.Builder(QueryActivity.this);
                LayoutInflater addInflater = LayoutInflater.from(QueryActivity.this);
                final View viewManageType = addInflater.inflate(R.layout.manage_type, null);
                final EditText manageType = viewManageType.findViewById(R.id.edit_manageType);
                final ListView listType = viewManageType.findViewById(R.id.list_view_type);


//                        listType.setAdapter(adapter);
//                        setTypeList(updateItem.getItemCategory());
                TypeListAdapter listAdapter = new TypeListAdapter(QueryActivity.this, mTypeList);
                listType.setAdapter(listAdapter);

                //将布局设置给Dialog
                addBuilder.setView(viewManageType);
                addBuilder.setTitle("类型管理");

                addBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TypeBean typeBean = new TypeBean();
                        typeBean.setTypeName(manageType.getText().toString());
                        typeBean.setTypeId(Long.toString(System.currentTimeMillis()));

                        if (!typeBean.getTypeName().isEmpty()) {
                            if (mTypeString.contains(typeBean.getTypeName())) {
                                Toast.makeText(QueryActivity.this, "此类型已经存在，添加失败", Toast.LENGTH_LONG).show();
                            } else {
                                //将新的类型数据添加到相应的类型表中
                                mDatabaseHelper.insertType(typeBean, "Type" + category);
                                //                                    mTypeString.add(typeBean.getTypeName());
                                //将新类型添加到类型List，并将选中值设为新添加的类型
                                mTypeList.add(typeBean);
                                mTypeString.add(typeBean.getTypeName());
                                type.setSelection(mTypeList.size() - 1);
                                //                                    itemBean.setItemType(typeBean.getTypeName());
                            }
                        } else {
                            Toast.makeText(QueryActivity.this, "类型名不能为空", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                addBuilder.setNegativeButton("取消", null);

                /**
                 * 类型列表项目的管理功能：点击修改，长按删除
                 */

                /**
                 * 设定点击修改事件的监听器
                 */
                listType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        AlertDialog.Builder updateTypeBuilder = new AlertDialog.Builder(QueryActivity.this);
                        LayoutInflater updateTypeInflate = LayoutInflater.from(QueryActivity.this);
                        View updateTypeView = updateTypeInflate.inflate(R.layout.manage_type, parent, false);
                        TextView text = updateTypeView.findViewById(R.id.textView_add_type);
                        EditText updateType = updateTypeView.findViewById(R.id.edit_manageType);
                        updateTypeView.findViewById(R.id.list_view_type).setVisibility(View.GONE);
                        text.setText("修改为：");

                        updateTypeBuilder.setView(updateTypeView);
                        final TypeBean update = mTypeList.get(position);

                        updateTypeBuilder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!updateType.getText().toString().isEmpty()) {   //检测是否为空
                                    if (mTypeString.contains(updateType.getText().toString())) {    //检查是否重复
                                        Toast.makeText(QueryActivity.this, "此类型已经存在，修改失败", Toast.LENGTH_LONG).show();
                                    } else {
                                        update.setTypeName(updateType.getText().toString());
                                        mDatabaseHelper.updateType(update, "Type" + category);

                                        //将list中的对应项也同步更新
                                        mTypeString.set(position, update.getTypeName());
                                        mTypeList.set(position,update);
//                                                setTypeList(updateItem.getItemCategory());
                                        //刷新列表
                                        listAdapter.notifyDataSetChanged();
                                    }
                                } else {    //类型名为空时，不能修改
                                    Toast.makeText(QueryActivity.this, "不能修改为空", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        updateTypeBuilder.setNegativeButton("取消", null);
                        updateTypeBuilder.create().show();
                    }
                });

                /**
                 * 设定长按删除事件的监听器
                 */
                listType.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        final TypeBean typeDelete = mTypeList.get(position);
                        Snackbar.make(view, "确认删除？", Snackbar.LENGTH_LONG).setAction("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDatabaseHelper.delete("Type" + category, typeDelete.getTypeId());
                                mTypeList.remove(typeDelete);
                                mTypeString.remove(typeDelete.getTypeName());

                                //更新列表
                                listAdapter.notifyDataSetChanged();

                                //删除后将类型设为默认，即类型中的第一个
                                type.setSelection(0);
                            }
                        }).show();
                        return true;
                    }
                });


                addBuilder.create().show();
            }
        });
    }
}
