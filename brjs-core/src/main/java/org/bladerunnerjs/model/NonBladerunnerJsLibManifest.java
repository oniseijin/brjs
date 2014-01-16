package org.bladerunnerjs.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bladerunnerjs.model.exception.ConfigException;

public class NonBladerunnerJsLibManifest extends ConfFile<YamlNonBladerunnerLibManifest>
{
	
	public static final String commaWithOptionalSpacesSeparator = "[\\s]*,[\\s]*";
	
	public NonBladerunnerJsLibManifest(AssetLocation assetLocation) throws ConfigException {
		super(assetLocation, YamlNonBladerunnerLibManifest.class, assetLocation.file("library.manifest"));
	}
	
	public List<String> getDepends() throws ConfigException
	{
		reloadConfIfChanged();
		return listify(conf.depends, null);
	}
	
	public List<String> getJs() throws ConfigException
	{
		reloadConfIfChanged();
		return listify(conf.js, ".*\\.js");
	}
	

	public List<String> getCss() throws ConfigException
	{
		reloadConfIfChanged();
		return listify(conf.css, ".*\\.css");
	}
	
	
	private List<String> listify(String value, String nullValueFallback)
	{
		if (value != null && !value.equals(""))
		{
			return Arrays.asList(value.split(commaWithOptionalSpacesSeparator));
		}
		return (nullValueFallback != null) ? Arrays.asList(nullValueFallback) : new ArrayList<String>();
	}
}