package org.bladerunnerjs.plugin.minifiers;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bladerunnerjs.api.BRJS;
import org.bladerunnerjs.api.model.exception.request.ContentProcessingException;
import org.bladerunnerjs.api.model.exception.request.ResourceNotFoundException;
import org.bladerunnerjs.api.plugin.InputSource;
import org.bladerunnerjs.api.plugin.MinifierPlugin;
import org.bladerunnerjs.api.plugin.base.AbstractMinifierPlugin;

import com.Ostermiller.util.ConcatReader;

public class ConcatentatingMinifierPlugin extends AbstractMinifierPlugin implements MinifierPlugin {
	private List<String> settingNames = new ArrayList<>();
	
	{
		settingNames.add("combined");
	}
	
	@Override
	public void setBRJS(BRJS brjs) {
		// do nothing
	}
	
	@Override
	public List<String> getSettingNames() {
		return settingNames;
	}
	
	@Override
	public Reader minify(String settingName, List<InputSource> inputSources) throws ContentProcessingException, ResourceNotFoundException {
		List<Reader> readers = new LinkedList<Reader>();
		
		for (InputSource inputSource : inputSources) {
			readers.add( inputSource.getContentPluginReader() );
			readers.add( new StringReader("\n\n") );
		}
		
		return new ConcatReader( readers.toArray(new Reader[0]) );
	}
	
	@Override
	public Reader generateSourceMap(String minifierLevel, List<InputSource> inputSources) throws ContentProcessingException {
		throw new RuntimeException("The ConcatentatingMinifierPlugin does not support souce-maps, so should never receive a request to create one.");
	}
}
