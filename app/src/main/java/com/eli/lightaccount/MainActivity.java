package com.eli.lightaccount;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<ItemBean> mItemBeanList;
    private List<String> mTypeString;
    private List<TypeBean> mTypeList;
    private DataBaseHelper mDatabaseHelper;
    private ItemListAdapter mAdapter;
    private String itemCategory = "Payment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDatabaseHelper = new DataBaseHelper(this);
        mItemBeanList = new ArrayList<>();
        mTypeList = new ArrayList<>();
        mTypeString = new ArrayList<>();
        ListView itemList = findViewById(R.id.list_view_main);
        mDatabaseHelper.getWritableDatabase();
        initItemData();
        mAdapter = new ItemListAdapter(this, mItemBeanList);
        itemList.setAdapter(mAdapter);

        //设置列表项点击事件
        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AlertDialog.Builder updateBuilder = new AlertDialog.Builder(MainActivity.this);
                final LayoutInflater updateInflater = LayoutInflater.from(MainActivity.this);
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

                //设置类型下拉框的项目，从Type表中获取


//                mTypeString = new ArrayList<>();

//                Cursor cursor= mDatabaseHelper.getAllTypeData("TypePayment");
//                if(cursor != null) {
//                    while (cursor.moveToNext()) {
//                        TypeBean typeBean = new TypeBean();
//                        typeBean.setTypeName(cursor.getString(cursor.getColumnIndex("name")));
//                        typeBean.setTypeId(cursor.getString(cursor.getColumnIndex("id")));
//                        mTypeString.add(typeBean.getTypeName());
//                        mTypeList.add(typeBean);
//                    }
//                    cursor.close();
//                }

