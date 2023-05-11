package com.pansy.robot.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import com.pansy.robot.APP;
import com.pansy.robot.R;
import com.pansy.robot.crypter.JNI_Security;
import com.pansy.robot.crypter.Md5;
import com.pansy.robot.dialog.ImitateIosDialog;
import com.pansy.robot.protocol.PCQQ;
import com.pansy.robot.protocol.QQAPI;
import com.pansy.robot.utils.ByteUtil;
import com.pansy.robot.utils.FileUtil;
import com.pansy.robot.utils.HttpRequest;
import com.pansy.robot.utils.PhoneUtil;
import com.pansy.robot.utils.SPHelper;
import com.pansy.robot.view.CircleImageView;
import com.pansy.robot.view.HorizonProgress;
import com.qihoo360.replugin.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import project.pyp9536.wanxiang.util.ScreenUtil;

public class LoginActivity extends AppCompatActivity {
    private EditText edt_account,edt_pwd;
    private static Button btn_login,btn_scan,btn_relogin;
    private LinearLayout linear_login;
    private ImitateIosDialog mProgressDialog;
    private static Spinner spi_server_list;
    public static Button getBtn_login() {
        return btn_login;
    }
    public static Button getBtn_scan() {
        return btn_scan;
    }
    public static Button getBtn_relogin() {
        return btn_relogin;
    }
    private CircleImageView img_head;
    private ExecutorService threadPool=Executors.newSingleThreadExecutor();
    private boolean pwd_encrypt=false;
    private HorizonProgress mHp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        APP.setLoginContext(this);
        linear_login=findViewById(R.id.linear_login);
        edt_account=findViewById(R.id.edt_account);
        edt_pwd=findViewById(R.id.edt_pwd);
        btn_login=findViewById(R.id.btn_login);
        btn_scan=findViewById(R.id.btn_scan);
        btn_relogin=findViewById(R.id.btn_relogin);
        spi_server_list=findViewById(R.id.spi_server_list);
        int login_server=SPHelper.readInt("login_server",-1);
        if(login_server>-1)
            spi_server_list.setSelection(login_server);

