package com.pansy.robot.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.pansy.robot.APP;
import com.pansy.robot.R;
import com.pansy.robot.adapter.FriendsAdapter;
import com.pansy.robot.protocol.QQAPI;
import com.pansy.robot.struct.QQFriend;
import com.pansy.robot.utils.ByteUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public  class FragmentFriends extends Fragment {
    private static FriendsAdapter adapter;
    private ExpandableListView exlv;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_friends,null);
        exlv=v.findViewById(R.id.expandaleListView_friends);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(adapter==null){
           loadFriends();
        }else{
            exlv.setAdapter(adapter);
        }
    }
    /**
     * 加载好友列表
     */
    private void loadFriends(){
        new Thread(()-> {
                String str=QQAPI.getFriendList();
                //System.out.println(str);
                JsonParser parser=new JsonParser();
                try {
                    JsonObject object = (JsonObject) parser.parse(str);
                    JsonObject result = object.get("result").getAsJsonObject();
                    String[] groups=new String[result.size()];
                    QQFriend[][] childs=new QQFriend[result.size()][];
                    int i=0;
                    for(String key:result.keySet()){
                        if(result.get(key).getAsJsonObject().has("gname")){
                            groups[i]=result.get(key).getAsJsonObject().get("gname").getAsString();
                        }else{
                            groups[i]="我的好友";
                        }
                        if(result.get(key).getAsJsonObject().has("mems")){
                            JsonArray mems=result.get(key).getAsJsonObject().get("mems").getAsJsonArray();
                            childs[i]=new QQFriend[mems.size()];
                            for(int j=0;j<mems.size();j++){
                                String nick=mems.get(j).getAsJsonObject().get("name").getAsString();
                                nick=ByteUtil.cancelESC(nick);
                                long QQ=mems.get(j).getAsJsonObject().get("uin").getAsLong();
                                childs[i][j]=new QQFriend(QQ,nick);
                            }
                        }
                        i++;
                    }
                    adapter=new FriendsAdapter(APP.getMainTabContext(),groups,childs);
                    new Handler(Looper.getMainLooper()).post(()-> {
                        exlv.setAdapter(adapter);
                    });

                }catch (Exception e){
                    e.printStackTrace();
                }
        }).start();
    }
}