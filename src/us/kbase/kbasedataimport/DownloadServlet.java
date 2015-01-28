package us.kbase.kbasedataimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import us.kbase.auth.AuthToken;
import us.kbase.common.service.UObject;
import us.kbase.shock.client.BasicShockClient;
import us.kbase.shock.client.ShockNodeId;
import us.kbase.workspace.ObjectIdentity;
import us.kbase.workspace.WorkspaceClient;

public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = -1L;
	
    private static File tempDir = null;
    private static String wsUrl = null;
    private static String shockUrl = null;

    private static File getTempDir() throws IOException {
		if (tempDir == null)
			tempDir = KBaseDataImportServer.getTempDir();
		return tempDir;
    }

    private static String getWsUrl() throws IOException {
		if (wsUrl == null)
			wsUrl = KBaseDataImportServer.getWorkspaceServiceURL();
		return wsUrl;
    }

    private static String getShockUrl() throws IOException {
		if (shockUrl == null)
			shockUrl = KBaseDataImportServer.getShockURL();
		return shockUrl;
    }

	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		setupResponseHeaders(request, response);
		response.setContentLength(0);
		response.getOutputStream().print("");
		response.getOutputStream().flush();
	}

	private static void setupResponseHeaders(HttpServletRequest request,
			HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		String allowedHeaders = request.getHeader("HTTP_ACCESS_CONTROL_REQUEST_HEADERS");
		response.setHeader("Access-Control-Allow-Headers", allowedHeaders);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	@SuppressWarnings("resource")
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		File f = null;
		try {
			String token = request.getParameter("token");
			String url = request.getParameter("url");
			String ws = request.getParameter("ws");
			String name = request.getParameter("name");
			String id = getNotNull(request, "id");
			String shockZipSuffix = request.getParameter("zip");
			String shockNeedDeletion = request.getParameter("del");
			response.setContentType("application/octet-stream");
			if (ws != null) {
				if (url == null)
					url = getWsUrl();
				WorkspaceClient wc = createWsClient(token, url);
				String fileName = removeWeirdChars(id);
				f = File.createTempFile("download_" + fileName, ".json", getTempDir());
				wc._setFileForNextRpcResponse(f);
				UObject data = wc.getObjects(Arrays.asList(new ObjectIdentity().withRef(ws + "/" + id))).get(0).getData();
				setupResponseHeaders(request, response);
				if (name == null)
					name = fileName + ".json";
				response.setHeader( "Content-Disposition", "attachment;filename=" + removeWeirdChars(name));
				data.write(response.getOutputStream());
			} else {
				if (url == null)
					url = getShockUrl();
				BasicShockClient client = createShockClient(token, url);
				try {
					setupResponseHeaders(request, response);
					if (shockZipSuffix == null) {
						if (name == null)
							name = id + ".node";
						response.setHeader( "Content-Disposition", "attachment;filename=" + name);
						client.getFile(new ShockNodeId(id), response.getOutputStream());
					} else {
						shockZipSuffix = shockZipSuffix.toLowerCase();
						f = File.createTempFile("download_" + removeWeirdChars(id), ".zip", getTempDir());
						FileOutputStream fos = new FileOutputStream(f);
						client.getFile(new ShockNodeId(id), fos);
						fos.close();
						ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
						try {
							while (true) {
								ZipEntry ze = zis.getNextEntry();
								if (ze == null) {
									// we didn't find necessary entry, throw an error
									// zis will be closed in finally block anyway
									throw new IllegalStateException("Can't find entry name matching " +
											"[" + shockZipSuffix + "] suffix in zip file for shock node " + id);
								}
								if (ze.getName().toLowerCase().endsWith(shockZipSuffix)) {
									if (name == null) {
										name = ze.getName();
										if (name.contains("/"))
											name = name.substring(name.lastIndexOf('/') + 1);
									}
									response.setHeader( "Content-Disposition", "attachment;filename=" + removeWeirdChars(name));
									OutputStream os = response.getOutputStream();
									byte[] buffer = new byte[10000];
									while (true) {
										int len = zis.read(buffer);
										if (len <= 0)
											break;
										os.write(buffer, 0, len);
									}
									os.flush();
									buffer = null;
									break;
								}
							}
						} finally {
							try { zis.close(); } catch (Exception ignore) {}
						}
					}
				} finally {
					if (shockNeedDeletion != null && !(shockNeedDeletion.equals("false") || shockNeedDeletion.equals("0")))
						try {
							client.deleteNode(new ShockNodeId(id));
						} catch (Exception ignore) {
							ignore.printStackTrace();
						}
				}
			}
		} catch (ServletException ex) {
			throw ex;
		} catch (IOException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ServletException(ex.getMessage(), ex);
		} finally {
			if (f != null && f.exists())
				try { f.delete(); } catch (Exception ignore) {}
		}
	}
	
	private static String removeWeirdChars(String text) {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (Character.isLetter(ch) || Character.isDigit(ch) || ch == '.') {
				ret.append(ch);
			} else {
				ret.append('_');
			}
		}
		return ret.toString();
	}
	
	private static String getNotNull(HttpServletRequest request, String param) throws ServletException {
		String value = request.getParameter(param);
		if (value == null)
			throw new ServletException("Parameter [" + param + "] wasn't defined");
		return value;
	}

    public static long copy(InputStream from, OutputStream to) throws IOException {
    	byte[] buf = new byte[10000];
    	long total = 0;
    	while (true) {
    		int r = from.read(buf);
    		if (r == -1) {
    			break;
    		}
    		to.write(buf, 0, r);
    		total += r;
    	}
    	return total;
    }

	public static WorkspaceClient createWsClient(String token, String wsUrl) throws Exception {
		WorkspaceClient ret;
		if (token == null) {
			ret = new WorkspaceClient(new URL(wsUrl));
		} else {
			ret = new WorkspaceClient(new URL(wsUrl), new AuthToken(token));
			ret.setAuthAllowedForHttp(true);
		}
		return ret;
	}
	
	public static BasicShockClient createShockClient(String token, String url) throws Exception {
		BasicShockClient ret;
		if (token == null) {
			ret = new BasicShockClient(new URL(url));
		} else {
			ret = new BasicShockClient(new URL(url), new AuthToken(token));
		}
		return ret;
	}

	public static WorkspaceClient createWsClient(String token) throws Exception {
		String wsUrl = getWsUrl();
		return createWsClient(token, wsUrl);
	}

	public static void main(String[] args) throws Exception {
		int port = 18888;
		if (args.length == 1)
			port = Integer.parseInt(args[0]);
		Server jettyServer = new Server(port);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		jettyServer.setHandler(context);
		context.addServlet(new ServletHolder(new DownloadServlet()),"/download");
		jettyServer.start();
		jettyServer.join();
	}
}
