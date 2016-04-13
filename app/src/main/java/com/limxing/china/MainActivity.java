package com.limxing.china;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.limxing.library.SVProgressHUD.SVProgressHUD;
import com.limxing.library.SweetAlert.SweetAlertDialog;
import com.limxing.library.utils.FileUtils;
import com.limxing.library.utils.LogUtils;
import com.limxing.library.utils.ToastUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 0;
    private SVProgressHUD svp;
    private ListView listView;

    private class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (svp.isShowing()) {
                svp.dismiss();
            }
            if (activity != null) {
                switch (msg.what) {
                    case 0:
                        ToastUtils.showLong(activity, "转换成功");
                        adapter.notifyDataSetChanged();
                        break;
                    case 1:
                        ToastUtils.showLong(activity, "文件不存在");
                        break;
                    case 2:
                        ToastUtils.showLong(activity, "转换失败,文件结构错误");
                        break;
                }

            }
        }
    }

    private final MyHandler mHandler = new MyHandler(this);
    List<String> list;
    MainListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        list = new ArrayList<String>();
        adapter = new MainListViewAdapter(list, this);
        listView.setAdapter(adapter);
        svp = new SVProgressHUD(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "选择一个文件进行转换"), FILE_SELECT_CODE);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_transfer_more) {
            Intent intent = new Intent(this, MoreActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        list.clear();
        new File(FileUtils.getExternalStoragePath()).list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {

                if (s.endsWith("_changed.xls")) {
                    list.add(s);
                    return true;
                }
                return false;
            }
        });
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    final String path = FileUtils.getPathFromSD(this, uri);
                    if (!path.endsWith(".xls")) {
                        ToastUtils.showLong(this, "选择的文件格式错误");
                        return;
                    }
                    final File file = new File(path);
                    String name = file.getName();
                    name = name.substring(0, name.lastIndexOf(".")) + "_changed.xls";
                    if (file.exists()) {
                        final File newFile = new File(FileUtils.getExternalStoragePath(), name);
                        LogUtils.i(newFile.toString());
                        if (newFile.exists()) {
                            SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
                            final String finalName = name;
                            dialog.setTitleText("已存在此转换文件").setContentText("是否替换?").setConfirmText("替换")
                                    .setCancelText("取消'").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                    newFile.delete();
                                    transfor(finalName, path);
                                }
                            }).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                }
                            });
                            dialog.show();
                        } else {
                            transfor(name, path);
                        }

                    } else {
//                        Snackbar.make(view, "文件不存在", Snackbar.LENGTH_SHORT)
//                                .setAction("Action", null).show();
                        mHandler.sendEmptyMessage(1);

                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void transfor(String name, String path) {
        svp.showWithProgress("正在转换", SVProgressHUD.SVProgressHUDMaskType.Black);
        final String finalName = name;
        Controller.start(MainActivity.this, path, new Controller.ControllerListener() {
            @Override
            public void finish() {
//                                Snackbar.make(view, "转换完成", Snackbar.LENGTH_LONG)
//                                        .setAction("Action", null).show();


                if (!list.contains(finalName)) {
                    list.add(finalName);
                }

                mHandler.sendEmptyMessage(0);
            }

            @Override
            public void error() {
                mHandler.sendEmptyMessage(2);
            }
        });

    }
}
