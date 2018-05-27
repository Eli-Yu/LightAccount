package com.eli.lightaccount;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.sql.RowSetEvent;

// TODO: 2018/5/7 或许可以实现可折叠性标题栏 和 浮动按钮提示

public class MainActivity extends AppCompatActivity {

    private List<ItemBean> mItemBeanList;
    private List<String> mTypeString;
    private List<TypeBean> mTypeList;
    private DataBaseHelper mDatabaseHelper;
    private ItemListAdapter mAdapter;
    private String itemCategory = "Payment";

    private String appDir;

    //存储权限管理类
    private StorageManager mStorageManager;
    //授权请求码
    private static final int PERMISSION_REQUEST_CODE = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            appDir = Environment.getExternalStorageDirectory().getCanonicalFile() + "/LightAccount";
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("app",appDir);

        //检测是否有外部存储的写权限，若没有则请求获取
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            //没有授权，则请求授权
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            //已经授权时候,创建应用文件夹，用于存放导入导出的csv文件
            createAppDir();
        }


        // TODO: 2018/5/7 限定一次加载的账目数量，而不是加载所有数据，从而加快加载速度，可以添加下拉（上拉刷新功能）

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
                View viewUpdate = updateInflater.inflate(R.layout.new_item_data, parent, false);
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
//                                View updateTypeView = updateTypeInflate.inflate(R.layout.manage_type, parent, false);
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

//                Toast.makeText(MainActivity.this, updateItem.getItemId(),Toast.LENGTH_LONG).show();

                updateBuilder.setTitle("修改记录");

                //设定确认事件
                updateBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (!money.getText().toString().isEmpty() && Float.parseFloat(money.getText().toString()) > 0) {
                            //如果修改后金额不为空或零，更新数据
                            updateItem.setItemType(type.getSelectedItem().toString());
                            updateItem.setItemMoney(money.getText().toString());
                            updateItem.setItemNote(note.getText().toString());
                            String month = date.getMonth() < 9 ? "0" + (date.getMonth() + 1) : Integer.toString(date.getMonth() + 1);
                            String day = date.getDayOfMonth() < 10 ? "0" + date.getDayOfMonth(): Integer.toString(date.getDayOfMonth());
                            updateItem.setItemDate(date.getYear() + "-" + month  + "-" + day);
                            mDatabaseHelper.updateItem(updateItem, updateItem.getItemCategory(), updateItem.getItemId());
                            //更新列表
                            mAdapter.notifyDataSetChanged();
//                        long timeStamp = System.currentTimeMillis();
//                        Date d = new Date(timeStamp);
//                        Toast.makeText(MainActivity.this, Long.toString(timeStamp)+ "和" + d, Toast.LENGTH_LONG ).show();
                        } else {
                            Toast.makeText(MainActivity.this, "金额不能为空且需大于0", Toast.LENGTH_LONG).show();
                        }
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

                if (category.getCheckedRadioButtonId() == R.id.radio_button_payment)
                    setTypeList("Payment");
                else setTypeList("Income");
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
                        //对日期进行处理，默认月份为0-11，改为1-12，同时把时间按YYYY-MM-dd的格式来存储
                        String month = date.getMonth() < 9 ? "0" + (date.getMonth() + 1) : Integer.toString(date.getMonth() + 1);
                        String day = date.getDayOfMonth() < 10 ? "0" + date.getDayOfMonth(): Integer.toString(date.getDayOfMonth());
                        itemBean.setItemDate(date.getYear() + "-" + month  + "-" + day);

                        /// 检测金额是否为空，为空则取消操作
                        if (!itemBean.getItemMoney().isEmpty() && Float.parseFloat(money.getText().toString()) > 0) {
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
//                            Toast.makeText(MainActivity.this, type.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
                            Toast.makeText(MainActivity.this, "金额不能为空且需大于0", Toast.LENGTH_LONG).show();
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
                AlertDialog.Builder builderQuery = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflaterQuery = LayoutInflater.from(MainActivity.this);
                View viewQuery = inflaterQuery.inflate(R.layout.query, null);

                RadioGroup category = viewQuery.findViewById(R.id.radio_group);
                Switch switchCategory = viewQuery.findViewById(R.id.switch_category);

                Spinner type = viewQuery.findViewById(R.id.spinner_type);
                Switch switchType = viewQuery.findViewById(R.id.switch_type);

                EditText money = viewQuery.findViewById(R.id.editText_money);
                Switch switchMoney = viewQuery.findViewById(R.id.switch_money);

                EditText note = viewQuery.findViewById(R.id.editText_note);
                Switch switchNote = viewQuery.findViewById(R.id.switch_note);

                DatePicker date = viewQuery.findViewById(R.id.datePicker_date);
                Switch switchDate = viewQuery.findViewById(R.id.switch_date);

                ItemBean itemBean = new ItemBean();

                //初始化显示
//                category.setEnabled(switchCategory.isChecked());
                //设定单选按钮组的可用性
                for (int i = 0; i < category.getChildCount(); i++) {
                    category.getChildAt(i).setEnabled(switchCategory.isChecked());
                }
                type.setEnabled(switchType.isChecked());
                money.setEnabled(switchMoney.isChecked());
                note.setEnabled(switchNote.isChecked());
                date.setEnabled(switchDate.isChecked());


                /**
                 * 设定各个switch按钮的状态变化监听器，实现动态控制各个控件的可用性变化
                 */
                switchCategory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        for (int i = 0; i < category.getChildCount(); i++) {
                            category.getChildAt(i).setEnabled(switchCategory.isChecked());
                        }
                    }
                });

                switchType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        type.setEnabled(switchType.isChecked());
                    }
                });

                switchMoney.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        money.setEnabled(switchMoney.isChecked());
                    }
                });

                switchNote.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        note.setEnabled(switchNote.isChecked());
                    }
                });

                switchDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        date.setEnabled(switchDate.isChecked());
                    }
                });

                //设置类型下拉框的项目，从Type表中获取
                if (category.getCheckedRadioButtonId() == R.id.radio_button_payment)
                    setTypeList("Payment");
                else setTypeList("Income");

                //建立Spinner的监听器，用来确定获取到的类型
                final ArrayAdapter<String> adapter;
                adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, mTypeString);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                type.setAdapter(adapter);

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
                                break;
                            case R.id.radio_button_payment:
                                itemCategory = "Payment";
                                break;
                            default:
                                return;
                        }
                        setTypeList(itemCategory);
                        type.setAdapter(adapter);
                    }
                });


                //将布局设置给Dialog
                builderQuery.setView(viewQuery);
                //设置对话框标题
                builderQuery.setTitle("查询");

                builderQuery.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

