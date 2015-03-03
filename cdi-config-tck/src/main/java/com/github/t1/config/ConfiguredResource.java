package com.github.t1.config;

import static javax.ws.rs.core.MediaType.*;

import java.io.Serializable;

import javax.inject.Singleton;
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

    @Config(name = "config-key")
    volatile String value;

    @GET
    public Response getConfig() {
        log.info("before");
        try {
            Thread.sleep(5000);
            return Response.ok("[" + id + " : " + value + "]\n", TEXT_PLAIN).build();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            log.info("after");
        }
    }
}
