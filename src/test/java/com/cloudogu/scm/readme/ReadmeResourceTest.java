/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.scm.readme;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.web.RestDispatcher;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadmeResourceTest {

  public static final String CONTENT = "content";
  public static final Optional<Readme> README = Optional.of(new Readme("master", CONTENT));
  private final ObjectMapper mapper = new ObjectMapper();
  private final MockHttpResponse response = new MockHttpResponse();
  @Mock
  ReadmeManager readmeManager;
  private ReadmeResource resource;
  private RestDispatcher dispatcher;

  @BeforeEach
  public void init() {
    resource = new ReadmeResource(readmeManager);
    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
  }

  @Test
  public void shouldGetReadme() throws URISyntaxException, IOException {
    when(readmeManager.getReadmeByRevisionAndPath("space", "repo", null, null)).thenReturn(README);
    MockHttpRequest request = MockHttpRequest.get("/" + ReadmeResource.PATH + "/space/repo");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    JsonNode jsonNode = mapper.readTree(response.getOutput());
    assertThat(jsonNode.get("revision").asText()).isEqualTo("master");
    assertThat(jsonNode.get(CONTENT).asText()).isEqualTo(CONTENT);
    assertThat(jsonNode.get("_links").get("self").get("href").asText()).isEqualTo("/v2/plugins/readme/space/repo");
  }

  @Test
  public void shouldGetNotFound() throws URISyntaxException {
    when(readmeManager.getReadmeByRevisionAndPath("space", "repo", null, null)).thenReturn(Optional.empty());
    MockHttpRequest request = MockHttpRequest.get("/" + ReadmeResource.PATH + "/space/repo");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  public void shouldGetForRevision() throws IOException, URISyntaxException {
    when(readmeManager.getReadmeByRevisionAndPath("space", "repo", "master", null)).thenReturn(README);
    MockHttpRequest request = MockHttpRequest.get("/" + ReadmeResource.PATH + "/space/repo/master");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    JsonNode jsonNode = mapper.readTree(response.getOutput());
    assertThat(jsonNode.get("revision").asText()).isEqualTo("master");
    assertThat(jsonNode.get(CONTENT).asText()).isEqualTo(CONTENT);
    assertThat(jsonNode.get("_links").get("self").get("href").asText()).isEqualTo("/v2/plugins/readme/space/repo/master");
  }

  @Test
  public void shouldGetNotFoundForRevision() throws URISyntaxException {
    when(readmeManager.getReadmeByRevisionAndPath("space", "repo", "master", null)).thenReturn(Optional.empty());
    MockHttpRequest request = MockHttpRequest.get("/" + ReadmeResource.PATH + "/space/repo/master");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  public void shouldGetForRevisionAndPath() throws IOException, URISyntaxException {
    when(readmeManager.getReadmeByRevisionAndPath("space", "repo", "master", "src/docs")).thenReturn(README);
    MockHttpRequest request = MockHttpRequest.get("/" + ReadmeResource.PATH + "/space/repo/master/src/docs");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    JsonNode jsonNode = mapper.readTree(response.getOutput());
    assertThat(jsonNode.get("revision").asText()).isEqualTo("master");
    assertThat(jsonNode.get(CONTENT).asText()).isEqualTo(CONTENT);
    assertThat(jsonNode.get("_links").get("self").get("href").asText()).isEqualTo("/v2/plugins/readme/space/repo/master/src/docs");
  }

  @Test
  public void shouldGetNotFoundForRevisionAndPath() throws URISyntaxException {
    when(readmeManager.getReadmeByRevisionAndPath("space", "repo", "master", "src/docs")).thenReturn(Optional.empty());
    MockHttpRequest request = MockHttpRequest.get("/" + ReadmeResource.PATH + "/space/repo/master/src/docs");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
  }
}
