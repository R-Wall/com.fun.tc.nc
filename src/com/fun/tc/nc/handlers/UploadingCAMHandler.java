package com.fun.tc.nc.handlers;

import java.io.File;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.fun.tc.nc.until.MyDatasetUtil;
import com.fun.tc.nc.until.RacDatasetUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;

public class UploadingCAMHandler extends AbstractHandler{

	private Shell parent;
	
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		
		try {
			InterfaceAIFComponent aifcomp = AIFUtility.getCurrentApplication().getTargetComponent();
			
			TCComponentBOPLine comp  = (TCComponentBOPLine) aifcomp;
			TCComponentItemRevision rev = comp.getItemRevision();
			String type = rev.getType();
			if(type.equals("MENCMachining Revision")) {	
				Display display = Display.getDefault();	
				parent = new Shell(display,SWT.SHELL_TRIM);
				FileDialog fd = new FileDialog(parent, SWT.OPEN | SWT.MULTI);
				fd.setFilterExtensions(new String[]{"*.MPF","*.*"});
				fd.setFilterNames(new String[]{"MPF Files(*.MPF)","所有文件"});
				fd.open();
				String[] fileNames = fd.getFileNames();
				String filterPath = fd.getFilterPath();
				String dir = null;
				File files = null;
				List<TCComponentDataset> datasets = RacDatasetUtil.getDatasets(rev);
				String refName = rev.getDefaultPasteRelation();
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
								rev.remove(refName, datasets.get(j));
							}else if(choice==SWT.NO){
								
							}
						}
					}
					MyDatasetUtil.createDateset(rev, fileNames[i], files,refName);
				}
				com.teamcenter.rac.util.MessageBox.post("上传成功。","提示",com.teamcenter.rac.util.MessageBox.INFORMATION);
				
				
			}else {
				com.teamcenter.rac.util.MessageBox.post("选择的不是工序版本！请选择工序版本进行上传操作！","错误",com.teamcenter.rac.util.MessageBox.ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
			com.teamcenter.rac.util.MessageBox.post("上传出错:"+e.toString(),"错误",com.teamcenter.rac.util.MessageBox.ERROR);
		}
		
		return null;
	}

}
