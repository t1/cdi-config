package com.github.t1.config;

import static javax.ws.rs.core.MediaType.*;

import java.io.Serializable;
import java.util.List;

import javax.inject.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Path("/configured")
public class ConfiguredResource implements Serializable {
    private static final long serialVersionUID = 1L;

    private static int nextId = 0;
    private final int id = nextId++;

    @Config(name = "config-key", description = "the value to be configured", defaultValue = "default-value",
            meta = "{'meta-string':'meta-value','meta-flag':true}")
    volatile String value;

    @GET
    public Response getConfig(@QueryParam("sleep") @DefaultValue("5000") long sleep) {
        log.info("before");
        try {
            Thread.sleep(sleep);
            return Response.ok("[" + id + " : " + value + "]\n", TEXT_PLAIN).build();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            log.info("after");
        }
    }

    @Inject
    List<ConfigInfo> configs;

    @GET
    @Path("/all")
    public Response getAllConfigs() {
        return Response.ok(configs.toString(), TEXT_PLAIN).build();
    }
}
