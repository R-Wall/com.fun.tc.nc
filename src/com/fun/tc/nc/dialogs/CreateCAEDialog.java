package com.fun.tc.nc.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.SwingUtilities;

import com.fun.tc.nc.until.MyDatasetUtil;
import com.fun.tc.nc.until.PropertyLOV;
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

	private JButton assignB1;

	private JButton assignB2;

	private PropertyTextField assign_text1;

	private PropertyTextField assign_text2;

	private JButton selectButton;

	private TCComponent form;

	private PropertyTextField jc_name;

	private PropertyTextField sb_no;

	private PropertyTextField os;

	
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
		String name = relationRev.getProperty("object_name");
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
			
		form = relationRev.getRelatedComponent("IMAN_master_form_rev");
		JSplitPane splitPane =  new JSplitPane();
		splitPane.setBorder(BorderFactory.createTitledBorder("相关信息"));
		JPanel machiningPanel = createLeftPanel(masterType);
		JPanel relationPanel = createRightPanel(form);
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
				List<TCComponentDataset> templates = getTemplates();
				for (TCComponentDataset template : templates) {
					TCComponentDataset dataset = template.saveAs(id + "-" + template.getProperty("object_name"));
					rev.add(rev.getDefaultPasteRelation(), dataset);
					if (dataset.getType().startsWith("MSWord")) {
						MyDatasetUtil.sign(dataset, getValues());
					}
				}
				MessageBox.post(this, machining + "创建成功", "提示", MessageBox.INFORMATION);
				dispose();
			} else if (obj.equals(assignButton)) {
				assign();
			} else if (obj.equals(closeButton)) {
				dispose();
			} else if (obj.equals(assignB1)) {
				assign1();
			} else if (obj.equals(assignB2)) {
				assign2();
			} else if (obj.equals(selectButton)) {
				SwingUtilities.invokeLater(new SelectDeviceDialog(this));
			}
		} catch (Exception e) {
			MessageBox.post(this, e);
			e.printStackTrace();
		}
		
	}
	
	public List<TCComponentDataset> getTemplates() throws TCException {
		List<TCComponentDataset> datasets = new ArrayList<>();
		String id = session.getPreferenceService().getStringValue(MENCMachiningTemplate);
		if (id == null || id.isEmpty()) {
			return datasets;
		}
		TCComponentItem[] items = itemType.findItems(id);
		if (items == null || items.length == 0) {
			return datasets;
		}
		TCComponentItem item = items[0];
		TCComponentItemRevision rev = item.getLatestItemRevision();
		TCComponent[] coms = rev.getRelatedComponents("IMAN_specification");
		for (TCComponent com : coms) {
			if (com instanceof TCComponentDataset) {
				datasets.add((TCComponentDataset)com);
				break;
			}
		}
		
		return datasets;
	}
	
	public JPanel createLeftPanel(TCComponentFormType masterType) throws Exception {
		JPanel machiningPanel = new JPanel();
		machiningPanel.setBorder(BorderFactory.createTitledBorder("数控工序信息"));
		machiningPanel.setLayout(new PropertyLayout());
		machiningPanel.setBackground(Color.WHITE);
		int row = 1;
		
		String property_name = "ae8cx_no";
		TCPropertyDescriptor desc = masterType.getPropDesc(property_name);
		if (desc != null) {
			PropertyNameLabel lable = new PropertyNameLabel();
			lable.load(desc);
			assign_text1 = new PropertyTextField();
			assign_text1.setProperty(property_name);
			assign_text1.load(desc);
//			assign_text1.setModifiable(false);
			machiningPanel.add(row + ".1.center.center", lable);
			machiningPanel.add(row + ".2.center.center", assign_text1);
			coms.add(assign_text1);
			
			property_name = "ae8cx_no_unit";
			desc = masterType.getPropDesc(property_name);
			InterfacePropertyComponent com =  new PropertyLOV(getLOV(property_name));
			com.setProperty(property_name);
			com.load(desc);
			machiningPanel.add(row + ".3.center.center", (Component) com);
			coms.add(com);
			
			assignB1 = new JButton("指派");
			assignB1.addActionListener(this);
			machiningPanel.add(row + ".4.center.center", assignB1);
			row++;
		}
		
		property_name = "ae8qrb_no";
		desc = masterType.getPropDesc(property_name);
		if (desc != null) {
			PropertyNameLabel lable = new PropertyNameLabel();
			lable.load(desc);
			assign_text2 = new PropertyTextField();
			assign_text2.setProperty(property_name);
			assign_text2.load(desc);
//			assign_text2.setModifiable(false);
			machiningPanel.add(row + ".1.center.center", lable);
			machiningPanel.add(row + ".2.center.center", assign_text2);
			coms.add(assign_text2);
			
			property_name = "ae8cx_no_unit";
			desc = masterType.getPropDesc(property_name);
			InterfacePropertyComponent com =  new PropertyLOV(getLOV(property_name));
			com.setProperty(property_name);
			com.load(desc);
			machiningPanel.add(row + ".3.center.center", (Component) com);
			coms.add(com);
			
			assignB2 = new JButton("指派");
			assignB2.addActionListener(this);
			machiningPanel.add(row + ".4.center.center", assignB2);
			row++;
		}
		
		property_name = "ae8gy_filename";
		desc = masterType.getPropDesc(property_name);
		if (desc != null) {
			PropertyNameLabel lable = new PropertyNameLabel();
			lable.load(desc);
			InterfacePropertyComponent com =  new PropertyTextField();
			com.setProperty(property_name);
			com.load(desc);
			com.setModifiable(false);
			com.setUIFValue(form.getProperty("ae8part_no"));
			machiningPanel.add(row + ".1.center.center", lable);
			machiningPanel.add(row + ".2.center.center", (Component) com);
			coms.add(com);
			
			property_name = "ae8gy_filename_unit";
			desc = masterType.getPropDesc(property_name);
			com = new PropertyLOV(getLOV(property_name));;
			com.setProperty(property_name);
			com.load(desc);
			machiningPanel.add(row + ".3.center.center", (Component) com);
			coms.add(com);
			
			property_name = "ae8gx_no1";
			desc = masterType.getPropDesc(property_name);
			com = new PropertyTextField();
			com.setProperty(property_name);
			com.load(desc);
			((PropertyTextField)com).setColumns(4);
			com.setUIFValue(form.getProperty("ae8gx_no"));
			com.setModifiable(false);
			
			machiningPanel.add(row + ".4.center.center", (Component) com);
			coms.add(com);
			row++;
		}
		
		property_name = "ae8cx_state";
		desc = masterType.getPropDesc(property_name);
		if (desc != null) {
			PropertyNameLabel lable = new PropertyNameLabel();
			lable.load(desc);
			InterfacePropertyComponent com =  new PropertyLOV(getLOV(property_name));
			com.setProperty(property_name);
			com.load(desc);
			machiningPanel.add(row + ".1.center.center", lable);
			machiningPanel.add(row + ".2.center.center", (Component) com);
			coms.add(com);
			row++;
		}
		
		property_name = "ae8cxfz";
		desc = masterType.getPropDesc(property_name);
		if (desc != null) {
			PropertyNameLabel lable = new PropertyNameLabel();
			lable.load(desc);
			InterfacePropertyComponent com = new PropertyLOV(getLOV(property_name));
			com.setProperty(property_name);
			com.load(desc);
			machiningPanel.add(row + ".1.center.center", lable);
			machiningPanel.add(row + ".2.center.center", (Component) com);
			coms.add(com);
			row++;
		}
		
		selectButton = new JButton("选择");
		selectButton.addActionListener(this);
		machiningPanel.add(row + ".1.center.center", new JLabel("设备选择:"));
		machiningPanel.add(row + ".2.center.center", selectButton);
		row++;
		
		property_name = "ae8sb_no";
		desc = masterType.getPropDesc(property_name);
		if (desc != null) {
			PropertyNameLabel lable = new PropertyNameLabel();
			lable.load(desc);
			jc_name =  new PropertyTextField();
			jc_name.setProperty(property_name);
			jc_name.load(desc);
			jc_name.setModifiable(false);
			machiningPanel.add(row + ".1.center.center", lable);
			machiningPanel.add(row + ".2.center.center", (Component) jc_name);
			coms.add(jc_name);
			row++;
		}
		
		property_name = "ae8jc_id";
		desc = masterType.getPropDesc(property_name);
		if (desc != null) {
			PropertyNameLabel lable = new PropertyNameLabel();
			lable.load(desc);
			sb_no =  new PropertyTextField();
			sb_no.setProperty(property_name);
			sb_no.load(desc);
			sb_no.setModifiable(false);
			machiningPanel.add(row + ".1.center.center", lable);
			machiningPanel.add(row + ".2.center.center", (Component) sb_no);
			coms.add(sb_no);
			row++;
		}
		
		property_name = "ae8os";
		desc = masterType.getPropDesc(property_name);
		if (desc != null) {
			PropertyNameLabel lable = new PropertyNameLabel();
			lable.load(desc);
			os =  new PropertyTextField();
			os.setProperty(property_name);
			os.load(desc);
			os.setModifiable(false);
			machiningPanel.add(row + ".1.center.center", lable);
			machiningPanel.add(row + ".2.center.center", (Component) os);
			coms.add(os);
			row++;
		}
		return machiningPanel;
		
	}
	
	public String[] getLOV(String property_name) {
		String[] values = session.getPreferenceService().getStringValues(property_name + "_lov");
		return values == null ? new String[0]: values;
	}
	
	public JPanel createRightPanel(TCComponent form) throws Exception {
		JPanel relationPanel = new JPanel();
		relationPanel.setBorder(BorderFactory.createTitledBorder("关联机加工序信息"));
		relationPanel.setLayout(new PropertyLayout());
		relationPanel.setBackground(Color.WHITE);
		if (form != null) {
			int row = 1;
			for (String formPro : relationFormPros) {
				TCProperty tcp = form.getTCProperty(formPro);
				if (tcp == null) {
					System.out.println(form.getType() + " 表单中没有属性：" + formPro);
					continue;
				}
				PropertyNameLabel lable = new PropertyNameLabel();
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
		Map<String, String> properties = getValues();
		form.setProperties(properties);
		return form;
	}
	
	public Map<String, String> getValues(){
		Map<String, String> properties = new HashMap<>();
		for (InterfacePropertyComponent com : coms) {
			Object obj = com.getEditableValue();
			if (obj == null) {
				obj = "";
			}
			properties.put(com.getProperty(), obj.toString());
		}
		return properties;
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
	
	public void assign1() throws TCException {
		String str = assign_text1.getText();
		if (str.isEmpty()) {
			assign_text1.setText(getNumber());
		}
	}
	
	public void assign2() throws TCException {
		String str = assign_text2.getText();
		if (str.isEmpty()) {
			assign_text2.setText(getNumber());
		}
	}
	
	public String getNumber() throws TCException {
		TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent("AE8ItemIDRule");
		
		return itemType.getNewID();
	}
	
	public void select() {
		
	}
	
	public void syncDeviceValue(TCComponent com) throws TCException {
		String id = "";
		String no = "";
		String type = "";
		if (com != null) {
			TCComponent form = com.getRelatedComponent("IMAN_master_form_rev");
			id = com.getProperty("item_id");
			no = form.getProperty("ae8device_no");
			type = form.getProperty("ae8control_type");
		}
		jc_name.setText(id);
		sb_no.setText(no);
		os.setText(type);
	}
}
