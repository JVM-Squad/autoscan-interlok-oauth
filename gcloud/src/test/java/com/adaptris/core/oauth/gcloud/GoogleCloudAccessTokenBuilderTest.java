package com.adaptris.core.oauth.gcloud;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.http.oauth.AccessToken;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.text.DateFormatUtil;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class GoogleCloudAccessTokenBuilderTest {

  @Test
  public void testConstruct() throws Exception {
    GoogleCloudAccessTokenBuilder service = new GoogleCloudAccessTokenBuilder();
    assertNotNull(service.getCredentials());
    assertTrue(service.getCredentials() instanceof ApplicationDefaultCredentials);
    service = new GoogleCloudAccessTokenBuilder(new KeyFileCredentials());
    assertNotNull(service.getCredentials());
    assertTrue(service.getCredentials() instanceof KeyFileCredentials);
  }


  @Test
  public void testBuild() throws Exception {
    AdaptrisMessage msg =  AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    GoogleCloudAccessTokenBuilder service = new GoogleCloudAccessTokenBuilder();
    Credentials credentials = Mockito.spy(new StubCredentials());
    service.setCredentials(credentials);
    LifecycleHelper.initAndStart(service);
    AccessToken accessToken = service.build(msg);
    LifecycleHelper.stopAndClose(service);
    assertEquals(accessToken.getToken(), StubCredentials.ACCESS_TOKEN);
    assertEquals(accessToken.getExpiry(), DateFormatUtil.format(StubCredentials.EXPIRATION));
    Mockito.verify(credentials, Mockito.times(1)).init();
    Mockito.verify(credentials, Mockito.times(1)).start();
    Mockito.verify(credentials, Mockito.times(1)).stop();
    Mockito.verify(credentials, Mockito.times(1)).close();
  }

}