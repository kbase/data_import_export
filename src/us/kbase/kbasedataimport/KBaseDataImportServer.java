package us.kbase.kbasedataimport;

import java.util.List;
import java.util.Map;
import us.kbase.auth.AuthToken;
import us.kbase.common.service.JsonServerMethod;
import us.kbase.common.service.JsonServerServlet;
import us.kbase.common.service.Tuple2;

//BEGIN_HEADER
import java.io.File;
import java.net.URL;

import org.ini4j.Ini;
//END_HEADER

/**
 * <p>Original spec-file module name: KBaseDataImport</p>
 * <pre>
 * </pre>
 */
public class KBaseDataImportServer extends JsonServerServlet {
    private static final long serialVersionUID = 1L;

    //BEGIN_CLASS_HEADER
    public static final String SYS_PROP_KB_DEPLOYMENT_CONFIG = "KB_DEPLOYMENT_CONFIG";
    public static final String SERVICE_DEPLOYMENT_NAME = "KBaseDataImport";
    
    public static final String CFG_PROP_SCRATCH = "scratch";
    public static final String CFG_PROP_WORKSPACE_SRV_URL = "workspace.srv.url";
    public static final String CFG_PROP_SHOCK_URL = "shock.url";
    public static final String CFG_PROP_AUTH_SERVICE_URL = "auth-service-url";
    
    public static final String VERSION = "0.0.2";
    
    private static Throwable configError = null;
    private static Map<String, String> config = null;
    
    public static Map<String, String> config() {
    	if (config != null)
    		return config;
        if (configError != null)
        	throw new IllegalStateException("There was an error while loading configuration", configError);
		String configPath = System.getProperty(SYS_PROP_KB_DEPLOYMENT_CONFIG);
		if (configPath == null)
			configPath = System.getenv(SYS_PROP_KB_DEPLOYMENT_CONFIG);
		if (configPath == null) {
			configError = new IllegalStateException("Configuration file was not defined");
		} else {
			System.out.println(KBaseDataImportServer.class.getName() + ": Deployment config path was defined: " + configPath);
			try {
				config = new Ini(new File(configPath)).get(SERVICE_DEPLOYMENT_NAME);
			} catch (Throwable ex) {
				System.out.println(KBaseDataImportServer.class.getName() + ": Error loading deployment config-file: " + ex.getMessage());
				configError = ex;
			}
		}
		if (config == null)
			throw new IllegalStateException("There was unknown error in service initialization when checking"
					+ "the configuration: is the ["+SERVICE_DEPLOYMENT_NAME+"] config group defined?");
		return config;
    }
    
    public static void injectConfigForTests(Map<String, String> config) {
        KBaseDataImportServer.config = config;
    }

    public static File getTempDir() {
    	String ret = config().get(CFG_PROP_SCRATCH);
    	if (ret == null)
    		throw new IllegalStateException("Parameter " + CFG_PROP_SCRATCH + " is not defined in configuration");
    	File dir = new File(ret);
    	if (!dir.exists())
    		dir.mkdirs();
    	return dir;
    }
    
    public static String getWorkspaceServiceURL() {
    	String ret = config().get(CFG_PROP_WORKSPACE_SRV_URL);
    	if (ret == null)
    		throw new IllegalStateException("Parameter " + CFG_PROP_WORKSPACE_SRV_URL + " is not defined in configuration");
    	return ret;
    }
    public static String getShockURL() {
    	String ret = config().get(CFG_PROP_SHOCK_URL);
    	if (ret == null)
    		throw new IllegalStateException("Parameter " + CFG_PROP_SHOCK_URL + " is not defined in configuration");
    	return ret;
    }
    
    public static String getAuthUrl() {
        String ret = config().get(CFG_PROP_AUTH_SERVICE_URL);
        if (ret == null) {
            throw new IllegalStateException("Parameter " + CFG_PROP_AUTH_SERVICE_URL + " is not defined in configuration");
        }
        return ret;
    }
    
    @Override
    protected String getAuthUrlFromConfig(Map<String, String> origConfig) {
        return super.getAuthUrlFromConfig(config);
    }
    //END_CLASS_HEADER

    public KBaseDataImportServer() throws Exception {
        super("KBaseDataImport");
        //BEGIN_CONSTRUCTOR
        System.out.println(KBaseDataImportServer.class.getName() + ": " + CFG_PROP_SCRATCH +" = " + getTempDir());
        System.out.println(KBaseDataImportServer.class.getName() + ": " + CFG_PROP_WORKSPACE_SRV_URL +" = " + getWorkspaceServiceURL());
        System.out.println(KBaseDataImportServer.class.getName() + ": " + CFG_PROP_AUTH_SERVICE_URL +" = " + getAuthUrl());
        //END_CONSTRUCTOR
    }

    /**
     * <p>Original spec-file function name: ver</p>
     * <pre>
     * Returns the current running version of the NarrativeMethodStore.
     * </pre>
     * @return   instance of String
     */
    @JsonServerMethod(rpc = "KBaseDataImport.ver")
    public String ver() throws Exception {
        String returnVal = null;
        //BEGIN ver
        config();
        returnVal = VERSION;
        //END ver
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: get_ncbi_genome_names</p>
     * <pre>
     * List of names of genomes that can be used for 'import_ncbi_genome' method
     * </pre>
     * @return   instance of list of String
     */
    @JsonServerMethod(rpc = "KBaseDataImport.get_ncbi_genome_names")
    public List<String> getNcbiGenomeNames() throws Exception {
        List<String> returnVal = null;
        //BEGIN get_ncbi_genome_names
        if (true)
            throw new IllegalStateException("Method is not supported anymore");
        //END get_ncbi_genome_names
        return returnVal;
    }

    /**
     * <p>Original spec-file function name: import_ncbi_genome</p>
     * <pre>
     * Import genome from NCBI FTP 'ftp://ftp.ncbi.nih.gov/genomes/Bacteria/' into worspace object
     * </pre>
     * @param   input   instance of type {@link us.kbase.kbasedataimport.ImportNcbiGenomeParams ImportNcbiGenomeParams} (original type "import_ncbi_genome_params")
     */
    @JsonServerMethod(rpc = "KBaseDataImport.import_ncbi_genome")
    public void importNcbiGenome(ImportNcbiGenomeParams input, AuthToken authPart) throws Exception {
        //BEGIN import_ncbi_genome
        if (true)
            throw new IllegalStateException("Method is not supported anymore");
        //END import_ncbi_genome
    }

    /**
     * <p>Original spec-file function name: upload</p>
     * <pre>
     * </pre>
     * @param   input   instance of String
     * @param   output   instance of String
     * @param   workspace   instance of String
     * @param   objectName   instance of String
     * @param   props   instance of mapping from String to String
     * @return   parameter "job_ids" of tuple of size 2: String, String
     */
    @JsonServerMethod(rpc = "KBaseDataImport.upload")
    public Tuple2<String, String> upload(String input, String output, String workspace, String objectName, Map<String,String> props, AuthToken authPart) throws Exception {
        Tuple2<String, String> returnVal = null;
        //BEGIN upload
        if (true)
            throw new IllegalStateException("Method is not supported anymore");
        //END upload
        return returnVal;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: <program> <server_port>");
            return;
        }
        new KBaseDataImportServer().startupServer(Integer.parseInt(args[0]));
    }
}
