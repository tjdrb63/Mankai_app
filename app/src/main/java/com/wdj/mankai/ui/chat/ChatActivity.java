package com.wdj.mankai.ui.chat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.wdj.mankai.R;
import com.wdj.mankai.adapter.RoomsAdapter;
import com.wdj.mankai.data.model.Room;
import com.wdj.mankai.ui.login.LoginActivity;
import com.wdj.mankai.ui.login.LoginRequest;
import com.wdj.mankai.ui.main.UserRequest;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.WebSocket;

public class ChatActivity extends FragmentActivity {
    TabLayout chat_room_tabs;

    ChatDMListFragment dmListFragment;
    ChatGroupListFragment groupListFragment;
    JSONObject currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        dmListFragment = new ChatDMListFragment();
        groupListFragment = new ChatGroupListFragment();

//        getSupportFragmentManager().beginTransaction().add(R.id.chat_container, dmListFragment).commit();

        chat_room_tabs = findViewById(R.id.chat_room_tabs);
        chat_room_tabs.addTab(chat_room_tabs.newTab().setText("DM"));
        chat_room_tabs.addTab(chat_room_tabs.newTab().setText("GROUP"));






        chat_room_tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Fragment selected = null;
                if (position == 0)
                    selected = dmListFragment;
                else if (position == 1)
                    selected = groupListFragment;

//                getSupportFragmentManager().beginTransaction().replace(R.id.chat_container, selected).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }





    public static void setRoomList(ViewGroup rootView) {
        RecyclerView roomList_list;
        RoomsAdapter roomsAdapter;

        roomList_list = (RecyclerView) rootView.findViewById(R.id.roomList_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(rootView.getContext(), LinearLayoutManager.VERTICAL, false);
        roomList_list.setLayoutManager(layoutManager);

        roomsAdapter = new RoomsAdapter(rootView.getContext());
        roomList_list.setAdapter(roomsAdapter);

    }
}
