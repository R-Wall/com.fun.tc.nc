package com.fun.tc.nc.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentFormType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.stylesheet.InterfacePropertyComponent;
import com.teamcenter.rac.stylesheet.PropertyNameLabel;
import com.teamcenter.rac.stylesheet.PropertyTextField;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;

public class CreateCAEDialog extends AbstractAIFDialog implements ActionListener {

	private static final long serialVersionUID = -3032849889897906912L;
	
	TCComponentItemRevision processRev;
	
	String type = "MENCMachining";
	
	String formType = "MENCMachining Revision Master";
	
	TCComponentFormType masterType;

	private JButton okButton;

	private JButton closeButton;

	private JTextField idText;

	private JTextField revText;

	private JTextField nameText;

	private JButton assignButton;
	
	TCComponentItemType itemType;
	
	TCComponentBOMLine line;

	TCSession session;
	
	String relationName = "AE8RelNC";
	
	TCComponentItemRevision relationRev;
	
	String[] relationFormPros;
	
	String[] formPros;
	
	String MENCMachiningRevisionMasterCreateProps = "MENCMachiningRevisionMasterCreateProps";
	
	String AE8OperationRevisionMasterDisplayProps = "AE8OperationRevisionMasterDisplayProps";
	
	String MENCMachiningTemplate = "MENCMachiningTemplate";
	
	List<InterfacePropertyComponent> coms = new ArrayList<>();

	
	public CreateCAEDialog(TCComponentBOMLine line, TCComponentItemRevision relationRev) throws Exception {
		super(AIFUtility.getActiveDesktop());
		setTitle("新建数控工序");
		this.line = line;
		this.processRev = line.getItemRevision();
		this.relationRev = relationRev;
		session = line.getSession();
		relationFormPros = session.getPreferenceService().getStringValues(AE8OperationRevisionMasterDisplayProps);
		formPros = session.getPreferenceService().getStringValues(MENCMachiningRevisionMasterCreateProps);
		itemType = (TCComponentItemType) session.getTypeComponent(type);
		masterType = (TCComponentFormType) session.getTypeComponent(formType);
		String name = processRev.getProperty("object_name");
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new PropertyLayout());
		titlePanel.setBorder(BorderFactory.createTitledBorder("MENCMachining"));
		titlePanel.add("1.1.center.center", new JLabel("  ID/版本.名称："));
		JPanel createPanel = new JPanel();
		idText = new JTextField(10);
		revText = new JTextField(2);
		nameText = new JTextField(18);
		nameText.setText(name + "数控工序");
		idText.setEditable(false);
		revText.setEditable(false);
		nameText.setEnabled(false);
		assignButton = new JButton("指派");
		assignButton.addActionListener(this);
		createPanel.add(idText);
		createPanel.add(new JLabel("/"));
		createPanel.add(revText);
		createPanel.add(new JLabel("."));
		createPanel.add(nameText);
		createPanel.add(assignButton);
		titlePanel.add("1.2.center.center", createPanel);
			
		JSplitPane splitPane =  new JSplitPane();
		splitPane.setBorder(BorderFactory.createTitledBorder("相关信息"));
		JPanel machiningPanel = createLeftPanel();
		JPanel relationPanel = createRightPanel(relationRev);
		splitPane.setLeftComponent(machiningPanel);
		splitPane.setRightComponent(relationPanel);
//		splitPane.setDividerLocation(0.5D);
		
