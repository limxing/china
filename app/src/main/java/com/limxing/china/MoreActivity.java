package com.limxing.china;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;

import com.limxing.library.SweetAlert.SweetAlertDialog;
import com.limxing.library.utils.FileUtils;
import com.limxing.library.utils.LogUtils;
import com.limxing.library.utils.ToastUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by limxing on 16/4/13.
 */
public class MoreActivity extends Activity implements View.OnClickListener {
    private ListView more_listview;
    private List<MoreBean> fileList;
    private List<MoreBean> finishList;
    MoreAdapter adapter;
    private int current = 0;
    private SweetAlertDialog dialog;
    int count = 0;
    int error = 0;

    private class MyHandler extends Handler {
        private final WeakReference<MoreActivity> mActivity;

        public MyHandler(MoreActivity activity) {
            mActivity = new WeakReference<MoreActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MoreActivity activity = mActivity.get();
            if (dialog != null && dialog.isShowing()) {
                dialog.setContentText(current + "/" + count);
            }
            if (activity != null) {
                switch (msg.what) {
                    case 0:

                        break;
                    case 1:
                        error++;
                        ToastUtils.showLong(activity, "文件不存在");
                        break;
                    case 2:
                        error++;
                        ToastUtils.showLong(activity, "转换失败,文件结构错误");
                        break;
                }
                current++;
                if (current < count) {
                    doTransfor();
                } else {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismissWithAnimation();
                    }
                    if(error==0){
                        ToastUtils.showLong(activity, "全部转换完成");
                    }else {
                        ToastUtils.showLong(activity, "部分转换完成,有" + error + "个没有转换成功");
                    }
                        fileList.removeAll(finishList);
                    adapter.notifyDataSetChanged();
                }

            }
        }
    }

    private final MyHandler mHandler = new MyHandler(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        findViewById(R.id.title_right_textview).setOnClickListener(this);
        more_listview = (ListView) findViewById(R.id.more_listview);
        fileList = new ArrayList<MoreBean>();
        finishList = new ArrayList<MoreBean>();
        adapter = new MoreAdapter(fileList, this);
        more_listview.setAdapter(adapter);


    }

    @Override
    protected void onResume() {
        super.onResume();
        fileList.clear();
        finishList.clear();

        String[] root = Environment.getExternalStorageDirectory().list();
        for (String s : root) {
            if (!s.equals("china")) {
                s = Environment.getExternalStorageDirectory() + "/" + s;
                File sFile = new File(s);
                //     LogUtils.i("ROOT:" + s);

                if (sFile.isDirectory()) {

                    String[] sRoot = sFile.list();
                    if (sRoot != null) {
                        for (String ss : sRoot) {

                            ss = s + "/" + ss;
                            if (new File(ss).isFile() && ss.endsWith(".xls")) {
                                fileList.add(new MoreBean(ss, false));

                            }
                        }
                    }
                } else if (s.endsWith(".xls")) {
                    fileList.add(new MoreBean(s, false));
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        current = 0;
        count = 0;
        error = 0;
        finishList.clear();
        for (MoreBean bean : fileList) {
            if (bean.isChecked()) {
                count++;
            }
        }
        if (count == 0) {
            ToastUtils.showShort(this, "没有选定任何表格");
        } else {
            dialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            dialog.setTitleText("正在批量转换");
            dialog.setCancelable(false);
            dialog.show();
            doTransfor();
        }
    }

    private void doTransfor() {
        final MoreBean bean = fileList.get(current);
        LogUtils.i(bean.getPath() + "=" + bean.isChecked());
        if (bean.isChecked()) {
            final File file = new File(bean.getPath());
            String name = file.getName();
            name = name.substring(0, name.lastIndexOf(".")) + "_changed.xls";
            if (file.exists()) {
                final File newFile = new File(FileUtils.getExternalStoragePath(), name);
                LogUtils.i(newFile.toString());
                if (newFile.exists()) {
                    SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
                    dialog.setTitleText("已存在:"+name).setContentText("是否替换?").setConfirmText("替换")
                            .setCancelText("取消'").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            newFile.delete();
                            transfor(bean);
                        }
                    }).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            mHandler.sendEmptyMessage(0);
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
                    dialog.show();
                } else {
                    transfor(bean);
                }

            } else {
//                        Snackbar.make(view, "文件不存在", Snackbar.LENGTH_SHORT)
//                                .setAction("Action", null).show();
                mHandler.sendEmptyMessage(1);

            }
        }

    }

    private void transfor(final MoreBean bean) {
        Controller.start(MoreActivity.this, bean.getPath(), new Controller.ControllerListener() {
            @Override
            public void finish() {
//                fileList.remove(bean);
                finishList.add(bean);
                mHandler.sendEmptyMessage(0);
            }

            @Override
            public void error() {
                mHandler.sendEmptyMessage(2);
            }
        });
    }
}