//                final ArrayAdapter<String> adapter;
//                adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, mTypeString);

                //建立Spinner的监听器，用来确定获取到的类型
                setTypeList(updateItem.getItemCategory());
                final ArrayAdapter<TypeBean> adapter;
                adapter = new ArrayAdapter<TypeBean>(MainActivity.this, android.R.layout.simple_spinner_item, mTypeList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                type.setAdapter(adapter);
//                type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                    @Override
//                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                        itemBean.setItemType(adapter.getItem(i));
//                        ((TextView)view).setGravity(android.view.Gravity.CENTER_HORIZONTAL);
//                        ((TextView)view).setTextColor(Color.RED);
//                        Toast.makeText(MainActivity.this, updateItem.getItemCategory(),Toast.LENGTH_LONG).show();
//                    }
//
//                    @Override
//                    public void onNothingSelected(AdapterView<?> adapterView) {
//                        Toast.makeText(MainActivity.this, "未选中", Toast.LENGTH_LONG).show();
//                    }
//                });

                //类型管理监听事件
                typeManage(manageType, type, updateItem.getItemCategory());
//                manageType.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        AlertDialog.Builder addBuilder = new AlertDialog.Builder(MainActivity.this);
//                        LayoutInflater addInflater = LayoutInflater.from(MainActivity.this);
//                        final View viewManageType = addInflater.inflate(R.layout.manage_type, null);
//                        final EditText manageType = viewManageType.findViewById(R.id.edit_manageType);
//                        final ListView listType = viewManageType.findViewById(R.id.list_view_type);
//
//
////                        listType.setAdapter(adapter);
////                        setTypeList(updateItem.getItemCategory());
//                        TypeListAdapter listAdapter = new TypeListAdapter(MainActivity.this, mTypeList);
//                        listType.setAdapter(listAdapter);
//
//                        //将布局设置给Dialog
//                        addBuilder.setView(viewManageType);
//                        addBuilder.setTitle("类型管理");
//
//                        addBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                TypeBean typeBean = new TypeBean();
//                                typeBean.setTypeName(manageType.getText().toString());
//                                typeBean.setTypeId(Long.toString(System.currentTimeMillis()));
//
//                                if (!typeBean.getTypeName().isEmpty()) {
//                                    if (mTypeString.contains(typeBean.getTypeName())) {
//                                        Toast.makeText(MainActivity.this, "此类型已经存在，添加失败", Toast.LENGTH_LONG).show();
//                                    } else {
//                                        //将新的类型数据添加到相应的类型表中
//                                        mDatabaseHelper.insertType(typeBean, "Type" + updateItem.getItemCategory());
//                                        //                                    mTypeString.add(typeBean.getTypeName());
//                                        //将新类型添加到类型List，并将选中值设为新添加的类型
//                                        mTypeList.add(typeBean);
//                                        mTypeString.add(typeBean.getTypeName());
//                                        type.setSelection(mTypeList.size() - 1);
//                                        //                                    itemBean.setItemType(typeBean.getTypeName());
//                                    }
//                                } else {
//                                    Toast.makeText(MainActivity.this, "类型名不能为空", Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        });
//                        addBuilder.setNegativeButton("取消", null);
//
//                        /**
//                         * 类型列表项目的管理功能：点击修改，长按删除
//                         */
//
//                        /**
//                         * 设定点击修改事件的监听器
//                         */
//                        listType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                            @Override
//                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                                AlertDialog.Builder updateTypeBuilder = new AlertDialog.Builder(MainActivity.this);
//                                LayoutInflater updateTypeInflate = LayoutInflater.from(MainActivity.this);
//                                View updateTypeView = updateTypeInflate.inflate(R.layout.manage_type, null);
//                                TextView text = updateTypeView.findViewById(R.id.textView_add_type);
//                                EditText updateType = updateTypeView.findViewById(R.id.edit_manageType);
//                                updateTypeView.findViewById(R.id.list_view_type).setVisibility(View.GONE);
//                                text.setText("修改为：");
//
//                                updateTypeBuilder.setView(updateTypeView);
//                                final TypeBean update = mTypeList.get(position);
//
//                                updateTypeBuilder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        if (!updateType.getText().toString().isEmpty()) {   //检测是否为空
//                                            if (mTypeString.contains(updateType.getText().toString())) {    //检查是否重复
//                                                Toast.makeText(MainActivity.this, "此类型已经存在，修改失败", Toast.LENGTH_LONG).show();
//                                            } else {
//                                                update.setTypeName(updateType.getText().toString());
//                                                mDatabaseHelper.updateType(update, "Type" + updateItem.getItemCategory());
////                                                setTypeList(updateItem.getItemCategory());
//                                                //刷新列表
//                                                listAdapter.notifyDataSetChanged();
//                                            }
//                                        } else {    //类型名为空时，不能修改
//                                            Toast.makeText(MainActivity.this, "不能修改为空", Toast.LENGTH_LONG).show();
//                                        }
//                                    }
//                                });
//                                updateTypeBuilder.setNegativeButton("取消", null);
//                                updateTypeBuilder.create().show();
//                            }
//                        });
//
//                        /**
//                         * 设定长按删除事件的监听器
//                         */
//                        listType.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//                            @Override
//                            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                                final TypeBean typeDelete = mTypeList.get(position);
//                                Snackbar.make(view, "确认删除？", Snackbar.LENGTH_LONG).setAction("确定", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        mDatabaseHelper.delete("Type" + updateItem.getItemCategory(), typeDelete.getTypeId());
//                                        mTypeList.remove(typeDelete);
//                                        mTypeString.remove(typeDelete.getTypeName());
//
//                                        //更新列表
//                                        listAdapter.notifyDataSetChanged();
//
//                                        //删除后将类型设为默认，即类型中的第一个
//                                        type.setSelection(0);
//                                    }
//                                }).show();
//                                return true;
//                            }
//                        });
//
//
//                        addBuilder.create().show();
//                    }
//                });


                //用列表项中的数据初始化控件
//                updateItem = mItemBeanList.get(position);
//                category.setSelected();
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
                        updateItem.setItemType(type.getSelectedItem().toString());
                        updateItem.setItemMoney(money.getText().toString());
                        updateItem.setItemNote(note.getText().toString());
                        updateItem.setItemDate(date.getYear() + "-" + (date.getMonth() + 1) + "-" + date.getDayOfMonth());
                        mDatabaseHelper.updateItem(updateItem, updateItem.getItemCategory(), updateItem.getItemId());
                        //更新列表
                        mAdapter.notifyDataSetChanged();
//                        long timeStamp = System.currentTimeMillis();
//                        Date d = new Date(timeStamp);
//                        Toast.makeText(MainActivity.this, Long.toString(timeStamp)+ "和" + d, Toast.LENGTH_LONG ).show();
                    }
                });

                //取消不做任何事情
                updateBuilder.setNegativeButton("取消", null);
                //显示dialog
                updateBuilder.create().show();
//                Toast.makeText(MainActivity.this,mItemBeanList.get(position).getItemNote(),Toast.LENGTH_LONG).show();

            }
        });

        //设置列表项长按事件
        itemList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
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
                return true;
            }
        });


        //增加账目按钮
        FloatingActionButton fabAdd = (FloatingActionButton) findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View viewDialog = inflater.inflate(R.layout.new_item_data, null);
                final RadioGroup category = viewDialog.findViewById(R.id.radio_group);
                final Spinner type = viewDialog.findViewById(R.id.spinner_type);
                final EditText money = viewDialog.findViewById(R.id.editText_money);
                final EditText note = viewDialog.findViewById(R.id.editText_note);
                final DatePicker date = viewDialog.findViewById(R.id.datePicker_date);
                Button manageType = viewDialog.findViewById(R.id.button_manageType);
                final ItemBean itemBean = new ItemBean();


                //设置类型下拉框的项目，从Type表中获取
