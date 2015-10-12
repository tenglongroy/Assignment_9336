package com.example.roy.ass_9336;

import android.os.Bundle;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class OperateExcel {
    //http://blog.csdn.net/ljz2009y/article/details/7592673

    private int page;
    WritableSheet sheet;
    OperateExcel(int sheetPage){
        //this.page = sheetPage;
        createExcel(sheetPage);
    }

    public void onCreate(Bundle savedInstanceState) {
        // createExcel();
        // readExcel();
        //writeExcel("mnt/sdcard/test.xls");
    }
    public void createExcel( int sheetPage) {
        try {
            // 创建或打开Excel文件
            WritableWorkbook book = Workbook.createWorkbook(
                    new File(Environment.getExternalStorageDirectory(), "ass_9336"));

            // 生成名为“第一页”的工作表,参数0表示这是第一页
            WritableSheet sheet = book.createSheet(""+(sheetPage+1), sheetPage);
            //WritableSheet sheet2 = book.createSheet("第三页", 2);

            // 在Label对象的构造函数中,元格位置是第一列第一行(0,0)以及单元格内容为test
            //Label label = new Label(0, 0, "test");
            // 将定义好的单元格添加到工作表中
            sheet.addCell(new Label(0, 0, "time"));
            sheet.addCell(new Label(1, 0, "x"));
            sheet.addCell(new Label(2, 0, "y"));
            sheet.addCell(new Label(3, 0, "z"));

            //生成一个保存数字的单元格.必须使用Number的完整包路径,否则有语法歧义
            //jxl.write.Number number = new jxl.write.Number(1, 0, 555.12541);

            // 写入数据并关闭文件
            book.write();
            book.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public static void writeExcel(String filePath) {
        try {
            // 创建工作薄
            WritableWorkbook wwb = Workbook.createWorkbook(new File(filePath));
            // 创建工作表
            WritableSheet ws = wwb.createSheet("Sheet1", 0);
            // 添加标签文本
            // Random rnd = new Random((new Date()).getTime());
            // int forNumber = rnd.nextInt(100);
            // Label label = new Label(0, 0, "test");
            // for (int i = 0; i < 3; i++) {
            // ws.addCell(label);
            // ws.addCell(new jxl.write.Number(rnd.nextInt(50), rnd
            // .nextInt(50), rnd.nextInt(1000)));
            // }
            // 添加图片(注意此处jxl暂时只支持png格式的图片)
            // 0,1分别代表x,y 2,5代表宽和高占的单元格数
            ws.addImage(new WritableImage(5, 5, 2, 5, new File(
                    "mnt/sdcard/nb.png")));
            wwb.write();
            wwb.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
    public void readExcel() {
        try {
            /**
             * 后续考虑问题,比如Excel里面的图片以及其他数据类型的读取
             **/
            InputStream is = new FileInputStream("mnt/sdcard/test.xls");

            Workbook book = Workbook.getWorkbook(new File("mnt/sdcard/test.xls"));
            book.getNumberOfSheets();
            // 获得第一个工作表对象
            Sheet sheet = book.getSheet(0);
            int Rows = sheet.getRows();
            int Cols = sheet.getColumns();
            System.out.println("当前工作表的名字:" + sheet.getName());
            System.out.println("总行数:" + Rows);
            System.out.println("总列数:" + Cols);
            for (int i = 0; i < Cols; ++i) {
                for (int j = 0; j < Rows; ++j) {
                    // getCell(Col,Row)获得单元格的值
                    System.out
                            .print((sheet.getCell(i, j)).getContents() + "\t");
                }
                System.out.print("\n");
            }
            // 得到第一列第一行的单元格
            Cell cell1 = sheet.getCell(0, 0);
            String result = cell1.getContents();
            System.out.println(result);
            book.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    /**
     * jxl暂时不提供修改已经存在的数据表,这里通过一个小办法来达到这个目的,不适合大型数据更新! 这里是通过覆盖原文件来更新的.     *
     * @param filePath
     */
    public void updateExcel(String filePath) {
        try {
            Workbook rwb = Workbook.getWorkbook(new File(filePath));
            WritableWorkbook wwb = Workbook.createWorkbook(new File(
                    "d:/new.xls"), rwb);// copy
            WritableSheet ws = wwb.getSheet(0);
            WritableCell wc = ws.getWritableCell(0, 0);
            // 判断单元格的类型,做出相应的转换
            Label label = (Label) wc;
            label.setString("The value has been modified");
            wwb.write();
            wwb.close();
            rwb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}