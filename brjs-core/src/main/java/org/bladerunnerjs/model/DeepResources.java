package org.bladerunnerjs.model;

import java.io.File;
import java.util.List;

import org.bladerunnerjs.model.file.AliasDefinitionsFile;

public class DeepResources implements Resources {
	public DeepResources(File dir) {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public AliasDefinitionsFile aliasDefinitions() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<LinkedAssetFile> seedResources() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<LinkedAssetFile> seedResources(String fileExtension) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<AssetFile> bundleResources(String fileExtension) {
		// TODO Auto-generated method stub
		return null;
	}
}