package com.example.hojun.healthcareserviceprojectd;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by HoJun on 2017-11-08.
 */

public class HomeFragment extends android.support.v4.app.Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */

    private static final String ARG_SECTION_NUMBER = "section_number";
    private JSONObject userInfoJson = null;
    private JSONObject detailScale = null;

    public HomeFragment() {
        JSONObject json = new JSONObject();

        try {
            json.put("request", "UpdateScale");
            json.put("scale_weight", "0");
            //json.put("scale_BMR", scaleRecord.BMR);
            json.put("scale_bone", "0");
            json.put("scale_fat", "0");
            json.put("scale_muscle", "0");
            //json.put("scale_visfat", scaleRecord.visfat);
            //json.put("scale_water", scaleRecord.water);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.detailScale = json;
    }

    public void setDetailScale(JSONObject detailScale) {
        this.detailScale = detailScale;
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static HomeFragment newInstance(int sectionNumber, JSONObject userInfoJson) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        fragment.setUserInfoJson(userInfoJson);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        setScaleMeterClickListener(rootView);
        try {

            LineChart lineChart = (LineChart) rootView.findViewById(R.id.Recent_Health_Info_Chart);
            drawRecentHealthGraph(lineChart);
            setHealthInfo(rootView);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rootView;
    }

    public void setScaleMeterClickListener(View view) {
        ImageView img = (ImageView) view.findViewById(R.id.Scale_Meter_Icon);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DetailScaleActivity.class);
                intent.putExtra("userInfoJson", HomeFragment.this.userInfoJson.toString());
                intent.putExtra("detailScale", HomeFragment.this.detailScale.toString());
                startActivity(intent);
            }
        });
    }

    public void setHealthInfo(View rootView) throws JSONException {

        JSONObject healthData = (JSONObject) this.userInfoJson.get("health");

        ((TextView)rootView.findViewById(R.id.Mass_of_Water)).setText(((Integer)healthData.get("mass_of_water")).toString()+"ML");


        JSONArray recentData = (JSONArray) healthData.get("RecentWeightData4Graph");

        JSONObject temp = (JSONObject) recentData.get(recentData.length()-1);
        ((TextView)rootView.findViewById(R.id.Scale_Weight)).setText(((Double)temp.get("weight")).toString()+"KG");

        recentData = (JSONArray) healthData.get("RecentBloodGlucoseData4Graph");
        temp = (JSONObject) recentData.get(recentData.length()-1);
        ((TextView)rootView.findViewById(R.id.Blood_Glucose)).setText(((Integer)temp.get("bloodglucose")).toString()+"mg/dl");

    }

    public void setUserInfoJson(JSONObject userInfoJson) {
        this.userInfoJson = userInfoJson;
    }

    public void drawRecentHealthGraph(LineChart lineChart) throws JSONException {
        JSONObject healthData = (JSONObject) this.userInfoJson.get("health");
        JSONArray graphData = (JSONArray) healthData.get("RecentBloodGlucoseData4Graph");


        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Entry> entries = new ArrayList<>();

        // Set X Labels
        for (int i = graphData.length() - 1; i >= 0; i--) {
            JSONObject temp = (JSONObject) graphData.get(i);
            String date = ((String) temp.get("date")).substring(0, 10);
            labels.add(date);
            entries.add(new Entry(new Float(temp.get("bloodglucose").toString()), i));

        }

        LineDataSet dataset = new LineDataSet(entries, "Recent Blood Glucose");
        dataset.setColors(ColorTemplate.VORDIPLOM_COLORS);
        dataset.setDrawCubic(true);
        dataset.setDrawFilled(true); //선아래로 색상표시
        dataset.setDrawValues(false);


        LineData data = new LineData(labels, dataset);
        lineChart.setData(data);
        lineChart.animateXY(1000, 1000);
        lineChart.invalidate();

    }

    public static class DetailScaleActivity extends AppCompatActivity {
        private JSONObject userInfoJson = null;
        private JSONObject detailScale = null;

        public DetailScaleActivity() {
        }

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.scale_detail_info);
            try {
                this.userInfoJson = new JSONObject(getIntent().getExtras().getString("userInfoJson"));
                this.detailScale = new JSONObject(getIntent().getExtras().getString("detailScale"));
                if(this.detailScale.get("scale_weight").equals("0"))
                    setInitializeScaleInfo();
                else
                    setScaleInfo(this.detailScale);
                LineChart lineChart = (LineChart) findViewById(R.id.Recent_Weight_Info_Chart);
                drawRecentWeightGraph(lineChart);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        public void drawRecentWeightGraph(LineChart lineChart) throws JSONException {

            JSONObject healthData = (JSONObject) this.userInfoJson.get("health");
            JSONArray graphData = (JSONArray) healthData.get("RecentWeightData4Graph");

            ArrayList<String> labels = new ArrayList<>();
            ArrayList<Entry> entries = new ArrayList<>();

            // Set X Labels
            for (int i = graphData.length() - 1; i >= 0; i--) {
                JSONObject temp = (JSONObject) graphData.get(i);
                String date = ((String) temp.get("date")).substring(0, 10);
                labels.add(date);
                entries.add(new Entry(new Float(temp.get("weight").toString()), i));
            }

            LineDataSet dataset = new LineDataSet(entries, "Recent Weight");
            dataset.setColors(ColorTemplate.VORDIPLOM_COLORS);
            dataset.setDrawCubic(true);
            dataset.setDrawFilled(true); //선아래로 색상표시
            dataset.setDrawValues(false);


            LineData data = new LineData(labels, dataset);
            lineChart.setData(data);
            lineChart.animateXY(1000, 1000);
            lineChart.invalidate();

        }
        public void setInitializeScaleInfo() throws JSONException {
            JSONObject healthData = (JSONObject) this.userInfoJson.get("health");
            JSONArray graphData = (JSONArray) healthData.get("RecentWeightData4Graph");

            JSONObject temp = (JSONObject) graphData.get(graphData.length()-1);

            ((TextView) findViewById(R.id.Weight)).setText(String.valueOf(temp.get("weight")) + "KG");
            ((TextView) findViewById(R.id.Bone)).setText(String.valueOf(temp.get("bone")) + "%");
            ((TextView) findViewById(R.id.Fat)).setText(String.valueOf(temp.get("fat")) + "%");
            ((TextView) findViewById(R.id.Muscle)).setText(String.valueOf(temp.get("muscle")) + "%");

        }
        public void setScaleInfo(JSONObject detailScale) {
            try {
                ((TextView) findViewById(R.id.Weight)).setText(String.valueOf(detailScale.get("scale_weight")) + "KG");
                ((TextView) findViewById(R.id.Bone)).setText(String.valueOf(detailScale.get("scale_bone")) + "%");
                ((TextView) findViewById(R.id.Fat)).setText(String.valueOf(detailScale.get("scale_fat")) + "%");
                ((TextView) findViewById(R.id.Muscle)).setText(String.valueOf(detailScale.get("scale_muscle")) + "%");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }


}


