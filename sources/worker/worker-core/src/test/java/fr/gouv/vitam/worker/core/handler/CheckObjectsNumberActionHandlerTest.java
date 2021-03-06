/*******************************************************************************
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 *******************************************************************************/
package fr.gouv.vitam.worker.core.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.gouv.vitam.common.guid.GUIDFactory;
import fr.gouv.vitam.common.model.ItemStatus;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.processing.common.exception.ProcessingException;
import fr.gouv.vitam.processing.common.parameter.WorkerParameters;
import fr.gouv.vitam.processing.common.parameter.WorkerParametersFactory;
import fr.gouv.vitam.worker.common.utils.ExtractUriResponse;
import fr.gouv.vitam.worker.common.utils.SedaUtils;
import fr.gouv.vitam.worker.common.utils.SedaUtilsFactory;
import fr.gouv.vitam.worker.core.impl.HandlerIOImpl;
import fr.gouv.vitam.workspace.api.exception.ContentAddressableStorageServerException;
import fr.gouv.vitam.workspace.client.WorkspaceClient;
import fr.gouv.vitam.workspace.client.WorkspaceClientFactory;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*")
@PrepareForTest({WorkspaceClientFactory.class, SedaUtilsFactory.class})
public class CheckObjectsNumberActionHandlerTest {

    private CheckObjectsNumberActionHandler checkObjectsNumberActionHandler;
    private static final String HANDLER_ID = "CHECK_MANIFEST_OBJECTNUMBER";

    private WorkerParameters workParams;

    private SedaUtils sedaUtils;

    private WorkspaceClient workspaceClient;
    private WorkspaceClientFactory workspaceClientFactory;

    private final List<URI> uriDuplicatedListManifestKO = new ArrayList<>();
    private final List<URI> uriListManifestOK = new ArrayList<>();
    private final List<URI> uriOutNumberListManifestKO = new ArrayList<>();

    private final List<URI> uriListWorkspaceOK = new ArrayList<>();
    private final List<URI> uriOutNumberListWorkspaceKO = new ArrayList<>();

    private ExtractUriResponse extractUriResponseOK;
    private ExtractUriResponse extractDuplicatedUriResponseKO;
    private ExtractUriResponse extractOutNumberUriResponseKO;

    private final List<String> messages = new ArrayList<>();
    HandlerIOImpl handlerIO;


    @Before
    public void setUp() throws Exception {
        workParams = WorkerParametersFactory.newWorkerParameters();
        workParams.setWorkerGUID(GUIDFactory.newGUID()).setUrlWorkspace("http://localhost:8083")
            .setUrlMetadata("http://localhost:8083")
            .setObjectName("objectName.json").setCurrentStep("currentStep")
            .setContainerName("CheckObjectsNumberActionHandlerTest");

        workspaceClient = mock(WorkspaceClient.class);
        PowerMockito.mockStatic(WorkspaceClientFactory.class);
        workspaceClientFactory = mock(WorkspaceClientFactory.class);
        PowerMockito.when(WorkspaceClientFactory.getInstance()).thenReturn(workspaceClientFactory);
        PowerMockito.when(WorkspaceClientFactory.getInstance().getClient()).thenReturn(workspaceClient);
        handlerIO = new HandlerIOImpl("CheckObjectsNumberActionHandlerTest", "workerId");

        PowerMockito.mockStatic(SedaUtilsFactory.class);
        sedaUtils = mock(SedaUtils.class);
        PowerMockito.when(SedaUtilsFactory.create(handlerIO)).thenReturn(sedaUtils);

        // URI LIST MANIFEST
        uriDuplicatedListManifestKO.add(new URI("content/file1.pdf"));
        uriDuplicatedListManifestKO.add(new URI("content/file1.pdf"));

        uriListManifestOK.add(new URI("content/file1.pdf"));
        uriListManifestOK.add(new URI("content/file2.pdf"));

        uriOutNumberListManifestKO.add(new URI("content/file1.pdf"));
        uriOutNumberListManifestKO.add(new URI("content/file2.pdf"));
        uriOutNumberListManifestKO.add(new URI("content/file3.pdf"));

        // URI LIST WORKSPACE

        uriListWorkspaceOK.add(new URI("content/file1.pdf"));
        uriListWorkspaceOK.add(new URI("content/file2.pdf"));
        // FIXME P1: ugly hack to be compatible with actual implementation
        // remove this add when the object count is fixed
        uriListWorkspaceOK.add(new URI("manifest.xml"));

        uriOutNumberListWorkspaceKO.add(new URI("content/file1.pdf"));
        uriOutNumberListWorkspaceKO.add(new URI("content/file2.pdf"));
        uriOutNumberListWorkspaceKO.add(new URI("content/file3.pdf"));
        // FIXME P1: ugly hack to be compatible with actual implementation
        // remove this add when the object count is fixed
        uriOutNumberListWorkspaceKO.add(new URI("manifest.xml"));

        extractUriResponseOK = new ExtractUriResponse();
        extractUriResponseOK.setUriListManifest(uriListManifestOK);

        messages.add("Duplicated digital objects " + "content/file1.pdf");
        extractDuplicatedUriResponseKO = new ExtractUriResponse();
        extractDuplicatedUriResponseKO.setUriListManifest(uriDuplicatedListManifestKO)
            .setErrorDuplicateUri(Boolean.TRUE).setErrorNumber(messages.size());

        extractOutNumberUriResponseKO = new ExtractUriResponse();
        extractOutNumberUriResponseKO.setUriListManifest(uriOutNumberListManifestKO);
    }

