package com.fun.tc.nc.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.fun.tc.nc.until.MyClassifyManager;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.classification.common.tree.G4MTree;
import com.teamcenter.rac.classification.common.tree.G4MTreeNode;
import com.teamcenter.rac.classification.common.tree.InterfaceG4MNodeClick2Listener;
import com.teamcenter.rac.kernel.TCClassificationService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.ics.ICSProperty;
import com.teamcenter.rac.kernel.ics.ICSSearchResult;
import com.teamcenter.rac.util.MessageBox;

public class SelectDeviceDialog extends AbstractAIFDialog implements ActionListener, InterfaceG4MNodeClick2Listener{

	private static final long serialVersionUID = 7223082702520376213L;
	private JButton okButton;
	private JButton closeButton;
	CreateCAEDialog dialog;
	private MyClassifyManager cm;
	private G4MTree tree;
	private JScrollPane jscPanel;
	private DefaultTableModel tableModel;
	private JTable table;
	String[] titles = new String[] {"设备信息"};
	G4MTreeNode node;

	public SelectDeviceDialog(CreateCAEDialog dialog) throws Exception {
		super(dialog);
		this.dialog = dialog;
		setTitle("选择设备");
		setLayout(new BorderLayout());
		JSplitPane splitPane =  new JSplitPane();
		splitPane.setBorder(BorderFactory.createTitledBorder("相关信息"));
		cm = new MyClassifyManager(dialog.session, "");
		tree = cm.getClassificationTree();
		tree.addG4MNodeClick2Listener(this);
		tableModel = new DefaultTableModel(new TCComponent[0][0], titles);
		table = new JTable(tableModel){
			// 重写方法,单元格不能编辑
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table.setPreferredSize(new Dimension(300, 600));
		jscPanel = new JScrollPane(table);
		splitPane.setLeftComponent(tree);
		splitPane.setRightComponent(jscPanel);
		
		JPanel buttonPanel = new JPanel();
		okButton = new JButton("确认");
		closeButton = new JButton("取消");
		okButton.addActionListener(this);
		closeButton.addActionListener(this);
		buttonPanel.add(okButton);
		buttonPanel.add(closeButton);
		add(buttonPanel, BorderLayout.SOUTH);
		add(splitPane, BorderLayout.CENTER);
		centerToScreen();
		pack();
	}



	@Override
	public void actionPerformed(ActionEvent event) {
		try {
			Object obj = event.getSource();
			TCComponent com = null;
			if (obj.equals(okButton)) {
				int row = table.getSelectedRow();
				if (row >= 0) {
					com = (TCComponent) table.getValueAt(row, 0);
				}
			}
			dialog.syncDeviceValue(com);
		} catch (TCException e) {
			e.printStackTrace();
		}
		dispose();
	}


	public TCComponent[] getICSComponent() throws TCException {
		TCSession session = (TCSession) AIFUtility.getDefaultSession();
		TCClassificationService service = session.getClassificationService();
		ICSSearchResult[] sr = service.searchICOs(node.getNodeName(), new ICSProperty[0], 8);
		TCComponent[] coms = new TCComponent[sr.length];
		if (sr != null) {
			for (int i = 0; i < coms.length; i++) {
				TCComponent com = session.getComponentManager().getTCComponent(sr[i].getWsoUid());
				TCComponentItemRevision rev = null;
				if (com instanceof TCComponentItem) {
					rev = ((TCComponentItem) com).getLatestItemRevision();
				} else if (com instanceof TCComponentItemRevision) {
					rev = (TCComponentItemRevision) com;
				}
				coms[i] = rev;
			}
		}
		return coms;
	}

	@Override
	public void nodeClicked(G4MTreeNode node) {
		this.node = node;
		try {
			TCComponent[] coms = getICSComponent();
			TCComponent[][] values = new TCComponent[coms.length][1];
			for (int i = 0; i < coms.length; i++) {
				values[i][0] = coms[i];
			}
			table.setModel(new DefaultTableModel(values, titles));
			
		} catch (Exception e) {
			MessageBox.post(this, e);
		}
	}

	@Override
	public void restore(G4MTreeNode arg0) {
		
	}
	
}
