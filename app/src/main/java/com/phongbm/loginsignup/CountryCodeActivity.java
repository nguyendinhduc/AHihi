package com.phongbm.loginsignup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.phongbm.ahihi.R;
import com.phongbm.common.CommonValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class CountryCodeActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener {
    private ListView listViewCountryCode;
    private CountryCodeAdapter countryCodeAdapter;
    private ArrayList<CountryCodeItem> countryCodeItems;
    private CountryCodesDBManager countryCodesDBManager;
    private String content = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_country_code);
        this.initializeComponent();
        countryCodesDBManager = new CountryCodesDBManager(this);
        countryCodeItems = countryCodesDBManager.getData();
        countryCodeAdapter = new CountryCodeAdapter();
        listViewCountryCode.setAdapter(countryCodeAdapter);
    }

    private void initializeComponent() {
        listViewCountryCode = (ListView) findViewById(R.id.listViewCountryCode);
        listViewCountryCode.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (countryCodeItems.get(position).getType() == 1) {
            return;
        }
        content = countryCodeItems.get(position).getCountryCode();
        this.finish();
    }

    private class CountryCodeAdapter extends BaseAdapter implements SectionIndexer {
        private LayoutInflater layoutInflater;
        private HashMap<String, Integer> alphaIndexer;
        private ArrayList<String> sections;

        public CountryCodeAdapter() {
            layoutInflater = LayoutInflater.from(CountryCodeActivity.this);

            alphaIndexer = new HashMap<>();
            for (int i = 0; i < countryCodeItems.size(); i++) {
                String firstLetter = countryCodeItems.get(i).getCountryCode().substring(0, 1).toUpperCase();
                if (!alphaIndexer.containsKey(firstLetter)) {
                    alphaIndexer.put(firstLetter, i);
                    countryCodeItems.add(i, new CountryCodeItem(firstLetter, 1));
                }
            }
            sections = new ArrayList<>(alphaIndexer.keySet());
            Collections.sort(sections);
        }

        @Override
        public int getCount() {
            return countryCodeItems.size();
        }

        @Override
        public CountryCodeItem getItem(int position) {
            return countryCodeItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (countryCodeItems.get(position).getType() == 0) {
                return 0;
            }
            return 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                if (getItemViewType(position) == 0) {
                    convertView = layoutInflater.inflate(R.layout.item_country_code, parent, false);
                } else {
                    convertView = layoutInflater.inflate(R.layout.item_country_code_header, parent, false);
                }
                viewHolder = new ViewHolder();
                viewHolder.txtCountryCode = (TextView) convertView.findViewById(R.id.txtCountryCode);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.txtCountryCode.setText(countryCodeItems.get(position).getCountryCode());
            return convertView;
        }

        @Override
        public Object[] getSections() {
            return sections.toArray();
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            return alphaIndexer.get(sections.get(sectionIndex));
        }

        @Override
        public int getSectionForPosition(int position) {
            return 0;
        }

        private class ViewHolder {
            TextView txtCountryCode;
        }
    }

    @Override
    protected void onDestroy() {
        countryCodesDBManager.closeDatabase();
        super.onDestroy();
    }

    @Override
    public void finish() {
        if (content != null) {
            Intent data = new Intent();
            data.putExtra(CommonValue.COUNTRY_CODE, content);
            this.setResult(Activity.RESULT_OK, data);
        } else {
            this.setResult(Activity.RESULT_OK);
        }
        super.finish();
    }

}