package com.cloudogu.scm.readme;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path(ReadmeResource.PATH)
public class ReadmeResource {

  public static final String PATH = "v2/plugins/readme";


  @GET
  @Path("/{namespace}/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get(@Context UriInfo uriInfo, @PathParam("namespace") String namespace, @PathParam("name") String name) {
    return Response.ok().build();
  }
}
