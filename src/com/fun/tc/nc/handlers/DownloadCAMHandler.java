package com.fun.tc.nc.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.fun.tc.nc.until.MyDatasetUtil;
import com.fun.tc.nc.until.RacDatasetUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

public class DownloadCAMHandler extends AbstractHandler{

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
				String dir = showSelectFileDialog();
				List<TCComponentDataset> datasets = MyDatasetUtil.getDatesetByMENCMachining(rev);
				for (int i = 0; i < datasets.size(); i++) {
					RacDatasetUtil.getTCFile(datasets.get(i), dir);
				}
					
				MessageBox.post("下载完成!请在选择的路径下查看。","提示",MessageBox.INFORMATION);
			}else {
				 MessageBox.post("选择的不是工序版本！请选择工序版本进行下载操作！","错误",MessageBox.ERROR);
			}
		} catch (Exception e) {
			MessageBox.post("下载出错"+e.toString(),"错误",MessageBox.ERROR);
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String showSelectFileDialog(){
		
		//新建文件夹（目录）对话框
		DirectoryDialog folderdlg=new DirectoryDialog(parent);
		//设置文件对话框的标题
		folderdlg.setText("文件选择");
		//设置初始路径
		folderdlg.setFilterPath("SystemDrive");
		//设置对话框提示文本信息
		folderdlg.setMessage("请选择相应的文件夹");
		//打开文件对话框，返回选中文件夹目录
		String selecteddir=folderdlg.open();
		if(selecteddir==null){
			return null;
		}
		else{
			System.out.println("您选中的文件夹目录为："+selecteddir);
		}
		 return  selecteddir;
	}

}
