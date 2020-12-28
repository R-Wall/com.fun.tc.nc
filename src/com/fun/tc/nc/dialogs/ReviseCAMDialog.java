package com.fun.tc.nc.dialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;

public class ReviseCAMDialog extends AbstractAIFDialog implements ActionListener {

	private static final long serialVersionUID = -3032849889897906912L;
	
	TCComponentItemRevision processRev;

	private JButton okButton;

	private JButton closeButton;

	private JTextField idText;

	private JTextField revText;

	private JTextField nameText;

	private JButton assignButton;
	
	TCSession session;
	
	String relationName = "AE8RelNC";

	private TCComponentItemRevision relationRev;
	
	TCComponentBOPLine line;
	
	TCComponentBOPLine top;
	
	public ReviseCAMDialog(TCComponentBOPLine line, TCComponentBOPLine top) throws TCException {
		super(AIFUtility.getActiveDesktop());
		setTitle("TC-CAPP数控工序修订");
		this.processRev = line.getItemRevision();
		session = processRev.getSession();
		this.line = line;
		this.top = top;
		JPanel propertyPanel = new JPanel();
		propertyPanel.setLayout(new PropertyLayout());
		propertyPanel.add("1.1.center.center", new JLabel("数控工序ID/版本.名称："));
		JPanel createPanel = new JPanel();
		idText = new JTextField(14);
		idText.setText(processRev.getProperty("item_id"));
		idText.setEditable(false);
		revText = new JTextField(2);
		revText.setEditable(false);
		revText.setText(processRev.getItem().getNewRev());
		nameText = new JTextField(18);
		nameText.setText(processRev.getProperty("object_name"));
		nameText.setEditable(false);
		assignButton = new JButton("指派");
		assignButton.addActionListener(this);
		createPanel.add(idText);
		createPanel.add(new JLabel("/"));
		createPanel.add(revText);
		createPanel.add(new JLabel("."));
		createPanel.add(nameText);
//		createPanel.add(assignButton);
		propertyPanel.add("1.2.center.center", createPanel);
		
		propertyPanel.add("2.1.center.center", new JLabel("关联机加工序ID/版本.名称："));
		JPanel relationPanel = new JPanel();
		JTextField idText = new JTextField(14);
		idText.setEditable(false);
		JTextField revText = new JTextField(2);
		revText.setEditable(false);
		JTextField nameText = new JTextField(18);
		nameText.setEditable(false);
		
		relationRev = getRelation();
		if (relationRev != null) {
			idText.setText(relationRev.getProperty("item_id"));
			revText.setText(relationRev.getProperty("item_revision_id"));
			nameText.setText(relationRev.getProperty("object_name"));
		}
		relationPanel.add(idText);
		relationPanel.add(new JLabel("/"));
		relationPanel.add(revText);
		relationPanel.add(new JLabel("."));
		relationPanel.add(nameText);
		propertyPanel.add("2.2.center.center", relationPanel);
		
		JPanel buttonPanel = new JPanel();
		okButton = new JButton("确认");
		closeButton = new JButton("关闭");
		okButton.addActionListener(this);
		closeButton.addActionListener(this);
		buttonPanel.add(okButton);
		buttonPanel.add(closeButton);
		setLayout(new BorderLayout());
		add(propertyPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		centerToScreen();
		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		Object obj = event.getSource();
		try {
			if (obj.equals(okButton)) {
				String revID = revText.getText();
				TCComponentItemRevision newRev = processRev.saveAs(revID);
				if (relationRev != null) {
					relationRev.add(relationName, newRev);
					relationRev.remove(relationName, processRev);
				}
				MessageBox.post(this, newRev + "修订成功！", "提示", MessageBox.INFORMATION);
				dispose();
			} if (obj.equals(closeButton)) {
				dispose();
			}
		} catch (Exception e) {
			MessageBox.post(this, e);
			e.printStackTrace();
		}
		
	}
	
	public TCComponentItemRevision getRelation() throws TCException {
		TCComponentItemRevision rev = null;
		AIFComponentContext[] contexts = processRev.getPrimary();
		for (AIFComponentContext context : contexts) {
			TCComponent com = (TCComponent) context.getComponent();
			if (relationName.equals(context.getContext()) && "AE8Operation Revision".equals(com.getType())) {
				rev = (TCComponentItemRevision) com;
				break;
			}
		}
		return rev;
	}

}
