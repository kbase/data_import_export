package us.kbase.kbasedataimport.test;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import us.kbase.kbasedataimport.DownloadServlet;
import us.kbase.kbasedataimport.KBaseDataImportServer;

public class TestServer {
    public static void main(String[] args) throws Exception {
        File tempDir = new File("temp_files");
        if (!tempDir.exists())
            tempDir.mkdir();
        Map<String, String> config = new LinkedHashMap<String, String>();
        config.put(KBaseDataImportServer.CFG_PROP_SCRATCH, tempDir.getAbsolutePath());
        KBaseDataImportServer.injectConfigForTests(config);
        int port = 12345;
        Server jettyServer = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        jettyServer.setHandler(context);
        context.addServlet(new ServletHolder(new DownloadServlet()),"/*");
        jettyServer.start();
        jettyServer.join();
    }
}
