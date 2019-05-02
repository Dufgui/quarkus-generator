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
import io.quarkus.cli.commands.writer.ZipWriter;
import io.quarkus.templates.SourceType;


@ApplicationScoped
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
            @QueryParam("cn") @DefaultValue("FruitResource") String className,
            @QueryParam("e") Set<String> extensions)
            throws Exception {
        // Remove empty values
        extensions.remove("");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            final Map<String, Object> context = new HashMap<>();
            final SourceType sourceType = CreateProject.determineSourceType(extensions);
            
            ZipWriter zipWrite = new ZipWriter(zos);
            boolean success = new CreateProject(zipWrite)
                    .groupId(groupId)
                    .artifactId(artifactId)
                    .version(projectVersion)
                    .sourceType(sourceType)
                    .className(className)
                    .doCreateProject(context);
    
            
            if (success) {
                new AddExtensions(zipWrite, "pom.xml")
                        .addExtensions(extensions);
            }
    
            //TODO
            //createMavenWrapper(zipWrite);
        }
        
//            zos.putNextEntry(new ZipEntry(artifactId + "/src/main/java/"));
//            zos.closeEntry();
//
//            zos.putNextEntry(new ZipEntry(artifactId + "/pom.xml"));
//            zos.write(engine.process("templates/pom.tl.xml", context).getBytes());
//            zos.closeEntry();
//
//            if (enableJAXRS(dependencies)) {
//                EndpointFilePathGenerator fpg = new EndpointFilePathGenerator(groupId, artifactId);
//                context.setVariable("endpointPackage", fpg.getEndpointPackage());
//                zos.putNextEntry(new ZipEntry(artifactId + fpg.getEndpointFilePath()));
//                zos.write(engine.process("templates/HelloWorldEndpoint.tl.java", context).getBytes());
//                zos.putNextEntry(new ZipEntry(artifactId + fpg.getApplicationPath()));
//                zos.write(engine.process("templates/RestApplication.tl.java", context).getBytes());
//                zos.closeEntry();
//            }
        

        return Response
                .ok(baos.toByteArray())
                .type("application/zip")
                .header("Content-Disposition", "attachment; filename=\"" + artifactId + ".zip\"")
                .build();
    }

    private boolean enableJAXRS(List<String> dependencies) {
        if (dependencies == null || dependencies.size() == 0) {
            return true;
        }
        return dependencies.stream().anyMatch(d -> d.contains("jaxrs") || d.contains("microprofile"));
    }

}
