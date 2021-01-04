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

public class ToolingCatalogueHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {	
			InterfaceAIFComponent aifcomp = AIFUtility.getCurrentApplication().getTargetComponent();			
			TCComponentBOPLine comp  = (TCComponentBOPLine) aifcomp;
			TCComponentItemRevision rev = comp.getItemRevision();
			String type = rev.getType();
			if (!type.equals("MEProcessRevision") && !type.equals("MENCMachining Revision")) {
				MessageBox.post("请选择数控程序集或数控工序进行操作", "提示", MessageBox.INFORMATION);
				return null;
			}
			
			String path = MyWriteExcelUntil.writeToolingCatalogueExcel(getValues(comp));
			MyWriteExcelUntil.addToNewStuff(path);
			MessageBox.post("报表输出成功！","提示",MessageBox.INFORMATION);
		} catch (Exception e) {
			MessageBox.post(e);
			e.printStackTrace();
		}
		return null;
	}
	
	public List<String[]> getValues(TCComponentBOPLine line) throws TCException{
		TCComponentItemRevision rev = line.getItemRevision();
		String type = rev.getType();
		List<TCComponentBOPLine> bops = new ArrayList<>();
		if (type.equals("MEProcessRevision")) {
			AIFComponentContext[] contexts = line.getChildren();
			for (AIFComponentContext context : contexts) {
				TCComponentBOPLine bopLine = (TCComponentBOPLine) context.getComponent();
				if (bopLine.getItemRevision().getType().equals("MENCMachining Revision")) {
					bops.add(bopLine);
				}
			}
		} else if (type.equals("MENCMachining Revision")) {
			bops.add(line);
		}
		return getValues(bops);
	}

	public List<String[]> getValues(List<TCComponentBOPLine> bops) throws TCException{
		List<String[]> values = new ArrayList<String[]>();
		int num = 0;
		for (int i = 0; i < bops.size(); i++) {
			TCComponentBOPLine bop = bops.get(i);
			TCComponentItemRevision topRev = bop.getItemRevision();
			AIFComponentContext[] contexts = bop.getChildren();
			for (AIFComponentContext context : contexts) {
				TCComponentBOPLine bopLine = (TCComponentBOPLine) context.getComponent();
				TCComponentItemRevision rev = bopLine.getItemRevision();
				if (rev.getType().equals("AE8Tool Revision")) {
					TCComponent form = rev.getRelatedComponent("IMAN_master_form_rev");
					if (form == null) {
						continue;
					}
					String[] value = new String[11];
					value[0] = ++num + "";
					value[1] = topRev.toDisplayString();
					value[2] = form.getProperty("ae8gx_no");
					value[3] = "";
					value[4] = "";
					value[5] = "";
					value[6] = "";
					value[7] = "";
					value[8] = "";
					value[9] = "";
					value[10] = "";
					values.add(value);
				}
			}
		}
		return values;
	}
	
}
