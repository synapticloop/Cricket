/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cricketmsf.microsite.auth;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cricketmsf.microsite.user.User;

public class Exchange extends HttpExchange {

    private HttpExchange httpExchange;
    private Map<String, List<String>> headers;
    private Map attributes;

    public Exchange(HttpExchange httpExchange, User user, User issuer) {
        this.httpExchange = httpExchange;
        attributes = new HashMap();
        attributes.put("parameters", httpExchange.getAttribute("parameters"));
        attributes.put("body", httpExchange.getAttribute("body"));
        headers = httpExchange.getRequestHeaders();
        ArrayList<String> al = new ArrayList<>();
        al.add(user.getUid());
        headers.put("X-user-id", al);
        List<String> roles = new ArrayList();
        if (issuer != null) {
            ArrayList<String> al2 = new ArrayList<>();
            al2.add(issuer.getUid());
            headers.put("X-issuer-id", al2);
            roles.add("guest");
        } else {
            try {
                roles = Arrays.asList(user.getRole().split(","));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        headers.put("X-user-role", roles);
    }

    /*
    private void setUserHeaders(Map<String,List<String>> headers) {
        try {
            if (headers.get("Authorization") != null) {
                Map<String,List<String>> userHeaders = (Map<String,List<String>>)Kernel.getInstance().handleEvent(new Event("UserHeaders", "getUserHeaders", "", "", headers.get("Authorization").get(0)));
                headers.put("x-user-id", userHeaders.get("x-user-id"));
                headers.put("x-tenant-id", userHeaders.get("x-tenant-id"));
                headers.put("x-user-role", userHeaders.get("x-user-role"));
            }
        }catch (Exception e) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, e);
        }
    }
     */
    @Override
    public Headers getRequestHeaders() {
        return (Headers) headers;
    }

    @Override
    public Headers getResponseHeaders() {
        return httpExchange.getResponseHeaders();
    }

    @Override
    public URI getRequestURI() {
        return httpExchange.getRequestURI();
    }

    @Override
    public String getRequestMethod() {
        return httpExchange.getRequestMethod();
    }

    @Override
    public HttpContext getHttpContext() {
        return httpExchange.getHttpContext();
    }

    @Override
    public void close() {
        httpExchange.close();
    }

    @Override
    public InputStream getRequestBody() {
        return httpExchange.getRequestBody();
    }

    @Override
    public OutputStream getResponseBody() {
        return httpExchange.getResponseBody();
    }

    @Override
    public void sendResponseHeaders(int i, long l) throws IOException {
        httpExchange.sendResponseHeaders(i, l);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return httpExchange.getRemoteAddress();
    }

    @Override
    public int getResponseCode() {
        return httpExchange.getResponseCode();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return httpExchange.getLocalAddress();
    }

    @Override
    public String getProtocol() {
        return httpExchange.getProtocol();
    }

    @Override
    public Object getAttribute(String string) {
        //return httpExchange.getAttribute(string);
        return attributes.get(string);
    }

    @Override
    public void setAttribute(String string, Object o) {
        //httpExchange.setAttribute(string, o);
        attributes.put(string, o);
    }

    @Override
    public void setStreams(InputStream in, OutputStream out) {
        httpExchange.setStreams(in, out);
    }

    @Override
    public HttpPrincipal getPrincipal() {
        return httpExchange.getPrincipal();
    }

}
