package com.fun.tc.nc.handlers;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.fun.tc.nc.until.MyDatasetUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.util.MessageBox;

public class UploadingReportHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent arg0) {

		InterfaceAIFComponent aifcomp = AIFUtility.getCurrentApplication().getTargetComponent();
		final TCComponent comp  = (TCComponent) aifcomp;
		String type = aifcomp.getType();
		 if(type.equals("AE8Operation Revision")) {			 
				Display display = Display.getDefault();
				Shell shell = new Shell(display);
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterPath(System.getProperty("JAVA.HOME"));
				fd.setFilterExtensions(new String[]{"*.pptx"});
				fd.setFilterNames(new String[]{"PPT Files(*.pptx)"});
				final String file = fd.open();
				final String name = fd.getFileName();
				final File files = new File(file);
				if(name.contains("仿真报告.")) {
					if(file!=null){
						new Thread(new Runnable() {		
							@Override
							public void run() {
								try {
									MyDatasetUtil.createDateset(comp, name, files, "TC_Attaches");
									MessageBox.post("仿真报告上传成功","提示",MessageBox.INFORMATION);
								} catch (Exception e) {
									MessageBox.post(e);
									e.printStackTrace();
								}				
							}
						}).start();
					}else{
						MessageBox.post("上传文件路径为空","提示",MessageBox.INFORMATION);
					}
				}else {
					 MessageBox.post("选择的数据集名称不是名称+仿真报告！","错误",MessageBox.ERROR);
					 return null;
				}
		 }else {
			 MessageBox.post("选择的不是工序版本！请选择工序版本进行上传操作！","错误",MessageBox.ERROR);
		 }
		return null;	
	}

}
