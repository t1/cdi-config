package com.github.t1.config;

import static javax.ws.rs.core.MediaType.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/configured")
public class ConfiguredResource {
    @Config(name = "config-key")
    volatile String value;

    @GET
    public Response getConfig() {
        return Response.ok("[" + value + "]\n", TEXT_PLAIN).build();
    }
}
