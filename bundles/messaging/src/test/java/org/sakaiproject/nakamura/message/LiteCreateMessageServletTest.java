/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.message;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.testing.sling.MockResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.SessionAdaptable;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.sakaiproject.nakamura.api.message.LiteMessagingService;
import org.sakaiproject.nakamura.api.message.MessageConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public class LiteCreateMessageServletTest {

  private LiteMessagingService messagingService;
  private LiteCreateMessageServlet servlet;

  @Before
  public void setUp() {
    servlet = new LiteCreateMessageServlet();
    messagingService = mock(LiteMessagingService.class);

    servlet.messagingService = messagingService;
  }

  @After
  public void tearDown() {
    servlet.messagingService = null;
  }

  @Test
  public void testAnon() throws ServletException, IOException {
    SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
    SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);
    Session session = mock(Session.class);
    ResourceResolver resolver = mock(ResourceResolver.class);
    javax.jcr.Session jcrSession = mock(javax.jcr.Session.class, Mockito.withSettings().extraInterfaces(SessionAdaptable.class));
    when(((SessionAdaptable)jcrSession).getSession()).thenReturn(session);
    when(resolver.adaptTo(javax.jcr.Session.class)).thenReturn(jcrSession);
    
    when(request.getResourceResolver()).thenReturn(resolver);
    when(request.getRemoteUser()).thenReturn("anonymous");

    servlet.doPost(request, response);

    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
        "Anonymous users can't send messages.");
  }

  @Test
  public void testNoType() throws ServletException, IOException {
    SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
    SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);
    Session session = mock(Session.class);
    ResourceResolver resolver = mock(ResourceResolver.class);
    
    javax.jcr.Session jcrSession = mock(javax.jcr.Session.class, Mockito.withSettings().extraInterfaces(SessionAdaptable.class));
    when(((SessionAdaptable)jcrSession).getSession()).thenReturn(session);
    when(resolver.adaptTo(javax.jcr.Session.class)).thenReturn(jcrSession);

    when(resolver.adaptTo(javax.jcr.Session.class)).thenReturn(jcrSession);
    when(request.getResourceResolver()).thenReturn(resolver);

    when(request.getRemoteUser()).thenReturn("foo");
    servlet.doPost(request, response);
    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST,
        "No type for this message specified.");
  }

  public RequestParameter createParam(String value) {
    RequestParameter param = mock(RequestParameter.class);
    when(param.getString()).thenReturn(value);
    return param;
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testProper() throws Exception {
    SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
    SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintWriter w = new PrintWriter(baos);
    when(response.getWriter()).thenReturn(w);

    when(request.getRemoteUser()).thenReturn("admin");

    // Request paramaters
    Map<String, RequestParameter[]> map = new HashMap<String, RequestParameter[]>();
    RequestParameter typeParam = createParam("internal");
    RequestParameter toParam = createParam("internal:anon");
    RequestParameter fooParam1 = createParam("alfa");
    RequestParameter fooParam2 = createParam("beta");

    map.put("sakai:type", new RequestParameter[] { typeParam });
    map.put("sakai:to", new RequestParameter[] { typeParam });
    map.put("foo", new RequestParameter[] { fooParam1, fooParam2 });

    RequestParameterMap rpMap = mock(RequestParameterMap.class);
    when(rpMap.entrySet()).thenReturn(map.entrySet());

    when(request.getRequestParameter(MessageConstants.PROP_SAKAI_TO)).thenReturn(toParam);
    when(request.getRequestParameter(MessageConstants.PROP_SAKAI_TYPE)).thenReturn(
        typeParam);
    when(request.getRequestParameterMap()).thenReturn(rpMap);
    RequestDispatcher dispatcher = mock(RequestDispatcher.class);
    when(
        request.getRequestDispatcher(Matchers.any(Resource.class), Matchers
            .any(RequestDispatcherOptions.class))).thenReturn(dispatcher);

    // JCR
    Session session = mock(Session.class);
    ContentManager contentManager = mock(ContentManager.class);
    when(session.getContentManager()).thenReturn(contentManager);
    ResourceResolver resolver = mock(ResourceResolver.class);
    Resource resource = new MockResource(resolver, "/_user/message", "sakai/messagestore");
    
    javax.jcr.Session jcrSession = mock(javax.jcr.Session.class, Mockito.withSettings().extraInterfaces(SessionAdaptable.class));
    when(((SessionAdaptable)jcrSession).getSession()).thenReturn(session);
    when(resolver.adaptTo(javax.jcr.Session.class)).thenReturn(jcrSession);
    
    when(request.getResource()).thenReturn(resource);
    when(request.getResourceResolver()).thenReturn(resolver);
    Content msgNode = new Content("/_user/message/bla/bla/admin/bla/bla/msg", null);
    msgNode.setProperty(MessageConstants.PROP_SAKAI_ID, "someid");
    when(messagingService.create(Matchers.eq(session), Matchers.anyMap())).thenReturn(
        msgNode);
    when(contentManager.get("/_user/message/bla/bla/admin/bla/bla/msg")).thenReturn(msgNode);

    servlet.doPost(request, response);

    w.flush();
    String s = baos.toString("UTF-8");
    JSONObject o = new JSONObject(s);
    assertEquals("someid", o.getString("id"));;
  }
}
