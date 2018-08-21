<%@ page session="false" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>

<%@ page import="java.io.BufferedReader,java.io.File,java.io.FileOutputStream,java.io.InputStreamReader"%>

<!-- <%
java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy");
request.setAttribute("year", sdf.format(new java.util.Date()));
request.setAttribute("tomcatUrl", "https://tomcat.apache.org/");
request.setAttribute("tomcatDocUrl", "/docs/");
request.setAttribute("tomcatExamplesUrl", "/examples/");
%> -->
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8" />
        <!-- <title><%=request.getServletContext().getServerInfo() %></title> -->
        <link href="favicon.ico" rel="icon" type="image/x-icon" />
        <link href="favicon.ico" rel="shortcut icon" type="image/x-icon" />
        <link href="tomcat.css" rel="stylesheet" type="text/css" />
    </head>

    <body>
        loading...<br/>
        <%
            String market = request.getParameter("market");
            String coin = request.getParameter("coin");
            String buy = request.getParameter("buy");
            String period = request.getParameter("period");

            String jarFileDirPath = new java.io.File(application.getRealPath(request.getRequestURI())).getParent();
            String jarFileFullPath = jarFileDirPath + "/btc_spider.jar";

            String cmd = "java -jar " + jarFileFullPath + " -f kink_link -m " + market + " -c " + coin + " -b " + buy + " -t " + period;

            out.print("cmd : " + cmd + "<br/>");
            out.print("ready to exec<br/>");

            //执行命令
            StringBuilder sb=new StringBuilder();
            BufferedReader br = null;
            Process p =null;
            try {
                p = Runtime.getRuntime().exec(cmd);
                br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                out.print(sb.toString());
            } catch (Exception e) {
                out.print("in Exception");
                e.printStackTrace();
                out.print(e.getMessage());
            } finally {
                out.print("in finally");
                if (br != null) {
                    try {
                        br.close();
                        p.destroy();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // 页面跳转
            RequestDispatcher rd = request.getRequestDispatcher("poly_line_chart.html"); 
            rd.forward(request,response);
        %>
    </body>

</html>
