package com.fun.tc.nc.handlers;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.fun.tc.nc.dialogs.ReviseCAMDialog;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class ReviseCAEHandler extends AbstractHandler {

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
			
			if (!"MENCMachining Revision".equals(processRev.getType())) {
				MessageBox.post("请选择数控工序版本进行操作", "提示", MessageBox.INFORMATION);
				return null;
			}
			TCComponentBOPLine top = (TCComponentBOPLine) line.getCachedParent();
			if (top == null) {
				MessageBox.post("当前视图找不到相应的数控工艺", "提示", MessageBox.INFORMATION);
				return null;
			}
			SwingUtilities.invokeLater(new ReviseCAMDialog(line, top));
		} catch (TCException e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		return null;
	}
}
