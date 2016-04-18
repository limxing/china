package com.limxing.china;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.limxing.library.SweetAlert.SweetAlertDialog;
import com.limxing.library.utils.FileUtils;
import com.limxing.library.utils.LogUtils;
import com.limxing.library.utils.MyThreadPool;
import com.limxing.library.utils.PhoneInfo;
import com.limxing.library.utils.StringUtils;
import com.limxing.library.utils.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by limxing on 16/4/10.
 */
public class WelcomeActivity extends Activity {
    private static final int INSTALL = 88;
    private boolean isNew = false;
    private SweetAlertDialog dialog;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismissWithAnimation();
                }
//                String path = FileUtils.getCacheDir() + "china.apk";
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.fromFile(new File(FileUtils.getCacheDir(),"china.apk")),
//                        "applicationnd.android.package-archive");
//               startActivityForResult(intent, 0);
                Intent install = new Intent();
                install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                install.setAction(android.content.Intent.ACTION_VIEW);
                install.setDataAndType(Uri.fromFile(new File(FileUtils.getCacheDir(), "china.apk")), "application/vnd.android.package-archive");
                startActivityForResult(install, INSTALL);
                return;
            }
            if (msg.what == 2) {
                dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                dialog.setTitleText("下载失败").setContentText("重新下载");
                dialog.setConfirmText("下载").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        toDownload();
                    }
                });
                return;
            }
            dialog = new SweetAlertDialog(WelcomeActivity.this, SweetAlertDialog.WARNING_TYPE);
            dialog.setTitleText("发现新版本").setContentText("是否进行下载更新");
            dialog.setConfirmText("下载").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismissWithAnimation();
                    toDownload();
                }
            }).setCancelText("取消").setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismiss();
                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            dialog.show();
        }
    };

//    private class MyHandler extends Handler {
//        private final WeakReference<WelcomeActivity> mActivity;
//
//        public MyHandler(WelcomeActivity activity) {
//            mActivity = new WeakReference<WelcomeActivity>(activity);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//
//
//        }
//    }

    private void toDownload() {
        dialog = new SweetAlertDialog(WelcomeActivity.this, SweetAlertDialog.PROGRESS_TYPE).setTitleText("正在下载");
        dialog.show();
        String url = "http://www.limxing.com/china.apk";
        LogUtils.i(FileUtils.getCacheDir() + "china.apk");
        File file = new File(FileUtils.getCacheDir() + "china.apk");
        if (file.exists()) {
            file.delete();
        }
        new HttpUtils().download(url, FileUtils.getCacheDir() + "china.apk", new RequestCallBack<File>() {
            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                mHandler.sendEmptyMessage(1);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                mHandler.sendEmptyMessage(2);
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {

                dialog.setContentText(StringUtils.formatFileSize(current) + "/" + StringUtils.formatFileSize(total));
            }
        });
    }

//    private final MyHandler mHandler = new MyHandler(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        File file = new File(FileUtils.getExternalStoragePath() + "country.xls");
        if (!file.exists()) {
            FileUtils.copyFromAssets(this, "country.xls", "country.xls");
        }

        MyThreadPool.excuteCachedTask(new Runnable() {
            @Override
            public void run() {
                try {
                    checkUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isNew) {
                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 3000);

    }

    /**
     * 检查更新
     *
     * @throws Exception
     */
    private void checkUpdate() throws Exception {
        PackageManager manager = this.getPackageManager();
        PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
        String version = info.versionName;
        URL url = new URL("http://www.limxing.com/china.json");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(5000);
        connection.setRequestMethod("GET");
        InputStream inStream = connection.getInputStream();
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[1024];
        for (int n; (n = inStream.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        String json = out.toString();
        LogUtils.i(version + "==" + json);
        JSONObject jsonObj = new JSONObject(json);
        if (jsonObj.getString("version").equals(version)) {
            isNew = false;
        } else {
            isNew = true;
            mHandler.sendEmptyMessage(0);

        }
        connection.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INSTALL) {
            LogUtils.i(requestCode + "==" + resultCode);
            ToastUtils.showLong(WelcomeActivity.this, "安装取消");
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }


}
