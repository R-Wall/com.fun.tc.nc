package com.fun.tc.nc.handlers;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.fun.tc.nc.dialogs.CreateCAEDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class CreateCAEHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		InterfaceAIFComponent aifCom = AIFUtility.getCurrentApplication().getTargetComponent();
		if (aifCom == null) {
			MessageBox.post("请选择BOPLine进行操作", "提示", MessageBox.INFORMATION);
			return null;
		}
		if (!(aifCom instanceof TCComponentBOPLine)) {
			MessageBox.post("请选择BOPLine进行操作", "提示", MessageBox.INFORMATION);
			return null;
		}
		try {
			TCComponentBOPLine line = (TCComponentBOPLine)aifCom;
			TCComponentItemRevision processRev = line.getItemRevision();
			if (!"MEProcessRevision".equals(processRev.getType())) {
				MessageBox.post("请选择数控程序集进行操作", "提示", MessageBox.INFORMATION);
				return null;
			}
			TCComponentItemRevision rev = line.getItemRevision();
			TCComponentItemRevision relationRev = getRelation(rev);
			if (relationRev == null) {
				MessageBox.post("机加工艺下无机加工序", "提示", MessageBox.INFORMATION);
				return null;
			}
			SwingUtilities.invokeLater(new CreateCAEDialog(line, relationRev));
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
		return null;
	}
	
	public TCComponentItemRevision getRelation(TCComponentItemRevision rev) throws TCException {
		TCComponentItemRevision relationRev = null;
		AIFComponentContext[] contexts = rev.getPrimary();
		TCComponentItemRevision process = null;
		for (AIFComponentContext context : contexts) {
			TCComponent com = (TCComponent) context.getComponent();
			if ("AE8_ASSONCMEP".equals(context.getContext()) && "AE8Process Revision".equals(com.getType())) {
				process = (TCComponentItemRevision) com;
				break;
			}
		}
		if (process != null) {
			TCComponent[] views = process.getRelatedComponents("view");
			for (TCComponent view : views) {
				String type = view.getType();
				if ("AE8Operation".equals(type)) {
					relationRev = ((TCComponentItem)view).getLatestItemRevision();
					break;
				}				
			}
		}
		return relationRev;
	}
}
