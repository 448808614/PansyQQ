package com.pansy.robot.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.pansy.robot.APP;
import com.pansy.robot.R;
import com.pansy.robot.activity.MainTabActivity;
import com.pansy.robot.dialog.ImitateIosDialog;
import com.pansy.robot.utils.FileUtil;
import com.pansy.robot.utils.Gzip;
import com.pansy.robot.utils.HttpRequest;
import com.pansy.robot.utils.SPHelper;
import com.pansy.robot.view.HorizonProgress;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import project.pyp9536.wanxiang.util.ScreenUtil;

public class FragmentPluginShop extends Fragment {
    private Context mContext;
    private ImitateIosDialog mProgressDialog;
    private WebView wv;
    private ExecutorService threadPool= Executors.newSingleThreadExecutor();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.tab_plugin_shop,null);
        mContext=APP.getMainTabContext();
        wv=v.findViewById(R.id.wv);
        WebSettings settings = wv.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        wv.setVerticalScrollBarEnabled(false);
        wv.setHorizontalScrollBarEnabled(false);
        wv.addJavascriptInterface(this,"Plugin");
        wv.loadUrl("http://"+APP.getMyService()+"/Pansy/develope/plugin_list.php");
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @JavascriptInterface
    public void download(String packageName,String pluginName,String type){
        new Handler(Looper.getMainLooper()).post(()->{
            mProgressDialog=new ImitateIosDialog(mContext);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setTitle("正在下载");
            mProgressDialog.setContent(pluginName);
            HorizonProgress hp=new HorizonProgress(mContext);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.leftMargin=ScreenUtil.INSTANCE.dp2px(mContext,10);
            lp.rightMargin=ScreenUtil.INSTANCE.dp2px(mContext,10);
            hp.setLayoutParams(lp);
            mProgressDialog.addView(hp);
            mProgressDialog.hideConfirm();
            mProgressDialog.hideCancel();
            mProgressDialog.hideV1();
            mProgressDialog.show();

            threadPool.execute(()-> {
                HttpRequest.post("http://"+APP.getMyService()+"/Pansy/develope/add_download.php","packageName="+packageName);
                HttpURLConnection connection;
                try{
                    String plugin_url="http://"+APP.getMyService()+"/Pansy/plugin/"+packageName+".apk";
                    URL url=new URL(plugin_url);
                    connection=(HttpURLConnection) url.openConnection();
                    connection.connect();
                    int fileLength=connection.getContentLength();
                    if(fileLength<1024){
                        plugin_url="http://"+APP.getMyService()+"/Pansy/plugin/"+packageName+".cpk";
                        url=new URL(plugin_url);
                        connection=(HttpURLConnection) url.openConnection();
                        connection.connect();
                        fileLength=connection.getContentLength();
                        if(fileLength<1024){
                            new Handler(Looper.getMainLooper()).post(()-> {
                                Toast.makeText(mContext, "下载失败，插件损坏", Toast.LENGTH_SHORT).show();
                                mProgressDialog.dismiss();
                            });
                            return;
                        }
                    }
                    int currentLength=0;
                    InputStream inputStream=connection.getInputStream();
                    final String path=APP.getPansyQQPath()+"plugin/";
                    //获取插件文件名
                    int index=-1;
                    for(int i=plugin_url.length()-1;i>=0;i--){
                        if(plugin_url.charAt(i)=='/'){
                            index=i;
                            break;
                        }
                    }
                    final String fileName=plugin_url.substring(index+1,plugin_url.length());
                    File file=new File(path+fileName);
                    OutputStream outputStream=new FileOutputStream(file);
                    byte[] buf=new byte[1024];
                    int count=0;
                    Handler handler=new Handler(Looper.getMainLooper()){
                        public void handleMessage(Message msg){
                            switch (msg.what){
                                case 1:
                                    hp.setProgress(msg.arg1);
                                    break;
                                case 2:
                                    mProgressDialog.dismiss();
                                    if(type.equals("aidl")){//aidl插件
                                        if (SPHelper.readBool("first_download_plugin_aidl",true)){
                                            SPHelper.writeBool("first_download_plugin_aidl",false);
                                            ImitateIosDialog dialog=new ImitateIosDialog(mContext);
                                            dialog.setCancelable(false);
                                            dialog.setTitle("下载完成");
                                            dialog.setContent("温馨提示：该插件需要安装后使用哦，如果无法安装请允许主程序安装应用权限");
                                            dialog.setConfirmText("我知道了");
                                            dialog.setCountdown(3);
                                            dialog.setOnConfirmListener(()->{
                                                FileUtil.installApk(APP.getMainTabContext(),"/plugin/"+fileName);
                                                return null;
                                            });
                                            dialog.hideCancel();
                                            dialog.show();
                                        }else{
                                            Toast.makeText(mContext, "下载完成", Toast.LENGTH_SHORT).show();
                                            FileUtil.installApk(APP.getMainTabContext(),"/plugin/"+fileName);
                                        }
                                    }else{
                                        if (SPHelper.readBool("first_download_plugin_replugin",true)){
                                            SPHelper.writeBool("first_download_plugin_replugin",false);
                                            ImitateIosDialog dialog=new ImitateIosDialog(mContext);
                                            dialog.setCancelable(false);
                                            dialog.setTitle("下载完成");
                                            dialog.setContent("温馨提示：左滑可以对插件进行设置哦");
                                            dialog.setConfirmText("我知道了");
                                            dialog.setCountdown(3);
                                            dialog.hideCancel();
                                            dialog.show();
                                        }else
                                            Toast.makeText(mContext, "下载完成", Toast.LENGTH_SHORT).show();
                                        //更新本地插件列表
                                        try{
                                            List<PluginInfo> list=MainTabActivity.getListInfo();
                                            PluginInfo info=null;
                                            if (fileName.endsWith(".apk")){
                                                info=RePlugin.install(path+fileName);
                                            }else if(fileName.endsWith(".cpk")){
                                                byte[] b=Gzip.pluginDecrypt(path+fileName);
                                                String temp=path+fileName.replace(".cpk",".apk");
                                                FileUtil.writeByteArray(temp,b);
                                                info=RePlugin.install(temp);
                                                FileUtil.delete(temp);
                                                FileUtil.delete(path+fileName);
                                            }
                                            if(info!=null) {
                                                list.add(info);
                                                Intent intent = new Intent();
                                                intent.setComponent(new ComponentName(info.getPackageName(), info.getPackageName() + ".event.PluginInitActivity"));
                                                RePlugin.startActivity(APP.getMainTabContext(),intent);
                                            }
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                    break;
                                case 3:

                                    break;
                                case 4:
                                    Toast.makeText(mContext, "下载失败", Toast.LENGTH_SHORT).show();
                                    mProgressDialog.dismiss();
                            }
                        }
                    };
                    Message msg=new Message();
                    msg.what=3;
                    handler.sendMessage(msg);
                    while((count=inputStream.read(buf))!=-1){
                        currentLength+=count;
                        outputStream.write(buf,0,count);
                        msg=new Message();
                        msg.what=1;
                        msg.arg1=(int)(((currentLength*1.0f)/(fileLength*1.0f))*100);
                        handler.sendMessage(msg);
                    }
                    msg=new Message();
                    msg.what=2;
                    handler.sendMessage(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        });
    }

    private void loadPluginShop(){
        /*threadPool.execute(()-> {
            String str=HttpRequest.get("http://"+APP.getMyService()+"/Pansy/develope/plugin_shop.php");
            try{
                List<Plugin> list=new ArrayList<>();
                JsonParser parser=new JsonParser();
                JsonArray arr=(JsonArray)parser.parse(str);
                for(int i=0;i<arr.size();i++){
                    try{
                        JsonObject obj=arr.get(i).getAsJsonObject();
                        String packageName=obj.get("packageName").getAsString();
                        String url="http://"+APP.getMyService()+"/Pansy/plugin/"+packageName+".apk";
                        String name=obj.get("name").getAsString();
                        String brief=obj.get("brief").getAsString();
                        String author=obj.get("author").getAsString();
                        String version=obj.get("version").getAsString();
                        int download=obj.get("download").getAsInt();
                        int type=obj.get("type").getAsInt();
                        Plugin plugin=new Plugin(url,packageName,name,brief,author,version,null,false);
                        plugin.download=download;
                        if(type==1) plugin.is_aidl=true;
                        list.add(plugin);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                adapter=new PluginShopAdapter(FragmentPluginShop.this.getContext(),list);
                new Handler(Looper.getMainLooper()).post(()-> {
                    lv_plugin_shop.setAdapter(adapter);
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        });*/
    }
}
