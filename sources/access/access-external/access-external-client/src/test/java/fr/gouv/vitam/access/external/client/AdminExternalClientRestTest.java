package fr.gouv.vitam.access.external.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.access.external.api.AdminCollections;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientNotFoundException;
import fr.gouv.vitam.common.client.ClientMockResultHelper;
import fr.gouv.vitam.common.exception.VitamApplicationServerException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.server.application.AbstractVitamApplication;
import fr.gouv.vitam.common.server.application.configuration.DefaultVitamApplicationConfiguration;
import fr.gouv.vitam.common.server.application.junit.VitamJerseyTest;

public class AdminExternalClientRestTest extends VitamJerseyTest {

    private static final String ID = "id";
    protected static final String HOSTNAME = "localhost";
    protected AdminExternalClientRest client;

    final String queryDsql =
        "{ \"$query\" : [ { \"$eq\" : { \"title\" : \"test\" } } ]}";

    public AdminExternalClientRestTest() {
        super(AdminExternalClientFactory.getInstance());
    }

    @Override
    public void beforeTest() throws VitamApplicationServerException {
        client = (AdminExternalClientRest) getClient();
    }

    @Override
    public StartApplicationResponse<AbstractApplication> startVitamApplication(int reservedPort) {
        final TestVitamApplicationConfiguration configuration = new TestVitamApplicationConfiguration();
        configuration.setJettyConfig(DEFAULT_XML_CONFIGURATION_FILE);
        final AbstractApplication application = new AbstractApplication(configuration);
        try {
            application.start();
        } catch (final VitamApplicationServerException e) {
            throw new IllegalStateException("Cannot start the application", e);
        }
        return new StartApplicationResponse<AbstractApplication>()
            .setServerPort(application.getVitamServer().getPort())
            .setApplication(application);
    }

    // Define your Application class if necessary
    public final class AbstractApplication
        extends AbstractVitamApplication<AbstractApplication, TestVitamApplicationConfiguration> {
        protected AbstractApplication(TestVitamApplicationConfiguration configuration) {
            super(TestVitamApplicationConfiguration.class, configuration);
        }

        @Override
        protected void registerInResourceConfig(ResourceConfig resourceConfig) {
            resourceConfig.registerInstances(new MockResource(mock));
        }
    }
    // Define your Configuration class if necessary
    public static class TestVitamApplicationConfiguration extends DefaultVitamApplicationConfiguration {
    }

    @Path("/admin-external/v1")
    public static class MockResource {
        private final ExpectedResults expectedResponse;

        public MockResource(ExpectedResults expectedResponse) {
            this.expectedResponse = expectedResponse;
        }

        @PUT
        @Path("{collections}")
        @Consumes(MediaType.APPLICATION_OCTET_STREAM)
        @Produces(MediaType.APPLICATION_JSON)
        public Response checkDocument(@PathParam("collection") String collection, InputStream document) {
            return expectedResponse.put();
        }

        @POST
        @Path("{collections}")
        @Consumes(MediaType.APPLICATION_OCTET_STREAM)
        @Produces(MediaType.APPLICATION_JSON)
        public Response importDocument(@PathParam("collection") String collection, InputStream document) {
            return expectedResponse.post();
        }

        @DELETE
        @Path("{collections}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response deleteDocuments(@PathParam("collection") String collection) {
            return expectedResponse.delete();
        }


        @POST
        @Path("{collections}")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response findDocuments(@PathParam("collection") String collection, JsonNode select) {
            return expectedResponse.post();
        }


        @POST
        @Path("/{collections}/{id_document}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response findDocumentByID(@PathParam("collection") String collection,
            @PathParam("id_document") String documentId) {
            return expectedResponse.get();
        }

    }

    @Test
    public void testCheckDocument()
        throws Exception {
        when(mock.put()).thenReturn(Response.status(Status.OK).build());
        assertEquals(
            client.checkDocuments(AdminCollections.FORMATS, new ByteArrayInputStream("test".getBytes())),
            Status.OK);
    }

    @Test(expected = AccessExternalClientNotFoundException.class)
    public void testCheckDocumentAccessExternalClientNotFoundException()
        throws Exception {
        when(mock.put()).thenReturn(Response.status(Status.NOT_FOUND).build());
        client.checkDocuments(AdminCollections.FORMATS, new ByteArrayInputStream("test".getBytes()));
    }

    @Test(expected = AccessExternalClientException.class)
    public void testCheckDocumentAccessExternalClientException()
        throws Exception {
        when(mock.put()).thenReturn(Response.status(Status.PRECONDITION_FAILED).build());
        client.checkDocuments(AdminCollections.FORMATS, new ByteArrayInputStream("test".getBytes()));
    }

    @Test
    public void testImportDocument()
        throws Exception {
        when(mock.post()).thenReturn(Response.status(Status.OK).build());
        assertEquals(
            client.createDocuments(AdminCollections.FORMATS, new ByteArrayInputStream("test".getBytes())),
            Status.OK);
    }

    @Test(expected = AccessExternalClientNotFoundException.class)
    public void testImportDocumentAccessExternalClientNotFoundException()
        throws Exception {
        when(mock.post()).thenReturn(Response.status(Status.NOT_FOUND).build());
        client.createDocuments(AdminCollections.FORMATS, new ByteArrayInputStream("test".getBytes()));
    }

    @Test(expected = AccessExternalClientException.class)
    public void testImportDocumentAccessExternalClientException()
        throws Exception {
        when(mock.post()).thenReturn(Response.status(Status.PRECONDITION_FAILED).build());
        client.createDocuments(AdminCollections.FORMATS, new ByteArrayInputStream("test".getBytes()));
    }

    @Test
    public void testFindDocuments()
        throws Exception {
        when(mock.post()).thenReturn(Response.status(Status.OK).entity(ClientMockResultHelper.getFormatList()).build());
        assertEquals(client.findDocuments(AdminCollections.FORMATS, JsonHandler.createObjectNode()).toString(),
            ClientMockResultHelper.getFormatList().toString());
    }

    @Test(expected = AccessExternalClientNotFoundException.class)
    public void testFindDocumentAccessExternalClientNotFoundException()
        throws Exception {
        when(mock.post()).thenReturn(Response.status(Status.NOT_FOUND).build());
        client.findDocuments(AdminCollections.FORMATS, JsonHandler.createObjectNode());
    }

    @Test(expected = AccessExternalClientException.class)
    public void testFindDocumentAccessExternalClientException()
        throws Exception {
        when(mock.post()).thenReturn(Response.status(Status.PRECONDITION_FAILED).build());
        client.findDocuments(AdminCollections.FORMATS, JsonHandler.createObjectNode());
    }

    @Test
    public void testFindDocumentById()
        throws Exception {
        when(mock.get()).thenReturn(Response.status(Status.OK).entity(ClientMockResultHelper.getFormat()).build());
        assertEquals(
            client.findDocumentById(AdminCollections.FORMATS, ID).toString(),
            ClientMockResultHelper.getFormat().toString());
    }

    @Test(expected = AccessExternalClientNotFoundException.class)
    public void testFindDocumentByIdAccessExternalClientNotFoundException()
        throws Exception {
        when(mock.get()).thenReturn(Response.status(Status.NOT_FOUND).build());
        client.findDocumentById(AdminCollections.FORMATS, ID);
    }

    @Test(expected = AccessExternalClientException.class)
    public void testFindDocumentByIdAccessExternalClientException()
        throws Exception {
        when(mock.get()).thenReturn(Response.status(Status.PRECONDITION_FAILED).build());
        client.findDocumentById(AdminCollections.FORMATS, ID);
    }


}
