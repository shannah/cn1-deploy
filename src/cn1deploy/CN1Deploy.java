/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn1deploy;

import com.codename1.processing.Result;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.lingala.zip4j.core.ZipFile;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author shannah
 */
public class CN1Deploy {

    private final File directory;
    
    public CN1Deploy(File dir) {
        this.directory = dir;
    }
    
    public File getDeploymentsDir() {
        return new File("cn1-deployments");
    }
    
    public File getJavascriptBuildDir() {
        return new File(getJavascriptDir(), "build");
    }
    
    public File getJavascriptDir() {
        return new File(getDeploymentsDir(), "javascript");
    }
    
    public File getJavaSEDir() {
        return new File(getDeploymentsDir(), "javase");
    }
    
    public Properties getCodenameOneProperties() throws IOException {
        Properties p = new Properties();
        p.load(new FileInputStream("codenameone_settings.properties"));
        return p;
        
    }
    
    private String getName(Properties props, boolean prefixPackage) {
        String name = props.getProperty("codename1.displayName");
        StringBuilder sb = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (!(Character.isAlphabetic(c) || Character.isDigit(c))) {
                sb.append("-");
                continue;
            }
            if (Character.isUpperCase(c)) {
                if (sb.length() > 0) {
                    sb.append("-");
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        
        String packageName = props.getProperty("codename1.packageName");
        packageName = packageName.replace(".", "-");
        if (prefixPackage) {
            return packageName +"-"+sb.toString();
        } else {
            return sb.toString();
        }
    }
    
    private String getVersion(Properties props) {
        String buildVersion = props.getProperty("codename1.version");
        return buildVersion + ".0";
    }
    
    private void initJavascript() throws IOException  {
        if (!getDeploymentsDir().exists()) {
            getDeploymentsDir().mkdir();
        }
        
        if (!getJavascriptDir().exists()) {
            getJavascriptDir().mkdir();
        }
        
        File packageJson = new File(getJavascriptDir(), "package.json");
        if (!packageJson.exists()) {
            /*
            {
                "name": "jdeploy",
                "version": "1.0.7",
                "repository": "https://github.com/shannah/jdeploy",
                "description": "Deploy java apps using NPM",
                "main": "index.js",
                "scripts": {
                  "test": "echo \"Error: no test specified\" && exit 1"
                },
                "bin": {
                  "jdeploy": "bin/jdeploy.js"
                },
                "preferGlobal": true,
                "author": "Steve Hannah",
                "license": "ISC",
                "dependencies": {
                  "shelljs": "^0.7.5"
                },
                "files": [
                  "bin"
                ]
              }
            */
            
            
            
            Properties p = getCodenameOneProperties();
            Map m = new HashMap();
            m.put("name", getName(p, true)+"-js");
            m.put("version", getVersion(p));
            m.put("repository", "");
            m.put("description", p.getProperty("codename1.description"));
            m.put("main", "index.js");
            Map bin = new HashMap();
            bin.put(getName(p, false)+"-js", "jdeploy-bundle");
            m.put("bin", bin);
            m.put("preferGlobal", true);
            m.put("author", p.getProperty("codename1.vendor"));
            
            Map scripts = new HashMap();
            scripts.put("test", "echo \"Error: no test specified\" && exit 1");
            
            
            m.put("scripts", scripts);
            m.put("license", "ISC");
            
            Map dependencies = new HashMap();
            dependencies.put("shelljs", "^0.7.5");
            m.put("dependencies", dependencies);
            
            List files = new ArrayList();
            files.add("jdeploy-bundle");
            
            m.put("files", files);
            
            Map jdeploy = new HashMap();
            //jdeploy.put("war", "web");
            m.put("jdeploy", jdeploy);
            
            Result res = Result.fromContent(m);
            FileUtils.writeStringToFile(packageJson, res.toString(), "UTF-8");
            System.out.println("Javascript deployment package created at "+getJavascriptDir());
            System.out.println("package.json generated at "+packageJson);
            
        } else {
            System.out.println("Javascript deployment found at "+getJavascriptDir());
            System.out.println("package.json found at "+packageJson);
        }
        
    }
    
    
    
    private void installAntScript(boolean overwrite) throws IOException {
        
        File dest = new File("cn1-deploy.xml");
        System.out.println("Installing ANT script at "+dest);
        if (dest.exists() && !overwrite) {
            System.out.println("ANT script already exists.  Not overwriting");
            return;
        }
        InputStream is = getClass().getResourceAsStream("cn1-deploy.xml");
        
        FileUtils.copyInputStreamToFile(is, dest);
        
    }
    
    private int runAntTask(String antFile, String target) throws IOException {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.inheritIO();
            pb.command("ant", "-f", antFile, target);
            Process p = pb.start();
            return p.waitFor();
            
        } catch (InterruptedException ex) {
            Logger.getLogger(CN1Deploy.class.getName()).log(Level.SEVERE, null, ex);
            return 1;
        }
    }
    private File sendJavascriptBuild() throws IOException {
        installAntScript(false);
        
        runAntTask("cn1-deploy.xml", "build-for-javascript-sync");
        
        File dist = new File("dist");
        return new File(dist, "result.zip");
        
    }
    
    
    private void initJavaSE() throws IOException {
        if (!getDeploymentsDir().exists()) {
            getDeploymentsDir().mkdir();
        }
        
        if (!getJavaSEDir().exists()) {
            getJavaSEDir().mkdir();
        }
        
        File packageJson = new File(getJavaSEDir(), "package.json");
        if (!packageJson.exists()) {
            /*
            {
                "name": "jdeploy",
                "version": "1.0.7",
                "repository": "https://github.com/shannah/jdeploy",
                "description": "Deploy java apps using NPM",
                "main": "index.js",
                "scripts": {
                  "test": "echo \"Error: no test specified\" && exit 1"
                },
                "bin": {
                  "jdeploy": "bin/jdeploy.js"
                },
                "preferGlobal": true,
                "author": "Steve Hannah",
                "license": "ISC",
                "dependencies": {
                  "shelljs": "^0.7.5"
                },
                "files": [
                  "bin"
                ]
              }
            */
            
            
            
            Properties p = getCodenameOneProperties();
            Map m = new HashMap();
            m.put("name", getName(p, true)+"-desktop");
            m.put("version", getVersion(p));
            m.put("repository", "");
            m.put("description", p.getProperty("codename1.description"));
            m.put("main", "index.js");
            Map bin = new HashMap();
            bin.put(getName(p, false)+"-desktop", "jdeploy-bundle/jdeploy.js");
            m.put("bin", bin);
            m.put("preferGlobal", true);
            m.put("author", p.getProperty("codename1.vendor"));
            
            Map scripts = new HashMap();
            scripts.put("test", "echo \"Error: no test specified\" && exit 1");
            
            
            m.put("scripts", scripts);
            m.put("license", "ISC");
            
            Map dependencies = new HashMap();
            dependencies.put("shelljs", "^0.7.5");
            m.put("dependencies", dependencies);
            
            List files = new ArrayList();
            files.add("jdeploy-bundle");
            
            m.put("files", files);
            
            Map jdeploy = new HashMap();
            //jdeploy.put("jar", "dist" + File.separator + p.getProperty("codename1.displayName")+".jar");
            m.put("jdeploy", jdeploy);
            
            Result res = Result.fromContent(m);
            FileUtils.writeStringToFile(packageJson, res.toString(), "UTF-8");
            
            System.out.println("JavaSE deployment package created at "+getJavaSEDir());
            System.out.println("package.json generated at "+packageJson);
        } else {
            System.out.println("JavaSE deployment package found at "+getJavaSEDir());
            System.out.println("package.json at "+packageJson);
        }
    }
    
    private void extractDMG(File dmg, File dest) {
        
    }
    
    private long mTimeRecursive(File root) {
        long mtime = root.lastModified();
        if (root.isDirectory()) {
            for (File f : root.listFiles()) {
                mtime = Math.max(mTimeRecursive(f), mtime);
            }
        }
        return mtime;
    }
    
    private void buildJavaSE() throws IOException  {
        System.out.println("Building project for JavaSE platform");
        try {
            File dist = new File(directory, "dist");
            File javaseDir = getJavaSEDir();
            File packageJson = new File(javaseDir, "package.json");
            if (!packageJson.exists()) {
                throw new RuntimeException("Please 'run cn1-deploy init javase' before trying to do a build. ");
            }
            javaseDir.mkdirs();
            File resultZip = new File(getJavaSEDir(), "result.zip");
            
            if (resultZip.exists() && resultZip.lastModified() >= mTimeRecursive(dist)) {
                // The project hasn't changed since we last built
                // so just use existing resultZip
            } else {
                
                installAntScript(true);
                System.out.println("Initiating synchronous build, and sending to the Codename One build server.\n This may take a few minutes");
                runAntTask("cn1-deploy.xml", "build-for-mac-os-x-desktop-sync");
                if (!new File(dist, "result.zip").exists()) {
                    throw new RuntimeException("There was a problem with the build.  No result.zip found");
                }
                FileUtils.copyFile(new File(dist, "result.zip"), resultZip);
            }
            
            
            ZipFile zipFile = new ZipFile(resultZip);
            
            
            
            File tmpDir = new File(getJavaSEDir(), "tmp");
            if (tmpDir.exists()) {
                FileUtils.deleteDirectory(tmpDir);
            }
            tmpDir.mkdirs();
            
            System.out.println("Extracting the result of the Codename One build server to temporary directory.");
            zipFile.extractAll(tmpDir.getAbsolutePath());
            
            File srcZip = null;
            for (File f : tmpDir.listFiles()) {
                if (f.getName().endsWith(".zip")) {
                    srcZip = f;
                }
            }
            
            if (srcZip == null) {
                throw new RuntimeException("Failed to extract zip file");
            }
            
            ZipFile srcZipFile = new ZipFile(srcZip);
            File srcProjDir = new File(getJavaSEDir(), "tmp-project");
            if (srcProjDir.exists()) {
                FileUtils.deleteDirectory(srcProjDir);
            }
            srcZipFile.extractAll(srcProjDir.getAbsolutePath());
            
            File javaseClean = new File(srcProjDir, "JavaSEClean.jar");
            File projectPropertiesFile = new File(srcProjDir+File.separator+"nbproject"+File.separator+"project.properties");
            
            Properties projectProperties = new Properties();
            projectProperties.load(new FileInputStream(projectPropertiesFile));
            projectProperties.setProperty("jnlp.enabled", "false");
            
            projectProperties.setProperty("libs.CopyLibs.classpath", System.getProperty("jdeploy.base")+File.separator+"lib"+File.separator+"org-netbeans-modules-java-j2seproject-copylibstask.jar");
            
            projectProperties.store(new FileOutputStream(projectPropertiesFile), "Disabled jnlp");
            
            
            System.out.println("Building deployment project");
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("ant", "jar");
            pb.directory(srcProjDir);
            pb.inheritIO();
            Process p = pb.start();
            int code = p.waitFor();
            if (code != 0) {
                throw new IOException("Failed to build distribution project.");
            }
            
            // For some reason javaseclean isn't auto-copied into the dist
            File javaseCleanDist = new File(srcProjDir, "dist" + File.separator + "lib" + File.separator + javaseClean.getName());
            FileUtils.copyFile(javaseClean, javaseCleanDist);
            
            // Now copy the dist directory up to the main level so that it can be built 
            
            File distDir = new File(getJavaSEDir(), "dist");
            if (distDir.exists()) {
                FileUtils.deleteDirectory(new File(getJavaSEDir(), "dist"));
            }
            System.out.println("Copying executable JAR and dependencies to "+new File(getJavaSEDir(), "dist"));
            FileUtils.copyDirectory(new File(srcProjDir, "dist"), new File(getJavaSEDir(), "dist"));
            
            
            FileUtils.deleteDirectory(tmpDir);
            FileUtils.deleteDirectory(srcProjDir);
            
           
            
        } catch (Exception ex) {
            Logger.getLogger(CN1Deploy.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        
    }
    
    private void installJavaSE() throws IOException {
        try {
            File dist = new File(directory, "dist");
            File resultZip = new File(getJavaSEDir(), "result.zip");
            if (!resultZip.exists()) {
                buildJavaSE();
            } else if (resultZip.lastModified() < mTimeRecursive(dist)) {
                System.out.println(resultZip + " may be out of date.  Try deleting it, or run cn1-deploy build to generate a new build.");
            }
            
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("jdeploy", "install");
            pb.directory(getJavaSEDir());
            pb.inheritIO();
            Process p = pb.start();
            int code = p.waitFor();
            if (code != 0) {
                System.err.println("install failed");
                System.exit(1);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(CN1Deploy.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
    
    private void help() {
        System.out.println("Usage: cn1-deploy [init|build|install|publish] [javascript|javase]");
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        CN1Deploy prog = new CN1Deploy(new File(".").getAbsoluteFile());
        if (args.length < 2) {
            prog.help();
            System.exit(1);
        }
        if ("init".equals(args[0])) {
            if ("javascript".equals(args[1])) {
                prog.initJavascript();
            } else if ("javase".equals(args[1])) {
                prog.initJavaSE();
            } else {
                System.err.println("Unrecognized platform. Expecting javascript or javase but found "+args[1]);
                System.exit(1);
            }
            
        } else if ("build".equals(args[0])) {
            if ("javascript".equals(args[1])) {
                prog.sendJavascriptBuild();
            } else if ("javase".equals(args[1])) {
                prog.buildJavaSE();
            } else {
                throw new RuntimeException("Unsupported build target "+args[1]);
            }
        } else if ("install".equals(args[0])) {
            if ("javase".equals(args[1])) {
                prog.installJavaSE();
            } else {
                throw new RuntimeException("Unsupported build target "+args[1]);
            }
        } else {
            System.err.println("Unrecognized command.  Expecting init but found "+args[0]);
            System.exit(1);
        }
    }
    
}
