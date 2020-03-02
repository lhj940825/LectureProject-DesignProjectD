package com.example.hojun.healthcareserviceprojectd;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    ListView MessageList;
    ChatAdapter adapter;
    DBHelper dbHelper;
    String userId;
    String ConsultantName;
    String ConsultantId;
    String ConsultantJob;
    int ConsultantImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        dbHelper = new DBHelper(getApplicationContext(), "appdb.db", null, 1);

        TextView viewId = (TextView) findViewById(R.id.viewId);
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        ConsultantName = intent.getStringExtra("ConsultantName");
        ConsultantId = intent.getStringExtra("ConsultantId");
        ConsultantJob = intent.getStringExtra("ConsultantJob");
        ConsultantImage = intent.getIntExtra("ConsultantImage", R.drawable.user_male);

        viewId.setText(ConsultantName + "  " + ConsultantJob);

        MessageList = (ListView) findViewById(R.id.textList);
        adapter = new ChatAdapter();

        if(ConsultantName.equals("김석환")) {
            adapter.addItem(new ChatItem("  Hello Doctor  ", 0, ConsultantImage ));
            adapter.addItem(new ChatItem("  Hello Client  ", 1, ConsultantImage ));
            adapter.addItem(new ChatItem("  What do you wants to ask  ", 1, ConsultantImage ));
            adapter.addItem(new ChatItem("  I have some headache since last week  ", 0, ConsultantImage ));
            adapter.addItem(new ChatItem("  You have to drink more water  ", 1, ConsultantImage ));
        }
        else if (ConsultantName.equals("김용재")) {
            adapter.addItem(new ChatItem("  Hello Trianer  ", 0, ConsultantImage ));
            adapter.addItem(new ChatItem("  Hello Client  ", 1, ConsultantImage ));
            adapter.addItem(new ChatItem("  What is your problem  ", 1, ConsultantImage ));
            adapter.addItem(new ChatItem("  I want to be a muscle man  ", 0, ConsultantImage ));
            adapter.addItem(new ChatItem("  First you have to loose your weight with running  ", 1, ConsultantImage ));
            adapter.addItem(new ChatItem( "  And you have to exercise more than 3times in a week  ", 1, ConsultantImage ));
        }

        String allMessage = dbHelper.getMessage(ConsultantId);
        if(allMessage != null) {
            String[] Messages = allMessage.split(":::");

            for(int i = 0 ; i < Messages.length; i++) {
                adapter.addItem(new ChatItem(Messages[i], 0, ConsultantImage));
            }

        }

        MessageList.setAdapter(adapter);
    }

    public void onBackButtonClicked(View view) {
        finish();
    }

    public void onSendButtonCliked(View view) {
        EditText writeMessage = (EditText) findViewById(R.id.writeMessage);

        String editText = "  ";
        editText += writeMessage.getText().toString();
        editText += "  ";
        writeMessage.setText("");

        adapter.addItem(new ChatItem(editText,0,ConsultantImage));
        dbHelper.insertRecord(ConsultantId, 0, editText);

        MessageList.setAdapter(adapter);
    }


    /////////////////////////////////////////////////////////////////////////////
    //                          code for ChatItem                              //
    /////////////////////////////////////////////////////////////////////////////

    class ChatItemView extends LinearLayout {
        ImageView userImage;
        TextView textMessage;

        public ChatItemView(Context context) {
            super(context);
            init(context);
        }

        public void init(Context context) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.chat_item, this, true);

            textMessage = (TextView) findViewById(R.id.textMessage);
            userImage = (ImageView) findViewById(R.id.userImage);
        }

        public void setTextMessage(String Message) {
            textMessage.setText(Message);
        }

        public void setUserImage(int resId) {
            userImage.setImageResource(resId);
        }
    }

    class ChatItem {
        String text;
        int whose;
        int resId;

        public ChatItem(String text) {
            this.text = text;
        }

        public ChatItem(String text,int whose, int resId) {
            this.text = text;
            this.whose = whose;
            this.resId = resId;
        }

        public int getResID() {
            return resId;
        }

        public int getWhose() {return whose;}

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setWhose(int whose) {this.whose =whose;}

        public void setResId(int resId) {
            this.resId = resId;
        }
    }

    class ChatAdapter extends BaseAdapter {
        ArrayList<ChatItem> items = new ArrayList<ChatItem>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(ChatItem item) {
            items.add(item);
        }

        public Object getItem(int position) {
            return items.get(position);
        }

        public int getWhose(int position) {return items.get(position).getWhose();}
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewgroup) {
            final Context context = viewgroup.getContext();
            int viewType = getWhose(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
                ChatItem temp = items.get(position);

                switch (viewType) {
                    case 0:
                        convertView = inflater.inflate(R.layout.youchat_item, viewgroup, false);
                        TextView Message = (TextView) convertView.findViewById(R.id.MyMessage) ;
                        Message.setText(temp.getText());
                        break;
                    case 1:
                        convertView = inflater.inflate(R.layout.chat_item, viewgroup, false);
                        TextView youMessage = (TextView) convertView.findViewById(R.id.textMessage) ;
                        ImageView image = (ImageView) convertView.findViewById(R.id.userImage);
                        youMessage.setText(temp.getText());
                        image.setImageResource(temp.getResID());
                        break;
                }
            }

            return convertView;
        }

    }
}
