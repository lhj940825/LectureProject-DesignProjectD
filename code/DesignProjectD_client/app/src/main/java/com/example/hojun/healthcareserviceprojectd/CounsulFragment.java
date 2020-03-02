package com.example.hojun.healthcareserviceprojectd;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ParseException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by HoJun on 2017-11-08.
 */

public class CounsulFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    ListView consultList;
    ListView non_consultList;
    private ArrayList<UserItem> c_list = new ArrayList<>();
    private ArrayList<UserItem> nc_list = new ArrayList<>();
    UserAdapter conAdapter;
    UserAdapter non_conAdapter;
    private static String JSON_CONSULT_ITEMS;
    private static String JSON_NONCONSULT_ITEMS;

    private static final String ARG_SECTION_NUMBER = "section_number";

    public CounsulFragment() {

    }

    public CounsulFragment(String JSON_CONSULT_ITEMS, String JSON_NONCONSULT_ITEMS) {
        this.JSON_CONSULT_ITEMS = JSON_CONSULT_ITEMS;
        this.JSON_NONCONSULT_ITEMS = JSON_NONCONSULT_ITEMS;
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static CounsulFragment newInstance(int sectionNumber, String jsonConsultItem, String jsonNonconsultItem) {
        CounsulFragment fragment = new CounsulFragment(jsonConsultItem, jsonNonconsultItem);
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_consult, container, false);
        consultList = (ListView) rootView.findViewById(R.id.ConsultList);
        non_consultList = (ListView) rootView.findViewById(R.id.NonconsultList);

        try {
            c_list = UserItem.createContactsList(JSON_CONSULT_ITEMS);
            nc_list = UserItem.createContactsList(JSON_NONCONSULT_ITEMS);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        conAdapter = new UserAdapter(c_list);
        non_conAdapter = new UserAdapter(nc_list);
        non_consultList.setAdapter(non_conAdapter);
        consultList.setAdapter(conAdapter);

        consultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                UserItem item = (UserItem) conAdapter.getItem(position);
                Intent intent = new Intent(getActivity().getApplicationContext(), ChatActivity.class);
                intent.putExtra("UserId", "bluemk11");
                intent.putExtra("ConsultantName", item.getName());
                intent.putExtra("ConsultantId", item.getId());
                intent.putExtra("ConsultantImage",item.getResID());
                intent.putExtra("ConsultantJob",item.getJob());
                startActivity(intent);
            }
        });

        non_consultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                UserItem item = (UserItem) non_conAdapter.getItem(position);
                showMessage(item);
            }
        });

        return rootView;
    }

    private void showMessage(UserItem item) {
        final String expert_id = item.getId();
        final String expert_name = item.getName();
        final String expert_job = item.getJob();
        final int expert_image = item.getResID();
        final UserItem userItem = item;
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle("상담 신청");
        builder.setMessage(item.getName() + " 님에게 상담을 신청하시겠습니까?");
        builder.setIcon(R.drawable.chat);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                JSONObject json = new JSONObject();
                try {
                    json.put("request", "Ask");
                    json.put("client", "bluemk11");
                    json.put("expert", expert_id);

                    SocketManager.JsonSubmitThread jsonSubmitThread = new SocketManager.JsonSubmitThread(json.toString());
                    jsonSubmitThread.start();

                    conAdapter.getList().add(0, new UserItem(expert_name, expert_id, expert_job, expert_image));
                    conAdapter.notifyDataSetChanged();
                    non_conAdapter.getList().remove(userItem);
                    non_conAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getActivity().getApplicationContext(),"신청되었습니다",Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getActivity().getApplicationContext(),"신청이 취소되었습니다",Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    /////////////////////////////////////////////////////////////////////////////
    //                          code for UserItem                              //
    /////////////////////////////////////////////////////////////////////////////

    class UserItemView extends LinearLayout {
        ImageView userImage;
        TextView userId;
        TextView userJob;

        public UserItemView(Context context) {
            super(context);
            init(context);
        }

        public void init(Context context) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.user_item, this, true);
            userId = (TextView) findViewById(R.id.userId);
            userImage = (ImageView) findViewById(R.id.userImage);
            userJob = (TextView) findViewById(R.id.userJob);
        }

        public void setId(String id) {
            userId.setText(id);
        }

        public void setUserImage(int resId) {
            userImage.setImageResource(resId);
        }

        public void setUserJob(String job) {userJob.setText(job);}
    }

    public static class UserItem {
        String name;
        String id;
        String job;
        int resId;

        public UserItem(String id) {
            this.id = id;
        }
        public UserItem(String name, String id, String job, int resId) {
            this.name = name;
            this.id = id;
            this.job = job;
            this.resId = resId;
        }
        public static ArrayList<UserItem> createContactsList(String jsonUserItems) throws JSONException {
            ArrayList<UserItem> contacts = new ArrayList<UserItem>();
            JSONArray itemArray = new JSONArray(jsonUserItems);
            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject temp = (JSONObject) itemArray.get(i);
                int check = Integer.parseInt(temp.getString("gender"));
                if(check % 10 == 1)
                    contacts.add(new UserItem(temp.get("name").toString(), temp.getString("gender") , temp.get("jobName").toString(),R.drawable.user_male));
                else
                    contacts.add(new UserItem(temp.get("name").toString(), temp.getString("gender"), temp.get("jobName").toString(),R.drawable.user_female));
            }

            return contacts;
        }
        public int getResID() {
            return resId;
        }
        public String getName() {return name;}
        public String getId() {
            return id;
        }

        public String getJob() {return job;}

        public void setId(String id) {
            this.id = id;
        }
        public void serName(String name) {this.name = name;}
        public void setResId(int resId) {
            this.resId = resId;
        }

        public void setJob(String job) {this.job = job;}
    }

    class UserAdapter extends BaseAdapter {
        ArrayList<UserItem> items = new ArrayList<UserItem>();

        public UserAdapter(ArrayList<UserItem> list) {
            this.items = list;
        }
        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(UserItem item) {
            items.add(item);
        }

        public Object getItem(int position) {
            return items.get(position);
        }
        public ArrayList<UserItem> getList() {return items;}
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewgroup) {
            UserItemView view = new UserItemView(getActivity().getApplicationContext());
            UserItem item = items.get(position);
            view.setId(item.getName());
            view.setUserImage(item.getResID());
            view.setUserJob(item.getJob());

            return view;
        }

    }
}
