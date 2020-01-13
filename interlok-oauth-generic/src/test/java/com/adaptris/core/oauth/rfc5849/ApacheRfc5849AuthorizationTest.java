/*
 * Copyright 2019 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.core.oauth.rfc5849;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.http.apache.HttpRequestService;
import com.adaptris.core.http.apache.NoConnectionManagement;

public class ApacheRfc5849AuthorizationTest extends ServiceCase {
  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testAuthorization_Exception() throws Exception {
    HttpRequestService service =
        new HttpRequestService("http://localhost");
    ApacheRfc5849Authenticator auth = new ApacheRfc5849Authenticator();
    service.setAuthenticator(auth);
    service.setClientConfig(new NoConnectionManagement());
    service.setMethod("POST");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("Hello World");
    try {
      ServiceCase.execute(service, msg);
      fail();
    } catch (ServiceException expected) {
    }
  }

  @Test
  public void testAuthorization() throws Exception {
    EmbeddedUndertow undertow = new EmbeddedUndertow();
    HttpRequestService service =
        new HttpRequestService("http://localhost:" + undertow.getPort() + "/index.blah");
    ApacheRfc5849Authenticator auth = new ApacheRfc5849Authenticator();
    AuthorizationDataTest.configure(auth.getAuthorizationData());
    service.setAuthenticator(auth);
    service.setClientConfig(new NoConnectionManagement());
    service.setMethod("POST");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("Hello World");
    try {
      undertow.start();
      ServiceCase.execute(service, msg);
    } finally {
      undertow.shutdown();
    }
    assertEquals(1, undertow.getMessages().size());
    AdaptrisMessage m2 = undertow.getMessages().get(0);
    assertEquals("POST", m2.getMetadataValue(CoreConstants.HTTP_METHOD));
    assertNotNull(m2.getMetadataValue("Authorization"));
    assertTrue(m2.getMetadataValue("Authorization").startsWith("OAuth"));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    HttpRequestService service =
        new HttpRequestService("https://my.server/path/to/oauth/resource?a=1&b=2");
    ApacheRfc5849Authenticator auth = new ApacheRfc5849Authenticator();
    AuthorizationDataTest.configure(auth.getAuthorizationData());
    service.setAuthenticator(auth);
    return service;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return object.getClass().getName() + "-OAUTH-RFC5849";
  }
}