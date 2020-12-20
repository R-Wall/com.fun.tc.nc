package com.fun.tc.nc.handlers;

import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.fun.tc.nc.until.MyDatasetUtil;
import com.fun.tc.nc.until.RacDatasetUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
//import com.teamcenter.rac.util.MessageBox;

public class UploadingCAMHandler extends AbstractHandler{

	private Shell parent;
	
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		
		
		InterfaceAIFComponent aifcomp = AIFUtility.getCurrentApplication().getTargetComponent();
		final TCComponent comp  = (TCComponent) aifcomp;
		String type = aifcomp.getType();
		Object[] options = {"覆盖","不覆盖"};
		if(type.equals("AE8Operation Revision")) {	
			Display display = Display.getDefault();	
			parent = new Shell(display,SWT.SHELL_TRIM);
			FileDialog fd = new FileDialog(parent, SWT.OPEN | SWT.MULTI);
//			fd.setFilterPath(System.getProperty("JAVA.HOME"));
			fd.setFilterExtensions(new String[]{"*.MPF","*.*"});
			fd.setFilterNames(new String[]{"MPF Files(*.MPF)","所有文件"});
			fd.open();
			String[] fileNames = fd.getFileNames();
			String filterPath = fd.getFilterPath();
			String dir = null;
			File files = null;
			try {
				List<TCComponentDataset> datasets = RacDatasetUtil.getDatasets(comp);
				for (int i = 0; i < fileNames.length; i++) {
					dir = filterPath+"\\"+fileNames[i];
					files = new File(dir);
					for (int j = 0; j < datasets.size(); j++) {
						if(datasets.get(j).toString().equals(fileNames[i])) {
							Shell temp = parent;
							MessageBox box = new MessageBox(temp,SWT.ICON_WARNING|SWT.YES|SWT.NO);
							box.setText("覆盖确认");
							box.setMessage("是否覆盖已存在的数控程序代码"+fileNames[i]);
							int choice = box.open();
							if(choice==SWT.YES) {
								comp.remove("TC_Attaches", datasets.get(j));
							}else if(choice==SWT.NO){

							}
						}
					}
					MyDatasetUtil.createDateset(comp, fileNames[i], files, "TC_Attaches");
				}
				com.teamcenter.rac.util.MessageBox.post("上传成功。","提示",com.teamcenter.rac.util.MessageBox.INFORMATION);
			} catch (Exception e) {
				com.teamcenter.rac.util.MessageBox.post("上传出错:"+e.toString(),"错误",com.teamcenter.rac.util.MessageBox.ERROR);
				e.printStackTrace();
			}		
		}else {
			com.teamcenter.rac.util.MessageBox.post("选择的不是工序版本！请选择工序版本进行上传操作！","错误",com.teamcenter.rac.util.MessageBox.ERROR);
		}
		
		return null;
	}

}
