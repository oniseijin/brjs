package org.bladerunnerjs.app.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.bladerunnerjs.model.ThreadSafeStaticBRJSAccessor;
import org.bladerunnerjs.api.BRJS;
import org.bladerunnerjs.api.logging.Logger;
import org.bladerunnerjs.api.model.exception.InvalidSdkDirectoryException;
import org.bladerunnerjs.app.service.RestApiService;
import org.bladerunnerjs.utility.FileUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RestApiServlet extends HttpServlet
{

	private static final long serialVersionUID = -719269762512274533L;

	private static final Pattern APPS_PATTERN = Pattern.compile("\\/apps");
	private static final Pattern SINGLE_APP_PATTERN = Pattern.compile("\\/apps/[a-zA-Z0-9_-]+");
	private static final Pattern APP_IMAGE_PATTERN = Pattern.compile("\\/apps/[a-zA-Z0-9_-]+/thumb");
	private static final Pattern BLADESET_PATTERN = Pattern.compile("\\/apps/[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+");
	private static final Pattern BLADE_PATTERN = Pattern.compile("\\/apps/[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+");
	private static final Pattern EXPORT_APP_PATTERN = Pattern.compile("\\/export/[a-zA-Z0-9_-]+");
	private static final Pattern RELASE_NOTE_PATTERN = Pattern.compile("\\/note/latest");
	private static final Pattern SDK_VERSION_PATTERN = Pattern.compile("\\/sdk\\/version");
	private static final Pattern TEST_BLADESET_PATTERN = Pattern.compile("\\/test/[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+");
	private static final Pattern TEST_BLADE_PATTERN = Pattern.compile("\\/test/[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+");
	
	private static final String COMMAND_PARAM = "command";
	private static final String NAMESPACE_PARAM = "namespace";
	private static final String APP_PARAM = "app";
	private static final String BLADESETS_PARAM = "bladesets";
	private static final String FILE_PARAM = "file";

	private static final String CREATE_APP_COMMAND = "create-app";
	private static final String IMPORT_MOTIF = "import-motif";
	private static final String CREATE_BLADESET_COMMAND = "create-bladeset";
	private static final String CREATE_BLADE_COMMAND = "create-blade";
	private static final String IMPORT_BLADES_COMMAND = "import-blades";
	private static final String GENERATE_DOCS_COMMAND = "generate-docs";
	//private static final String TEST_COMMAND = "test";	// is this needed?
	
	private static final String IMPORT_BLADESETS_NEWBLADESET_NAME_KEY = RestApiService.IMPORT_BLADESETS_NEWBLADESET_NAME_KEY;
	private static final String IMPORT_BLADESETS_BLADES_KEY = RestApiService.IMPORT_BLADESETS_BLADES_KEY;

	private static final String TEST_TYPE = "ALL";
	
	private RestApiService apiService;
	private ServletContext context;
	private BRJS brjs;
	private Logger logger;
	private ConcurrentMap<String, Lock> applicationLockMap = null; 

	public RestApiServlet()
	{
	}
	
	protected RestApiServlet(RestApiService apiService)
	{
		this.apiService = apiService;
	}

	@Override
	public void init(final ServletConfig config) throws ServletException
	{
		try {
			context = config.getServletContext();
			
			File contextDir = new File( context.getRealPath("/") );
			brjs = ThreadSafeStaticBRJSAccessor.initializeModel( contextDir, contextDir );
			
			if (apiService == null) { apiService = new RestApiService(brjs); };
			logger = brjs.logger(this.getClass());
		}
		catch (InvalidSdkDirectoryException e) {
			throw new ServletException(e);
		}
	}
	
	@Override
	public void destroy()
	{
		try
		{
			ThreadSafeStaticBRJSAccessor.destroy();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException
	{
		boolean responseHandled = false;
		String serviceResponse = "";
		String requestPath = getRequestPath(request);
		String appName = getAppNameFromRequest(requestPath);

		logger.debug("rest api servlet got 'GET' request for " + requestPath);
		try
		{
			if (APPS_PATTERN.matcher(requestPath).matches())
			{
				serviceResponse = apiService.getApps();
				responseHandled = true;
			}
			else if (SINGLE_APP_PATTERN.matcher(requestPath).matches())
			{
				serviceResponse = apiService.getApp(appName);
				responseHandled = true;
			}
			else if (APP_IMAGE_PATTERN.matcher(requestPath).matches())
			{
				InputStream input = apiService.getAppImageInputStream(appName);
				
				response.setContentType("image/png");
				
				IOUtils.copy(input, response.getOutputStream());
				response.flushBuffer();
				return;
			}
			else if (EXPORT_APP_PATTERN.matcher(requestPath).matches())
			{
				File targetDir = FileUtils.createTemporaryDirectory( this.getClass() );
				try {
    				File warTempFile = new File(targetDir, "x.war");
    				apiService.exportWar(appName, brjs.getMemoizedFile(warTempFile));
    				response.setContentType("application/octet-stream");
    				response.setHeader("Content-Disposition", "attachment; filename=\""+appName+".war\"");
    				
    				response.addHeader("Content-Length", Long.toString(warTempFile.length()));
    				
    				IOUtils.copy(new FileInputStream(warTempFile), response.getOutputStream());
    				response.flushBuffer();
				} finally {
					org.apache.commons.io.FileUtils.deleteQuietly(targetDir);
				}
				return;
			}
			else if (RELASE_NOTE_PATTERN.matcher(requestPath).matches())
			{
				serviceResponse = apiService.getCurrentReleaseNotes();
				responseHandled = true;
			}
			else if (SDK_VERSION_PATTERN.matcher(requestPath).matches())
			{
				serviceResponse = apiService.getSdkVersion();
				responseHandled = true;
			}
		}
		catch (Exception ex)
		{
			send500Response(response, ex);
			return;
		}

		if (responseHandled)
		{
			response.getWriter().println(serviceResponse);
			response.flushBuffer();
		}
		else
		{
			send404Response(request.getRequestURL().toString(),response);
		}
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException
	{
		boolean responseHandled = false;
		String serviceResponse = "";
		try
		{
			String requestPath = getRequestPath(request);
			logger.debug("rest api servlet got 'POST' request for " + requestPath);

			String appName = getAppNameFromRequest(requestPath);
			String bladesetName = getBladesetNameFromRequest(requestPath);
			String bladeName = getBladeNameFromRequest(requestPath);

			/* handle multipart content for file upload */
			if (ServletFileUpload.isMultipartContent(request))
			{
				responseHandled = handleMultiparRequest(appName, request);
			}
			else
			{
				String requestBody = getRequestBody(request);
								
				JsonObject requestJson = null;
				if (!requestBody.equals(""))
				{
					requestJson = new JsonParser().parse(requestBody).getAsJsonObject();
				}

				String command = (requestJson != null)?requestJson.get(COMMAND_PARAM).getAsString():"";

				if (SINGLE_APP_PATTERN.matcher(requestPath).matches())
				{
					if (command.equals(CREATE_APP_COMMAND))
					{
						apiService.createApp(appName, requestJson.get(NAMESPACE_PARAM).getAsString());
						responseHandled = true;
					}
					else if (command.equals(GENERATE_DOCS_COMMAND))
					{
						Lock appLock = getAppLock(appName);
						
						if (appLock.tryLock()) {
							try {
								apiService.getJsdocForApp(appName);
							} finally {
								appLock.unlock();
							}
						} else {
							appLock.lock();
							appLock.unlock();
						}
						responseHandled = true;
					}
					else if (command.equals(IMPORT_BLADES_COMMAND))
					{
						Map<String,Map<String,List<String>>> bladesetsMap = new LinkedHashMap<String,Map<String,List<String>>>();
						
						JsonObject bladesetsJson = requestJson.get(BLADESETS_PARAM).getAsJsonObject();							
						
						Iterator<Entry<String,JsonElement>> bladesetsIterator = bladesetsJson.entrySet().iterator();									
						while (bladesetsIterator.hasNext())
						{
							Entry<String,JsonElement> nextEntry = bladesetsIterator.next();
							
							String thisBladesetName = nextEntry.getKey();
							JsonObject thisBladesetJson = nextEntry.getValue().getAsJsonObject();
							Map<String,List<String>> newBladesetMap = new LinkedHashMap<String,List<String>>();
							
							String newBladesetName = thisBladesetJson.get(IMPORT_BLADESETS_NEWBLADESET_NAME_KEY).getAsString();							
							newBladesetMap.put(RestApiService.IMPORT_BLADESETS_NEWBLADESET_NAME_KEY, Arrays.asList(newBladesetName));

							JsonArray bladesJson = thisBladesetJson.get(IMPORT_BLADESETS_BLADES_KEY).getAsJsonArray();
							Iterator<JsonElement> bladesIterator = bladesJson.iterator();
							List<String> blades = new ArrayList<String>();
							while (bladesIterator.hasNext())
							{
								String thisBladeName = bladesIterator.next().getAsString();
								blades.add(thisBladeName);
							}
							newBladesetMap.put(RestApiService.IMPORT_BLADESETS_BLADES_KEY, blades);
							
							bladesetsMap.put(thisBladesetName, newBladesetMap);
						}
						apiService.importBladeset(requestJson.get(APP_PARAM).getAsString(), bladesetsMap, appName);
						responseHandled = true;
					}
				}
				else if (BLADESET_PATTERN.matcher(requestPath).matches())
				{
					if (command.equals(CREATE_BLADESET_COMMAND))
					{
						apiService.createBladeset(appName, bladesetName);
						responseHandled = true;
					}
				}
				else if (BLADE_PATTERN.matcher(requestPath).matches())
				{
					if (command.equals(CREATE_BLADE_COMMAND))
					{
						apiService.createBlade(appName, bladesetName, bladeName);
						responseHandled = true;
					}
				}
				else if (TEST_BLADESET_PATTERN.matcher(requestPath).matches())
				{
					serviceResponse = apiService.runBladesetTests(appName, bladesetName, TEST_TYPE);
					responseHandled = true;
				}
				else if (TEST_BLADE_PATTERN.matcher(requestPath).matches())
				{
					serviceResponse = apiService.runBladeTests(appName, bladesetName, bladeName, TEST_TYPE);
					responseHandled = true;
				}
			}
		}
		catch (Exception ex)
		{
			send500Response(response, ex);
			return;
		}

		if (responseHandled)
		{
			response.getWriter().println(serviceResponse);
			response.getWriter().flush();
			response.flushBuffer();
		}
		else
		{
			send404Response(request.getRequestURL().toString(),response);
		}
	}

	private Lock getAppLock(String appName) {
		if (applicationLockMap == null)
		{
			applicationLockMap = new ConcurrentHashMap<String, Lock>();
		}
		
		applicationLockMap.putIfAbsent(appName, new ReentrantLock());
		
		return applicationLockMap.get(appName);
	}

	private boolean handleMultiparRequest(String appName, HttpServletRequest request) throws Exception
	{
		boolean responseHandled = false;
		String command = "";

		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		List<?> items = upload.parseRequest(request);

		String namespace = "";
		File zipFile = null;
		Iterator<?> iter = items.iterator();
		while (iter.hasNext())
		{
			FileItem item = (FileItem) iter.next();
			if (item.getFieldName().equals(COMMAND_PARAM))
			{
				command = item.getString();
			}
			else if (item.getFieldName().equals(NAMESPACE_PARAM))
			{
				namespace = item.getString();
			}
			else if (item.getFieldName().equals(FILE_PARAM))
			{
				zipFile = FileUtils.createTemporaryFile(this.getClass(), ".zip");
				item.write(zipFile);
			}
		}

		if (!namespace.equals("") && zipFile != null && command.equals(IMPORT_MOTIF))
		{
			apiService.importMotif(appName, namespace, brjs.getMemoizedFile(zipFile));
			responseHandled = true;
		}
		return responseHandled;
	}

	private String getAppNameFromRequest(String requestPath)
	{
		String[] pathSplit = splitRequestPath(requestPath);
		String appName = (pathSplit.length > 1) ? pathSplit[1] : "";
		return appName;
	}

	private String getBladesetNameFromRequest(String requestPath)
	{
		String[] pathSplit = splitRequestPath(requestPath);
		String bladesetName = (pathSplit.length > 2) ? pathSplit[2] : "";
		return bladesetName;
	}

	private String getBladeNameFromRequest(String requestPath)
	{
		String[] pathSplit = splitRequestPath(requestPath);
		String bladesetName = (pathSplit.length > 3) ? pathSplit[3] : "";
		return bladesetName;
	}

	private String[] splitRequestPath(String requestPath)
	{
		if (requestPath.startsWith("/"))
		{
			requestPath = requestPath.replaceFirst("/", "");
		}
		String[] pathSplit = requestPath.split("/");
		return pathSplit;
	}

	private String getRequestPath(HttpServletRequest request)
	{
		return request.getPathInfo();
	}

	private String getRequestBody(HttpServletRequest request) throws IOException
	{
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try
		{
			InputStream inputStream = request.getInputStream();
			if (inputStream != null)
			{
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0)
				{
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			}
			else
			{
				stringBuilder.append("");
			}
		}
		finally
		{
			if (bufferedReader != null)
			{
				bufferedReader.close();	
			}
		}
		return stringBuilder.toString();
	}

	private void send500Response(HttpServletResponse response, Exception ex) throws IOException
	{
		String exceptionMessage = (ex.getMessage() != null)?ex.getMessage():"";
				
		Map<String,String> responseMap = new LinkedHashMap<String,String>();
		responseMap.put("cause", ex.getClass().getCanonicalName());
		responseMap.put("message", exceptionMessage);
				
		String jsonBody = new Gson().toJson(responseMap);
		
		response.setStatus(500);
		response.getOutputStream().println(jsonBody);
	}

	private void send404Response(String url, HttpServletResponse response) throws IOException
	{
		Map<String,String> responseMap = new LinkedHashMap<String,String>();
		responseMap.put("cause", "Not Found");
		responseMap.put("message", "Invalid URL ("+url+") - page not found.");
				
		String jsonBody = new Gson().toJson(responseMap);
				
		response.setStatus(404);
		response.getOutputStream().println(jsonBody);
	}
	
}
