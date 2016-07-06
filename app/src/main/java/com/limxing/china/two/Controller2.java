package com.limxing.china.two;

import android.app.Activity;

import com.limxing.china.Controller;
import com.limxing.library.utils.FileUtils;
import com.limxing.library.utils.LogUtils;
import com.limxing.library.utils.MyThreadPool;
import com.limxing.library.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import jxl.Cell;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Created by limxing on 16/7/6.
 */
public class Controller2 {
    private static WritableCellFormat wcf;

    public static void start(final Activity activity, final String path, final Controller.ControllerListener listener) {
        MyThreadPool.excuteCachedTask(new Runnable() {
            @Override
            public void run() {
                boolean isError = false;
                Workbook book = null;
                Workbook book1 = null;
                WritableWorkbook book2 = null;
                File file = new File(path);
                String name = file.getName();
                if (!name.contains(".")) {
                    listener.error();
                    return;
                }
                name = name.substring(0, name.lastIndexOf("."));
                try {
                    FileUtils.copyFromAssets(activity, "moban2.xls", name + "_changed.xls");
                    book = Workbook.getWorkbook(file);
                    book1 = Workbook.getWorkbook(new File(FileUtils.getExternalStoragePath(), name + "_changed.xls"));

                    book2 = Workbook.createWorkbook(new File(FileUtils.getExternalStoragePath(), name + "_changed.xls"), book1);
                    // 获得第一个工作表对象
                    Sheet sheet = book.getSheet(3);
                    int rownum = sheet.getRows();// 得到行数



                    WritableSheet sheet2 = book2.getSheet(0);
                    //给表格加边框的format
                    Cell cell = sheet2.getCell(0, 0);
                    wcf = new WritableCellFormat(cell.getCellFormat());
                    wcf.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);

                    //开始循环便利
                    for (int i=1;i<rownum;i++){
                        Cell s= sheet.getCell(14,i);//列 行
                        Cell cell1=sheet.getCell(0,i);
                       
                        break;
                    }








                } catch (Exception e) {
                    isError = true;
                    e.printStackTrace();
                } finally {
                    try {
                        if (book2 != null) {
                            book2.write();
                            book2.close();
                        }
                        if (book1 != null) {
                            book1.close();
                        }

                        if (book != null) {
                            book.close();
                        }

                    } catch (IOException | WriteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        isError = true;
                    }
                    if (isError) {
                        listener.error();
                        File errorFile = new File(FileUtils.getExternalStoragePath(), name + "_changed.xls");
                        if (errorFile.exists()) {
                            errorFile.delete();
                        }
                    } else {
                        listener.finish();
                        System.out.println("完成");
                    }


                }
            }
        });

    }




    @SuppressWarnings("static-access")
    public static String getFormat(String day){

          String  format = "d";

        try {
            Calendar calendar = new GregorianCalendar();
            calendar.set(1900, 0, 1);
            calendar.add(calendar.DATE, Integer.valueOf(day)-2);// 把日期往后增加一天.整数往后推,负数往前移动
            SimpleDateFormat sFormat = new SimpleDateFormat(format);
            return sFormat.format(calendar.getTime());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return day;
    }

    @SuppressWarnings("static-access")
    public static String getyyyyMM(String day){
        try {
            Calendar calendar = new GregorianCalendar();
            calendar.set(1900, 0, 1);
            calendar.add(calendar.DATE, Integer.valueOf(day));// 把日期往后增加一天.整数往后推,负数往前移动
            SimpleDateFormat sFormat = new SimpleDateFormat("yyyy年MM月");
            return sFormat.format(calendar.getTime());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return day;
    }

}
