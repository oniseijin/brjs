package org.bladerunnerjs.model.utility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bladerunnerjs.model.AliasDefinition;
import org.bladerunnerjs.model.BundleSet;
import org.bladerunnerjs.model.LinkedAssetFile;
import org.bladerunnerjs.model.Resources;
import org.bladerunnerjs.model.SourceFile;
import org.bladerunnerjs.model.exception.ModelOperationException;


public class BundleSetBuilder {
	Set<LinkedAssetFile> seedFiles = new HashSet<>();
	Set<SourceFile> sourceFiles = new HashSet<>();
	Set<AliasDefinition> activeAliases = new HashSet<>();
	Set<Resources> resources = new HashSet<>();
	
	public BundleSet createBundleSet() throws ModelOperationException {
		List<AliasDefinition> activeAliasList = new ArrayList<>();
		List<Resources> resourcesList = new ArrayList<>();
		
		activeAliasList.addAll(activeAliases);
		resourcesList.addAll(resources);
		
		return new BundleSet(orderSourceFiles(sourceFiles), activeAliasList, resourcesList);
	}
	
	public void addSeedFile(LinkedAssetFile seedFile) throws ModelOperationException {
		seedFiles.add(seedFile);
		activeAliases.addAll(seedFile.getAliases());
	}
	
	public boolean addSourceFile(SourceFile sourceFile) throws ModelOperationException {
		boolean isNewSourceFile = false;
		
		if(sourceFiles.add(sourceFile)) {
			isNewSourceFile = true;
			activeAliases.addAll(sourceFile.getAliases());
			resources.add(sourceFile.getResources());
		}
		
		return isNewSourceFile;
	}
	
	private List<SourceFile> orderSourceFiles(Set<SourceFile> sourceFiles) throws ModelOperationException {
		List<SourceFile> sourceFileList = new ArrayList<>();
		Set<LinkedAssetFile> metDependencies = new HashSet<>();
		
		while(!sourceFiles.isEmpty()) {
			for(SourceFile sourceFile : sourceFiles) {
				if(dependenciesHaveBeenMet(sourceFile, metDependencies)) {
					sourceFileList.add(sourceFile);
					metDependencies.add(sourceFile);
				}
			}
		}
		
		return sourceFileList;
	}
	
	private boolean dependenciesHaveBeenMet(LinkedAssetFile sourceFile, Set<LinkedAssetFile> metDependencies) throws ModelOperationException {
		for(LinkedAssetFile dependentSourceFile : sourceFile.getDependentSourceFiles()) {
			if(!metDependencies.contains(dependentSourceFile)) {
				return false;
			}
		}
		
		return false;
	}
}