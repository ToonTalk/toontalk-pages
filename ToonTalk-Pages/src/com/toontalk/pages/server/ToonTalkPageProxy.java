/**
 * 
 */
package com.toontalk.pages.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Serves ToonTalk published pages with content type text/html and allows cross origin access * 
 * 
 * @author Ken Kahn
 *
 */

public class ToonTalkPageProxy extends HttpServlet {

    private static final long serialVersionUID = 3075405196542994862L;
    private static final String HTML_CONTENT_TYPE = "text/html; charset=utf-8";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, java.io.IOException {
        respondToGet(request, response, true);
    }

    protected void respondToGet(HttpServletRequest request, HttpServletResponse response, boolean firstTime) 
            throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            // seen in logs due to "GET /p"
            return;
        }
        String URL = java.net.URLDecoder.decode(pathInfo.substring(1), "UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        try {
            response.setContentType(HTML_CONTENT_TYPE);
            if (copyConentsOfURL(URL, out, "toontalk.js")) {
                response.setContentType(HTML_CONTENT_TYPE);
                // see http://stackoverflow.com/questions/16351849/origin-is-not-allowed-by-access-control-allow-origin-how-to-enable-cors-using
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
                response.addHeader("Access-Control-Allow-Headers", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
                response.addHeader("Access-Control-Max-Age", "1728000");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }
    
    public static boolean copyConentsOfURL(String urlString, PrintWriter out, String mustContain) {
        boolean containsMustContain = false;
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            // following fixes Error 403 from HTTP
//            connection.setRequestProperty("User-Agent", clientState.getAgentDescription());
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                out.print(line); // + "\r"
                if (!containsMustContain && line.contains(mustContain)) {
                    containsMustContain = true;
                }
            }
            in.close();
        } catch (Exception e) {     
            out.print("Error reading " + "'" + urlString + "':" + e.getMessage());
        }
        return containsMustContain;
    }

    public void writeHTMLPage(String message, String title, HttpServletResponse response, PrintWriter out) {
        response.setContentType(HTML_CONTENT_TYPE);
        out.println("<html>");
        out.println("<head><meta http-equiv='Content-Type' content='text/html;charset=utf-8' ><title>" + 
                title + 
                "</title></head>");
        out.println("<body>");
        out.println(message);
        out.println("</body></html>");
    }


}
