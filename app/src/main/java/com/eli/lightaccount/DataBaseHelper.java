package com.eli.lightaccount;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String[] typePayment = new String[]{"三餐","饮料","水果","零食","买菜","交通","电影",
            "游戏","演出","学习","数码","运动","医疗","住房","维修",
            "保险","话费","家居","水电网","日用品","衣物鞋包","护肤彩妆"};
    private static final String[] typeIncome = new String[]{"工资","兼职","理财","红包","奖金","报销","生活费"};

    public static final String CREATE_TYPE_INCOME = "create table if not exists TypeIncome ("
            + "id integer primary key,"
            + "name text)";
    public static final String CREATE_TYPE_PAYMENT = "create table if not exists TypePayment ("
            + "id integer primary key,"
            + "name text)";
    public static final String CREATE_INCOME = "create table if not exists Income ("
            + "id integer primary key,"
            + "type integer not null,"
            + "date text not null,"
            + "money numeric not null,"
            + "note text,"
            + "foreign key(type) references TypeIncome(id))";
    public static final String CREATE_PAYMENT = "create table if not exists Payment ("
            + "id integer primary key,"
            + "type integer not null,"
            + "date text not null,"
            + "money numeric not null,"
            + "note text,"
            + "foreign key(type) references TypePayment(id))";

    private Context mContext;

    public DataBaseHelper(Context context) {
        super(context, "LightAccountDB", null, 1);
        mContext = context;
    }

    //创建数据库
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TYPE_INCOME);
            db.execSQL(CREATE_TYPE_PAYMENT);
            db.execSQL(CREATE_INCOME);
            db.execSQL(CREATE_PAYMENT);

            //初始化TypePayment表
            initTypePayment(db);

            //初始化TypeIncome表
            initTypeIncome(db);


            Toast.makeText(mContext,"成功创建数据库", Toast.LENGTH_SHORT).show();
//            Log.i("DB","success");
        } catch (android.database.SQLException e) {
//            Log.i("DB","failed");
            Toast.makeText(mContext,"创建数据库失败",Toast.LENGTH_LONG).show();
        }
    }

    //增加账目
    public void insertItem(ItemBean itemBean, String table) {
        //获取一个可写的数据库对象
        SQLiteDatabase database = getWritableDatabase();
        ContentValues item_cv = new ContentValues();
//        item_cv.put("id",itemBean.getItemId());
        item_cv.put("id",Long.parseLong(itemBean.getItemId()));
        item_cv.put("type", itemBean.getItemType());
        item_cv.put("note", itemBean.getItemNote());
        item_cv.put("date", itemBean.getItemDate());
        item_cv.put("money", itemBean.getItemMoney());
        database.insert(table, null, item_cv);
    }

    //增加类型
    public void insertType(TypeBean typeBean, String table) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues type_cv = new ContentValues();
        type_cv.put("id", Long.parseLong(typeBean.getTypeId()));
        type_cv.put("name", typeBean.getTypeName());
        database.insert(table, null, type_cv);
    }

    //删除数据
    public void delete(String table, String id) {
        SQLiteDatabase database = getWritableDatabase();
        database.delete(table, "id = ?", new String[]{id});
    }

    //修改账目数据
    public void updateItem(ItemBean itemBean, String table, String id) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues item_cv = new ContentValues();
        item_cv.put("type", itemBean.getItemType());
        item_cv.put("note", itemBean.getItemNote());
        item_cv.put("date", itemBean.getItemDate());
        item_cv.put("money", itemBean.getItemMoney());
        database.update(table,item_cv,"id = ?", new String[] {id});
    }

    //修改类型数据
    public void updateType(TypeBean typeBean, String id, String table) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues type_cv = new ContentValues();
        type_cv.put("name", typeBean.getTypeName());
        database.update(table, type_cv, "id = ?" ,new String[] {id});
    }

    //获取账目数据
    public Cursor getAllItemData(String table) {
        SQLiteDatabase database = getWritableDatabase();
        return database.query(table,null,null,null,null,null,"date ASC");
    }

    //获取类型数据
    public Cursor getAllTypeData(String table) {
        SQLiteDatabase database = getWritableDatabase();
        return database.query(table, null, null ,null ,null, null, null);
    }

    //查询账目数据
//    public Cursor queryItemData(ItemBean itemBean, String table) {
//        SQLiteDatabase database = getWritableDatabase();
//    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 初始化TypePayment数据库
     * @param db 数据库对象
     */
    public void initTypePayment(SQLiteDatabase db) {

        for(int i = 0; i<typePayment.length; i++)
            db.execSQL("insert into TypePayment(id,name) values(?,?)", new String[]{Long.toString(i+1),typePayment[i]});
    }

    /**
     * 初始化TypeIncome表
     * @param db 数据库对象
     */
    public void initTypeIncome(SQLiteDatabase db) {

        for(int i = 0; i<typeIncome.length; i++)
            db.execSQL("insert into TypeIncome(id,name) values(?,?)", new String[]{Long.toString(i+1),typeIncome[i]});
    }
}
