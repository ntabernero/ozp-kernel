package io.bicycle.proxy;


import com.google.common.io.ByteStreams;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ProxyServlet extends HttpServlet {

    private static final String HEADER_LOCATION = "Location";
    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private static final String HEADER_HOST_NAME = "Host";

    final Logger logger = LoggerFactory.getLogger(ProxyServlet.class);

    private ProxyTarget proxyTarget;

    public String getServletInfo() {
        return "ProxyServlet";
    }

    /**
     * Initialize the <code>io.bicycle.proxy.ProxyServlet</code>
     *
     * @param servletConfig The Servlet configuration passed in by the servlet container
     */
    public void init(final ServletConfig servletConfig) {

        final String protocol = "https".equalsIgnoreCase(servletConfig.getInitParameter("proxyProtocol")) ? "https" : "http";

        final String stringProxyHostNew = servletConfig.getInitParameter("proxyHost");
        if (isNullOrEmpty(stringProxyHostNew)) {
            throw new IllegalArgumentException("Proxy host not set, please set init-param 'proxyHost' in web.xml");
        }
        final String configProxyPort = servletConfig.getInitParameter("proxyPort");
        int proxyPort = "https".equals(protocol) ? 443 : 80;
        if (!isNullOrEmpty(configProxyPort)) {
            proxyPort = Integer.parseInt(configProxyPort);
        }
        // Get the proxy path if specified
        final String stringProxyPathNew =
                !isNullOrEmpty(servletConfig.getInitParameter("proxyPath")) ? servletConfig.getInitParameter("proxyPath") : "";

        this.proxyTarget = new ProxyTarget(protocol, stringProxyHostNew, proxyPort, stringProxyPathNew);
    }

    @Override
    public void doGet(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        HttpGet getMethodProxyRequest = new HttpGet(this.createProxyUrl(httpServletRequest));

        setProxyRequestHeaders(httpServletRequest, getMethodProxyRequest);

        this.executeProxyRequest(getMethodProxyRequest, httpServletRequest, httpServletResponse);
    }

    @Override
    public void doPost(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        final HttpPost postMethodProxyRequest = new HttpPost(this.createProxyUrl(httpServletRequest));
        setProxyRequestHeaders(httpServletRequest, postMethodProxyRequest);

        this.handleRequestBody(postMethodProxyRequest, httpServletRequest);

        this.executeProxyRequest(postMethodProxyRequest, httpServletRequest, httpServletResponse);
    }

    @Override
    public void doPut(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws IOException, ServletException {
        final HttpPut putMethodProxyRequest = new HttpPut(this.createProxyUrl(httpServletRequest));

        this.handleRequestBody(putMethodProxyRequest, httpServletRequest);

        this.executeProxyRequest(putMethodProxyRequest, httpServletRequest, httpServletResponse);
    }

    @Override
    public void doDelete(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws IOException, ServletException {
        final HttpDelete deleteMethodProxyRequest = new HttpDelete(this.createProxyUrl(httpServletRequest));

        this.executeProxyRequest(deleteMethodProxyRequest, httpServletRequest, httpServletResponse);
    }

    /**
     * Sets up the given {@link HttpEntityEnclosingRequest} to send the same
     * data as was sent in the given {@link HttpServletRequest}
     *
     * @param postMethodProxyRequest The {@link HttpEntityEnclosingRequest} that we are
     *                               configuring to forward along data
     * @param httpServletRequest     The {@link HttpServletRequest} that contains
     *                               the data to be sent via the {@link HttpPost}
     */
    private void handleRequestBody(HttpEntityEnclosingRequest postMethodProxyRequest, HttpServletRequest httpServletRequest) {
        // Set the proxy requests body to match this input stream
        BasicHttpEntity entity = new BasicHttpEntity();
        try {
            entity.setContent(httpServletRequest.getInputStream());
        } catch (IOException e) {
            logger.error("Could not read stream from request");
            e.printStackTrace();
        }
        entity.setContentType(httpServletRequest.getContentType());
        entity.setContentLength(httpServletRequest.getContentLength());
        postMethodProxyRequest.setEntity(entity);
    }

    /**
     * Executes the {@link HttpRequestBase} passed in and sends the proxy response
     * back to the client via the given {@link HttpServletResponse}
     * Will change any Location headers to match the proxy host instead of the proxy target
     *
     * @param httpMethodProxyRequest An object representing the proxy request to be made
     * @param httpServletResponse    An object by which we can send the proxied
     *                               response back to the client
     * @throws java.io.IOException      Can be thrown by the {@link HttpClient}.executeMethod
     * @throws ServletException Can be thrown to indicate that another error has occurred
     */
    private void executeProxyRequest(
            final HttpRequestBase httpMethodProxyRequest,
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse)
            throws IOException, ServletException
    {
        final HttpClient httpClient = new DefaultHttpClient();
        // Don't follow redirects
        HttpClientParams.setRedirecting(httpClient.getParams(), false);

        HttpResponse response = httpClient.execute(httpMethodProxyRequest);
        int proxyResponseCode = response.getStatusLine().getStatusCode();

        if (proxyResponseCode >= 500) {
            StatusLine statusLine = response.getStatusLine();
            StringBuilder builder = new StringBuilder();
            builder.append(String.format("%s %d %s", statusLine.getProtocolVersion().toString(), proxyResponseCode, statusLine.getReasonPhrase()));
            builder.append('\n');
            builder.append("For request: \n");
            builder.append(httpMethodProxyRequest.getMethod()).append(": ").append(httpMethodProxyRequest.getURI().toString()).append("\n");
            for(Header header : httpMethodProxyRequest.getAllHeaders()) {
                builder.append(String.format("%s: %s\n", header.getName(), header.getValue()));
            }
            if(httpMethodProxyRequest instanceof HttpPost) {
                HttpPost post = (HttpPost)httpMethodProxyRequest;
                if(post.getEntity() != null) {
                    builder.append(post.getEntity().toString());
                }
            }
            logger.error(builder.toString());
        }

        // Change the location header to point to proxy server
        HeaderIterator it = response.headerIterator();
        while (it.hasNext()) {
            Header header = it.nextHeader();
            if(header.getName().equalsIgnoreCase(HEADER_LOCATION)) {
                // Modify the location value to point to this proxy servlet rather that the proxied host
                String location = header.getValue();
                String myHostName = httpServletRequest.getServerName();
                if (httpServletRequest.getServerPort() != 80) {
                    myHostName += ":" + httpServletRequest.getServerPort();
                }
                myHostName += httpServletRequest.getContextPath();
                location = location.replace(this.proxyTarget.getProxyHostAndPort() + this.proxyTarget.getProxyPath(), myHostName);
                httpServletResponse.addHeader(header.getName(), location);
            } else if (header.getName().equalsIgnoreCase("Transfer-Encoding")) {
                continue;
            } else {
                httpServletResponse.addHeader(header.getName(), header.getValue());
            }
        }

        // Pass the response code back to the client
        httpServletResponse.setStatus(proxyResponseCode);

        // Send the content to the client
        if(response.getEntity() != null) {
            ByteStreams.copy(response.getEntity().getContent(), httpServletResponse.getOutputStream());
        }
    }

    /**
     * Retrieves all of the headers from the servlet request and sets them on
     * the proxy request. Changes host header to match proxy target
     *
     * @param httpServletRequest     The request object representing the client's
     *                               request to the servlet engine
     * @param httpMethodProxyRequest The request that we are about to send to
     *                               the proxy host
     */
    @SuppressWarnings("unchecked")
    private void setProxyRequestHeaders(final HttpServletRequest httpServletRequest, final HttpRequestBase httpMethodProxyRequest) {
        final Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            if (headerName.equalsIgnoreCase(HEADER_CONTENT_LENGTH))
                continue;
            // As per the Java Servlet API 2.5 documentation:
            //		Some headers, such as Accept-Language can be sent by clients
            //		as several headers each with a different value rather than
            //		sending the header as a comma separated list.
            // Thus, we get an Enumeration of the header values sent by the client
            final Enumeration<String> enumerationOfHeaderValues = httpServletRequest.getHeaders(headerName);
            while (enumerationOfHeaderValues.hasMoreElements()) {
                String stringHeaderValue = enumerationOfHeaderValues.nextElement();
                // In case the proxy host is running multiple virtual servers,
                // rewrite the Host header to ensure that we get content from
                // the correct virtual server
                if (headerName.equalsIgnoreCase(HEADER_HOST_NAME)) {
                    stringHeaderValue = this.proxyTarget.getProxyHostAndPort();
                }
                // Set the same header on the proxy request
                httpMethodProxyRequest.setHeader(headerName, stringHeaderValue);
            }
        }
    }

    private String createProxyUrl(final HttpServletRequest httpServletRequest) {
        String proxyUrl = this.proxyTarget.getProxyBaseURl() + httpServletRequest.getPathInfo();

        // Handle the query string
        if (httpServletRequest.getQueryString() != null) {
            proxyUrl += "?" + httpServletRequest.getQueryString();
        }
        return proxyUrl;
    }
}