//                        boolean[] columns = new boolean[4];
//                        columns[0] = switchType.isChecked();
//                        columns[1] = switchMoney.isChecked();
//                        columns[2] = switchNote.isChecked();
//                        columns[3] = switchDate.isChecked();
//                        boolean isQuery = columns[0];
//                        for (boolean b : columns)
//                            isQuery = isQuery || b;
////                        String temp = new String();
////                        for (boolean b: columns)
////                            temp += Boolean.toString(b) ;
//                        Log.i("temp", Boolean.toString(isQuery));
////                        Toast.makeText(MainActivity.this, temp, Toast.LENGTH_LONG).show();
//                        if (switchCategory.isChecked() || isQuery)  {
//                            if (!isQuery)
//                                mQueryList = mItemBeanList;
//                            else {
//                                String[] condition = new String[]{type.getSelectedItem().toString(), money.getText().toString(),
//                                note.getText().toString(), };
//                                condition
//                            }
//                        }

                        //创建一个StringBuffer对象用于拼接查询语句
                        StringBuffer col = new StringBuffer();
                        List<String> columns = new ArrayList<>();
                        List<String> condition = new ArrayList<>();
                        List<ItemBean> mQueryList = new ArrayList<>();
                        if (switchType.isChecked() || switchMoney.isChecked() || switchNote.isChecked() || switchDate.isChecked()) {
                            if (switchType.isChecked()) {
                                columns.add("type");
                                condition.add(type.getSelectedItem().toString());
                            }
                            if (switchMoney.isChecked()) {
                                columns.add("money");
                                condition.add(money.getText().toString());
                            }
                            if (switchNote.isChecked()) {
                                columns.add("note");
                                condition.add(note.getText().toString());
                            }
                            if (switchDate.isChecked()) {
                                columns.add("date");
                                //对日期进行处理，默认月份为0-11，改为1-12，同时把时间按YYYY-MM-dd的格式来存储
                                String month = date.getMonth() < 9 ? "0" + (date.getMonth() + 1) : Integer.toString(date.getMonth() + 1);
                                String day = date.getDayOfMonth() < 10 ? "0" + date.getDayOfMonth(): Integer.toString(date.getDayOfMonth());
                                condition.add(date.getYear() + "-" + month + "-" + day);
                            }
                        }
                        if (switchCategory.isChecked()) { //如果类别选项开启，分类查询（支出或收入）
                            if (columns.isEmpty()) { //如果其他限定条件不设置时候，查询该类别下所有数据
                                if (category.getCheckedRadioButtonId() == R.id.radio_button_payment) {
                                    getItemData("Payment", mQueryList);
                                } else {
                                    getItemData("Income", mQueryList);
                                }
                            } else {//拼接查询条件
                                if (columns.size() >= 2) {
                                    for (int i = 0; i < columns.size() - 1; i++) {
                                        col.append(columns.get(i));
                                        col.append(" = ? and ");
//                                        col = columns.get(i).toString() + " = ? and";
                                    }
//                                    col = columns.get(columns.size() - 1) + " = ?";
                                    col.append(columns.get(columns.size() - 1));
                                    col.append(" = ?");
                                } else {
//                                    col = columns.get(0) + " = ?";
                                    col.append(columns.get(0));
                                    col.append(" = ?");
                                }

                                //按类别查询
                                Cursor cursor;
                                String table;
                                if (category.getCheckedRadioButtonId() == R.id.radio_button_payment) {
                                    table = "Payment";
                                } else {
                                    table = "Income";
                                }
//                                Log.i("query", col.toString());
                                cursor = mDatabaseHelper.queryItemData(table, col.toString(), condition.toArray(new String[condition.size()]));
                                if (cursor != null) {
                                    while (cursor.moveToNext()) {
                                        ItemBean itemBean = new ItemBean();
                                        itemBean.setItemId(cursor.getString(cursor.getColumnIndex("id")));
                                        itemBean.setItemType(cursor.getString(cursor.getColumnIndex("type")));
                                        itemBean.setItemNote(cursor.getString(cursor.getColumnIndex("note")));
                                        itemBean.setItemDate(cursor.getString(cursor.getColumnIndex("date")));
                                        itemBean.setItemMoney(cursor.getString(cursor.getColumnIndex("money")));
                                        itemBean.setItemCategory(table);
                                        mQueryList.add(itemBean);
                                    }
                                    cursor.close();
                                }
                            }
                        } else { //类别选项不开启时候，同时查询两个类别中满足条件的结果
                            if (!columns.isEmpty()) {   //拼接查询条件
                                if (columns.size() >= 2) {
                                    for (int i = 0; i < columns.size() - 1; i++) {
//                                        col = columns.get(i) + " = ? and";
                                        col.append(columns.get(i));
                                        col.append(" = ? and ");
                                    }
//                                    col = columns.get(columns.size() - 1) + " = ?";
                                    col.append(columns.get(columns.size() - 1));
                                    col.append(" = ?");
                                } else {
//                                    col = columns.get(0) + " = ?";
                                    col.append(columns.get(0));
                                    col.append(" = ?");
                                }
                                Cursor cursor;
                                cursor = mDatabaseHelper.queryItemData("Payment", col.toString(), condition.toArray(new String[condition.size()]));
                                if (cursor != null) {
                                    while (cursor.moveToNext()) {
                                        ItemBean itemBean = new ItemBean();
                                        itemBean.setItemId(cursor.getString(cursor.getColumnIndex("id")));
                                        itemBean.setItemType(cursor.getString(cursor.getColumnIndex("type")));
                                        itemBean.setItemNote(cursor.getString(cursor.getColumnIndex("note")));
                                        itemBean.setItemDate(cursor.getString(cursor.getColumnIndex("date")));
                                        itemBean.setItemMoney(cursor.getString(cursor.getColumnIndex("money")));
                                        itemBean.setItemCategory("Payment");
                                        mQueryList.add(itemBean);
                                    }
                                    cursor.close();
                                }
                                cursor = mDatabaseHelper.queryItemData("Income", col.toString(), condition.toArray(new String[condition.size()]));
                                if (cursor != null) {
                                    while (cursor.moveToNext()) {
                                        ItemBean itemBean = new ItemBean();
                                        itemBean.setItemId(cursor.getString(cursor.getColumnIndex("id")));
                                        itemBean.setItemType(cursor.getString(cursor.getColumnIndex("type")));
                                        itemBean.setItemNote(cursor.getString(cursor.getColumnIndex("note")));
                                        itemBean.setItemDate(cursor.getString(cursor.getColumnIndex("date")));
                                        itemBean.setItemMoney(cursor.getString(cursor.getColumnIndex("money")));
                                        itemBean.setItemCategory("Income");
                                        mQueryList.add(itemBean);
                                    }
                                    cursor.close();
                                }
                            } else {    //不设置任何限定条件时候，查询所有数据
                                getItemData("Payment", mQueryList);
                                getItemData("Income", mQueryList);
                            }

                            //因为同时查询了两个类别的数据（即收入表和支出表），所以对查询结果按时间进行排序
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                //Java8中支持的Lambda排序
                                mQueryList.sort((ItemBean i1, ItemBean i2) -> i2.getItemDate().compareTo(i1.getItemDate()));
                            } else {
                                //不支持Java8的环境下使用，传统的新建匿名内部类排序
                                new Comparator<ItemBean>() {
                                    @Override
                                    public int compare(ItemBean i1, ItemBean i2) {
                                        return i2.getItemDate().compareTo(i1.getItemDate());
                                    }
                                };

                                Collections.sort(mQueryList, new Comparator<ItemBean>() {
                                    @Override
                                    public int compare(ItemBean o1, ItemBean o2) {
                                        return o1.getItemDate().compareTo(o2.getItemDate());
                                    }
                                });
                            }

                        }


                        Intent intent = new Intent(MainActivity.this, QueryActivity.class);
                        intent.putExtra("item_list", (Serializable) mQueryList);
                        mDatabaseHelper.close();
                        startActivity(intent);

