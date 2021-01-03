package com.fun.tc.nc.handlers;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.fun.tc.nc.dialogs.CreateCAMDialog;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class CreateCAMHandler extends AbstractHandler {

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
			TCComponent[] coms = processRev.getRelatedComponents("AE8_ASSONCMEP");
			for (TCComponent obj : coms) {
				if ("MEProcessRevision".equals(obj.getType())) {
					MessageBox.post("<" + processRev + ">已关联数控程序集<" + obj+ ">", "提示", MessageBox.INFORMATION);
					return null;
				}
			}
			if (!"AE8Process Revision".equals(processRev.getType())) {
				MessageBox.post("请选择机加工艺版本进行操作", "提示", MessageBox.INFORMATION);
				return null;
			}
			TCComponentBOPLine top = (TCComponentBOPLine) line.window().getTopBOMLine();
			TCComponentItemRevision topRev = top.getItemRevision();
			if (!"AE8RootProcess Revision".equals(topRev.getType())) {
				MessageBox.post("当前BOP视图顶层不是总工艺", "提示", MessageBox.INFORMATION);
				return null;
			}
			
			SwingUtilities.invokeLater(new CreateCAMDialog(line, top));
		} catch (TCException e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		return null;
	}
	
}
