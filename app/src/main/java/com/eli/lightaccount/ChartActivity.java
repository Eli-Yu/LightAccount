package com.eli.lightaccount;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ChartActivity extends AppCompatActivity {

    private List<ItemBean> mPaymentList;
    private List<ItemBean> mIncomeList;
    //用Map来存放处理好的数据
    private Map<String, Float> mPaymentData = new TreeMap<>();
    private Map<String, Float> mIncomeData = new TreeMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        LineChart lineChartMonth = findViewById(R.id.lineChart_month);
        RadioGroup category = findViewById(R.id.radio_group_chart);

        //从intent中获取serializable数据
        mPaymentList = (List<ItemBean>) getIntent().getSerializableExtra("payment_list");
        mIncomeList = (List<ItemBean>) getIntent().getSerializableExtra("income_list");

        //数据处理
        preProcessByDay(mPaymentList, mPaymentData);
        preProcessByDay(mIncomeList, mIncomeData);

        //默认显示支出图
        generateLineChart(lineChartMonth, mPaymentData, ((RadioButton)findViewById(category.getCheckedRadioButtonId())).getText().toString());

        /**
         * 设置一个单选按钮组的监听器，
         * 监听账目分类选择变化事件，
         * 为了实现根据选中情况，动态linechart
         */

        category.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_button_income:
                        generateLineChart(lineChartMonth, mIncomeData, ((RadioButton)findViewById(checkedId)).getText().toString());
                        break;
                    case R.id.radio_button_payment:
                        generateLineChart(lineChartMonth, mPaymentData, ((RadioButton)findViewById(checkedId)).getText().toString());
                        break;
                    default:
                        return;
                }
            }
        });



//        generateLineChart(lineChartMonth, mPaymentData);
//        generateLineChart(lineChartMonth, mIncomeData);


//        List<Entry> entries = new ArrayList<Entry>();
//        List<Entry> entries2 = new ArrayList<Entry>();
//        //遍历Map将数据添加到LineChart中
//        for (Map.Entry<String, Float> entry: mPaymentData.entrySet()) {
//            entries.add(new Entry(Float.parseFloat(entry.getKey()), entry.getValue()));
//        }
//        for (Map.Entry<String, Float> entry: mIncomeData.entrySet()) {
//            entries2.add(new Entry(Float.parseFloat(entry.getKey()), entry.getValue()));
//        }
//
//        LineDataSet setComp1 = new LineDataSet(entries, "支出");
//        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
//        setComp1.setColor(Color.RED);
//        LineDataSet setComp2 = new LineDataSet(entries2, "收入");
//        setComp2.setAxisDependency(YAxis.AxisDependency.LEFT);
//        setComp2.setColor(Color.GREEN);

        //得到X轴
        XAxis xAxis = lineChartMonth.getXAxis();
        //设定X轴在最下面
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //设定X轴最小间隔
        xAxis.setGranularity(1f);
        //设定X轴刻度数量
        xAxis.setLabelCount(31,false);
        //设定X轴最小值
        xAxis.setAxisMinimum(1f);


        //得到Y轴
        YAxis leftYAxis = lineChartMonth.getAxisLeft();
//        leftYAxis.setAxisMinimum(0f);
        //不显示右边的Y轴
        lineChartMonth.getAxisRight().setEnabled(false);

        //隐藏description
        lineChartMonth.getDescription().setEnabled(false);



//        // use the interface ILineDataSet
//        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
//        dataSets.add(setComp1);
//        dataSets.add(setComp2);
//
//        LineData data = new LineData(dataSets);
//        lineChartMonth.setData(data);
//        lineChartMonth.invalidate(); // refresh


    }

    /**
     * 生成linechart
     * @param lineChart linechart对象
     * @param map 数据map
     * @param label 图例标识
     */
    private void generateLineChart(LineChart lineChart, Map<String, Float> map, String label) {
        List<Entry> entries = new ArrayList<Entry>();

        //遍历Map将数据添加到LineChart中
        for (Map.Entry<String, Float> entry: map.entrySet()) {
            entries.add(new Entry(Float.parseFloat(entry.getKey()), entry.getValue()));
        }

        LineDataSet dataSet = new LineDataSet(entries, label); // add entries to dataset
        switch (label) {
            case "支出":
                dataSet.setColor(Color.RED);
                break;
            case "收入":
                dataSet.setColor(Color.GREEN);
                break;
            default:
                return;
        }
        dataSet.setValueTextColor(Color.BLUE); // styling, ...

        LineData lineData = new LineData(dataSet);

        if (!map.isEmpty()) {
            lineChart.setData(lineData);
            Log.i("data","lineChart");
        } else {
            Log.i("data","noLineChart");
            Toast.makeText(ChartActivity.this, "数据不够", Toast.LENGTH_LONG).show();
        }

        lineChart.invalidate(); // refresh
    }

    /**
     * 将数据按天进行处理，即将每天的数据按照金额累加，加入到map中
     * @param list 数据list
     * @param map 存储处理好的数据的map
     */
    private void preProcessByDay(List<ItemBean> list, Map<String, Float> map) {

        if (list != null) {
            for (ItemBean itemBean: list) {

                //从数据中的到日期和金额
                String day = itemBean.getItemDate().split("-")[2];
                float money = Float.parseFloat(itemBean.getItemMoney());

                //如果map中已经有当前日期的数据，那么将金额进行累加，否则直接将数据添加进入map中
                if (!map.containsKey(day)) {
                    map.put(day, money);
                } else {
                    float originMoney = map.get(day);
                    map.put(day, originMoney + money);
                }
            }
        }
    }
}