//                        Toast.makeText(MainActivity.this, Boolean.toString(switchMoney.isChecked()), Toast.LENGTH_LONG).show();
                    }
                });

                builderQuery.setNegativeButton("取消", null);

                builderQuery.create().show();
            }
        });

    }


    // TODO: 2018/5/7 实现数据同步的关键，现在为重新从数据库加载数据，耗时大，可考虑优化

    /**
     * 重写onRestart函数，使得能够刷新账目列表
     */
    @Override
    protected void onRestart() {
        super.onRestart();

        //刷新账目列表
        initItemData();
        mAdapter.notifyDataSetChanged();
    }


//    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("import", "grant success.");
                    //授权成功
                    importData();
                } else {
                    Toast.makeText(MainActivity.this, "没有权限", Toast.LENGTH_LONG).show();
                }
                break;
            default:
        }
    }

    public static int getPermissionRequestCode() {
        return PERMISSION_REQUEST_CODE;
    }


    private void createAppDir() {
        File test = new File(appDir);
        if (!test.exists()) {
            boolean isSuccess = test.mkdirs();
            if (isSuccess) {
                Toast.makeText(MainActivity.this,"创建成功",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "创建失败，没有写入外部存储的权限，请赋予权限",Toast.LENGTH_LONG).show();
            }
        }
    }


    /**
     * 数据导入方法
     */
    private void importData() {
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder row = new StringBuilder();
        Log.i("import", "start");
        try {
            String filename = appDir + "/" + "import.csv";
//            in = openFileInput("import.csv");
            in = new FileInputStream(filename);
            Log.i("import", "find it");
            reader = new BufferedReader(new InputStreamReader(in));

            List<String[]> data = new ArrayList<>();
//            String[] singleData = new String[]{};
            String line = "";
            String table = "";
            List<String> all = new ArrayList<>();
            List<Integer> index = new ArrayList<>();
            int i = 0;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Table") && !line.contains(",")) {
                    index.add(i);
                }
                i++;

                all.add(line);
            }
            in.close();
            for (String str:all)
                Log.i("import",str);
            //创建一个用于保存Table的index的数组
