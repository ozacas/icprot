/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.onekp.icprot.servlets;

import au.edu.unimelb.plantcell.onekp.interfaces.FileVisitor;
import java.io.File;
import java.io.FileFilter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 * @author acassin
 */
public class ServiceCore {
    public final static String ROOT = "/1kp/icprot";
    
    public static final Map<String, String> splitQuery(final String query) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), 
                            URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }
    
    // from http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    
    public static File find_root(final String key) throws ServletException {
        if (key == null || !key.matches("^\\w+$")) {
            throw new ServletException("Bogus key to ServiceCore::find_root");
        } else if (key.equals("phyloxml_per_clade")) {
            File phyloxml_folder = new File(ROOT, key);
            if (phyloxml_folder.exists() && phyloxml_folder.isDirectory()) {
                return phyloxml_folder;
            }
            // fallthru
        }
        return null;
    }
    
    public static String encodeFile(final File f) {
        assert(f != null);
        return Base64.getEncoder().encodeToString(f.getAbsolutePath().getBytes());
    }
    
    /**
     * Returns true if a file of the given extension should be available to the user
     * as a file download, false otherwise.
     * @param name name of the file including extension eg. .docx (should be lowercased by caller) and must not be null
     * @return true if the filename is an acceptable download, false otherwise
     */
    public final static boolean acceptableFileExtensions(final String name) {
        assert(name != null && name.length() > 0);
        return (name.endsWith(".png") || name.endsWith(".xls") || name.endsWith(".table") ||
                        name.endsWith(".eps") || name.endsWith(".zip") || name.endsWith(".fasta") ||
                        name.endsWith(".csv") || name.endsWith(".gz") || name.endsWith(".jpg") ||
                        name.endsWith(".fa")  || name.endsWith(".docx") || name.endsWith(".pdf") ||
                        name.endsWith(".xlsx")|| name.endsWith(".pptx"));
    }
 
    
    private static void doVisit(final File folder, final FileFilter filter, final FileVisitor visitor) {
         // want subfolders too?
        File[] folders = folder.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return (pathname.isDirectory());
            }
            
        });
        for (File f: folders) {
            if (filter.accept(f)) {
                doVisit(f, filter, visitor);
            }
        }
        File[] plain_files = folder.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return (pathname.canRead() && pathname.isFile() && filter.accept(pathname));
            }
            
        });
        for (File f : plain_files) {
            visitor.process(f);
        }
    }
    
    public static final void visitFiles(final File folder, final FileFilter filter, final FileVisitor visitor) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return;
        }
        if (filter == null || visitor == null) {
            return;
        }
        visitor.beforeVisit();
        doVisit(folder, filter, visitor);
        visitor.afterVisit();
    }
    
    public static final void visitFiles(List<File> files, final FileFilter filter, final FileVisitor visitor) {
        if (filter == null || visitor == null) {
            return;
        }
        visitor.beforeVisit();
        for (File f : files) {
            if (filter.accept(f)) {
                visitor.process(f);
            }
        }
        visitor.afterVisit();
    }
    
    /**
     * Converts an absolute path on the hrgp site eg. /1kp/hrgp/classical-AGP/publication-figures/...
     * to one relative to the root.
     * 
     * @param path should be a path located under {@code ServiceCore.ROOT}. Must not be null
     * @return relative path
     */
    public static final String relativize(final File path) {
        assert(path != null);
        String relative = new File(ROOT).toURI().relativize(new File(path.getAbsolutePath()).toURI()).getPath();
        
        return relative;
    }
}
