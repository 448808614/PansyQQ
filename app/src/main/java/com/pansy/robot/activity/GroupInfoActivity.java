package com.pansy.robot.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pansy.robot.APP;
import com.pansy.robot.R;
import com.pansy.robot.dialog.ImitateIosDialog;
import com.pansy.robot.protocol.PCQQ;
import com.pansy.robot.protocol.QQAPI;
import com.pansy.robot.view.CircleImageView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GroupInfoActivity extends AppCompatActivity {
    private Button btn_send_group,btn_quit_group;
    private long gn;
    private String name;
    private TextView tv_groupName,tv_myGroupCard,tv_groupNumber,tv_groupMembersNum,tv_groupCreateTime;
    private CircleImageView img_group_head;
    private float touchX;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.group_info_layout);
        gn=getIntent().getLongExtra("gn",-1);
        name=getIntent().getStringExtra("name");

        tv_groupNumber=findViewById(R.id.tv_groupNumber);
        tv_groupNumber.setText("群号码:"+gn+"");
        tv_groupName=findViewById(R.id.tv_groupName);
        if(name!=null)
            tv_groupName.setText("群名称:"+name);
        tv_myGroupCard=findViewById(R.id.tv_myGroupCard);
        img_group_head=findViewById(R.id.img_group_head);
        tv_groupMembersNum=findViewById(R.id.tv_groupMembersNum);
        tv_groupCreateTime=findViewById(R.id.tv_groupCreateTime);

        new Thread(()-> {
                try {
                    final String card = QQAPI.getGroupCard(gn, APP.getQQ());
                    String url = "http://p.qlogo.cn/gh/" + gn + "/" + gn + "/140";
                    final Bitmap bitmap = BitmapFactory.decodeStream(new URL(url).openStream());

                    String info = QQAPI.getGroupInfo(gn);
                    JsonObject jsonObject = (JsonObject) new JsonParser().parse(info);
                    final String gMemNum=jsonObject.get("gMemNum").getAsInt()+"";
                    long gCrtTime=jsonObject.get("gCrtTime").getAsLong();
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    final String time=sdf.format(new Date(gCrtTime*1000));
                    runOnUiThread(()-> {
                            tv_myGroupCard.setText("我的群名片:" + card);
                            img_group_head.setImageBitmap(bitmap);
                            tv_groupMembersNum.setText("群人数:"+gMemNum);
                            tv_groupCreateTime.setText("群创建时间:"+time);
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
        }).start();
        btn_send_group=findViewById(R.id.btn_send_group);
        btn_quit_group=findViewById(R.id.btn_quit_group);
        btn_send_group.setOnClickListener((v)-> {
                finish();
        });
        btn_quit_group.setOnClickListener((v)->{
                ImitateIosDialog dialog=new ImitateIosDialog(GroupInfoActivity.this);
                dialog.setContent("是否退出该群？");
                dialog.setOnConfirmListener(()-> {
                        PCQQ.exitGroup(gn);
                        Toast.makeText(GroupInfoActivity.this,"退出成功",Toast.LENGTH_LONG).show();
                        finish();
                        return null;
                });
                dialog.show();
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
}
