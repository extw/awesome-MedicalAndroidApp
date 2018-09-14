package com.example.zjubme.teethmanagement;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CalendarActivity extends AppCompatActivity {
    private MaterialCalendarView materialCalendarView;
    private CalendarViewDate calendarViewDate = new CalendarViewDate();
    private List<CalendarViewDate> calendarViewDateList = new ArrayList<>();
    private List<CalendarDay> calendarDayList = new ArrayList<>();
    private SignDayDecorator signDayDecorator = new SignDayDecorator(CalendarActivity.this);
    private float timeData;
    private List<Long> timeDataList = new ArrayList<>();
    private List<BarEntry> entries = new ArrayList<>();

    public void loadtime() {
        AVQuery<AVObject> avQueryTime = new AVQuery<>("TimeData");
        Date NowTime = new Date();
        long Weekdata = NowTime.getTime()/1000/60/60/24/7;
        avQueryTime.whereEqualTo("WeekData", Weekdata);
        avQueryTime.orderByAscending("createdAt");
        avQueryTime.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                BarChart chart = (BarChart) findViewById(R.id.chart);
                if (list==null){
                    entries.add(new BarEntry(0,0f));
                    entries.add(new BarEntry(1,0f));
                    entries.add(new BarEntry(2,0f));
                    entries.add(new BarEntry(3,0f));
                    entries.add(new BarEntry(4,0f));
                    entries.add(new BarEntry(5,0f));
                    entries.add(new BarEntry(6,0f));
                    return;
                }
                if (e==null){
                    for (int k=0; k<list.size(); k++){
                        timeData = (float)(list.get(k).getLong("DaTime")/60.0);
                        entries.add(new BarEntry(k,timeData));
                    }
                    for (int k=6; k>list.size()-1; k--){
                        entries.add(new BarEntry(k,0f));
                    }
                }
                BarDataSet dataSet = new BarDataSet(entries,"");
                dataSet.setColor(Color.GREEN);//0x-(00--ff)-xxxxxx

                BarData data = new BarData(dataSet);
                data.setBarWidth(0.9f);
                data.setValueTextColor(Color.BLACK);
                data.setValueTextSize(12f);

                chart.setData(data);
                chart.setFitBars(true);
                chart.invalidate();
                chart.setScaleEnabled(false);
                chart.setDrawGridBackground(false);
                chart.setBorderColor(Color.BLUE);
                chart.setDrawBorders(true);

                Description good = new Description();
                good.setText("");
                chart.setDescription(good);

                XAxis xAixs = chart.getXAxis();
                xAixs.setDrawGridLines(false);

                YAxis rightAxis = chart.getAxisRight();
                rightAxis.setEnabled(false);

                YAxis leftAxis = chart.getAxisLeft();
                leftAxis.setAxisMaximum(22f);
                leftAxis.setDrawGridLines(true);
                leftAxis.setDrawAxisLine(true);
                leftAxis.setDrawLabels(true);
                LimitLine ll = new LimitLine(140f, "ganleni");
                ll.setLineColor(Color.RED);
                ll.setLineWidth(4f);
                ll.setTextColor(Color.BLACK);
                ll.setTextSize(12f);
                leftAxis.addLimitLine(ll);
            }
        });
    }

    public void save(CalendarDay day, int i){
        AVObject calendarDay = new AVObject("CalendarDay");
        calendarDay.put("Tag",i);
        calendarDay.put("Date",day.getDate());
        calendarDay.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if(e == null){
                    Log.d("huni", "sucess");
                }
                else{
                    Log.d("huni", "fail");
                }
            }
        });
    }

    public void load(){
        AVQuery<AVObject> avQuery = new AVQuery<>("CalendarDay");
        avQuery.whereEqualTo("Tag", 1);
        AVQuery<AVObject> endQuery = new AVQuery<>("CalendarDay");
        endQuery.whereEqualTo("Tag", 2);
        AVQuery<AVObject> query = AVQuery.or(Arrays.asList(avQuery, endQuery));
        query.orderByAscending("Date");

        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e==null){
                    calendarViewDateList = new ArrayList<>();
                    for (int k=0; k<list.size(); k++){
                        CalendarViewDate calendarViewdate = new CalendarViewDate();
                        calendarViewdate.setDATE_SELECTED(list.get(k).getInt("Tag"));
                        calendarViewdate.setDate(list.get(k).getDate("Date"));
                        calendarViewdate.setCalendarDay(CalendarDay.from(list.get(k).getDate("Date")));
                        calendarViewDateList.add(calendarViewdate);
                    }
                    init();
                    click();
                }

            }
        });

    }
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_calendar_layout);
        materialCalendarView = (MaterialCalendarView)findViewById(R.id.calendar_view);
        ActionBar actionBar = getSupportActionBar();

        loadtime();
        if (actionBar != null){
            actionBar.hide();
        }
        materialCalendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_MULTIPLE);
        calendarViewDate.setDate(new Date());
        calendarViewDate.setDATE_SELECTED(0);

         load();



    }

    public void init(){
        if (calendarViewDateList.size()>0){
            CalendarDay calendarDay1 = calendarViewDateList.get(calendarViewDateList.size()-1).getCalendarDay();
            Date date = new Date();
            CalendarDay calendarDay2 = CalendarDay.from(date);
            int d = calendarDay2.getDay()-calendarDay1.getDay();
            if (d>=2){
                for (int i=1; i<d; i++){
                    CalendarDay day = CalendarDay.from(calendarDay1.getYear(), calendarDay1.getMonth(), calendarDay1.getDay()+i);
                    save(day,2);
                    NotSignDayDecorator notSignDayDecorator = new NotSignDayDecorator(CalendarActivity.this, day);
                    materialCalendarView.addDecorator(notSignDayDecorator);
                }
            }}


        for(int i = 0 ; i < calendarViewDateList.size() ; i++) {
            CalendarDay calendarDay = calendarViewDateList.get(i).getCalendarDay();
            if (calendarViewDateList.get(i).getDATE_SELECTED()==1){
            SignDayDecorator signDayDecorator = new SignDayDecorator(CalendarActivity.this, calendarDay);
            materialCalendarView.addDecorator(signDayDecorator);}
            else if (calendarViewDateList.get(i).getDATE_SELECTED()==2){
                NotSignDayDecorator notSignDayDecorator = new NotSignDayDecorator(CalendarActivity.this, calendarDay);
                materialCalendarView.addDecorator(notSignDayDecorator);
            }
        }
        calendarViewDate.setDATE_SELECTED(0);
    }

    public void click(){
        SimpleDateFormat dff = new SimpleDateFormat("yyyy:MM:dd");
        dff.setTimeZone(TimeZone.getTimeZone("GMT+08"));
        final String curDate = dff.format(new Date());
        final String currentDate[] = curDate.split(":");
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay, boolean b) {

                int year = Integer.parseInt(currentDate[0]);
                int month = Integer.parseInt(currentDate[1]);
                int day = Integer.parseInt(currentDate[2]);
                int DATE_SELECTED=0;
                CalendarViewDate clickDay = new CalendarViewDate(calendarDay);
                boolean existInList = false;
                for (int i=0; i<calendarViewDateList.size(); i++){
                    if (calendarDay.getDate().equals(calendarViewDateList.get(i).getDate())){
                        existInList = true;
                        DATE_SELECTED = calendarViewDateList.get(i).getDATE_SELECTED();
                        break;
                    }
                }
                if ((day == calendarDay.getDay())&&(month == (calendarDay.getMonth()+1))&&(year == calendarDay.getYear())) {
                    if (!existInList) {
                        SignDayDecorator signDayDecorator = new SignDayDecorator(CalendarActivity.this, calendarDay);
                        materialCalendarView.addDecorator(signDayDecorator);
                        CalendarViewDate calendarviewDate = new CalendarViewDate();
                        calendarviewDate.setDATE_SELECTED(1);
                        calendarviewDate.setDate(calendarDay.getDate());
                        calendarviewDate.setCalendarDay(calendarDay);
                        calendarViewDateList.add(calendarviewDate);
                        calendarDayList.add(calendarDay);
                        save(calendarDay, 1);
                    }
                    else {
                        SignDayDecorator signDayDecorator = new SignDayDecorator(CalendarActivity.this, calendarDay);
                        materialCalendarView.addDecorator(signDayDecorator);
                    }
                }
                else if (DATE_SELECTED == 0)
                {
                    ToBeSignedDayDecorator toBeSignedDayDecorator = new ToBeSignedDayDecorator(CalendarActivity.this, calendarDay);
                    materialCalendarView.addDecorator(toBeSignedDayDecorator);
                }
                else if (DATE_SELECTED == 2){
                    NotSignDayDecorator notSignDayDecorator = new NotSignDayDecorator(CalendarActivity.this, calendarDay);
                    materialCalendarView.addDecorator(notSignDayDecorator);
                }
                else if (DATE_SELECTED == 1){
                    SignDayDecorator signDayDecorator = new SignDayDecorator(CalendarActivity.this, calendarDay);
                    materialCalendarView.addDecorator(signDayDecorator);
                }
            }
        });
}
}
