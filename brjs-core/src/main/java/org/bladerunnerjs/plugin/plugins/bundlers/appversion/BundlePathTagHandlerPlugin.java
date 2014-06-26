package org.bladerunnerjs.plugin.plugins.bundlers.appversion;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.BundleSet;
import org.bladerunnerjs.plugin.Locale;
import org.bladerunnerjs.plugin.base.AbstractTagHandlerPlugin;
import org.bladerunnerjs.utility.ServedAppMetadataUtility;


public class BundlePathTagHandlerPlugin extends AbstractTagHandlerPlugin
{

	@Override
	public String getTagName()
	{
		return "bundle.path";
	}

	@Override
	public void writeDevTagContent(Map<String, String> tagAttributes, BundleSet bundleSet, Locale locale, Writer writer, String version) throws IOException
	{
		writeProdTagContent(tagAttributes, bundleSet, locale, writer, version);
	}

	@Override
	public void writeProdTagContent(Map<String, String> tagAttributes, BundleSet bundleSet, Locale locale, Writer writer, String version) throws IOException
	{
		boolean includeVersion = true;
		if (tagAttributes.containsKey("version")) {
			String versionAttribute = tagAttributes.get("version");
			includeVersion = versionAttribute.toLowerCase().equals("yes") || Boolean.parseBoolean(versionAttribute);
		}
		
		if (includeVersion) {
			writer.write( ServedAppMetadataUtility.getVersionedBundlePath(version) );
		} else {
			writer.write( ServedAppMetadataUtility.getUnversionedBundlePath() );
		}
	}

	@Override
	public void setBRJS(BRJS brjs)
	{
	}
	
}
