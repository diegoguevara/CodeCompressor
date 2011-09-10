/*!
 * @(#)CodeCompressor.java
 *
 * @author: Diego Guevara
 * diego.guevara@ritbox.com
 *
 * Copyright 2011, Diego Guevara - Ritbox.com
 * www.ritbox.com
 *
 * Last Update : 2011.08
 *
 * Required:
 * yuicompressor-2.4.6.jar
 * commons-io-2.0.1.jar
 */
package rbx.codecompressor.main;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

/**
 * Compress multiple CSS or JS code files in a minified single file
 * @author Diego Guevara
 * @version 1.0.0
 * @since 2011.08
 */
public class CodeCompressor {

    /**
     * CodeCompressor main method
     * @param args the command line arguments
     * args[0] = filelist, args[1] = output file, args[2] = files type(js,css)
     */
    public static void main(String[] args) {
        CodeCompressor ccomp = new CodeCompressor();
        /*
        if (args.length < 3 || args.length > 3){
            ccomp.printHelp();  // prints help info
            System.exit(0);     // exits
        }*/
        
        String sourcefile   = "/Users/Diego/archivos.txt";//args[0];
        String outputfile   = "/Users/Diego/Dev/php/account/public_html/app/js/common.min.js";//args[1];
        //String filetype     = args[2];
        
        List<String> filelst = ccomp.readSourceFile(sourcefile);
        
        String filecontents = "";
        for(int i=0; i<filelst.size();i++){
            filecontents += ccomp.readFileData(filelst.get(i));
        }
        
        try {
            ccomp.compressJavascript(filecontents, outputfile);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CodeCompressor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CodeCompressor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    /**
     * Read source configuration file with process file list
     * @param sourcefile String with sourcefile path
     * @return List with files to process
     */
    private List<String> readSourceFile(String sourcefile){
        List<String> filelst = new ArrayList<String>();
        
        Options o = new Options();
        
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(sourcefile), o.charset));
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    filelst.add(line);
                }
            } finally {
                input.close();
            }
        } catch (IOException e) {
            Logger.getLogger(CodeCompressor.class.getName()).log(Level.SEVERE, "Error reading source file.", e);
        }
        
        return filelst;
    }
    
    
    /**
     * Read file content as string
     * @param file file name
     * @return String with file content
     */
    private String readFileData(String file){
        StringBuilder contents = new StringBuilder();
        
        Options o = new Options();
        
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file), o.charset));
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (IOException e) {
            Logger.getLogger(CodeCompressor.class.getName()).log(Level.SEVERE, "Error reading file.", e);
        }

        return contents.toString();
    }
    
    
    /**
     * Call YUI Compressor tools to compress javascript text code and creates output file
     * @param text          Content of source javascript files
     * @param outputfile    Output filename
     * @throws UnsupportedEncodingException
     * @throws IOException 
     */
    private void compressJavascript(String text, String outputfile) throws UnsupportedEncodingException, IOException{
        Options o = new Options();
        
        Reader in = null;
        Writer out = null;
        try {
            in = new InputStreamReader( new ByteArrayInputStream(text.getBytes( o.charset )),  o.charset );

            JavaScriptCompressor compressor = new JavaScriptCompressor(in, new YuiCompressorErrorReporter());
            in.close();
            in = null;

            out = new OutputStreamWriter(new FileOutputStream(outputfile), o.charset);
            compressor.compress(out, o.lineBreakPos, o.munge, o.verbose, o.preserveAllSemiColons, o.disableOptimizations);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }
    
    
    
    
    /**
     * Prints help information
     */
    private void printHelp(){
        System.out.println("CodeCompressor 1.0.0 - developed by Diego Guevara - Ritbox.com");
        //System.out.println("");
        //System.out.println("Arguments:");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("CodeCompressor.jar [source file] [output file] [css/js]");
        System.out.println("");
        System.out.println("[source file]   text file with list of files in order to process.");
        System.out.println("[output file]   output file name");
        System.out.println("[css/js]        Type of files to process");
        //System.out.println("");
        //System.out.println("");
        //System.out.println("");
        //System.out.println("");
    }
    
    
    
    
    
    private static class YuiCompressorErrorReporter implements ErrorReporter {

        @Override
        public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
            if (line < 0) {
                Logger.getLogger(CodeCompressor.class.getName()).log(Level.WARNING, message);
            } else {
                Logger.getLogger(CodeCompressor.class.getName()).log(Level.WARNING, line + ':' + lineOffset + ':' + message);
            }
        }

        @Override
        public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
            if (line < 0) {
                Logger.getLogger(CodeCompressor.class.getName()).log(Level.SEVERE, message);
            } else {
                Logger.getLogger(CodeCompressor.class.getName()).log(Level.SEVERE, line + ':' + lineOffset + ':' + message);
            }
        }

        @Override
        public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
            error(message, sourceName, line, lineSource, lineOffset);
            return new EvaluatorException(message);
        }
    }
    
    
    
    
    
    public static class Options {

        public String charset = "UTF-8";
        public int lineBreakPos = -1;
        public boolean munge = true;
        public boolean verbose = false;
        public boolean preserveAllSemiColons = false;
        public boolean disableOptimizations = false;
    }
}