//                mTypeString = new ArrayList<>();
//                Cursor cursor= mDatabaseHelper.getAllTypeData("TypePayment");
//                if(cursor != null) {
//                    while (cursor.moveToNext()) {
//                        String temp = cursor.getString(cursor.getColumnIndex("name"));
//                        mTypeString.add(temp);
//                    }
//                    cursor.close();
//                }

                setTypeList(itemCategory);
//                setTypeList("Payment");
                //建立Spinner的监听器，用来确定获取到的类型
                final ArrayAdapter<String> adapter;
                adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, mTypeString);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                type.setAdapter(adapter);
                type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        itemBean.setItemType(adapter.getItem(i));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        Toast.makeText(MainActivity.this, "未选中", Toast.LENGTH_LONG).show();
                    }
                });


                /**
                 * 设置一个单选按钮组的监听器，
                 * 监听账目分类选择变化事件，
                 * 为了实现根据选中情况，动态调整spinner中的类型
                 */

                category.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId) {
                            case R.id.radio_button_income:
                                itemCategory = "Income";
//                                type.setEnabled(false);
//                                Log.i("radio",itemCategory);
                                break;
                            case R.id.radio_button_payment:
                                itemCategory = "Payment";
//                                itemCategory = "Payment";
//                                Log.i("radio",itemCategory);
                                break;
                            default:
                                return;
                        }
                        setTypeList(itemCategory);
                        type.setAdapter(adapter);
                        typeManage(manageType, type, itemCategory);
//                        RadioButton radioButton = viewDialog.findViewById(checkedId);
//                        Toast.makeText(MainActivity.this, checkedId + "&&" + R.id.radio_button_income,Toast.LENGTH_LONG).show();
                    }
                });

                //类型管理监听事件
                typeManage(manageType, type, itemCategory);