		JPanel buttonPanel = new JPanel();
		okButton = new JButton("确认");
		closeButton = new JButton("关闭");
		okButton.addActionListener(this);
		closeButton.addActionListener(this);
		buttonPanel.add(okButton);
		buttonPanel.add(closeButton);
		setLayout(new BorderLayout());
		add(titlePanel, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.SOUTH);
		add(splitPane, BorderLayout.CENTER);
		centerToScreen();
		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		Object obj = event.getSource();
		try {
			if (obj.equals(okButton)) {
				String id = idText.getText();
				String revID = revText.getText();
				if (id.isEmpty() || revID.isEmpty()) {
					MessageBox.post(this, "请指派ID", "提示", MessageBox.INFORMATION);
					return;
				}
				TCComponentForm form = createForm(id + "/" + revID);
				TCComponentItem machining = itemType.create(id, revID, type, nameText.getText(), "", null, null, form);
				TCComponentItemRevision rev = machining.getLatestItemRevision();
				line.addBOMLine(line, rev, null);
				line.window().save();
				if (relationRev != null) {
					relationRev.add(relationName, rev);
				}
				rev.setRelated("IMAN_METarget", processRev.getRelatedComponents("IMAN_METarget"));
				TCComponentDataset template = getTemplate();
				if (template != null) {
					TCComponentDataset dataset = template.saveAs(id + "-" + template.getProperty("object_name"));
					if (dataset != null) {
						rev.add(rev.getDefaultPasteRelation(), dataset);
					}
				}
				MessageBox.post(this, machining + "创建成功", "提示", MessageBox.INFORMATION);
				dispose();
			} else if (obj.equals(assignButton)) {
				assign();
			} else if (obj.equals(closeButton)) {
				dispose();
			}
		} catch (Exception e) {
			MessageBox.post(this, e);
			e.printStackTrace();
		}
		
	}
	
	public TCComponentDataset getTemplate() throws TCException {
		String id = session.getPreferenceService().getStringValue(MENCMachiningTemplate);
		if (id == null || id.isEmpty()) {
			return null;
		}
		TCComponentItem[] items = itemType.findItems(id);
		if (items == null || items.length == 0) {
			return null;
		}
		TCComponentItem item = items[0];
		TCComponentItemRevision rev = item.getLatestItemRevision();
		TCComponent[] datasets = rev.getRelatedComponents("IMAN_specification");
		TCComponentDataset dataset = null;
		for (TCComponent com : datasets) {
			if (com instanceof TCComponentDataset) {
				dataset = (TCComponentDataset) com;
				break;
			}
		}
		
		return dataset;
		
	}
	
	public JPanel createLeftPanel() throws Exception {
		JPanel machiningPanel = new JPanel();
		machiningPanel.setBorder(BorderFactory.createTitledBorder("数控工序信息"));
		machiningPanel.setLayout(new PropertyLayout());
		machiningPanel.setBackground(Color.WHITE);
		
		int row = 1;
		for (String formPro : formPros) {
			PropertyNameLabel lable = new PropertyNameLabel();
			TCPropertyDescriptor desc = masterType.getPropDesc(formPro);
			lable.load(desc);
			
			PropertyTextField textField = new PropertyTextField();
			textField.load(desc);
			textField.setProperty(formPro);
			machiningPanel.add(row + ".1.center.center", lable);
			machiningPanel.add(row + ".2.center.center", textField);
			machiningPanel.add(row + ".3.center.center", new JLabel("   "));
			coms.add(textField);
			row++;
		}
		return machiningPanel;
	}
	
	
	public JPanel createRightPanel(TCComponent com) throws Exception {
		JPanel relationPanel = new JPanel();
		relationPanel.setBorder(BorderFactory.createTitledBorder("关联机加工序信息"));
		relationPanel.setLayout(new PropertyLayout());
		relationPanel.setBackground(Color.WHITE);
		TCComponent form = com.getRelatedComponent("IMAN_master_form_rev");
		if (form != null) {
			int row = 1;
			for (String formPro : relationFormPros) {
				PropertyNameLabel lable = new PropertyNameLabel();
				TCProperty tcp = form.getTCProperty(formPro);
				lable.load(tcp);
				
				PropertyTextField textField = new PropertyTextField();
				textField.load(tcp);
				textField.setEditable(false);
				relationPanel.add(row + ".1.center.center", lable);
				relationPanel.add(row + ".2.center.center", textField);
				row++;
			}
		}
		return relationPanel;
	}
	
	public TCComponentForm createForm(String name) throws TCException {
		TCComponentForm form = masterType.create(name, "", formType);
		Map<String, String> properties = new HashMap<>();
		for (InterfacePropertyComponent com : coms) {
			properties.put(com.getProperty(), com.getEditableValue().toString());
		}
		form.setProperties(properties);
		return form;
	}
	
	
	public void assign() throws TCException {
		String id = idText.getText();
		String revID = revText.getText();
		if (id.isEmpty() && revID.isEmpty()) {
			id = itemType.getNewID();
			revID = itemType.getNewRev(null);
			idText.setText(id);
			revText.setText(revID);
			idText.setEditable(false);
			revText.setEditable(false);
		}
	}
}