//            int[] index = new int[4];
//            int i = 0;
//            for (int j = 0; j < all.size() && i < 4; j++) {
//                if (all.get(j).contains("Table") && !all.get(j).contains(",")) {
//                    index[i++] = j;
//                }
//            }

            for(int a: index)
                Log.i("import",Integer.toString(a));

            for (int j = 0; j < index.size() - 1; j++) {
                table = all.get(index.get(j)).substring(6);
                String fields = all.get(index.get(j) + 1);
                for (int k = index.get(j) + 2; k < index.get(j + 1); k++) {
                    data.add(all.get(k).split(","));
                }
                Log.i("im", table);
                Log.i("im", fields);
                for (String str[]: data)
                    for(String s: str)
                        Log.i("im",s);
                try {
                    mDatabaseHelper.importData(table, fields, data);
                    data.clear();
                    Log.i("im","clear");
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "导入失败，可能存在重复记录1。", Toast.LENGTH_SHORT).show();
                    Log.i("failed",Integer.toString(j) + "不开心");
                    e.printStackTrace();
                }
            }
//            data.clear();
            table = all.get(index.get(index.size() - 1)).substring(6);
            String fields = all.get(index.get(index.size() - 1) + 1);
            for (int k = index.get(index.size() - 1) + 2; k < all.size(); k++) {
                data.add(all.get(k).split(","));
            }

            Log.i("im2", table);
            Log.i("im2", fields);
            for (String str[]: data)
                for(String s: str)
                    Log.i("im2",s);

            try {
                mDatabaseHelper.importData(table, fields, data);
                //导入后重新加载数据
                initItemData();
                mAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "导入成功。", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "导入失败，可能存在重复记录2。", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }


            Log.i("im", table);
            Log.i("im", fields);
            for (String str[]: data)
                for(String s: str)
                    Log.i("im",s);
