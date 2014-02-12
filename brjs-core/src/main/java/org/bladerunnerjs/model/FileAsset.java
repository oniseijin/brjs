package org.bladerunnerjs.model;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.bladerunnerjs.model.exception.ConfigException;
import org.bladerunnerjs.utility.RelativePathUtility;
import org.bladerunnerjs.utility.UnicodeReader;

public class FileAsset implements Asset {
	private File file;
	private AssetLocation assetLocation;
	private String defaultInputEncoding;
	private String assetPath;
	
	@Override
	public void initialize(AssetLocation assetLocation, File dir, String assetName) throws AssetFileInstantationException {
		try {
			this.file = new File(dir, assetName);
			this.assetLocation = assetLocation;
			defaultInputEncoding = assetLocation.root().bladerunnerConf().getDefaultInputEncoding();
			assetPath = RelativePathUtility.get(assetLocation.getAssetContainer().getApp().dir(), file);
		}
		catch(ConfigException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Reader getReader() throws IOException {
		return new UnicodeReader(file, defaultInputEncoding);
	}
	
	@Override
	public AssetLocation getAssetLocation() {
		return assetLocation;
	}
	
	@Override
	public File dir()
	{
		return file.getParentFile();
	}
	
	@Override
	public String getAssetName() {
		return file.getName();
	}
	
	@Override
	public String getAssetPath() {
		return assetPath;
	}
}
