/*******************************************************************************
 * This file is part of Vitam Project.
 * <p>
 * Copyright Vitam (2012, 2015)
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated
 * by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 * <p>
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 * <p>
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you
 * accept its terms.
 *******************************************************************************/
package fr.gouv.vitam.worker.server.rest;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jhades.JHades;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.SystemPropertyUtil;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamApplicationServerException;
import fr.gouv.vitam.common.junit.JunitHelper;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.server.BasicVitamServer;
import fr.gouv.vitam.common.server.VitamServer;
import fr.gouv.vitam.common.server.VitamServerFactory;
import fr.gouv.vitam.processing.common.exception.HandlerNotFoundException;
import fr.gouv.vitam.processing.common.exception.ProcessingException;
import fr.gouv.vitam.processing.common.model.EngineResponse;
import fr.gouv.vitam.processing.common.model.ProcessResponse;
import fr.gouv.vitam.worker.core.api.Worker;
import fr.gouv.vitam.worker.core.impl.WorkerImpl;
import fr.gouv.vitam.worker.server.registration.WorkerRegistrationListener;

public class WorkerResourceTest {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(WorkerResourceTest.class);

    private static final String WORKER_RESOURCE_URI = "worker/v1";
    private static final String WORKER_STATUS_URI = "/status";
    private static final String WORKER_STEP_URI = "/tasks";


    private static VitamServer vitamServer;
    private static JunitHelper junitHelper;
    private static int serverPort;
    private static File newWorkerConf;


    private static Worker worker;

    private static final String BODY_TEST_NOT_JSON = "body_test";

    private static final String WORKER_CONF = "worker-test.conf";


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        worker = Mockito.mock(WorkerImpl.class);
        // Identify overlapping in particular jsr311
        new JHades().overlappingJarsReport();

        junitHelper = new JunitHelper();
        serverPort = junitHelper.findAvailablePort();

        final File workerFile = PropertiesUtils.findFile(WORKER_CONF);
        final WorkerConfiguration realWorker = PropertiesUtils.readYaml(workerFile, WorkerConfiguration.class);
        realWorker.setRegisterServerPort(serverPort).setRegisterServerHost("localhost")
            .setRegisterServerContextPath("/").setRegisterUseSSL(false).setRegisterDelay(1).setRegisterRetry(1)
            .setProcessingUrl("http://localhost:8888");

        newWorkerConf = File.createTempFile("test", WORKER_CONF, workerFile.getParentFile());
        PropertiesUtils.writeYaml(newWorkerConf, realWorker);

        // TODO verifier la compatibilité avec les tests parallèles sur jenkins
        SystemPropertyUtil.set(VitamServer.PARAMETER_JETTY_SERVER_PORT, Integer.toString(serverPort));

        RestAssured.port = serverPort;
        RestAssured.basePath = WORKER_RESOURCE_URI;


        final WorkerConfiguration newWorker = PropertiesUtils.readYaml(newWorkerConf, WorkerConfiguration.class);
        try {
            vitamServer = buildTestServer(newWorker);
            ((BasicVitamServer) vitamServer).start();
        } catch (final VitamApplicationServerException e) {
            LOGGER.error(e);
            throw new IllegalStateException(
                "Cannot start the Worker Application Server", e);
        }



    }

    public static VitamServer buildTestServer(WorkerConfiguration configuration)
        throws VitamApplicationServerException {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(new WorkerResource(configuration, worker));

        final ServletContainer servletContainer = new ServletContainer(resourceConfig);
        final ServletHolder sh = new ServletHolder(servletContainer);
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addServlet(sh, "/*");
        context.addEventListener(new WorkerRegistrationListener(configuration));
        String jettyConfig = configuration.getJettyConfig();
        vitamServer = VitamServerFactory.newVitamServerByJettyConf(jettyConfig);
        vitamServer.getServer().setHandler(context);
        return vitamServer;
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        LOGGER.debug("Ending tests");
        try {
            WorkerApplication.stop();
        } catch (final VitamApplicationServerException e) {
            LOGGER.error(e);
        }
        junitHelper.releasePort(serverPort);
        newWorkerConf.delete();
    }


    @Test
    public final void testGetStatus() {
        get(WORKER_STATUS_URI).then().statusCode(200);
    }

    @Test
    public final void testStepsList() {
        get(WORKER_STEP_URI).then()
            .statusCode(Status.NOT_IMPLEMENTED.getStatusCode());
    }

    @Test
    public final void testStepStatus() {
        get(WORKER_STEP_URI + "/idAsync").then()
            .statusCode(Status.NOT_IMPLEMENTED.getStatusCode());
    }

    @Test
    public final void testModifyStep() {
        given().contentType(ContentType.JSON).body("").when()
            .put(WORKER_STEP_URI + "/idAsync").then()
            .statusCode(Status.NOT_IMPLEMENTED.getStatusCode());
    }

    @Test
    public final void testSubmitEmptyStepThenBadRequest() {
        given().contentType(ContentType.JSON).body("").when()
            .post(WORKER_STEP_URI).then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public final void testSubmitIncorrectStepThenBadRequest() {
        given().contentType(ContentType.JSON).body(BODY_TEST_NOT_JSON).when()
            .post(WORKER_STEP_URI).then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public final void testSubmitStepOK()
        throws InvalidParseOperationException, IOException, HandlerNotFoundException, IllegalArgumentException,
        ProcessingException {
        ProcessResponse processResponse = new ProcessResponse();
        List<EngineResponse> responses = new ArrayList<>();
        responses.add(processResponse);
        Mockito.reset(worker);

        when(worker.run(anyObject(), anyObject())).thenReturn(responses);

        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("descriptionStep.json");
        String body = IOUtils.toString(stream);

        given().contentType(ContentType.JSON).body(body).when().post(WORKER_STEP_URI).then()
            .statusCode(Status.OK.getStatusCode());
    }

    @Test
    public final void testSubmitStepWrongHandler()
        throws InvalidParseOperationException, IOException, HandlerNotFoundException, IllegalArgumentException,
        ProcessingException {
        Mockito.reset(worker);
        when(worker.run(anyObject(), anyObject())).thenThrow(new HandlerNotFoundException(""));

        InputStream stream =
            Thread.currentThread().getContextClassLoader().getResourceAsStream("descriptionStep_wrong_handler.json");
        String body = IOUtils.toString(stream);

        given().contentType(ContentType.JSON).body(body).when().post(WORKER_STEP_URI).then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public final void testSubmitStepProcessingException()
        throws InvalidParseOperationException, IOException, HandlerNotFoundException, IllegalArgumentException,
        ProcessingException {
        Mockito.reset(worker);
        when(worker.run(anyObject(), anyObject())).thenThrow(new ProcessingException(""));

        InputStream stream =
            Thread.currentThread().getContextClassLoader().getResourceAsStream("descriptionStep_wrong_handler.json");
        String body = IOUtils.toString(stream);

        given().contentType(ContentType.JSON).body(body).when().post(WORKER_STEP_URI).then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

}
