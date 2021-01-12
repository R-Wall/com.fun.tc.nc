package com.fun.tc.nc.until;

import javax.swing.JComboBox;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.stylesheet.InterfacePropertyComponent;

public class PropertyLOV extends JComboBox<Object> implements InterfacePropertyComponent {

	private static final long serialVersionUID = 2963208683369763204L;
	
	String[] values;
	
	String property_name;
	
	boolean modifiable = false;

	public PropertyLOV(String[] values) {
		super(values);
		this.values = values;
	}

	public PropertyLOV() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return getSelectedItem();
	}

	@Override
	public String getProperty() {
		// TODO Auto-generated method stub
		return property_name;
	}

	@Override
	public boolean isMandatory() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPropertyModified(TCComponent arg0) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPropertyModified(TCProperty arg0) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void load(TCComponent arg0) throws Exception {
		load(arg0.getTCProperty(getProperty()));
		
	}

	@Override
	public void load(TCProperty arg0) throws Exception {
		load(arg0.getDescriptor());
		
	}

	@Override
	public void load(TCComponentType arg0) throws Exception {
		load(arg0.getPropDesc(getProperty()));
		
	}

	@Override
	public void load(TCPropertyDescriptor arg0) throws Exception {
		property_name = arg0.getName();
		
	}

	@Override
	public void save(TCComponent arg0) throws Exception {
		arg0.setProperty(getProperty(), getEditableValue().toString());
		
	}

	@Override
	public void save(TCProperty arg0) throws Exception {
		save(arg0.getTCComponent());
		
	}

	@Override
	public void setMandatory(boolean arg0) {
		
	}

	@Override
	public void setModifiable(boolean arg0) {
		modifiable = arg0;
		
	}

	@Override
	public void setProperty(String arg0) {
		property_name = arg0;
		
	}

	@Override
	public void setUIFValue(Object arg0) {
		setSelectedItem(arg0);
		
	}

}
