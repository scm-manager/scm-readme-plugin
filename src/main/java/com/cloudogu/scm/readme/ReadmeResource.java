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

import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

import java.net.URI;

@OpenAPIDefinition(tags = {
  @Tag(name = "Readme Plugin", description = "Readme plugin provided endpoints")
})
@Slf4j
@Path(ReadmeResource.PATH)
public class ReadmeResource {

  public static final String PATH = "v2/plugins/readme";
  static final String MEDIA_TYPE = VndMediaType.PREFIX + "readme" + VndMediaType.SUFFIX;
  private final ReadmeManager readmeManager;

  @Inject
  public ReadmeResource(ReadmeManager readmeManager) {
    this.readmeManager = readmeManager;
  }

  @GET
  @Path("/{namespace}/{name}")
  @Produces(MEDIA_TYPE)
  @Operation(summary = "Get readme", description = "Returns the README.md.", tags = "Readme Plugin", operationId = "readme_get_readme")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MEDIA_TYPE,
      schema = @Schema(implementation = ReadmeDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(responseCode = "404", description = "repository does not have a readme")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response get(@Context UriInfo uriInfo, @PathParam("namespace") String namespace, @PathParam("name") String name) {
    return getReadme(uriInfo, namespace, name, null, null);
  }

  @GET
  @Path("/{namespace}/{name}/{revision}")
  @Produces(MEDIA_TYPE)
  public Response getByRevision(@Context UriInfo uriInfo,
                                @PathParam("namespace") String namespace,
                                @PathParam("name") String name,
                                @PathParam("revision") String revision) {
    return getReadme(uriInfo, namespace, name, revision, null);
  }

  @GET
  @Path("/{namespace}/{name}/{revision}/{path:.*}")
  @Produces(MEDIA_TYPE)
  public Response getByRevisionAndPath(@Context UriInfo uriInfo,
                                       @PathParam("namespace") String namespace,
                                       @PathParam("name") String name,
                                       @PathParam("revision") String revision,
                                       @PathParam("path") String path) {
    return getReadme(uriInfo, namespace, name, revision, path);
  }

  private Response getReadme(UriInfo uriInfo, String namespace, String name, String revision, String path) {
    URI uri = uriInfo.getAbsolutePath();
    return readmeManager.getReadmeByRevisionAndPath(namespace, name, revision, path)
      .map(readme -> new ReadmeDto(readme, uri.toASCIIString()))
      .map(dto -> Response.ok(dto).build())
      .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }
}
