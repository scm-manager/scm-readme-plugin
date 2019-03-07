package com.cloudogu.scm.readme;

import com.google.inject.Inject;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

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
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    return readmeManager.getReadmeContent(namespace, name)
      .map(content -> Response.ok(content).build())
      .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }
}
