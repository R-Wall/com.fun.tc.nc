package com.fun.tc.nc.until;

import java.io.File;

import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.util.FileUtility;

public class TCFileUtil {

	public TCComponentDataset dataset = null;
	private String namedRef = null;	
	
	public String path = null;
	File file = null;
	File sign_file = null;
	String fileName = "";
	public TCFileUtil(TCComponentDataset dataset) {
		this.dataset = dataset;
	}
	
	public String getFile() throws Exception {
		String folderName = System.getProperty("user.home");
		TCComponentDatasetDefinition datasetDef = dataset.getDatasetDefinitionComponent();
		
		NamedReferenceContext[] nameRefContexts = datasetDef.getNamedReferenceContexts();
		if ((nameRefContexts == null) || (nameRefContexts.length == 0)) return null;
		
		NamedReferenceContext nf = nameRefContexts[0];
		namedRef = nf.getNamedReference();
		
		String[] fileNames = dataset.getFileNames(namedRef);
		if ((fileNames == null) || (fileNames.length == 0)) return null;
		
		for (String fileName : fileNames) {
			file = dataset.getFile(namedRef, fileName, folderName);
			String[] strs = fileName.split("[.]");
			fileName = "temp"+"." + strs[1];
			System.out.println(dataset + " = " + fileName);
			if (file != null) {
				sign_file = new File(folderName, fileName);
				if (!sign_file.exists()) {
					sign_file.createNewFile();
				}
				FileUtility.copyFile(file, sign_file);
				break;
			}
		}
		return sign_file.getAbsolutePath();
	}
	
	
	public void updateFile() throws Exception {
		FileUtility.copyFile(sign_file, file);
		dataset.setFiles(new String[] {file.getAbsolutePath()}, new String[] {namedRef});
	}
}
