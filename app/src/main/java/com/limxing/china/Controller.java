package com.limxing.china;

import android.app.Activity;

import com.limxing.library.utils.FileUtils;
import com.limxing.library.utils.MyThreadPool;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jxl.Cell;
import jxl.DateCell;
import jxl.LabelCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.CellFormat;
import jxl.read.biff.BiffException;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.DateRecord;
import jxl.write.biff.RowsExceededException;

/**
 * Created by limxing on 16/4/10.
 */
public class Controller {
    static Workbook country1 = null;
    static Map<String, String> rongMap;
    static Map<String, String> fangMap;
    static boolean isEmpty;
    static String tomorrow;
    private static String today;
    private static DateCell dc;
    private static WritableCellFormat wcf;
    private static WritableCellFormat dateWcf;
    private static WritableCellFormat dateFormat;

    public static void start(final Activity activity, final String path, final ControllerListener listener) {
        MyThreadPool.excuteCachedTask(new Runnable() {
            @Override
            public void run() {
                boolean isError = false;
                Workbook book = null;
                Workbook book1 = null;
                WritableWorkbook book2 = null;
                isEmpty = false;
                File file = new File(path);
                String name = file.getName();
                if (!name.contains(".")) {
                    listener.error();
                    return;
                }
                name = name.substring(0, name.lastIndexOf("."));
                try {
                    initCountryMaps();
                    FileUtils.copyFromAssets(activity, "moban.xls", name + "_changed.xls");

                    book = Workbook.getWorkbook(file);
                    book1 = Workbook.getWorkbook(new File(FileUtils.getExternalStoragePath(), name + "_changed.xls"));
                    book2 = Workbook.createWorkbook(new File(FileUtils.getExternalStoragePath(), name + "_changed.xls"), book1);
                    // 获得第一个工作表对象
                    Sheet sheet = book.getSheet(0);
                    // WritableSheet sheet2 = book2.getSheet(1);
                    // 得到第一列第一行的单元格
                    // int columnum = sheet.getColumns();// 得到列数
                    int rownum = sheet.getRows();// 得到行数
                    // System.out.println(columnum);
                    // System.out.println(rownum);
                    // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd
                    // HH:mm:ss");
                    SimpleDateFormat dateFormat1 = new SimpleDateFormat("M月d日");
                    long time = System.currentTimeMillis();// 当前时间
                    today = dateFormat1.format(new Date(time));
//                    today = new Date(time);
                    time = time + 86400000;
                    tomorrow = dateFormat1.format(new Date(time));
//                    tomorrow = new Date(time);


                    //获取日期格式
                    WritableSheet sheet2 = book2.getSheet(0);
                    Cell cell1Sheet2 = sheet2.getCell(18, 0);
                    dateWcf = new WritableCellFormat(cell1Sheet2.getCellFormat());
                    dateWcf.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
                    //给表格加边框的format
//                    Label label = new Label(0, 0, "");
//                    CellFormat s = label.getCellFormat();
                    cell1Sheet2 = sheet2.getCell(0, 0);
                    wcf = new WritableCellFormat(cell1Sheet2.getCellFormat());
                    wcf.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);

                    cell1Sheet2 = sheet2.getCell(1, 0);
                    dateFormat = new WritableCellFormat(cell1Sheet2.getCellFormat());
                    dateFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);


                    int guiDang = 0;
                    int xiaoYuan = 0;
                    int paiDan = 0;
                    int cheDan = 0;

                    for (int i = 1; i < rownum; i++)// 循环进行读写
                    {
                        Cell cell = sheet.getCell(27, i);
                        String result = cell.getContents();
                        switch (result) {
                            case "执行中":
                                paiDan++;
                                zhixingzhong(sheet, book2, i, 2, paiDan, true, "派单");
                                break;
                            case "已开通":
                                guiDang++;
                                //归档
                                zhixingzhong(sheet, book2, i, 0, guiDang, true, "归档");
                                break;
                            case "已归档":
                                cell = sheet.getCell(3, i);
                                result = cell.getContents();
                                if (result.equals("OTTTV 拆机流程")) {
                                    guiDang++;
                                    //校园里并OTTTV
                                    zhixingzhong(sheet, book2, i, 0, guiDang, true, "归档");
                                } else if (result.equals("执行中")) {
                                    paiDan++;
                                    zhixingzhong(sheet, book2, i, 2, paiDan, true, "派单");
                                } else {
                                    xiaoYuan++;
                                    zhixingzhong(sheet, book2, i, 1, xiaoYuan, false, "");
                                }
                                break;
                            case "已撤单":
                                cheDan++;
                                zhixingzhong(sheet, book2, i, 3, cheDan, false, "");
                                break;
                            case "已拆除":
                                guiDang++;
                                //拆机
                                zhixingzhong(sheet, book2, i, 0, guiDang, true, "拆机");
                                break;
                            case "开通失败":
                                cheDan++;
                                zhixingzhong(sheet, book2, i, 3, cheDan, false, "");
                                break;

                            case "开通失败,驳回CRM":
                                cheDan++;
                                zhixingzhong(sheet, book2, i, 3, cheDan, false, "");
                                break;
                            default:
                                break;
                        }

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
                        if (country1 != null) {
                            country1.close();
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

    /**
     * 执行程序－关键
     *
     * @param sheet
     * @param book
     * @param i
     * @param sheetLocation
     * @param column
     * @throws RowsExceededException
     * @throws WriteException
     */

    private static void zhixingzhong(Sheet sheet, WritableWorkbook book, int i, int sheetLocation, final int column,
                                     boolean ispaiOrGui, String guidang) throws WriteException {
        // TODO Auto-generated method stub
        WritableSheet sheet2 = book.getSheet(sheetLocation);

        // 工单编号
        Cell cell = sheet.getCell(0, i);
        String result = cell.getContents();
        sheet2.addCell(new Label(0, column, result, wcf));
        sheet2.addCell(new Label(17, column, result, wcf));
        // 派单时间
        Cell c = sheet.getCell(14, i);
        result = c.getContents();
        if (!result.isEmpty() && result.length() > 0) {
            dc = (DateCell) c;
            DateTime dataTime = new DateTime(dc);
//            sheet2.addCell(dataTime.copyTo(1, column));
//            long l = dc.getDate().getTime() - 28800;
            sheet2.addCell(new DateTime(1, column, dc.getDate(), dateFormat,DateTime.GMT));
        } else {
            sheet2.addCell(new Label(1, column, "", wcf));
        }
//        sheet2.addCell(new Label(1, column, timeFormat(result)));

        // 期望时间
        c = sheet.getCell(13, i);
        result = c.getContents();
        if (!result.isEmpty() && result.length() > 0) {
            dc = (DateCell) c;
//            sheet2.addCell(new DateTime(dc).copyTo(2, column));
//            long l = dc.getDate().getTime() - 28800;
            sheet2.addCell(new DateTime(2, column, dc.getDate(), dateFormat,DateTime.GMT));
        } else {
            sheet2.addCell(new Label(1, column, "", wcf));
        }


        // 账号
        cell = sheet.getCell(22, i);
        result = cell.getContents();
        if (result.length() > 0) {
            sheet2.addCell(new Number(3, column, Double.parseDouble(result), wcf));
        }
//        sheet2.addCell(new Label(3, column, result));
        // 客户姓名
        cell = sheet.getCell(8, i);
        result = cell.getContents();
        sheet2.addCell(new Label(4, column, result, wcf));
        // 小区
        cell = sheet.getCell(11, i);
        result = cell.getContents();
        // 小区裁剪
        if (result.startsWith("枣庄滕州市")) {
            result = result.substring(5);
        }
        if (result.contains("0号楼0单元")) {
            String[] s = result.split("0号楼0单元");
            result = s[0] + s[1];
        }
        if (result.contains("0楼0单元")) {
            String[] s = result.split("0楼0单元");
            result = s[0] + s[1];
        }
        if (result.contains("0单元")) {
            String[] s = result.split("0单元");
            result = s[0] + s[1];
        }
        if (result.startsWith("铁通-")) {
            result = result.substring(3);
        }
        if (result.contains("\\")) {
            result = result.replaceAll("\\\\", "");
        }
        if (result.contains("(")) {
            result = result.replaceAll("\\(", "（");
        }
        if (result.contains(")")) {
            result = result.replaceAll("\\)", "）");
        }
        // Matcher matcher = Pattern.compile("[0-9]").matcher(result);
        // if (matcher.find()) {
        // // System.out.println(matcher.start());
        // result = result.substring(5, matcher.start());
        // } else {
        // // System.out.println("Not found!");
        // result = result.substring(5);
        // }
        // 小区名称去重
        // data.clear();
        // for (int j = 0; j < result.length(); j++) {
        // String s = result.substring(j, j + 1);
        // if(s.matches("[0-9]+")){
        // data.add(s);
        // }
        // else if (!data.contains(s)) {
        // data.add(s);
        // }
        //
        // }
        // result = "";
        // for (String s : data) {
        // result += s;
        // }
        result = checkRepestString(result);
        sheet2.addCell(new Label(5, column, result, wcf));

        // 工单分类
        cell = sheet.getCell(3, i);
        result = cell.getContents();
        if (result.equals("家客业务开通2.0")) {
            result = "";
        }
        if (result.equals("家客业务拆机2.0")) {
            result = "拆机";
            isEmpty = true && ispaiOrGui;
        }
        if (result.equals("OTTTV 开通流程")) {
            result = "OTTTV";
            isEmpty = true && ispaiOrGui;
        }
        if (result.equals("OTTTV 拆机流程")) {
            result = "OTTTV 拆机";
            isEmpty = true && ispaiOrGui;
        }
        if (result.equals("家客业务移机2.0")) {
            result = "移机";
        }
        if (result.equals("业务融合开机流程")) {
            result = "IMS";
            isEmpty = true && ispaiOrGui;
        }
        if (result.equals("校园宽带开通流程")) {
            result = "校园宽带";
        }
        if (result.equals("校园宽带拆机流程")) {
            result = "校园宽带拆机";
        }
        sheet2.addCell(new Label(8, column, result, wcf));
        //反馈
        if (ispaiOrGui) {
            if (result.equals("OTTTV")) {
                sheet2.addCell(new Label(13, column, "开通OTTTV后，需归档", wcf));
            } else if (result.equals("IMS")) {
                sheet2.addCell(new Label(13, column, "开通IMS后，需归档", wcf));
            } else if (result.equals("OTTTV 拆机") || result.equals("拆机")) {
                sheet2.addCell(new Label(13, column, "释放端口，归档", wcf));
            } else {
                sheet2.addCell(new Label(13, column, "", wcf));
            }
        } else {
            sheet2.addCell(new Label(13, column, "", wcf));
        }

//统计日期
        if (guidang.equals("派单")) {
            sheet2.addCell(new Label(18, column, tomorrow, wcf));
//            sheet2.addCell(new DateTime(18, column, tomorrow, dateWcf));
//            sheet2.addCell(dateTime.copyTo(18, column));
            //归档
            sheet2.addCell(new Label(14, column, "", wcf));
        } else {
            sheet2.addCell(new Label(18, column, today, wcf));
//            sheet2.addCell(new DateTime(18, column, today, dateWcf));
//            dateTime.setDate(today);
//            sheet2.addCell(dateTime.copyTo(18, column));
            //归档
            if (result.equals("OTTTV 拆机")) {
                sheet2.addCell(new Label(14, column, "拆机", wcf));
            } else {
                sheet2.addCell(new Label(14, column, guidang, wcf));
            }
        }


        // 乡镇
        cell = sheet.getCell(6, i);
        result = cell.getContents();
        if (result.contains("-")) {
            result = result.substring(result.indexOf("-") + 1);
        }
        sheet2.addCell(new Label(6, column, result, wcf));

        // 溶解
        String rongValue = rongMap.get(result);
        if (rongValue.equals("填空")) {
            rongValue = "";
        }
//		判断溶解是否需要换人
        cell = sheet.getCell(28, i);
        if (cell.getContents().length() > 0 && !cell.getContents().isEmpty() && guidang.equals("归档")) {
            rongValue = cell.getContents();
        }
        sheet2.addCell(new Label(12, column, rongValue, wcf));

        // 放线
        String fangValue = fangMap.get(result);
        if (fangValue.equals("填空") || isEmpty) {
            fangValue = "";
            isEmpty = false;
        }
        sheet2.addCell(new Label(16, column, fangValue, wcf));
        //

        // 客户联系人电话
        cell = sheet.getCell(10, i);
        result = cell.getContents();
        if (result.length() > 0) {
            sheet2.addCell(new Number(7, column, Double.parseDouble(result), wcf));
        }
//        sheet2.addCell(new Label(7, column, result));

        // 授权吗
        cell = sheet.getCell(24, i);
        result = cell.getContents();
        sheet2.addCell(new Label(9, column, result, wcf));

        // 第K列 可以不填
        // 带宽
        cell = sheet.getCell(21, i);
        result = cell.getContents();
        if (result.equals("6") || result.equals("10") || result.equals("20")) {
            result = "";
        }
        sheet2.addCell(new Label(11, column, result, wcf));

        // cell = sheet.getCell(21, i);
        // result = cell.getContents();
//记录日期
//        DateTime dateTime = new DateTime(dc);

//        dateTime.setDate(tomorrow);


        //和TV单(K)

        sheet2.addCell(new Label(10, column, "", wcf));
        //是否防线
        sheet2.addCell(new Label(15, column, "", wcf));


//
//        DateFormat dateFormat=new DateFormat("MMM-yy");
//        CellFormat dd=dc.getCellFormat();
//
//        WritableCellFormat writableCellFormat= new WritableCellFormat(dateFormat);

//        jxl.write.DateTime cc=new jxl.write.DateTime(19,column,new Date());
//        sheet2.addCell(cc);

    }


    /**
     * 初始化城镇负责人
     *
     * @throws IOException
     * @throws BiffException
     */
    private static void initCountryMaps() throws BiffException, IOException {
        rongMap = new HashMap<String, String>();
        fangMap = new HashMap<String, String>();
        country1 = Workbook.getWorkbook(new File(FileUtils.getExternalStoragePath(), "country.xls"));
        Sheet countrySheet = country1.getSheet(0);
        int rownum3 = countrySheet.getRows();// 得到3行数
        for (int i = 2; i < rownum3; i++)// 循环进行读写
        {
            String key = countrySheet.getCell(0, i).getContents();
            String value = countrySheet.getCell(1, i).getContents();
            rongMap.put(key, value);
            key = countrySheet.getCell(3, i).getContents();
            value = countrySheet.getCell(4, i).getContents();
            fangMap.put(key, value);
        }

    }


    private static String timeFormat(String time) {
        if (time.isEmpty() && time.length() == 0) {
            return "";
        }
        String[] s = time.split(" ");
        String[] year = s[0].split("/");
        return "20" + year[2] + "/" + year[0] + "/" + year[1] + " " + s[1];
    }

    public interface ControllerListener {
        void finish();

        void error();
    }

    /**
     * @param result
     * @return
     */
    private static String checkRepestString(String result) {
        String s = new String(result);
        int l = 2;
        String congfu = "";
        for (int i = 0; i < s.length() - l; i++) {
            String ss = s.substring(i, i + l + 1);
            String sss = s.replaceFirst(ss, "");
            if (sss.contains(ss)) {
                congfu = ss;
                l++;
                i--;
            }
        }
//        System.out.println(congfu);
        s = s.replaceFirst(congfu, "");
//        System.out.println(s);
        return s;
    }


}