//            List<String> data = new ArrayList<>();
//            String line = "";
//            String table = "";
//            List<String> all = new ArrayList<>();
//            List<Integer> index = new ArrayList<>();
//            int i = 0;
//            while ((line = reader.readLine()) != null) {
//                if (line.contains("Table") && !line.contains(",")) {
//                    index.add(i);
//                }
//                i++;
//
//                all.add(line);
//            }
//            in.close();
//
//            for (int j = 0; j < index.size() - 1; j++) {
//                table = all.get(index.get(j)).substring(6);
//                String fields = all.get(index.get(j) + 1);
//                for (int k = index.get(j) + 2; k < index.get(j + 1); k++) {
//                    data.add(all.get(k));
//                }
//                mDatabaseHelper.importData(table, fields, data);
//                data.clear();
//            }
//            table = all.get(index.get(index.size() - 1)).substring(6);
//            String fields = all.get(index.get(index.size() - 1) + 1);
//            for(int k = index.get(index.size() - 1) + 2; k < all.size(); k++) {
//                data.add(all.get(k));
//            }
//            mDatabaseHelper.importData(table, fields, data);
        } catch (FileNotFoundException e) {
            Log.i("error","文件找不到");
            Toast.makeText(MainActivity.this, "import.csv文件找不到", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
            Log.i("error","路径访问失败，请授权");
            Toast.makeText(MainActivity.this, "路径访问失败，请授予文件读写权限", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * 数据导出方法
     *
     * @throws IOException
     */
    private void exportData() throws IOException {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        StringBuffer row = new StringBuffer();
        try {
//            String filename = Environment.getExternalStorageDirectory().getCanonicalPath() + "/" + "import.csv";
            String filename = appDir + "/export.csv";
            Log.i("dir",filename);
//            out = openFileOutput("export.csv", Context.MODE_PRIVATE);
            out = new FileOutputStream(filename);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            Cursor cursor = mDatabaseHelper.getAllItemData("Payment");
            if (cursor != null) {
                row.append("Table Payment\n");
                row.append("id, ");
                row.append("type, ");
                row.append("date,  ");
                row.append("money, ");
                row.append("note\n");
                while (cursor.moveToNext()) {
                    row.append(cursor.getString(cursor.getColumnIndex("id")));
                    row.append(",");
                    row.append(cursor.getString(cursor.getColumnIndex("type")));
                    row.append(",");
                    row.append(cursor.getString(cursor.getColumnIndex("date")));
                    row.append(",");
                    row.append(cursor.getString(cursor.getColumnIndex("money")));
                    row.append(",");
                    row.append(cursor.getString(cursor.getColumnIndex("note")));
                    row.append("\n");
                }
                cursor.close();
            }
            cursor = mDatabaseHelper.getAllItemData("Income");
            if (cursor != null) {
                row.append("Table Income\n");
                row.append("id, ");
                row.append("type, ");
                row.append("date,  ");
                row.append("money, ");
                row.append("note\n");
                while (cursor.moveToNext()) {
                    row.append(cursor.getString(cursor.getColumnIndex("id")));
                    row.append(",");
                    row.append(cursor.getString(cursor.getColumnIndex("type")));
                    row.append(",");
                    row.append(cursor.getString(cursor.getColumnIndex("date")));
                    row.append(",");
                    row.append(cursor.getString(cursor.getColumnIndex("money")));
                    row.append(",");
                    row.append(cursor.getString(cursor.getColumnIndex("note")));
                    row.append("\n");
                }
                cursor.close();
            }
            cursor = mDatabaseHelper.getAllTypeData("TypePayment");
            if (cursor != null) {
                row.append("Table TypePayment\n");
                row.append("id, ");
                row.append("name\n");
                while (cursor.moveToNext()) {
                    if (cursor.getColumnIndex("id") > DataBaseHelper.getTypePaymentLength()) {
                        row.append(cursor.getString(cursor.getColumnIndex("id")));
                        row.append(",");
                        row.append(cursor.getString(cursor.getColumnIndex("name")));
                        row.append("\n");
                    }
                }
                cursor.close();
            }
            cursor = mDatabaseHelper.getAllTypeData("TypeIncome");
            if (cursor != null) {
                row.append("Table TypeIncome\n");
                row.append("id, ");
                row.append("name\n");
                while (cursor.moveToNext()) {
                    if (cursor.getColumnIndex("id") > DataBaseHelper.getTypeIncomeLength()) {
                        row.append(cursor.getString(cursor.getColumnIndex("id")));
                        row.append(",");
                        row.append(cursor.getString(cursor.getColumnIndex("name")));
                        row.append("\n");
                    }
                }
                cursor.close();
            }
            writer.write(row.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                    Toast.makeText(MainActivity.this, "导出成功。", Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void getItemData(String table, List<ItemBean> list) {
        Cursor cursor = mDatabaseHelper.getAllItemData(table);

        // TODO: 2018/5/7 可以吧这里提出出来作为单独函数处理，进一步提高代码的可重用性
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ItemBean itemBean = new ItemBean();
                itemBean.setItemId(cursor.getString(cursor.getColumnIndex("id")));
                itemBean.setItemType(cursor.getString(cursor.getColumnIndex("type")));
                itemBean.setItemNote(cursor.getString(cursor.getColumnIndex("note")));
                itemBean.setItemDate(cursor.getString(cursor.getColumnIndex("date")));
                itemBean.setItemMoney(cursor.getString(cursor.getColumnIndex("money")));
                itemBean.setItemCategory(table);
                list.add(itemBean);
            }
            cursor.close();
        }
    }

    private void initItemData() {
//        for (int i = 0; i < 5; ++i) {
//            ItemBean itemBean = new ItemBean();
//            itemBean.setItemType(Integer.toString(i));
//            itemBean.setItemDate("2018-04-30");
//            itemBean.setItemMoney("15");
//            mDatabaseHelper.insertItem(itemBean,"Income");
//        }
//        mItemBeanList.clear();
//        Cursor cursor = mDatabaseHelper.getAllItemData("Payment");
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                ItemBean itemBean = new ItemBean();
//                itemBean.setItemId(cursor.getString(cursor.getColumnIndex("id")));
//                itemBean.setItemType(cursor.getString(cursor.getColumnIndex("type")));
//                itemBean.setItemNote(cursor.getString(cursor.getColumnIndex("note")));
//                itemBean.setItemDate(cursor.getString(cursor.getColumnIndex("date")));
//                itemBean.setItemMoney(cursor.getString(cursor.getColumnIndex("money")));
//                itemBean.setItemCategory("Payment");
//                mItemBeanList.add(itemBean);
//            }
//            cursor.close();
//        }
//        cursor = mDatabaseHelper.getAllItemData("Income");
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                ItemBean itemBean = new ItemBean();
//                itemBean.setItemId(cursor.getString(cursor.getColumnIndex("id")));
//                itemBean.setItemType(cursor.getString(cursor.getColumnIndex("type")));
//                itemBean.setItemNote(cursor.getString(cursor.getColumnIndex("note")));
//                itemBean.setItemDate(cursor.getString(cursor.getColumnIndex("date")));
//                itemBean.setItemMoney(cursor.getString(cursor.getColumnIndex("money")));
//                itemBean.setItemCategory("Income");
//                mItemBeanList.add(itemBean);
//            }
//            cursor.close();
//        }
        if(!mItemBeanList.isEmpty()) {
            //mItemBeanList不为空时候清空list
            mItemBeanList.clear();
        }
        getItemData("Payment", mItemBeanList);
        getItemData("Income", mItemBeanList);
        //排序对数据按照时间排序
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //Java8中支持的Lambda排序
            mItemBeanList.sort((ItemBean i1, ItemBean i2) -> i2.getItemDate().compareTo(i1.getItemDate()));
        } else {
            //不支持Java8的环境下使用，传统的新建匿名内部类排序
            new Comparator<ItemBean>() {
                @Override
                public int compare(ItemBean i1, ItemBean i2) {
                    return i2.getItemDate().compareTo(i1.getItemDate());
                }
            };

            Collections.sort(mItemBeanList, new Comparator<ItemBean>() {
                @Override
                public int compare(ItemBean o1, ItemBean o2) {
                    return o2.getItemDate().compareTo(o1.getItemDate());
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
                                        Toast.makeText(MainActivity.this, "此类型已经存在，修改失败", Toast.LENGTH_LONG).show();
                                    } else {
                                        update.setTypeName(updateType.getText().toString());
                                        mDatabaseHelper.updateType(update, "Type" + category);

                                        //将list中的对应项也同步更新
                                        mTypeString.set(position, update.getTypeName());
                                        mTypeList.set(position, update);
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
        if (id == R.id.action_charts) {

            Intent intent = new Intent(MainActivity.this, ChartActivity.class);

//            List<ItemBean> monthPaymentList = new ArrayList<>();
//            List<ItemBean> monthIncomeList = new ArrayList<>();
//            getMonthData("Payment");
            //通过serializable的方式将当前月份的数据传递
            intent.putExtra("payment_list", (Serializable) getMonthData("Payment"));
            Log.i("data","paymentList");
//            getMonthData("Income");
            intent.putExtra("income_list", (Serializable) getMonthData("Income"));
            Log.i("data", "incomeList");
//            mDatabaseHelper.close();
            startActivity(intent);
            return true;
        }


        //导出功能
        if (id == R.id.action_export) {
            //检测是否有外部存储的写权限，若没有则请求获取
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                //没有授权，则请求授权
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                //已经授权时候,
                try {
                    exportData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }


        //导入功能
        if (id == R.id.action_import) {
            //检测是否有外部存储的写权限，若没有则请求获取
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                //没有授权，则请求授权
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                //已经授权时候,
                importData();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 获取当前月份的数据
     * @param table 表名
     * @return 包含当前月份数据的list
     */
    private List<ItemBean> getMonthData(String table) {

        Date currentDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
//            Toast.makeText(MainActivity.this, dateFormat.format(currentDate),Toast.LENGTH_LONG).show();
        Cursor cursor = mDatabaseHelper.getMonthData(table, simpleDateFormat.format(currentDate));
        List<ItemBean> itemBeanList = new ArrayList<>();
        if (cursor != null) {
//            //如果列表不为空，则先清空，防止数据混乱
//            if (!list.isEmpty()) {
//                list.clear();
//            }
            while (cursor.moveToNext()) {
                ItemBean itemBean = new ItemBean();
                itemBean.setItemId(cursor.getString(cursor.getColumnIndex("id")));
                itemBean.setItemType(cursor.getString(cursor.getColumnIndex("type")));
                itemBean.setItemMoney(cursor.getString(cursor.getColumnIndex("money")));
                itemBean.setItemNote(cursor.getString(cursor.getColumnIndex("note")));
                itemBean.setItemDate(cursor.getString(cursor.getColumnIndex("date")));
                itemBean.setItemCategory(table);
                itemBeanList.add(itemBean);
            }
            cursor.close();
        }
        return itemBeanList;
    }
}