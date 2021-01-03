package com.fun.tc.nc.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.fun.tc.nc.until.MyWriteExcelUntil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

public class CNCProgramCatalogueHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {	
			InterfaceAIFComponent aifcomp = AIFUtility.getCurrentApplication().getTargetComponent();			
			TCComponentBOPLine comp  = (TCComponentBOPLine) aifcomp;
			TCComponentItemRevision rev = comp.getItemRevision();
			String type = rev.getType();
			if ("MEProcessRevision".equals(type)||"MENCMachining Revision".equals(type)) {
				String path = MyWriteExcelUntil.writeCNCProgramCatalogueExcel(rev);
				MyWriteExcelUntil.addToNewStuff(path);
				MessageBox.post("报表输出成功！","提示",MessageBox.INFORMATION);

			}else {
				MessageBox.post("请选择数控程序集或数控工序进行操作", "提示", MessageBox.INFORMATION);
				return null;
			}		
		} catch (Exception e) {
			MessageBox.post(e.toString(),"错误",MessageBox.ERROR);
			e.printStackTrace();
		}
		return null;
	}

}
