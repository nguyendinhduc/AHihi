package com.phongbm.message;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.phongbm.ahihi.R;
import com.phongbm.common.CommonMethod;
import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class MessageActivity extends AppCompatActivity implements View.OnClickListener,
        ViewTreeObserver.OnGlobalLayoutListener {
    private static final String TAG = "MessageActivity";
    private static final int REQUEST_ATTACH = 0;
    private static final int REQUEST_PICTURE = 1;
    private static final int NUMBER_COLLECTION_EMOTICON = 20;

    private RelativeLayout layoutMain, menu;
    private LinearLayout emoticons;
    private InputMethodManager inputMethodManager;
    private ListView listViewMessage;
    private MessageAdapter messageAdapter;
    private String outGoingMessageId, inComingMessageId;
    private ReentrantLock reentrantLock = new ReentrantLock();
    private EditText edtContent;
    private ImageButton btnAttach;
    private ImageView btnSend, imgEmoticon, btnPicture;
    private BroadcastMessage broadcastMessage;
    private String inComingFullName, content;
    private CommonMethod commonMethod;
    private boolean isOpenEmoticons = false;
    private TabLayout tabs;
    private ViewPager viewPager;

    private int[] emoticonIds;
    private EmoticonAdapter[] emoticonAdapters = new EmoticonAdapter[NUMBER_COLLECTION_EMOTICON];
    private ArrayList<CollectionEmoticonItem> collectionEmoticonItems;
    private CollectionEmoticonAdapter collectionEmoticonAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_message);

        commonMethod = CommonMethod.getInstance();

        Intent intent = this.getIntent();
        outGoingMessageId = ParseUser.getCurrentUser().getObjectId();
        inComingMessageId = intent.getStringExtra(CommonValue.INCOMING_CALL_ID);
        inComingFullName = intent.getStringExtra(CommonValue.INCOMING_MESSAGE_FULL_NAME);

        this.initializeToolbar();
        this.initializeComponent();
        this.registerBroadcastMessage();

        messageAdapter = new MessageAdapter(this, inComingMessageId);

        listViewMessage.setAdapter(messageAdapter);
        this.getData();
    }

    private void initializeToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(inComingFullName);
    }

    private void initializeComponent() {
        inputMethodManager = (InputMethodManager) this.
                getSystemService(Context.INPUT_METHOD_SERVICE);
        layoutMain = (RelativeLayout) findViewById(R.id.layoutMain);
        layoutMain.getViewTreeObserver().addOnGlobalLayoutListener(this);
        menu = (RelativeLayout) findViewById(R.id.menu);

        emoticons = (LinearLayout) findViewById(R.id.emoticons);

        listViewMessage = (ListView) findViewById(R.id.listViewMessage);
        listViewMessage.setSelected(false);

        btnAttach = (ImageButton) findViewById(R.id.btnAttach);
        btnAttach.setOnClickListener(this);
        btnPicture = (ImageView) findViewById(R.id.btnPicture);
        btnPicture.setOnClickListener(this);

        btnSend = (ImageView) findViewById(R.id.btnSend);
        btnSend.setEnabled(false);
        btnSend.setOnClickListener(this);
        edtContent = (EditText) findViewById(R.id.edtContent);
        edtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s == null || s.length() == 0) {
                    btnSend.setEnabled(false);
                    btnSend.setImageResource(R.drawable.ic_sent_negative);
                } else {
                    btnSend.setEnabled(true);
                    btnSend.setImageResource(R.drawable.ic_sent_active);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        imgEmoticon = (ImageView) findViewById(R.id.imgEmoticon);
        imgEmoticon.setOnClickListener(this);

        emoticonIds = new int[]{R.drawable.finch_1, R.drawable.finch_2, R.drawable.finch_3,
                R.drawable.finch_4, R.drawable.finch_5, R.drawable.finch_6, R.drawable.finch_7,
                R.drawable.finch_8, R.drawable.finch_9, R.drawable.finch_10, R.drawable.finch_11,
                R.drawable.finch_12, R.drawable.finch_13, R.drawable.finch_14, R.drawable.finch_15,
                R.drawable.finch_16};
        ArrayList<EmoticonItem> emoticonItems0 = new ArrayList<EmoticonItem>();
        for (int i = 0; i < emoticonIds.length; i++) {
            emoticonItems0.add(new EmoticonItem(emoticonIds[i]));
        }
        emoticonAdapters[0] = new EmoticonAdapter(this, emoticonItems0, inComingMessageId);

        emoticonIds = new int[]{R.drawable.sallyfrien_1, R.drawable.sallyfrien_2, R.drawable.sallyfrien_3,
                R.drawable.sallyfrien_4, R.drawable.sallyfrien_5, R.drawable.sallyfrien_6, R.drawable.sallyfrien_7,
                R.drawable.sallyfrien_8, R.drawable.sallyfrien_9, R.drawable.sallyfrien_10, R.drawable.sallyfrien_11,
                R.drawable.sallyfrien_12, R.drawable.sallyfrien_13, R.drawable.sallyfrien_14, R.drawable.sallyfrien_15,
                R.drawable.sallyfrien_16, R.drawable.sallyfrien_17, R.drawable.sallyfrien_18, R.drawable.sallyfrien_19,
                R.drawable.sallyfrien_20, R.drawable.sallyfrien_21, R.drawable.sallyfrien_22, R.drawable.sallyfrien_23,
                R.drawable.sallyfrien_24, R.drawable.sallyfrien_25, R.drawable.sallyfrien_26, R.drawable.sallyfrien_27,
                R.drawable.sallyfrien_28, R.drawable.sallyfrien_29, R.drawable.sallyfrien_30, R.drawable.sallyfrien_31,
                R.drawable.sallyfrien_32, R.drawable.sallyfrien_33, R.drawable.sallyfrien_34, R.drawable.sallyfrien_35,
                R.drawable.sallyfrien_36, R.drawable.sallyfrien_37, R.drawable.sallyfrien_38, R.drawable.sallyfrien_39,
                R.drawable.sallyfrien_40};
        ArrayList<EmoticonItem> emoticonItems1 = new ArrayList<EmoticonItem>();
        for (int i = 0; i < emoticonIds.length; i++) {
            emoticonItems1.add(new EmoticonItem(emoticonIds[i]));
        }
        emoticonAdapters[1] = new EmoticonAdapter(this, emoticonItems1, inComingMessageId);

        emoticonIds = new int[]{R.drawable.lcil_1, R.drawable.lcil_2, R.drawable.lcil_3,
                R.drawable.lcil_4, R.drawable.lcil_5, R.drawable.lcil_6, R.drawable.lcil_7,
                R.drawable.lcil_8, R.drawable.lcil_9, R.drawable.lcil_10, R.drawable.lcil_11,
                R.drawable.lcil_12, R.drawable.lcil_13, R.drawable.lcil_14, R.drawable.lcil_15,
                R.drawable.lcil_16, R.drawable.lcil_17, R.drawable.lcil_18, R.drawable.lcil_19,
                R.drawable.lcil_20, R.drawable.lcil_21, R.drawable.lcil_22, R.drawable.lcil_23,
                R.drawable.lcil_24, R.drawable.lcil_25, R.drawable.lcil_26, R.drawable.lcil_27,
                R.drawable.lcil_28, R.drawable.lcil_29, R.drawable.lcil_30, R.drawable.lcil_31,
                R.drawable.lcil_32, R.drawable.lcil_33, R.drawable.lcil_34, R.drawable.lcil_35,
                R.drawable.lcil_36, R.drawable.lcil_37, R.drawable.lcil_38, R.drawable.lcil_39,
                R.drawable.lcil_40};
        ArrayList<EmoticonItem> emoticonItems2 = new ArrayList<EmoticonItem>();
        for (int i = 0; i < emoticonIds.length; i++) {
            emoticonItems2.add(new EmoticonItem(emoticonIds[i]));
        }
        emoticonAdapters[2] = new EmoticonAdapter(this, emoticonItems2, inComingMessageId);

        emoticonIds = new int[]{R.drawable.yaya_1, R.drawable.yaya_2, R.drawable.yaya_3,
                R.drawable.yaya_4, R.drawable.yaya_5, R.drawable.yaya_6, R.drawable.yaya_7,
                R.drawable.yaya_8, R.drawable.yaya_9, R.drawable.yaya_10, R.drawable.yaya_11,
                R.drawable.yaya_12, R.drawable.yaya_13, R.drawable.yaya_14, R.drawable.yaya_15,
                R.drawable.yaya_16, R.drawable.yaya_17, R.drawable.yaya_18, R.drawable.yaya_19,
                R.drawable.yaya_20, R.drawable.yaya_21, R.drawable.yaya_22, R.drawable.yaya_23,
                R.drawable.yaya_24, R.drawable.yaya_25, R.drawable.yaya_26, R.drawable.yaya_27,
                R.drawable.yaya_28, R.drawable.yaya_29, R.drawable.yaya_30, R.drawable.yaya_31,
                R.drawable.yaya_32};
        ArrayList<EmoticonItem> emoticonItems3 = new ArrayList<EmoticonItem>();
        for (int i = 0; i < emoticonIds.length; i++) {
            emoticonItems3.add(new EmoticonItem(emoticonIds[i]));
        }
        emoticonAdapters[3] = new EmoticonAdapter(this, emoticonItems3, inComingMessageId);

        collectionEmoticonItems = new ArrayList<>();
        collectionEmoticonItems.add(new CollectionEmoticonItem(emoticonAdapters[0]));
        collectionEmoticonItems.add(new CollectionEmoticonItem(emoticonAdapters[1]));
        collectionEmoticonItems.add(new CollectionEmoticonItem(emoticonAdapters[2]));
        collectionEmoticonItems.add(new CollectionEmoticonItem(emoticonAdapters[3]));

        collectionEmoticonAdapter = new CollectionEmoticonAdapter(this, collectionEmoticonItems);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(collectionEmoticonAdapter);

        tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        tabs.getTabAt(0).setIcon(R.drawable.finch_1);
        tabs.getTabAt(1).setIcon(R.drawable.sallyfrien_1);
        tabs.getTabAt(2).setIcon(R.drawable.lcil_1);
        tabs.getTabAt(3).setIcon(R.drawable.yaya_1);
    }

    private void getData() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
        String[] ids = new String[]{outGoingMessageId, inComingMessageId};
        query.whereContainedIn("senderId", Arrays.asList(ids));
        query.whereContainedIn("receiverId", Arrays.asList(ids));
        query.orderByDescending("createdAt");
        query.setLimit(100);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e != null) {
                    return;
                }
                for (ParseObject message : list) {
                    reentrantLock.lock();
                    int type = MessageAdapter.TYPE_INCOMING;
                    if (outGoingMessageId.equals(message.getString("senderId"))) {
                        type = MessageAdapter.TYPE_OUTGOING;
                    }
                    String content = message.getString("content");
                    if (!content.contains(CommonValue.AHIHI_KEY)) {
                        messageAdapter.addMessage(0, new MessageItem(type,
                                SpannableString.valueOf(content), 0));
                    } else {
                        String key = content.substring(0, CommonValue.KEY_LENGTH + 1);
                        content = content.substring(CommonValue.KEY_LENGTH + 1);
                        switch (key) {
                            case CommonValue.AHIHI_KEY_EMOTICON:
                                int emoticonId = Integer.parseInt(content);
                                SpannableString emoticon = commonMethod.toSpannableString(
                                        MessageActivity.this, emoticonId);
                                messageAdapter.addMessage(0, new MessageItem(type, emoticon, 1));
                                break;
                            case CommonValue.AHIHI_KEY_FILE:
                                messageAdapter.addMessage(0, new MessageItem(type,
                                        SpannableString.valueOf(content), 2));
                                break;
                            case CommonValue.AHIHI_KEY_PICTURE:
                                messageAdapter.addMessage(0, new MessageItem(type,
                                        SpannableString.valueOf(content), 3));
                                break;
                        }

                    }
                    reentrantLock.unlock();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAttach:
                Intent intentAttach = new Intent();
                intentAttach.setAction(Intent.ACTION_GET_CONTENT);
                intentAttach.setType("file/*");
                this.startActivityForResult(intentAttach, REQUEST_ATTACH);
                break;
            case R.id.btnSend:
                content = edtContent.getText().toString();
                edtContent.setText("");
                Intent intentSend = new Intent();
                intentSend.setAction(CommonValue.ACTION_SEND_MESSAGE);
                intentSend.putExtra(CommonValue.INCOMING_MESSAGE_ID, inComingMessageId);
                intentSend.putExtra(CommonValue.MESSAGE_CONTENT, content);
                intentSend.putExtra(CommonValue.INCOMING_MESSAGE_FULL_NAME, inComingFullName);

                MessageActivity.this.sendBroadcast(intentSend);
                break;
            case R.id.imgEmoticon:
                if (!isOpenEmoticons) {
                    isOpenEmoticons = true;
                    emoticons.setVisibility(View.VISIBLE);
                    menu.setVisibility(View.GONE);
                    listViewMessage.setSelection(messageAdapter.getCount());
                } else {
                    isOpenEmoticons = false;
                    emoticons.setVisibility(View.GONE);
                    menu.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btnPicture:
                Intent intentPicture = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                this.startActivityForResult(intentPicture, REQUEST_PICTURE);
                break;
        }
    }

    private void registerBroadcastMessage() {
        if (broadcastMessage == null) {
            broadcastMessage = new BroadcastMessage();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(CommonValue.STATE_MESSAGE_SENT);
            intentFilter.addAction(CommonValue.STATE_MESSAGE_INCOMING);
            MessageActivity.this.registerReceiver(broadcastMessage, intentFilter);
        }
    }

    @Override
    public void onGlobalLayout() {
        Rect rect = new Rect();
        layoutMain.getWindowVisibleDisplayFrame(rect);
        int heightDiff = layoutMain.getRootView().getHeight() - (rect.bottom + rect.top);
        if (heightDiff > 100) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    menu.setVisibility(View.GONE);
                    emoticons.setVisibility(View.GONE);
                    isOpenEmoticons = false;
                }
            });
        } else if (heightDiff <= 100 && emoticons.getVisibility() == View.GONE) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    menu.setVisibility(View.VISIBLE);
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_ATTACH:
                String path = data.getData().getPath();
                Intent intentSend = new Intent();
                intentSend.setAction(CommonValue.ACTION_SEND_MESSAGE);
                intentSend.putExtra(CommonValue.INCOMING_MESSAGE_ID, inComingMessageId);
                intentSend.putExtra(CommonValue.MESSAGE_CONTENT, path);
                intentSend.putExtra(CommonValue.AHIHI_KEY, CommonValue.AHIHI_KEY_FILE);
                MessageActivity.this.sendBroadcast(intentSend);
                break;
            case REQUEST_PICTURE:
                String pathPicture = this.getPathFromUri(data.getData());
                Intent intentPicture = new Intent();
                intentPicture.setAction(CommonValue.ACTION_SEND_MESSAGE);
                intentPicture.putExtra(CommonValue.INCOMING_MESSAGE_ID, inComingMessageId);
                intentPicture.putExtra(CommonValue.MESSAGE_CONTENT, pathPicture);
                intentPicture.putExtra(CommonValue.AHIHI_KEY, CommonValue.AHIHI_KEY_PICTURE);
                MessageActivity.this.sendBroadcast(intentPicture);
                break;
        }
    }

    private String getPathFromUri(Uri uri) {
        Cursor cursor = this.getContentResolver().query(uri,
                new String[]{MediaStore.Images.Media.DATA}, null, null, null);
        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();
        return path;
    }

    private class BroadcastMessage extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case CommonValue.STATE_MESSAGE_SENT:
                    String key = intent.getStringExtra(CommonValue.AHIHI_KEY);
                    if (key == null) {
                        messageAdapter.addMessage(messageAdapter.getCount(), new MessageItem(
                                MessageAdapter.TYPE_OUTGOING, SpannableString.valueOf(intent
                                .getStringExtra(CommonValue.MESSAGE_CONTENT)), 0));
                    } else {
                        String content = intent.getStringExtra(CommonValue.MESSAGE_CONTENT);
                        switch (key) {
                            case CommonValue.AHIHI_KEY_EMOTICON:
                                int emoticonId = Integer.parseInt(content);
                                messageAdapter.addMessage(messageAdapter.getCount(),
                                        new MessageItem(MessageAdapter.TYPE_OUTGOING, commonMethod
                                                .toSpannableString(MessageActivity.this, emoticonId), 1));
                                break;
                            case CommonValue.AHIHI_KEY_FILE:
                                Toast.makeText(MessageActivity.this, "OK", Toast.LENGTH_SHORT).show();
                                messageAdapter.addMessage(messageAdapter.getCount(),
                                        new MessageItem(MessageAdapter.TYPE_OUTGOING, SpannableString
                                                .valueOf(content), 2));
                                break;
                            case CommonValue.AHIHI_KEY_PICTURE:
                                messageAdapter.addMessage(messageAdapter.getCount(),
                                        new MessageItem(MessageAdapter.TYPE_OUTGOING,
                                                ((GlobalApplication) getApplication()).getPictureSend(), 3));
                                break;
                        }
                    }
                    break;
                case CommonValue.STATE_MESSAGE_INCOMING:
                    String keyIncoming = intent.getStringExtra(CommonValue.AHIHI_KEY);
                    if (keyIncoming == null) {
                        messageAdapter.addMessage(messageAdapter.getCount(),
                                new MessageItem(MessageAdapter.TYPE_INCOMING, SpannableString
                                        .valueOf(intent.getStringExtra(CommonValue.MESSAGE_CONTENT)), 0));
                    } else {
                        String content = intent.getStringExtra(CommonValue.MESSAGE_CONTENT);
                        switch (keyIncoming) {
                            case CommonValue.AHIHI_KEY_EMOTICON:
                                int emoticonId = Integer.parseInt(content);
                                messageAdapter.addMessage(messageAdapter.getCount(),
                                        new MessageItem(MessageAdapter.TYPE_INCOMING, commonMethod
                                                .toSpannableString(MessageActivity.this, emoticonId), 1));
                                break;
                            case CommonValue.AHIHI_KEY_PICTURE:
                                messageAdapter.addMessage(messageAdapter.getCount(), new
                                        MessageItem(MessageAdapter.TYPE_INCOMING, SpannableString
                                        .valueOf(content), 3));
                                break;
                        }
                    }
                    break;
            }
            listViewMessage.setSelection(messageAdapter.getCount());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isOpenEmoticons) {
            emoticons.setVisibility(View.GONE);
            isOpenEmoticons = false;
            listViewMessage.setSelection(messageAdapter.getCount());
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(broadcastMessage);
        super.onDestroy();
    }

}