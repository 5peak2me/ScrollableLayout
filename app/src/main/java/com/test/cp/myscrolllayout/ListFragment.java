package com.test.cp.myscrolllayout;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class ListFragment extends Fragment {

    private ListView mListview;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        mListview = (ListView) view.findViewById(R.id.listview);
        List<String> strlist = new ArrayList<String>();
        for (int i = 0; i < 100; i++) {
            strlist.add(String.valueOf(i));
        }
        mListview.setAdapter(new MyAdapter(getActivity(), strlist));
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "点击item" + position, Toast.LENGTH_SHORT).show();
            }
        });
        mListview.setOnTouchListener(new View.OnTouchListener() {

            private float x;
            private float y;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_MOVE:
                        x = event.getX();
                        y = event.getY();
                        Log.v("Listview:","ACTION_MOVE");
                        break;
                    case MotionEvent.ACTION_UP:
                        float cx = event.getX();
                        float cy = event.getY();
                        float deltaX = cx - x;
                        float deltaY = cy - y;
                        if(Math.abs(deltaX)>2 || Math.abs(deltaY)>2){
                            event.setAction(MotionEvent.ACTION_CANCEL);
                        }
                        Log.v("Listview:","ACTION_UP");
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        Log.v("Listview:","ACTION_CANCEL");
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        return view;
    }

    public ListView getListView(){
        return mListview;
    }
}