//                manageType.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        AlertDialog.Builder addBuilder = new AlertDialog.Builder(MainActivity.this);
//                        LayoutInflater addInflater = LayoutInflater.from(MainActivity.this);
//                        final View viewManageType = addInflater.inflate(R.layout.manage_type, null);
//                        final EditText manageType = viewManageType.findViewById(R.id.edit_manageType);
//
//                        //将布局设置给Dialog
//                        addBuilder.setView(viewManageType);
//
//                        addBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                TypeBean typeBean = new TypeBean();
//                                typeBean.setTypeName(manageType.getText().toString());
//
//                                if (mTypeString.contains(typeBean.getTypeName())) {
//                                    Toast.makeText(MainActivity.this, "此类型已经存在", Toast.LENGTH_LONG).show();
//                                } else {
//                                    mDatabaseHelper.insertType(typeBean, "TypePayment");
//                                    mTypeString.add(typeBean.getTypeName());
//                                    type.setSelection(mTypeString.size() - 1);
////                                    itemBean.setItemType(typeBean.getTypeName());
//                                }
//                            }
//                        });
//                        addBuilder.setNegativeButton("取消", null);
//                        addBuilder.create().show();
//                    }
//                });


                //将布局设置给Dialog
                builder.setView(viewDialog);
                //设置对话框标题
                builder.setTitle("记入");

                //设定确认按钮事件
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //获取选中项的文字
//                        String Tag = (String) ((RadioButton) viewDialog.findViewById(category.getCheckedRadioButtonId())).getText();
                        int tableTag = category.getCheckedRadioButtonId();

                        //获取当前时间戳作为主键值
                        itemBean.setItemId(Long.toString(System.currentTimeMillis()));
                        itemBean.setItemMoney(money.getText().toString());
                        itemBean.setItemNote(note.getText().toString());
                        itemBean.setItemDate(date.getYear() + "-" + (date.getMonth() + 1) + "-" + date.getDayOfMonth());

                        /// 检测金额是否为空，为空则取消操作
                        if (!itemBean.getItemMoney().isEmpty() && Integer.parseInt(itemBean.getItemMoney()) > 0) {
                            //将数据插入数据库
                            if (tableTag == R.id.radio_button_payment) {
                                itemBean.setItemCategory("Payment");
                                mDatabaseHelper.insertItem(itemBean, "Payment");
                            } else {
                                itemBean.setItemCategory("Income");
                                mDatabaseHelper.insertItem(itemBean, "Income");
                            }
                            //将数据插入到列表中，以便能够刷新显示
                            mItemBeanList.add(itemBean);
                            //刷新列表，显示最新插入的数据
                            mAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(MainActivity.this, "金额不能为空且大于0", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                //取消按钮
                builder.setNegativeButton("取消", null);

                //创建并显示dialog
                builder.create().show();
            }
        });


        //查询账目按钮
        FloatingActionButton fabQuery = (FloatingActionButton) findViewById(R.id.fab_query);

        /**
         * 设定查询按钮监听事件
         */
        fabQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder queryBuilder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater queryInflater = LayoutInflater.from(MainActivity.this);
                View queryView = queryInflater.inflate(R.layout.query, null);


                //将布局设置给Dialog
                queryBuilder.setView(queryView);
                //设置对话框标题
                queryBuilder.setTitle("查询");

                queryBuilder.create().show();
            }
        });
    }

    private void initItemData() {
//        for (int i = 0; i < 5; ++i) {
//            ItemBean itemBean = new ItemBean();
//            itemBean.setItemType(Integer.toString(i));
//            itemBean.setItemDate("2018-04-30");
//            itemBean.setItemMoney("15");
//            mDatabaseHelper.insertItem(itemBean,"Income");
//        }
        Cursor cursor = mDatabaseHelper.getAllItemData("Payment");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ItemBean itemBean = new ItemBean();
                itemBean.setItemId(cursor.getString(cursor.getColumnIndex("id")));
                itemBean.setItemType(cursor.getString(cursor.getColumnIndex("type")));
                itemBean.setItemNote(cursor.getString(cursor.getColumnIndex("note")));
                itemBean.setItemDate(cursor.getString(cursor.getColumnIndex("date")));
                itemBean.setItemMoney(cursor.getString(cursor.getColumnIndex("money")));
                itemBean.setItemCategory("Payment");
                mItemBeanList.add(itemBean);
            }
            cursor.close();
        }
        cursor = mDatabaseHelper.getAllItemData("Income");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ItemBean itemBean = new ItemBean();
                itemBean.setItemId(cursor.getString(cursor.getColumnIndex("id")));
                itemBean.setItemType(cursor.getString(cursor.getColumnIndex("type")));
                itemBean.setItemNote(cursor.getString(cursor.getColumnIndex("note")));
                itemBean.setItemDate(cursor.getString(cursor.getColumnIndex("date")));
                itemBean.setItemMoney(cursor.getString(cursor.getColumnIndex("money")));
                itemBean.setItemCategory("Income");
                mItemBeanList.add(itemBean);
            }
            cursor.close();
        }
        //排序对数据按照时间排序
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //Java8中支持的Lambda排序
            mItemBeanList.sort((ItemBean i1, ItemBean i2) -> i1.getItemDate().compareTo(i2.getItemDate()));
        } else {
            //不支持Java8的环境下使用，传统的新建匿名内部类排序
            new Comparator<ItemBean>() {
                @Override
                public int compare(ItemBean i1, ItemBean i2) {
                    return i1.getItemDate().compareTo(i2.getItemDate());
                }
            };

            Collections.sort(mItemBeanList, new Comparator<ItemBean>() {
                @Override
                public int compare(ItemBean o1, ItemBean o2) {
                    return o1.getItemDate().compareTo(o2.getItemDate());
                }
            });
        }
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
                AlertDialog.Builder addBuilder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater addInflater = LayoutInflater.from(MainActivity.this);
                final View viewManageType = addInflater.inflate(R.layout.manage_type, null);
                final EditText manageType = viewManageType.findViewById(R.id.edit_manageType);
                final ListView listType = viewManageType.findViewById(R.id.list_view_type);


//                        listType.setAdapter(adapter);
//                        setTypeList(updateItem.getItemCategory());
                TypeListAdapter listAdapter = new TypeListAdapter(MainActivity.this, mTypeList);
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
                                Toast.makeText(MainActivity.this, "此类型已经存在，添加失败", Toast.LENGTH_LONG).show();
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
                            Toast.makeText(MainActivity.this, "类型名不能为空", Toast.LENGTH_LONG).show();
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
                        AlertDialog.Builder updateTypeBuilder = new AlertDialog.Builder(MainActivity.this);
                        LayoutInflater updateTypeInflate = LayoutInflater.from(MainActivity.this);
                        View updateTypeView = updateTypeInflate.inflate(R.layout.manage_type, null);
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
                                        Toast.makeText(MainActivity.this, "此类型已经存在，修改失败", Toast.LENGTH_LONG).show();
                                    } else {
                                        update.setTypeName(updateType.getText().toString());
                                        mDatabaseHelper.updateType(update, "Type" + category);
//                                                setTypeList(updateItem.getItemCategory());
                                        //刷新列表
                                        listAdapter.notifyDataSetChanged();
                                    }
                                } else {    //类型名为空时，不能修改
                                    Toast.makeText(MainActivity.this, "不能修改为空", Toast.LENGTH_LONG).show();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
