package com.fun.tc.nc.until;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;



public class MyWriteExcelUntil {

	public static String writeToolingCatalogueExcel(TCComponentItemRevision rev) throws Exception {
		
		String path = "C:\\Temp\\工装明细表.xls";
		InputStream in = MyWriteExcelUntil.class.getResourceAsStream("/工装明细表.xlsx");
		Workbook wb = new XSSFWorkbook(in);		
		XSSFSheet sheet = (XSSFSheet) wb.getSheet("工装明细表");
		FileOutputStream fileOut;	    	    	    
	    for (int i = 3; i < 5; i++) {
    	XSSFRow row = sheet.createRow(i+1);		
		 for (int j = 0; j < 2; j++) {
			XSSFCell cell = row.createCell(j);
		    cell.setCellValue("wall"+j);
		}
	}
	    //将数据流程保存到excel
		fileOut = new FileOutputStream(path);
		wb.write(fileOut);
		in.close();
		fileOut.close();
		return path;
	}
	
	public static String writeCNCProgramCatalogueExcel(TCComponentItemRevision rev) throws Exception {
		
		String path = "C:\\Temp\\数控程序确认表.xls";
		InputStream in = MyWriteExcelUntil.class.getResourceAsStream("/数控程序确认表.xlsx");
		Workbook wb = new XSSFWorkbook(in);		
		XSSFSheet sheet = (XSSFSheet) wb.getSheet("数控程序确认表");
		FileOutputStream fileOut;	    	    	    
	    for (int i = 3; i < 5; i++) {
    	XSSFRow row = sheet.createRow(i+1);		
		 for (int j = 0; j < 2; j++) {
			XSSFCell cell = row.createCell(j);
		    cell.setCellValue("wall"+j);
		}
	}
	    //将数据流程保存到excel
		fileOut = new FileOutputStream(path);
		wb.write(fileOut);
		in.close();
		fileOut.close();	
		return path;
	}
	
	/**
	 * 将路径下文件上传至NewStuff
	 * @param path
	 * @throws Exception
	 */
	public static void addToNewStuff(String path) throws Exception {
		TCSession session = (TCSession) AIFUtility.getDefaultSession();
		TCComponentFolder folder = session.getUser().getNewStuffFolder();
		File files = new File(path);
		String name = files.getName();
		MyDatasetUtil.createDateset(folder, name, files, "contents");
	}
	
}
