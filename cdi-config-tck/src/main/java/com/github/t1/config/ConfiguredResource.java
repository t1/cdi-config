package com.github.t1.config;

import javax.ws.rs.*;

@Path("/configured")
public class ConfiguredResource {
    @Config
    String value;

    @GET
    public String getConfig() {
        return "[" + value + "]";
    }
}
