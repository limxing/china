package com.limxing.china;

import com.limxing.library.utils.LogUtils;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import jxl.DateCell;
import jxl.write.DateTime;

/**
 * Created by limxing on 16/4/13.
 */
public class MyDateCell extends  DateTime {


    public MyDateCell(DateCell dc) {
        super(dc);
    }

    @Override
    public DateFormat getDateFormat() {
        return new SimpleDateFormat("M月d日");
    }
}
