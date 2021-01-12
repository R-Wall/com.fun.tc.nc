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
			if (!type.equals("AE8Process Revision")) {
				MessageBox.post("请选机加工艺进行操作", "提示", MessageBox.INFORMATION);
				return null;
			}
			
			String path = MyWriteExcelUntil.writeToolingCatalogueExcel(getValues(comp));
			MyWriteExcelUntil.addToNewStuff(path);
			MessageBox.post(aifcomp  + "报表输出成功！","提示",MessageBox.INFORMATION);
		} catch (Exception e) {
			MessageBox.post(e);
			e.printStackTrace();
		}
		return null;
	}
	
	public List<String[]> getValues(TCComponentBOPLine line) throws TCException{
		AIFComponentContext[] contexts = line.getChildren();
		List<TCComponentItemRevision> revs = new ArrayList<>();
		for (AIFComponentContext context : contexts) {
			TCComponentBOPLine bopLine = (TCComponentBOPLine) context.getComponent();
			TCComponentItemRevision rev = bopLine.getItemRevision();
			if ("AE8Operation Revision".equals(rev.getType())) {
				AIFComponentContext[] ors = bopLine.getChildren();
				for (AIFComponentContext or : ors) {
					TCComponentBOPLine orb = (TCComponentBOPLine) or.getComponent();
					TCComponentItemRevision or_rev = orb.getItemRevision();
					if ("AE8Tool Revision".equals(or_rev.getType())) {
						if (!revs.contains(or_rev)) {
							revs.add(or_rev);
						}
					}
				}
				TCComponent[] mrs = rev.getRelatedComponents("AE8RelNC");
				for (TCComponent mr : mrs) {
					if ("MENCMachining Revision".equals(mr.getType())) {
						TCComponent[] mrvis = mr.getRelatedComponents("ps_children");
						for (TCComponent mrvi : mrvis) {
							if ("AE8Tool Revision".equals(mrvi.getType())) {
								if (!revs.contains(mrvi)) {
									revs.add((TCComponentItemRevision) mrvi);
								}
							}
						}
					}
				}
			}
		}
		return getValue(revs);
	}

	public List<String[]> getValue(List<TCComponentItemRevision> revs) throws TCException{
		List<String[]> values = new ArrayList<String[]>();
		int num = 0;
		for (TCComponentItemRevision rev : revs) {
			TCComponent form = rev.getRelatedComponent("IMAN_master_form_rev");
			if (form == null) {
				continue;
			}
			String[] value = new String[11];
			String gz_type = form.getProperty("ae8gz_type");
			String gz_id = rev.getProperty("item_id");
			value[0] = ++num + "";
			value[1] = form.getProperty("ae8part_no");
			value[2] = form.getProperty("ae8gx_no");
			if ("夹具".equals(gz_type)) {
				value[3] =gz_id;
			} else if ("刀具".equals(gz_type)) {
				value[4] = gz_id;
			} else if ("量具".equals(gz_type)) {
				value[5] = gz_id;
			}else if ("模具".equals(gz_type)) {
				value[6] = gz_id;
			} else {
				continue;
			}
			value[7] = "";
			value[8] = rev.getProperty("object_name");
			value[9] = "";
			value[10] = "";
			values.add(value);
		}
		return values;
	}
}
