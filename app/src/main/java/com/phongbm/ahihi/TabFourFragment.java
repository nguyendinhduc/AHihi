package com.phongbm.ahihi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.phongbm.common.GlobalApplication;
import com.phongbm.settings.AccountActivity;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressLint("ValidFragment")
public class TabFourFragment extends Fragment {
    private static final String TAG = "TabFourFragment";
    private View view;
    private ListView listSettting;
    private ArrayList<ItemSetting> itemSettings;
    private AdapterSetting adapterSetting;
    private CircleImageView imgAvatar;
    private TextView txtFullName;

    public TabFourFragment(Context context, ViewGroup viewGroup) {
//        LayoutInflater layoutInflater = LayoutInflater.from(context);
//        view = layoutInflater.inflate(R.layout.tab_four, viewGroup, false);

    }
    public static TabFourFragment instantTabContactFragment( Context context, ViewGroup viewGroup ) {
        TabFourFragment tabFourFragment = new TabFourFragment(context, viewGroup);
        Bundle args = new Bundle();
        args.putString("address", "TabFourFragment");
        tabFourFragment.setArguments(args);
        return  tabFourFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()...");
//        this.initializeComponent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()...");
//        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.tab_four, container, false);
        this.initializeComponent();
        return view;
    }

    private void initializeComponent() {
        imgAvatar = (CircleImageView) view.findViewById(R.id.imgAvatar);
        imgAvatar.setImageBitmap(((GlobalApplication) getActivity().getApplication()).getAvatar());
        txtFullName = (TextView) view.findViewById(R.id.txtFullName);
        txtFullName.setText(((GlobalApplication) getActivity().getApplication()).getFullName());

        listSettting = (ListView) view.findViewById(R.id.listSettting);
        itemSettings = new ArrayList<>();
        itemSettings.add(new ItemSetting(R.drawable.ic_profile, "Profile"));
        itemSettings.add(new ItemSetting(R.drawable.ic_account, "Account"));
        itemSettings.add(new ItemSetting(R.drawable.ic_privacy, "Privacy"));
        itemSettings.add(new ItemSetting(R.drawable.ic_sound, "Sound"));
        itemSettings.add(new ItemSetting(R.drawable.ic_light, "Light"));
        itemSettings.add(new ItemSetting(R.drawable.ic_help, "Help"));

        adapterSetting = new AdapterSetting();
        listSettting.setAdapter(adapterSetting);
    }

    private class ItemSetting {
        private String contentItem;
        private int idIcon;

        public ItemSetting(int idIcon, String contentItem) {
            this.idIcon = idIcon;
            this.contentItem = contentItem;
        }
    }

    private class AdapterSetting extends BaseAdapter {
        @Override
        public int getCount() {
            return itemSettings.size();
        }

        @Override
        public Object getItem(int position) {
            return itemSettings.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_setting, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
                viewHolder.txtContent = (TextView) convertView.findViewById(R.id.txtContent);
                viewHolder.switchOnOf = (Switch) convertView.findViewById(R.id.switchOnOf);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            switch (itemSettings.get(position).idIcon) {
                case R.drawable.ic_sound:
                case R.drawable.ic_light:
                    viewHolder.switchOnOf.setVisibility(View.VISIBLE);
                    viewHolder.switchOnOf.setOnCheckedChangeListener(
                            new CustomOnCheckedChangeListener(position, itemSettings.get(position).idIcon));
                    break;
                default:
                    viewHolder.switchOnOf.setOnCheckedChangeListener(null);
                    viewHolder.switchOnOf.setVisibility(View.GONE);
            }
            convertView.setOnClickListener(new CustomOnClickListener(position, itemSettings.get(position).idIcon));
            viewHolder.txtContent.setText(itemSettings.get(position).contentItem);
            viewHolder.icon.setImageResource(itemSettings.get(position).idIcon);
            return convertView;
        }
    }

    private class ViewHolder {
        private ImageView icon;
        private TextView txtContent;
        private Switch switchOnOf;
    }

    private class CustomOnClickListener implements View.OnClickListener {
        private int position, idIcon;

        public CustomOnClickListener(int position, int idIcon) {
            this.position = position;
            this.idIcon = idIcon;
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(getContext(), "OnClick item", Toast.LENGTH_SHORT).show();
            switch (idIcon) {
                case R.drawable.ic_account:
                    Intent intentAccount = new Intent();
                    intentAccount.setClass(getActivity(), AccountActivity.class);
                    startActivity(intentAccount);
                    break;
            }
        }
    }

    private class CustomOnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        private int position, idIcon;

        public CustomOnCheckedChangeListener(int position, int idIcon) {
            this.position = position;
            this.idIcon = idIcon;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Toast.makeText(TabFourFragment.this.getContext(), "" + isChecked, Toast.LENGTH_SHORT).show();
            if (isChecked) {
            } else {

            }
        }
    }

}