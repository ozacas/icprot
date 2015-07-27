/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.onekp.icprot.servlets;

import java.io.IOException;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author acassin
 */
public class TreeDownload extends HttpServlet {
    private final static Logger log = Logger.getLogger("AvailableDownloadsServlet");
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processGET(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Map<String,String> params = ServiceCore.splitQuery(request.getQueryString());
        if (!params.containsKey("clade")) {
            throw new ServletException("No 1KP taxonomic clade specified!");
        }
        boolean as_file = "1".equals(params.get("file"));
        File root = ServiceCore.find_root("phyloxml_per_clade");
        if (root == null) {
            throw new ServletException("Cannot find root");
        }
        log.log(Level.INFO, "Locating tree in {0}", new Object[] { root.getAbsolutePath() });
        
        File phyloxml = new File(root, params.get("clade")+".phyloxml");
        if (!phyloxml.isFile() || !phyloxml.canRead()) {
            throw new ServletException("Cannot read "+phyloxml.getAbsolutePath());
        }
        try (OutputStream out = response.getOutputStream()) {
            response.setContentType("application/xml");
            if (as_file) {
                response.setHeader("Content-disposition", " attachment; filename=\""+suggestedFilename(phyloxml)+"\"");
            }
            Files.copy(phyloxml.toPath(), out);
        }
    }

    private String suggestedFilename(File phyloxml) throws ServletException {
        if (phyloxml == null) {
            throw new ServletException("No file specified!");
        }
        return phyloxml.getName().replaceAll("\\s+", "_");
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processGET(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Returns a bootstrap-compatible HTML table with available downloads for a given GET query";
    }// </editor-fold>

}
