package us.kbase.kbasedataimport.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import sun.misc.IOUtils;
import us.kbase.auth.AuthService;

public class KBaseSessionBackupTester {
    private static final String KBASE_SESSION_KEY = "kbase_session_backup";
    private static final String URL_END_POINT = "https://ci.kbase.us";
    private static final String PRIVATE_OBJECT_REF = "16579/2/1";
    
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: <program> <user> <password>");
            System.exit(1);
        }
        String user = args[0];
        String password = args[1];
        String wsUrl = URL_END_POINT + "/services/ws";
        String wsUrlMasked = URLEncoder.encode(wsUrl, "utf-8");
        String url = URL_END_POINT + "/services/data_import_export/download?ref=" + 
                PRIVATE_OBJECT_REF + "&url=" + wsUrlMasked + "&wszip=1&name=object.JSON.zip";
        String token = AuthService.login(user, password).getTokenString();
        String cookies = KBASE_SESSION_KEY + "=" + URLEncoder.encode(
                mungeToken(user, token), "utf-8");
        HttpURLConnection conn = (HttpURLConnection)(new URL(url).openConnection());
        conn.setRequestMethod("GET");
        conn.addRequestProperty("Cookie", cookies);
        int responseCode = conn.getResponseCode();
        System.out.println("Response Code : " + responseCode);
        InputStream in = conn.getInputStream();
        byte[] arr = IOUtils.readFully(in, -1, true);
        in.close();
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(arr));
        while (true) {
            ZipEntry ze = zis.getNextEntry();
            if (ze == null)
                break;
            byte[] arr2 = IOUtils.readFully(zis, -1, false);
            if (ze.getName().contains("KBase_object_details")) {
                continue;
            }
            System.out.println("---------------------------------------------------\n" +
            		"Content of [" + ze.getName() + "]:");
            System.out.println(new String(arr2));
        }
        zis.close();
    }

    public static String mungeToken(String user, String token) {
        return "un=" + user + "|" + 
                "kbase_sessionid=somecoolhexcode|" +
                "user_id=" + user + "|" +
                "token=" + token.replace("|", "PIPESIGN").replace("=", "EQUALSSIGN");
    }
}