    @After
    public void clean() {
        handlerIO.partialClose();
    }

    @Test
    public void givenWorkspaceExistWhenExecuteThenRaiseXMLStreamExceptionAndReturnResponseFATAL()
        throws XMLStreamException, IOException, ProcessingException, ContentAddressableStorageServerException {
        Mockito.doThrow(new ProcessingException("")).when(sedaUtils).getAllDigitalObjectUriFromManifest(anyObject());

        checkObjectsNumberActionHandler =
            new CheckObjectsNumberActionHandler();
        assertThat(CheckObjectsNumberActionHandler.getId()).isEqualTo(HANDLER_ID);
        final ItemStatus response = checkObjectsNumberActionHandler.execute(workParams, handlerIO);
        assertThat(response).isNotNull();
        assertThat(response.getGlobalStatus()).isEqualTo(StatusCode.FATAL);
    }

    @Test
    public void givenWorkspaceNotExistWhenExecuteThenRaiseProcessingExceptionReturnResponseFATAL()
        throws XMLStreamException, IOException, ProcessingException, ContentAddressableStorageServerException {

        Mockito.doThrow(new ProcessingException("")).when(sedaUtils).getAllDigitalObjectUriFromManifest(anyObject());

        checkObjectsNumberActionHandler =
            new CheckObjectsNumberActionHandler();
        assertThat(CheckObjectsNumberActionHandler.getId()).isEqualTo(HANDLER_ID);
        final ItemStatus response = checkObjectsNumberActionHandler.execute(workParams, handlerIO);
        assertThat(response).isNotNull();
        assertThat(response.getGlobalStatus()).isEqualTo(StatusCode.FATAL);
        assertThat(response.getItemsStatus().get(HANDLER_ID).getStatusMeter().get(StatusCode.FATAL.getStatusLevel()))
            .isEqualTo(1);
    }

    @Test
    public void givenWorkpaceExistWhenExecuteThenReturnResponseOK()
        throws XMLStreamException, IOException, ProcessingException, ContentAddressableStorageServerException {

        checkObjectsNumberActionHandler =
            new CheckObjectsNumberActionHandler();

        when(sedaUtils.getAllDigitalObjectUriFromManifest(anyObject())).thenReturn(extractUriResponseOK);
        when(workspaceClient.getListUriDigitalObjectFromFolder(anyObject(), anyObject())).thenReturn
            (uriListWorkspaceOK);

        assertThat(CheckObjectsNumberActionHandler.getId()).isEqualTo(HANDLER_ID);


        final ItemStatus response = checkObjectsNumberActionHandler.execute(workParams, handlerIO);
        assertThat(response).isNotNull();
        assertThat(response.getGlobalStatus()).isEqualTo(StatusCode.OK);
        assertThat(response.getItemsStatus().get(HANDLER_ID).getStatusMeter().get(StatusCode.OK.getStatusLevel()))
            .isEqualTo(2);
    }

