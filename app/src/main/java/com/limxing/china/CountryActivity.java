package com.limxing.china;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.limxing.library.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 * Created by 李利锋 on 2016/4/17.
 */
public class CountryActivity extends AppCompatActivity {
    private HashMap<String, String> rongMap;
    private HashMap<String, String> fangMap;
    private RelativeLayout country_container;
    private RadioGroup country_rg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country);


    }

    /**
     * 初始化城镇负责人
     *
     * @throws IOException
     * @throws BiffException
     */
    private void initCountryMaps() throws BiffException, IOException {
        rongMap = new HashMap<String, String>();
        fangMap = new HashMap<String, String>();
        Workbook country1 = Workbook.getWorkbook(new File(FileUtils.getExternalStoragePath(), "country.xls"));
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
}
