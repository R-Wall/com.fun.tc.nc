package com.fun.tc.nc.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.fun.tc.nc.until.MyWriteExcelUntil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class CNCProgramCatalogueHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {	
			InterfaceAIFComponent aifcomp = AIFUtility.getCurrentApplication().getTargetComponent();			
			TCComponentBOPLine comp  = (TCComponentBOPLine) aifcomp;
			TCComponentItemRevision rev = comp.getItemRevision();
			String type = rev.getType();
			if (!type.equals("MEProcessRevision") && !type.equals("AE8RootProcess Revision")) {
				MessageBox.post("请选择总工艺或数控程序集进行操作", "提示", MessageBox.INFORMATION);
				return null;
			}
			String path = MyWriteExcelUntil.writeCNCProgramCatalogueExcel(getValues(comp));
			MyWriteExcelUntil.addToNewStuff(path);
			MessageBox.post("报表输出成功！","提示",MessageBox.INFORMATION);
		} catch (Exception e) {
			MessageBox.post(e.toString(),"错误",MessageBox.ERROR);
			e.printStackTrace();
		}
		return null;
	}
	
	public List<String[]> getValues(TCComponentBOPLine line) throws TCException{
		TCComponentItemRevision rev = line.getItemRevision();
		String type = rev.getType();
		List<TCComponentItemRevision> revs = new ArrayList<>();
		if (type.equals("AE8RootProcess Revision")) {
			AIFComponentContext[] contexts = line.getChildren();
			for (AIFComponentContext context : contexts) {
				TCComponentBOPLine bopLine = (TCComponentBOPLine) context.getComponent();
				rev = bopLine.getItemRevision();
				if (bopLine.getItemRevision().getType().equals("MEProcessRevision")) {
					revs.add(rev);
				}
			}
		} else if (type.equals("MEProcessRevision")) {
			
			revs.add(rev);
		}
		
		return getValues(revs);
	}

	public List<String[]> getValues(List<TCComponentItemRevision> revs) throws TCException{
		List<String[]> values = new ArrayList<String[]>();
		int num = 0;
		for (int i = 0; i < revs.size(); i++) {
			TCComponentItemRevision rev = revs.get(i);
			TCComponent form = rev.getRelatedComponent("IMAN_master_form_rev");
			if (form == null) {
				continue;
			}
			String[] value = new String[10];
			value[0] = ++num + "";
			value[1] = form.getProperty("ae8_att1");
			value[2] = form.getProperty("ae8_att4");
			value[3] = "";
			value[4] = "";
			value[5] = "";
			value[6] = "";
			value[7] = "";
			value[8] = "";
			value[9] = "";
			values.add(value);
		}
		return values;
	}

}
