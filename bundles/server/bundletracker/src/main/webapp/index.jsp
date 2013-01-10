<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.json.JSONStringer" %>
<%@ page import="org.ozoneplatform.kernel.modules.server.bundletracker.Tracker" %>
<%@ page contentType="text/json" %>
<%
    out.println(Tracker.getCurrentStateJson());
%>