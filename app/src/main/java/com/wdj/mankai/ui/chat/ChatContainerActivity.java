package com.wdj.mankai.ui.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.wdj.mankai.R;
import com.wdj.mankai.adapter.MessagesAdapter;
import com.wdj.mankai.data.model.Message;
import com.wdj.mankai.data.model.Room;
import com.wdj.mankai.ui.chat.ui.ChatBottomSheetDialog;
import com.wdj.mankai.ui.chat.ui.ChatInviteActivity;
import com.wdj.mankai.ui.main.ChatFragment;
import com.wdj.mankai.ui.main.UserRequest;
import com.pusher.client.Pusher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class ChatContainerActivity extends AppCompatActivity implements ChatBottomSheetDialog.BottomSheetListener {
    Room room;
    RecyclerView chat_message_list;
    MessagesAdapter messagesAdapter;
    ProgressBar progressBar;
    TextView textName;
    Button btLeave, btInvite, btMyMemo;
    FrameLayout layoutSend;
    ImageView imageBack, fileSend;
    EditText inputMessage;
    JSONObject currentUser;
    String fcmToken;
    private DrawerLayout drawerLayout;
    private View drawerView;
    private ArrayList<Message> messageList = new ArrayList<Message>();
    String res;
    private int currentPage = 1;
    private int last_page = 1;
    private String userID;
    private ChatFragment chatFragment = new ChatFragment();
    private Parcelable recyclerViewState;
    private int chat_count = 0;
    Channel channel;
    PusherOptions options = new PusherOptions() .setCluster("ap3");
    Pusher pusher = new Pusher("04847be41be2cbe59308",options);
    LinearLayoutManager layoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_container);
        progressBar = findViewById(R.id.progressBar);
        textName = findViewById(R.id.textName);
        room = (Room) getIntent().getSerializableExtra("room");
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerView = (View)findViewById(R.id.drawer2);
        btLeave = findViewById(R.id.btLeave);
        inputMessage = findViewById(R.id.inputMessage);
        layoutSend = findViewById(R.id.layoutSend);
        fileSend = findViewById(R.id.fileSend);
        btInvite = findViewById(R.id.btInvite);
        imageBack = findViewById(R.id.imageBack);
        btMyMemo = findViewById(R.id.btMyMemo);


        System.out.println(room.id);

        SharedPreferences sharedPreferences1 = getSharedPreferences("current_room_id",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences1.edit();
        editor.putString("current_room_id",room.id);
        editor.commit();

        channel = pusher.subscribe("room."+room.id); // ?????? ??????

        channel.bind("send-message", new SubscriptionEventListener() {
            @Override
            public void onEvent(PusherEvent event) {
                try {

                    JSONObject jsonObject = new JSONObject(event.getData());
                    System.out.println("event : " + jsonObject.getJSONObject("message"));
                    SimpleDateFormat newDtFormat2 = new SimpleDateFormat("h:mm");
                    SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date formatDate2 = dtFormat.parse(jsonObject.getJSONObject("message").getString("created_at"));
                    Message message = new Message(jsonObject.getJSONObject("message").getString("id"), jsonObject.getJSONObject("message").getString("user_id"), jsonObject.getJSONObject("message").getString("room_id"), jsonObject.getJSONObject("message").getString("type"), jsonObject.getJSONObject("message").getString("message"), newDtFormat2.format(formatDate2), jsonObject.getJSONObject("message").getString("user"),0);
                    ChatContainerActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messagesAdapter.addEventMessage(message);
                            messagesAdapter.notifyItemInserted(0);

                        }
                    });


                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }

//                messagesAdapter.addMessage(event.getData());
            }
        });
        btMyMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatContainerActivity.this, MemoListActivity.class);
                intent.putExtra("room", room);
                startActivity(intent);
                drawerLayout.closeDrawer(drawerView);

            }
        });

        fileSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatBottomSheetDialog chatBottomSheetDialog = new ChatBottomSheetDialog(room, currentUser);
                chatBottomSheetDialog.show(getSupportFragmentManager(), "chatBottomSheet");
            }
        });


        layoutSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(inputMessage.getText().length() != 0) {
                    sendMessage(String.valueOf(inputMessage.getText()), "message");
                }

            }
        });

        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        SharedPreferences sharedPreferences= getSharedPreferences("login_token", MODE_PRIVATE);
        String token = sharedPreferences.getString("login_token","");
        getUser(token);



        chat_message_list = (RecyclerView) findViewById(R.id.chat_message_list);
        layoutManager = new LinearLayoutManager(ChatContainerActivity.this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setReverseLayout(false);
        layoutManager.setStackFromEnd(true);
        chat_message_list.setLayoutManager(layoutManager);

        chat_message_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

//
//                if (layoutManager != null && layoutManager.findLastVisibleItemPosition() == messageList.size()-1) {
//                    if(currentPage<last_page) {
//                        currentPage+=1;
//                        loadMore(token, room.id,userID,currentPage);
//
//                        Log.e("TAG",String.valueOf(currentPage + "?????????")+last_page);
//                    }
//                }
            }
        });


        drawerLayout.addDrawerListener(listener);
        drawerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        // ????????????
        btLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveRoom();
            }
        });

        btInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatContainerActivity.this, ChatInviteActivity.class);
                intent.putExtra("room", room);
                startActivity(intent);
                drawerLayout.closeDrawer(drawerView);
            }
        });


    }


    DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            //???????????? ?????????
        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
            //Drawer??? ????????? ???????????? ??????
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
            // ?????? ????????? ??? ??????
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            // ??????????????? ????????? ??? ??????
        }
    };


    // ????????? ??? ??????
    public void btnOnclick(View view) {
        switch (view.getId()){
            case R.id.imageInfo:
                drawerLayout.openDrawer(drawerView);
        }
    }

    // ?????? ??????
    private String userName(String users, String roomType) throws JSONException {
        JSONArray users2 = new JSONArray(users);
        ArrayList<JSONObject> roomUsers = new ArrayList<>();
        if(users2.length() > 0) {
            for(int i = 0; i< users2.length(); i++) {
                if(!currentUser.getString("id").equals(users2.getJSONObject(i).getString("user_id"))) {
                    roomUsers.add(users2.getJSONObject(i));
                }
            }
        }

        if(roomType.equals("dm")) {
            return roomUsers.get(0).getString("user_name");
        }else {
            String title = "";
            for (int i = 0; i< roomUsers.size(); i++) {
                if(i != roomUsers.size()-1) {
                    title += (roomUsers.get(i).getString("user_name") + ", ");

                }else{
                    title += roomUsers.get(i).getString("user_name");
                }
            }

            return title;
        }


    }

    // ??????
    private String translation (String message, String country) {

        if(message.equals("")) {
            return "";
        }else {
            JSONObject reqJsonObject = new JSONObject(); //JSON ????????? ??????
            try {
                reqJsonObject.put("text", message);
                reqJsonObject.put("country", country);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println(reqJsonObject);
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    "https://api.mankai.shop/api/translate/text",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("TAG", "??????");
                            System.out.println(response);
                            res = response;

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //?????? ERROR
                            System.out.println(error);
                        }
                    }
            ) {
                @Override
                public byte[] getBody() throws AuthFailureError {
                    return reqJsonObject.toString().getBytes();
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
            };
            Volley.newRequestQueue(ChatContainerActivity.this).add(stringRequest);

        }
        return res;
    }

    // ????????? ?????????
    private void sendMessage(String message, String type) {
        JSONObject reqJsonObject = new JSONObject(); //JSON ????????? ??????
        JSONArray toUsers = null;
        JSONArray toUsers2 = new JSONArray();
        try {
            toUsers = new JSONArray(room.users);
            for(int i = 0; i < toUsers.length(); i++) {
                toUsers2.put(toUsers.getJSONObject(i).getString("user_id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        try {
            if(type.equals("video")) {

                reqJsonObject.put("message", currentUser.getString("name") + translation(" ?????? ??????????????? ??????????????????", currentUser.getString("country")));
                reqJsonObject.put("room_id", room.id);
                reqJsonObject.put("to_users", toUsers2);
                reqJsonObject.put("user_id", currentUser.getString("id"));
                reqJsonObject.put("type", type);
            }else {
                reqJsonObject.put("message", message);
                reqJsonObject.put("room_id", room.id);
                reqJsonObject.put("to_users", toUsers2);
                reqJsonObject.put("user_id", currentUser.getString("id"));
                reqJsonObject.put("type", type);
            }

        } catch (JSONException e) {
            //JSON error
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                "https://api.mankai.shop/api/message/send",
                reqJsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", "??????");

                        if(type.equals("message")) {
                            try {
                                transBotChat(currentUser.getString("name"), message);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //?????? ERROR
                    }
                }
        );
        Volley.newRequestQueue(ChatContainerActivity.this).add(jsonObjectRequest);
        inputMessage.setText("");
    }

    // ?????? ??? ??????
    private void transBotChat(String name, String message) throws JSONException {
        System.out.println(name);
        JSONObject reqJsonObject = new JSONObject(); //JSON ????????? ??????
        JSONArray toUsers = new JSONArray(room.users);
        JSONArray toUsers2 = new JSONArray();
        for(int i = 0; i < toUsers.length(); i++) {
            toUsers2.put(toUsers.getJSONObject(i).getString("user_id"));
        }
        for(int i=0; i< toUsers.length(); i++) {
            if(toUsers.getJSONObject(i).getString("position").equals("official")) {
                try {
                    reqJsonObject.put("id", room.id);
                    reqJsonObject.put("message", message);
                    reqJsonObject.put("room_id", room.id);
                    reqJsonObject.put("to_users", toUsers2);
                    reqJsonObject.put("user_id", toUsers.getJSONObject(i).getString("user_id"));
                    reqJsonObject.put("type", "message");
                    reqJsonObject.put("name", name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        "https://api.mankai.shop/api/messageBot/send",
                        reqJsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("TAG", "?????? ???");
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //?????? ERROR
                            }
                        }
                );
                Volley.newRequestQueue(ChatContainerActivity.this).add(jsonObjectRequest);
            }
        }
    }

    // ??? ?????????
    private void leaveRoom() {
        JSONObject reqJsonObject = new JSONObject(); //JSON ????????? ??????

        JSONObject json = new JSONObject();
        try {
            json.put("id", room.id);
            json.put("title", room.title);
            json.put("last_message", room.last_message);
            json.put("type", room.type);
            json.put("users", room.users);
            json.put("updated_at", room.updated_at);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            reqJsonObject.put("room", json); //?????? ????????? ??????
            reqJsonObject.put("user_id", currentUser.getString("id"));
        } catch (JSONException e) {
            //JSON error
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                "https://api.mankai.shop/api/room/check",
                reqJsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", "??????");
                        // ??? ???????????? ?????? ??? *********************************************
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //?????? ERROR
                    }
                }
        );
        Volley.newRequestQueue(ChatContainerActivity.this).add(jsonObjectRequest);
    }

    // message ??????
    private void setMessages(JSONObject messages, String userId) throws JSONException, ParseException {
        messageList.clear();
        try {
            textName.setText(userName(room.users, room.type));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray jsonArray = messages.getJSONArray("data");

        for (int i = 0; i< jsonArray.length(); i++) {
            JSONObject message = jsonArray.getJSONObject(i);
            SimpleDateFormat newDtFormat2 = new SimpleDateFormat("h:mm");
            SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date formatDate2 = dtFormat.parse(message.getString("created_at"));
            if(message.getInt("id") == Integer.parseInt(userID)) {
                messageList.add(0,new Message(message.getString("id"), message.getString("user_id"), message.getString("room_id"), message.getString("type"), message.getString("message")+chat_count, newDtFormat2.format(formatDate2), message.getString("user"),0));
            } else {
                messageList.add(0,new Message(message.getString("id"), message.getString("user_id"), message.getString("room_id"), message.getString("type"), message.getString("message")+chat_count, newDtFormat2.format(formatDate2), message.getString("user"),1));
            }
            messagesAdapter.notifyItemInserted(0);
            chat_count+=1;
        }

        loading(false);
    }





    private void loadMore(String token, String roomId, String userId , int pageId) {
        loading(true);
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");

                    last_page = jsonObject.getInt("last_page");
                    for (int i = 0; i< jsonArray.length(); i++) {
                        JSONObject message = jsonArray.getJSONObject(i);
                        SimpleDateFormat newDtFormat2 = new SimpleDateFormat("h:mm");
                        SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        Date formatDate2 = dtFormat.parse(message.getString("created_at"));

                        if(message.getInt("id") == Integer.parseInt(userID)) {
                            messageList.add(0,new Message(message.getString("id"), message.getString("user_id"), message.getString("room_id"), message.getString("type"), message.getString("message")+chat_count, newDtFormat2.format(formatDate2), message.getString("user"),0));
                        } else {
                            messageList.add(0,new Message(message.getString("id"), message.getString("user_id"), message.getString("room_id"), message.getString("type"), message.getString("message")+chat_count, newDtFormat2.format(formatDate2), message.getString("user"),1));
                        }

                        chat_count+=1;
                    }
                    loading(false);
                    messagesAdapter.notifyDataSetChanged();

                    Log.e("?????????", String.valueOf(messageList.size()-1));
                } catch(JSONException | ParseException err) {
                    err.printStackTrace();
                }
            }
        };
        ChatRoomMessageRequest chatRoomMessageRequest = new ChatRoomMessageRequest(token, roomId, userId,pageId,responseListener);
        RequestQueue queue = Volley.newRequestQueue(ChatContainerActivity.this);
        queue.add(chatRoomMessageRequest);
    }



    // message ????????????
    private void getMessages(String token, String roomId, String userId , int pageId) {
        loading(true);
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    last_page = jsonObject.getInt("last_page");
                    currentPage = jsonObject.getInt("current_page");
                    setMessages(jsonObject, userId);
                    messagesAdapter.notifyDataSetChanged();
                } catch(JSONException | ParseException err) {
                    err.printStackTrace();
                }
            }
        };
        ChatRoomMessageRequest chatRoomMessageRequest = new ChatRoomMessageRequest(token, roomId, userId,pageId,responseListener);
        RequestQueue queue = Volley.newRequestQueue(ChatContainerActivity.this);
        queue.add(chatRoomMessageRequest);
    }

    // loading
    private void loading(Boolean isLoading) {
        if(isLoading) {
            progressBar.setVisibility(View.VISIBLE);

        }else {
            progressBar.setVisibility(View.INVISIBLE);

        }
    }
    private void channelSubscribe(String roomID) {

        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                System.out.println("State changed to " + change.getCurrentState() +
                        " from " + change.getPreviousState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                System.out.println("There was a problem connecting!");
                System.out.println(code);
                System.out.println(message);
            }
        }, ConnectionState.ALL);



    }
    private void getUser(String token) {
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String userName = jsonObject.getString("name");
                    SharedPreferences sharedPreferences = getSharedPreferences("user_info",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("user_info",response);
                    editor.commit();
                    if(userName != null) {
                        System.out.println("?????? ?????? ?????????");
                        currentUser = jsonObject;
                        userID = jsonObject.getString("id");
                        messageList = new ArrayList<>();
                        messagesAdapter = new MessagesAdapter(ChatContainerActivity.this, currentUser, room.id,userID , messageList);
                        messagesAdapter.setHasStableIds(true);
                        getMessages(token, room.id, jsonObject.getString("id"),currentPage);
                        channelSubscribe(room.id);

                        chat_message_list.setAdapter(messagesAdapter);

                    } else{
                        Toast.makeText(ChatContainerActivity.this,"?????? ?????? ?????? ?????????", Toast.LENGTH_SHORT).show();
                    }
                } catch(JSONException err) {
                    err.printStackTrace();
                }
            }
        };
        UserRequest userRequest = new UserRequest(token,responseListener);
        RequestQueue queue = Volley.newRequestQueue(ChatContainerActivity.this);
        queue.add(userRequest);
    }

    @Override
    public void onButtonClicked(String text) {

    }
}