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
package org.cricketmsf.microsite.in.http;

import com.cedarsoftware.util.io.JsonReader;
import java.util.List;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.microsite.cms.CmsException;
import org.cricketmsf.microsite.cms.CmsIface;
import org.cricketmsf.microsite.cms.Document;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class ContentRequestProcessor {

    private static String ADMIN = "admin";
    private static String REDACTOR = "redactor";

    private boolean hasAccessRights(String userID, List<String> roles) {
        if (userID == null || userID.isEmpty()) {
            return false;
        }
        if (roles.contains(ADMIN) || roles.contains(REDACTOR)) {
            return true;
        } else {
            return false;
        }
    }

    public Object processRequest(Event event, CmsIface adapter) {
        String method = event.getRequest().method.toUpperCase();
        switch (method) {
            case "GET":
                return processGet(event, adapter);
            case "POST":
                return processPost(event, adapter);
            case "PUT":
                return processPut(event, adapter);
            case "DELETE":
                return processDelete(event, adapter);
            default:
                StandardResult result = new StandardResult();
                result.setCode(HttpAdapter.SC_METHOD_NOT_ALLOWED);
                return result;
        }
    }

    public Object processGetPublished(Event event, CmsIface adapter) {
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        String language = (String) request.parameters.getOrDefault("language", "");

        String pathExt = request.pathExt;
        Document doc;
        if (pathExt != null && !pathExt.isEmpty()) {
            try {
                doc = adapter.getDocument("/" + pathExt, language, "published");
                if (doc != null) {
                    if (doc.getType() == Document.FILE) {
                        doc.setContent("*****");
                    }
                    System.out.println("DOCUMENT2 "+doc.getPublished());
                    result.setData(doc);
                } else {
                    result.setCode(HttpAdapter.SC_NOT_FOUND);
                }
            } catch (CmsException ex) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
                result.setCode(HttpAdapter.SC_NOT_FOUND);
                result.setMessage(ex.getMessage());
                result.setData(ex.getMessage());
            }
        } else {
            //find
            String path = (String) request.parameters.getOrDefault("path", "");
            try {
                result.setData(adapter.findByPath(path, language, "published"));
            } catch (CmsException ex) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
                result.setCode(HttpAdapter.SC_NOT_FOUND);
                result.setMessage(ex.getMessage());
                result.setData(ex.getMessage());
            }
        }
        return result;
    }

    public Object processGet(Event event, CmsIface adapter) {
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        String userID = request.headers.getFirst("X-user-id");
        List<String> roles = request.headers.get("X-user-role");

        String requiredStatus = (String) request.parameters.get("status");
        String language = (String) request.parameters.getOrDefault("language", "");

        if ("wip".equals(requiredStatus) && !hasAccessRights(userID, roles)) {
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            return result;
        }

        String pathExt = request.pathExt;
        Document doc;
        if (pathExt != null && !pathExt.isEmpty()) {
            String uid = "/" + pathExt;
            try {
                doc = adapter.getDocument(uid, language, requiredStatus);
                if (doc != null) {
                    result.setData(doc);
                } else {
                    result.setCode(HttpAdapter.SC_NOT_FOUND);
                    System.out.println("doc is null");
                }
            } catch (CmsException ex) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
                result.setCode(HttpAdapter.SC_NOT_FOUND);
                result.setMessage(ex.getMessage());
            }
        } else {
            //find
            String path = (String) request.parameters.getOrDefault("path", "");
            String pathsOnly = (String) request.parameters.getOrDefault("pathsonly", "false");
            try {
                if ("true".equalsIgnoreCase(pathsOnly)) {
                    result.setData(adapter.getPaths());
                } else {
                    result.setData(adapter.findByPath(path, language, requiredStatus));
                }
            } catch (CmsException ex) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
                result.setCode(HttpAdapter.SC_NOT_FOUND);
                result.setMessage(ex.getMessage());
                result.setData(ex.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public Object processPost(Event event, CmsIface adapter) {
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        String userID = request.headers.getFirst("X-user-id");
        List<String> roles = request.headers.get("X-user-role");

        if (!hasAccessRights(userID, roles)) {
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            return result;
        }

        String contentType = request.headers.getFirst("Content-Type");
        try {
            if ("application/json".equalsIgnoreCase(contentType)) {
                String jsonString = request.body;
                //System.out.println(jsonString);
                jsonString
                        = "{\"@type\":\"org.cricketmsf.microsite.cms.Document\","
                        + jsonString.substring(jsonString.indexOf("{") + 1);

                Document doc = null;
                try {
                    doc = (Document) JsonReader.jsonToJava(jsonString);
                } catch (Exception e) {
                    Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), "deserialization problem - check @type declaration"));
                    e.printStackTrace();
                }
                try {
                    if (doc != null) {
                        doc.validateUid(); // prepend doc.uid with "/" if needed and update doc.path
                        doc.setStatus("wip");
                        doc.setCreatedBy(userID);
                        // make sure that content is allways Base64 encoded
                        doc.setContent(doc.getContent());
                        try {
                            adapter.addDocument(doc);
                        } catch (CmsException ex) {
                            Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), ex.getMessage()));
                        }
                        result.setData(doc);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    adapter.addDocument(request.parameters, userID);
                    result.setData(adapter.getDocument((String) request.parameters.get("uid"), (String) request.parameters.get("language")));
                } catch (CmsException ex) {
                    Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), ex.getMessage()));
                    if (ex.getCode() >= 400 && ex.getCode() < 600) {
                        result.setCode(ex.getCode());
                    } else {
                        result.setCode(HttpAdapter.SC_BAD_REQUEST);
                    }
                    result.setMessage(ex.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object processPut(Event event, CmsIface adapter) {

        //TODO: implement document update
        StandardResult result = new StandardResult();
        try {
            RequestObject request = event.getRequest();

            String userID = request.headers.getFirst("X-user-id");
            List<String> roles = request.headers.get("X-user-role");
            if (!hasAccessRights(userID, roles)) {
                result.setCode(HttpAdapter.SC_FORBIDDEN);
                return result;
            }

            String uid = "/" + event.getRequest().pathExt;
            if (uid == null || uid.isEmpty()) {
                result.setCode(HttpAdapter.SC_NOT_FOUND);
                return result;
            }

            String contentType = request.headers.getFirst("Content-Type");

            Document doc = null;

            if ("application/json".equalsIgnoreCase(contentType)) {
                //create new document and adapter.modify(document)
                String jsonString = request.body;
                jsonString
                        = "{\"@type\":\"org.cricketmsf.microsite.cms.Document\","
                        + jsonString.substring(jsonString.indexOf("{") + 1);

                try {
                    doc = (Document) JsonReader.jsonToJava(jsonString);
                    doc.setUid(uid); //overwrite uid and path from JSON representation
                } catch (Exception e) {
                    Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), "deserialization problem - check @type declaration"));
                    e.printStackTrace();
                    result.setCode(HttpAdapter.SC_BAD_REQUEST);
                    return result;
                }
                try {
                    adapter.updateDocument(doc);
                    result.setData(doc);
                } catch (CmsException ex) {
                    Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), ex.getMessage()));
                    result.setCode(HttpAdapter.SC_BAD_REQUEST);
                }
            } else {
                try {
                    adapter.updateDocument(uid, (String) request.parameters.get("language"), request.parameters);
                    result.setData(adapter.getDocument(uid, (String) request.parameters.get("language")));
                } catch (CmsException ex) {
                    Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), ex.getMessage()));
                    result.setCode(HttpAdapter.SC_BAD_REQUEST);
                }
                //read original document, update parameters and adapter.modify(original)

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object processDelete(Event event, CmsIface adapter) {
        //TODO: implement document removal
        StandardResult result = new StandardResult();
        RequestObject request = event.getRequest();

        String userID = request.headers.getFirst("X-user-id");
        List<String> roles = request.headers.get("X-user-role");
        if (!hasAccessRights(userID, roles)) {
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            return result;
        }

        String uid = "/" + event.getRequest().pathExt;
        if (uid == null || uid.isEmpty()) {
            result.setCode(HttpAdapter.SC_NOT_FOUND);
            return result;
        }
        try {
            adapter.removeDocument(uid);
        } catch (CmsException ex) {
            result.setCode(HttpAdapter.SC_NOT_FOUND);
            result.setData(ex.getMessage());
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
        }
        return result;
    }

}
