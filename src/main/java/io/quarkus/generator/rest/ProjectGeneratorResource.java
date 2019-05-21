package io.quarkus.generator.rest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import io.quarkus.cli.commands.AddExtensions;
import io.quarkus.cli.commands.CreateProject;
import io.quarkus.cli.commands.writer.ZipProjectWriter;
import io.quarkus.templates.SourceType;

@Path("/generator")
public class ProjectGeneratorResource {
    private static final String QUARKUS_VERSION = "999-SNAPSHOT";

    public ProjectGeneratorResource() {
    }

    @GET
    @Produces("application/zip")
    public Response generate(
            @QueryParam("qv") @DefaultValue(QUARKUS_VERSION) String quarkusVersion,
            @QueryParam("g") @DefaultValue("com.example") @NotNull(message = "Parameter 'g' (Group Id) must not be null") String groupId,
            @QueryParam("a") @DefaultValue("demo") @NotNull(message = "Parameter 'a' (Artifact Id) must not be null") String artifactId,
            @QueryParam("pv") @DefaultValue("0.0.1-SNAPSHOT") String projectVersion,
            @QueryParam("cn") @DefaultValue("FruitResource") @NotNull(message = "Parameter 'cn' (Class name) must not be null") String className,
            @QueryParam("e") Set<String> extensions)
            throws Exception {
        
        Set<String> cleanedExtensions = new HashSet<>(extensions);
        cleanedExtensions.remove("");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            final Map<String, Object> context = new HashMap<>();
            final SourceType sourceType = CreateProject.determineSourceType(extensions);
            
            ZipProjectWriter zipWrite = new ZipProjectWriter(zos);
            boolean success = new CreateProject(zipWrite)
                    .groupId(groupId)
                    .artifactId(artifactId)
                    .version(projectVersion)
                    .sourceType(sourceType)
                    .className(className)
                    .doCreateProject(context);
    
            
            if (success) {
                new AddExtensions(zipWrite, "pom.xml")
                        .addExtensions(cleanedExtensions);
            }
    
            //TODO
            //createMavenWrapper(zipWrite);
        }

        return Response
                .ok(baos.toByteArray())
                .type("application/zip")
                .header("Content-Disposition", "attachment; filename=\"" + artifactId + ".zip\"")
                .build();
    }

}