    @Test
    public void givenWorkspaceExistWhenExecuteThenReturnResponseKOAndDuplicatedURIManifest()
        throws XMLStreamException, IOException, ProcessingException, ContentAddressableStorageServerException {

        checkObjectsNumberActionHandler =
            new CheckObjectsNumberActionHandler();

        when(sedaUtils.getAllDigitalObjectUriFromManifest(anyObject())).thenReturn(extractDuplicatedUriResponseKO);
        when(workspaceClient.getListUriDigitalObjectFromFolder(anyObject(), anyObject())).thenReturn
            (uriListWorkspaceOK);

        assertThat(CheckObjectsNumberActionHandler.getId()).isEqualTo(HANDLER_ID);

        final ItemStatus response = checkObjectsNumberActionHandler.execute(workParams, handlerIO);
        assertThat(response).isNotNull();
        assertThat(response.getGlobalStatus()).isEqualTo(StatusCode.KO);
        assertThat(response.getItemsStatus().get(HANDLER_ID).getStatusMeter().get(StatusCode.KO.getStatusLevel()))
            .isEqualTo(1);
    }


    @Test
    public void givenWorkspaceExistWhenExecuteThenReturnResponseKOAndOutNumberManifest()
        throws XMLStreamException, IOException, ProcessingException, ContentAddressableStorageServerException {

        checkObjectsNumberActionHandler =
            new CheckObjectsNumberActionHandler();

        when(sedaUtils.getAllDigitalObjectUriFromManifest(anyObject())).thenReturn(extractOutNumberUriResponseKO);
        when(workspaceClient.getListUriDigitalObjectFromFolder(anyObject(), anyObject())).thenReturn
            (uriListWorkspaceOK);

        assertThat(CheckObjectsNumberActionHandler.getId()).isEqualTo(HANDLER_ID);

        final ItemStatus response = checkObjectsNumberActionHandler.execute(workParams, handlerIO);
        assertThat(response).isNotNull();
        assertThat(response.getItemsStatus().get(HANDLER_ID).getData().get("errorNumber")).isEqualTo(1);
        assertThat(response.getGlobalStatus()).isEqualTo(StatusCode.KO);
        assertThat(response.getItemsStatus().get(HANDLER_ID).getStatusMeter().get(StatusCode.KO.getStatusLevel()))
            .isEqualTo(1);
        assertThat(response.getItemsStatus().get(HANDLER_ID).getStatusMeter().get(StatusCode.OK.getStatusLevel()))
            .isEqualTo(2);
    }

    @Test
    public void givenWorkspaceExistWhenExecuteThenReturnResponseKOAndOutNumberWorkspace()
        throws XMLStreamException, IOException, ProcessingException, ContentAddressableStorageServerException {

        checkObjectsNumberActionHandler =
            new CheckObjectsNumberActionHandler();

        when(sedaUtils.getAllDigitalObjectUriFromManifest(anyObject())).thenReturn(extractUriResponseOK);
        when(workspaceClient.getListUriDigitalObjectFromFolder(anyObject(), anyObject())).thenReturn
            (uriOutNumberListWorkspaceKO);

        assertThat(CheckObjectsNumberActionHandler.getId()).isEqualTo(HANDLER_ID);

        final ItemStatus response = checkObjectsNumberActionHandler.execute(workParams, handlerIO);
        assertThat(response).isNotNull();
        assertThat(response.getGlobalStatus()).isEqualTo(StatusCode.KO);
        assertThat(response.getItemsStatus().get(HANDLER_ID).getStatusMeter().get(StatusCode.KO.getStatusLevel()))
            .isEqualTo(1);
        assertThat(response.getItemsStatus().get(HANDLER_ID).getStatusMeter().get(StatusCode.OK.getStatusLevel()))
            .isEqualTo(2);
    }

    @Test
    public void givenWorkspaceExistWhenExecuteThenReturnResponseKOAndNotFoundFile()
        throws XMLStreamException, IOException, ProcessingException, ContentAddressableStorageServerException {

        checkObjectsNumberActionHandler =
            new CheckObjectsNumberActionHandler();

        when(sedaUtils.getAllDigitalObjectUriFromManifest(anyObject())).thenReturn(extractUriResponseOK);
        when(workspaceClient.getListUriDigitalObjectFromFolder(anyObject(), anyObject())).thenReturn
            (uriOutNumberListWorkspaceKO);

        assertThat(CheckObjectsNumberActionHandler.getId()).isEqualTo(HANDLER_ID);

        final ItemStatus response = checkObjectsNumberActionHandler.execute(workParams, handlerIO);
        assertThat(response).isNotNull();
        assertThat(response.getGlobalStatus()).isEqualTo(StatusCode.KO);
        assertThat(response.getItemsStatus().get(HANDLER_ID).getStatusMeter().get(StatusCode.KO.getStatusLevel()))
            .isEqualTo(1);
        assertThat(response.getItemsStatus().get(HANDLER_ID).getStatusMeter().get(StatusCode.OK.getStatusLevel()))
            .isEqualTo(2);
    }
}
