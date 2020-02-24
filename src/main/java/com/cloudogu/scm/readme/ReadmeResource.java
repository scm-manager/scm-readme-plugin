package com.cloudogu.scm.readme;

import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(tags = {
  @Tag(name = "Readme Plugin", description = "Readme plugin provided endpoints")
})
@Slf4j
@Path(ReadmeResource.PATH)
public class ReadmeResource {

  public static final String PATH = "v2/plugins/readme";

  private final ReadmeManager readmeManager;

  @Inject
  public ReadmeResource(ReadmeManager readmeManager) {
    this.readmeManager = readmeManager;
  }

  @GET
  @Path("/{namespace}/{name}")
  @Operation(summary = "Get readme", description = "Returns the README.md.", tags = "Readme Plugin")
  @ApiResponse(responseCode = "200", description = "success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    return readmeManager.getReadmeContent(namespace, name)
      .map(content -> Response.ok(content).build())
      .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }
}
