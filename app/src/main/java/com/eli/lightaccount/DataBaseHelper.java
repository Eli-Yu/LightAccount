package com.eli.lightaccount;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class DataBaseHelper extends SQLiteOpenHelper {

    /**
     * 获取默认支付类型的数量
     * @return 支付类型的数量
     */
    public static int getTypePaymentLength() {
        return typePayment.length;
    }

    /**
     * 获取默认收入类型的数量
     * @return 收入类型的数量
     */
    public static int getTypeIncomeLength() {
        return typeIncome.length;
    }

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


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

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

    /**
     * 删除类型
     * @param table 表类型：收入还是支出，函数中连接为表名
     * @param id 类型记录的id
     */
    public void deleteType(String table, String id) {
        SQLiteDatabase database = getWritableDatabase();
        database.delete(table, "id = ?", new String[] {id});
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
    public void updateType(TypeBean typeBean, String table) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues type_cv = new ContentValues();
        type_cv.put("name", typeBean.getTypeName());
        database.update(table, type_cv, "id = ?" ,new String[] {typeBean.getTypeId()});
    }

    //获取所有账目数据
    public Cursor getAllItemData(String table) {
        SQLiteDatabase database = getWritableDatabase();
        return database.query(table,null,null,null,null,null,"date ASC");
    }

    //获取所有类型数据
    public Cursor getAllTypeData(String table) {
        SQLiteDatabase database = getWritableDatabase();
        return database.query(table, null, null ,null ,null, null, null);
    }

    //查询账目数据
    public Cursor queryItemData(String table, boolean[] columns, String[] condition) {
        SQLiteDatabase database = getWritableDatabase();
        String[] allCol = new String[]{"type", "money", "note","date"};
        String col = new String();
        int sum = 0;
        for (boolean b: columns) if (b) sum++;
        for (int i = 0; i < columns.length; i++)
            if (columns[i]) {
                col = col + allCol[i] + "=? ";
                if (--sum > 0) col += "and ";
            }
        return database.query(table,null, col, condition,null,null,"date ASC");
    }

    /**
     * 查询账目数据
     * @param table 表名
     * @param columns 查询条件表达式
     * @param condition 查询条件具体的值
     * @return 包含查询结果的Cursor游标
     */
    public Cursor queryItemData(String table, String columns, String[] condition) {
        SQLiteDatabase database = getWritableDatabase();
        return database.query(table,null, columns, condition,null,null,"date ASC");
    }

    /**
     * 导入数据方法
     * @param table 导入数据的表名
     * @param fields 导入数据的字段名
     * @param data 导入数据的数据内容
     */
    public void importData(String table, String fields, List<String[]> data) {
        SQLiteDatabase database = getWritableDatabase();
        StringBuffer sql = new StringBuffer();
        sql.append("insert into ");
        sql.append(table);
        sql.append("(");
        sql.append(fields);
        sql.append(") values(");
        for(int i = 0; i < fields.split(",").length - 1; i++) {
            sql.append("?, ");
        }
        sql.append("?)");
        for (String[] d: data) {
            Log.i("add",sql.toString());
            for (String str: d)
                Log.i("add", str);
            Log.i("add","NEXT");
            database.execSQL(sql.toString(), d);
        }
    }

    /**
     * 导入数据方法
     * @param table 导入数据的表名
     * @param fields 导入数据的字段名
     * @param data 导入数据的数据内容
     */
//    public void importData(String table, String fields, List<String> data) {
//        SQLiteDatabase database = getWritableDatabase();
//        StringBuffer sql = new StringBuffer();
//
////        for(int i = 0; i < fields.split(",").length - 1; i++) {
////            sql.append("?, ");
////        }
////        sql.append("?)");
//        for (String d: data) {
////            String test = sql.toString();
////            database.execSQL("insert into Payment(id, type, date,  money, note) values(?, ?, ?, ?, ?)", d);
//
//            sql.append("insert into ");
//            sql.append(table);
//            sql.append("(");
//            sql.append(fields);
//            sql.append(") values(");
//            String[] strings = d.split(",");
//            for(String s: strings) {
//                sql.append("\'");
//                sql.append(s);
//                sql.append("\',");
//            }
//            sql.deleteCharAt(sql.length() - 1);
//            sql.append(")");
////            Log.i("add",sql.toString());
////            Log.i("add","NEXT");
//            database.execSQL(sql.toString());
//            sql.delete(0,sql.length());
//        }
//    }

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
