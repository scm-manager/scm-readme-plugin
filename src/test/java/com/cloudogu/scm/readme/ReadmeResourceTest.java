package com.cloudogu.scm.readme;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadmeResourceTest {

  public static final Optional<String> CONTENT = Optional.of("content");
  private ReadmeResource resource;

  @Mock
  ReadmeManager readmeManager;

  private Dispatcher dispatcher;
  private final MockHttpResponse response = new MockHttpResponse();


  @BeforeEach
  public void init() {
    resource = new ReadmeResource(readmeManager);
    dispatcher = MockDispatcherFactory.createDispatcher();
    dispatcher.getRegistry().addSingletonResource(resource);
  }

  @Test
  public void shouldGetReadmeContent() throws URISyntaxException, UnsupportedEncodingException {
    when(readmeManager.getReadmeContent("space", "repo")).thenReturn(CONTENT);
    MockHttpRequest request = MockHttpRequest
      .get("/" + ReadmeResource.PATH + "/space/repo")
      .accept(MediaType.APPLICATION_JSON);

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).isEqualTo(CONTENT.get());
  }

  @Test
  public void shouldGetNotFound() throws URISyntaxException {
    when(readmeManager.getReadmeContent("space", "repo")).thenReturn(Optional.ofNullable(null));
    MockHttpRequest request = MockHttpRequest
      .get("/" + ReadmeResource.PATH + "/space/repo")
      .accept(MediaType.APPLICATION_JSON);

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
  }

}