        //申请权限
        int writeStoragePermission=checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int phoneStatePermission=checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE);
        if(writeStoragePermission!=PackageManager.PERMISSION_GRANTED || phoneStatePermission!=PackageManager.PERMISSION_GRANTED )
            ActivityCompat.requestPermissions(LoginActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE},0);

        if(hasNewVersion()){
            btn_login.setEnabled(false);
            btn_scan.setEnabled(false);
            btn_relogin.setEnabled(false);
            showUpdateDialog();
        }
        img_head=findViewById(R.id.img_head);
        edt_pwd.setOnFocusChangeListener(new FocusChangeListener());
        edt_pwd.setOnClickListener((v)->{
            edt_pwd.setText("");
            pwd_encrypt=false;
        });
        btn_login.setOnClickListener(new LoginClickListener());
        btn_scan.setOnClickListener(new QRLoginClickListener());
        btn_relogin.setOnClickListener(new ReLoginClickListener());
        spi_server_list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SPHelper.writeInt("login_server",position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        readQQ();
        //如果是重复加载LoginActivity则销毁
        if(APP.getQQ()>0)
            PCQQ.loadMainTabActivity();
        loadBg();
        checkXposed();
    }

    private void checkXposed(){
        ClassLoader ctxLoader=this.getClassLoader();
        if(ctxLoader.toString().contains("io.va.exposed"))
            System.exit(0);
    }

    private void loadBg(){
        try{
            FileInputStream inputStream=new FileInputStream(APP.getPansyQQPath()+"bg.jpg");
            byte bytes[]=ByteUtil.inputStream2ByteArray(inputStream);
            Drawable drawable=ByteUtil.byteArray2Drawable(bytes);
            linear_login.setBackground(drawable);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static Spinner getSpi_server_list(){return spi_server_list;}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==0){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){
            }else{
                Toast.makeText(LoginActivity.this,"软件没有权限，无法运行",Toast.LENGTH_LONG).show();
                finish();
            }
        }else if(requestCode==1){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                FileUtil.installApk(this,"PansyQQ.apk");
            else{
                //将用户引导至安装未知应用界面。
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                startActivityForResult(intent, 1);
            }
        }
    }

    private void showUpdateDialog(){
        ImitateIosDialog dialog=new ImitateIosDialog(this);
        dialog.setCancelable(false);
        dialog.setCountdown(3);
        dialog.setTitle("发现新版本，是否更新");
        dialog.setOnConfirmListener(()-> {
            downloadNewApk();
            return null;
        });
        dialog.setOnCancelListener(()->{
            Toast.makeText(LoginActivity.this,"你取消了更新",Toast.LENGTH_LONG).show();
            finish();
            return null;
        });
        String update=getUpdateContent();
        if(update.length()>1)
            dialog.setContent(update);
        dialog.show();
    }
    //获取更新内容
    private String getUpdateContent(){
        Future<String> future=threadPool.submit(()->{
            String str=HttpRequest.get("http://"+APP.getMyService() +"/Pansy/update.txt");
            return str;
        });
        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    private void readQQ(){
        String[] arr=SPHelper.read_QQ();
        if(arr!=null){
            edt_account.setText(arr[0]);
            edt_pwd.setText(arr[1]);
            pwd_encrypt=true;
        }
    }

    /**
     * 检测服务器是否有新版本
     */
    private boolean hasNewVersion(){
        Future<String> future= threadPool.submit(()->{
            String ver=HttpRequest.get("http://"+APP.getMyService() +"/Pansy/ver.txt");
            return ver;
        });

        try {
            String ver=future.get();
            if(ver.equals(APP.getVersion()) || ver.equals("")){
                return false;
            }else{
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 下载服务器上最新的apk
     */
    private void downloadNewApk(){
        mProgressDialog=new ImitateIosDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.noAutoDismiss();
        mProgressDialog.setTitle("正在更新");
        mProgressDialog.setContent("请勿中途退出");
        mProgressDialog.hideCancel();
        mProgressDialog.hideConfirm();
        mProgressDialog.hideV1();
        mHp=new HorizonProgress(this);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin=ScreenUtil.INSTANCE.dp2px(this,10);
        lp.rightMargin=ScreenUtil.INSTANCE.dp2px(this,10);
        mHp.setLayoutParams(lp);
        mProgressDialog.addView(mHp);
        mProgressDialog.show();
        threadPool.execute(()-> {
            try {
                String u = "http://"+APP.getMyService()+"/Pansy/PansyQQ.apk?t="+System.currentTimeMillis();
                File file = new File(APP.getPansyQQPath());
                if (!file.exists())
                    file.mkdir();
                file=new File(APP.getPansyQQPath()+"PansyQQ.apk");
                if(file.exists()){
                    file.delete();
                }
                HttpURLConnection connection;
                URL url=new URL(u);
                connection=(HttpURLConnection) url.openConnection();
                connection.connect();
                int fileLength=connection.getContentLength();
                int currentLength=0;
                InputStream inputStream=connection.getInputStream();
                OutputStream outputStream=new FileOutputStream(file);
                byte[] buf=new byte[1024];
                int count=0;
                Message msg=new Message();
                msg.what=3;
                msg.arg1=fileLength;
                handler.sendMessage(msg);
                while((count=inputStream.read(buf))!=-1){
                    currentLength+=count;
                    outputStream.write(buf,0,count);
                    msg=new Message();
                    msg.what=2;
                    msg.arg1=(int)(((currentLength*1.0f)/(fileLength*1.0f))*100);
                    handler.sendMessage(msg);
                }
                msg=new Message();
                msg.what=4;
                handler.sendMessage(msg);
            }catch (Exception e){
                Message msg=new Message();
                msg.what=1;
                handler.sendMessage(msg);
            }
        });

    }

    @SuppressLint("HandlerLeak")
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Toast.makeText(LoginActivity.this,"无法下载，请开启存储空间权限",Toast.LENGTH_LONG).show();
                    finish();
                    break;
                case 2:
                    mHp.setProgress(msg.arg1);
                    break;
                case 3:

                    break;
                case 4:
                    mProgressDialog.dismiss();
                    //Toast.makeText(LoginActivity.this,"如果安装失败请卸载旧版后到PansyQQ目录下手动安装",Toast.LENGTH_LONG).show();
                    //安卓8.0需要判断是否有安装权限
                    if(Build.VERSION.SDK_INT>=26){
                        boolean installAllowed=getPackageManager().canRequestPackageInstalls();
                        if (!installAllowed) {
                            Toast.makeText(LoginActivity.this,"请允许安装应用权限",Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, 1);
                        }else
                            FileUtil.installApk(LoginActivity.this,"PansyQQ.apk");
                    }else
                        FileUtil.installApk(LoginActivity.this,"PansyQQ.apk");

                    ImitateIosDialog dialog=new ImitateIosDialog(LoginActivity.this);
                    dialog.noAutoDismiss();
                    dialog.setCancelable(false);
                    dialog.setContent("请确认安装");
                    dialog.setCancelText("退出");
                    dialog.setOnConfirmListener(()->{
                        FileUtil.installApk(LoginActivity.this,"PansyQQ.apk");
                        return null;
                    });
                    dialog.setOnCancelListener(()->{
                        System.exit(0);
                        return null;
                    });
                    dialog.show();
                    break;
            }
        }
    };

    class LoginClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            String account=edt_account.getText().toString();
            String pwd=edt_pwd.getText().toString();
            if(account.equals("")==false && pwd.equals("")==false){
                if(pwd_encrypt==false || pwd.length()!=32){
                    pwd=new Md5().d(pwd).toUpperCase();
                    edt_pwd.setText(pwd);
                    pwd_encrypt=true;
                }
                PCQQ.login(LoginActivity.this,Long.parseLong(account),pwd,1);
            }else
                Toast.makeText(v.getContext(),"请输入QQ帐号或密码",Toast.LENGTH_LONG).show();
        }
    }
    class QRLoginClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            String account=edt_account.getText().toString();
            if(account.equals("")==false)
                PCQQ.login(LoginActivity.this,Long.parseLong(account),"",2);
            else
                Toast.makeText(LoginActivity.this,"扫码登录需要输入QQ帐号，无需密码",Toast.LENGTH_LONG).show();
        }
    }

    class ReLoginClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            long QQ=SPHelper.readLong("login_info","QQ",0);
            if(QQ==0){
                ImitateIosDialog dialog=new ImitateIosDialog(LoginActivity.this);
                dialog.setTitle("提示");
                dialog.setContent("没有找到你的登录记录，请先使用密码或扫码方式进行登录");
                dialog.setContentColor(Color.RED);
                dialog.hideCancel();
                dialog.show();
            }else
                PCQQ.login(LoginActivity.this,QQ,"",3);
        }
    }

    class FocusChangeListener implements View.OnFocusChangeListener{

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            edt_pwd.setText("");
            pwd_encrypt=false;
            if(edt_account.getText().toString().equals("")==false && hasFocus)
                threadPool.execute(new GetHeadThread());
        }

    }
    class GetHeadThread implements Runnable{
        @Override
        public void run() {
            final Bitmap bitmap=QQAPI.getHead(Long.parseLong(edt_account.getText().toString()));
            if(bitmap!=null){
                new Handler(Looper.getMainLooper()).post(()->{
                    img_head.setImageBitmap(bitmap);
                    img_head.setVisibility(View.VISIBLE);
                });
            }
        }
    }

    private void getCrc(){
       try{
           String apkPath = LoginActivity.this.getPackageCodePath();
           ZipFile zipfile = new ZipFile(apkPath);
           ZipEntry dexentry = zipfile.getEntry("classes.dex");
           long crc = dexentry.getCrc();
           //System.out.println("crc:"+crc);
          // if (crc != dexCrc) {

          // }
       }catch (Exception e){
           e.printStackTrace();
       }

   }

    private void getSignature() {
        PackageManager pm = this.getPackageManager();
        PackageInfo pi;
        StringBuilder sb = new StringBuilder();
        try {
            pi = pm.getPackageInfo(this.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signatures = pi.signatures;
            for (Signature signature : signatures) {
                sb.append(signature.toCharsString());
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //System.out.println("sign:"+sb.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
