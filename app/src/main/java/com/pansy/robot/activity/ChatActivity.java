package com.pansy.robot.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.pansy.robot.APP;
import com.pansy.robot.R;
import com.pansy.robot.adapter.ChatAdapter;
import com.pansy.robot.protocol.PCQQ;
import com.pansy.robot.protocol.SendQQMessage;
import com.pansy.robot.struct.QQMessage;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private static List<QQMessage> msgList=new ArrayList<>();
    private EditText txt_input;
    private Button btn_send,btn_back;
    private TextView txt_name;
    private static RecyclerView recyclerView_chat;
    private static ChatAdapter adapter;
    public static int msg_type=-1;
    public static long gn=-1;//QQ或群号
    private String name;//好友或群昵称
    private String curMsg;
    public static long QQ;
    private ImageView iv_group_icon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.chat_layout);
        Intent intent=getIntent();
        msg_type=intent.getIntExtra("msg_type",-1);
        gn=intent.getLongExtra("gn",-1);
        QQ=intent.getLongExtra("QQ",-1);
        name=intent.getStringExtra("name");
        curMsg=intent.getStringExtra("msg");

        iv_group_icon=findViewById(R.id.iv_group_icon);
        if(msg_type==0)
            iv_group_icon.setVisibility(View.VISIBLE);
        iv_group_icon.setOnClickListener((v)-> {
            Intent intent1=new Intent(ChatActivity.this,GroupInfoActivity.class);
            intent1.putExtra("gn",gn);
            intent1.putExtra("name",name);
            startActivity(intent1);
        });
        txt_input=findViewById(R.id.edt_chat);
        btn_send=findViewById(R.id.btn_chat_send);
        btn_back=findViewById(R.id.btn_chat_back);
        txt_name=findViewById(R.id.txt_chat_name);
        txt_name.setText(name);
        recyclerView_chat=findViewById(R.id.recyclerView_chat);
        btn_back.setOnClickListener((v)->{
            msg_type=-1;
            QQ=-1;
            gn=-1;
            ChatActivity.this.finish();
        });
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView_chat.setLayoutManager(layoutManager);
        msgList.clear();
        if(curMsg!=null){
            msgList.add(new QQMessage(gn,QQ,curMsg,"",msg_type));
        }
        adapter=new ChatAdapter(msgList);
        recyclerView_chat.setAdapter(adapter);
        btn_send.setOnClickListener((v)->{
                String content=txt_input.getText().toString();
                if(content.equals("")==false){
                    if(msg_type==0){
                        SendQQMessage.sendGroupMessage(gn,content);
                    }else if(msg_type==1) {
                        QQMessage msg = new QQMessage(-1, APP.getQQ(), content, "", msg_type);
                        SendQQMessage.sendFriendMessage(QQ,content);
                        msgList.add(msg);
                        adapter.notifyItemInserted(msgList.size()-1);
                        recyclerView_chat.scrollToPosition(msgList.size()-1);
                    }
                    txt_input.setText("");
                }
        });
        //滑动切换
        int primary = getResources().getColor(R.color.colorPrimaryDark);
        int secondary = getResources().getColor(R.color.colorAccent);
        SlidrConfig config = new SlidrConfig.Builder().primaryColor(primary)
                .secondaryColor(secondary)
                .scrimColor(Color.BLACK)
                .position(SlidrPosition.LEFT)
                .scrimEndAlpha(0f)
                .sensitivity(1f)
                .scrimColor(Color.BLACK)
                .scrimStartAlpha(0.8f)
                .scrimEndAlpha(0f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .edge(true)
                .edgeSize(0.18f)
                .build();
        Slidr.attach(this, config);

    }
    public static void addMsg(final QQMessage qm){
        if(recyclerView_chat!=null && adapter!=null){
            new Handler(Looper.getMainLooper()).post(()-> {
                try {
                    msgList.add(qm);
                    adapter.notifyItemInserted(msgList.size() - 1);
                    recyclerView_chat.scrollToPosition(msgList.size() - 1);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });

        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        //if(APP.getNoReceiveHeartbeat()>=3)
            //PCQQ.relogin();
    }

    @Override
    protected void onDestroy() {
        msg_type=-1;
        gn=-1;
        QQ=-1;
        super.onDestroy();
    }

}

