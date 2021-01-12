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
			if (!type.equals("MEProcessRevision")) {
				MessageBox.post("请选择数控程序集进行操作", "提示", MessageBox.INFORMATION);
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
		AIFComponentContext[] contexts = line.getChildren();
		List<String[]> values = new ArrayList<String[]>();
		int num = 0;
		for (AIFComponentContext context : contexts) {
			TCComponentBOPLine bopLine = (TCComponentBOPLine) context.getComponent();
			rev = bopLine.getItemRevision();
			TCComponent form = rev.getRelatedComponent("IMAN_master_form_rev");
			if (form == null) {
				continue;
			}
			String state = form.getProperty("ae8cx_state");
			if (!state.equalsIgnoreCase("D")) {
				continue;
			}
			String[] value = new String[10];
			value[0] = ++num + "";
			value[1] = form.getProperty("ae8cx_no");
			value[2] = state;
			value[3] = rev.getProperty("item_revision");
			value[4] = form.getProperty("ae8qrb_no");
			value[5] = form.getProperty("date_released");
			value[6] = form.getProperty("ae8part_no");
			value[7] = form.getProperty("ae8gf_rev");
			value[8] = form.getProperty("ae8gx_no");
			value[9] = form.getProperty("ae8memo");
			values.add(value);
		}
		
		return values;
	}

}
