package com.example.hojun.healthcareserviceprojectd;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.widget.LinearLayout.VERTICAL;

/**
 * Created by HoJun on 2017-11-08.
 */

public class BoardFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static String JSON_BOARD_ITEMS;
    /**
     * Private Variables for using Recycle View
     */
    private RecyclerView recyclerView;
    private MainViewAdapter adapter;
    private ArrayList<BoardItem> list = new ArrayList<>();

    public BoardFragment() {

    }

    public BoardFragment(String JSON_BOARD_ITEMS) {
        this.JSON_BOARD_ITEMS = JSON_BOARD_ITEMS;
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static BoardFragment newInstance(int sectionNumber, String jsonBoardItems) {
        BoardFragment fragment = new BoardFragment(jsonBoardItems);
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*
        View rootView = inflater.inflate(R.layout.fragment_board, container, false);
//        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//        textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
        return rootView;
         */
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_board, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        DividerItemDecoration decoration = new DividerItemDecoration(getActivity(), VERTICAL);
        recyclerView.addItemDecoration(decoration);

        try {
            list = BoardItem.createContactsList(JSON_BOARD_ITEMS);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        recyclerView.setHasFixedSize(true);
        adapter = new MainViewAdapter(getActivity(), list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        Log.e("Frag", "MainFragment");
        return rootView;


    }

    /**
     * Inner Data Class for using recycle View
     */
    public static class BoardItem {
        public String title;
        public String content;

        // 화면에 표시될 문자열 초기화
        public BoardItem(String title, String content) {
            this.title = title;
            this.content = content;
        }

        // 입력받은 숫자의 리스트생성
        public static ArrayList<BoardItem> createContactsList(String jsonBoardItems) throws JSONException {
            ArrayList<BoardItem> contacts = new ArrayList<BoardItem>();
            Log.d("XXX", jsonBoardItems);
            JSONArray itemArray = new JSONArray(jsonBoardItems);
            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject temp = (JSONObject) itemArray.get(i);
                contacts.add(new BoardItem(temp.get("title").toString(), temp.get("contents").toString()));
            }

/*

            for (int i = 1; i <= numContacts; i++) {
                contacts.add(new BoardItem("Person ", "wohahahaha"));
            }
*/

            return contacts;
        }
    }


    /**
     * Inner Adapter Class for using recycle View
     */
    public class MainViewAdapter extends RecyclerView.Adapter<MainViewAdapter.MyViewHolder> {


        private Context context;
        private List<BoardItem> list = new ArrayList<>();

        public MainViewAdapter(Context context, List<BoardItem> list) {
            this.context = context;
            this.list = list;
        }

        // ViewHolder 생성
        // row layout을 화면에 뿌려주고 holder에 연결
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_item, parent, false);

            MyViewHolder holder = new MyViewHolder(view);
            return holder;
        }

        /*
        *  Todo 만들어진 ViewHolder에 data 삽입 ListView의 getView와 동일
        *  TODO set Data propery(male or female, content)
        *
        * */
        @Override
        public void onBindViewHolder(MyViewHolder myViewHolder, final int position) {
            // 각 위치에 문자열 세팅
            int itemposition = position;
            myViewHolder.wordText.setText(list.get(itemposition).title);
            myViewHolder.meaningText.setText(list.get(itemposition).content);
            myViewHolder.imageView.setImageResource(R.drawable.male);
            myViewHolder.wordText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Implement Item Click Event
                    Log.d("XXX", String.valueOf(position));
                    Log.d("XXX",list.get(position).title);
                }
            });


            Log.e("StudyApp", "onBindViewHolder" + itemposition);
        }

        // 몇개의 데이터를 리스트로 뿌려줘야하는지 반드시 정의해줘야한다
        @Override
        public int getItemCount() {
            return list.size(); // RecyclerView의 size return
        }

        // ViewHolder는 하나의 View를 보존하는 역할을 한다
        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView wordText;
            public TextView meaningText;

            public de.hdodenhof.circleimageview.CircleImageView imageView;

            public MyViewHolder(View view) {
                super(view);

                imageView = (de.hdodenhof.circleimageview.CircleImageView) view.findViewById(R.id.imageIcon);
                wordText = (TextView) view.findViewById(R.id.board_title);
                meaningText = (TextView) view.findViewById(R.id.board_content);
            }

        }
        public List<BoardItem> getList(){
            return list;
        }
    }

    public MainViewAdapter getAdapter(){
        return this.adapter;
    }
}
