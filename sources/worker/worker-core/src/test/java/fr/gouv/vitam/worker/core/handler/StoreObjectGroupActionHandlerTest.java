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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.guid.GUIDFactory;
import fr.gouv.vitam.common.model.CompositeItemStatus;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.metadata.client.MetaDataClient;
import fr.gouv.vitam.metadata.client.MetaDataClientFactory;
import fr.gouv.vitam.processing.common.parameter.WorkerParameters;
import fr.gouv.vitam.processing.common.parameter.WorkerParametersFactory;
import fr.gouv.vitam.storage.engine.client.StorageClient;
import fr.gouv.vitam.storage.engine.client.StorageClientFactory;
import fr.gouv.vitam.storage.engine.client.exception.StorageServerClientException;
import fr.gouv.vitam.storage.engine.common.model.response.StoredInfoResult;
import fr.gouv.vitam.worker.core.api.HandlerIO;
import fr.gouv.vitam.workspace.client.WorkspaceClient;
import fr.gouv.vitam.workspace.client.WorkspaceClientFactory;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*")
@PrepareForTest({WorkspaceClientFactory.class, MetaDataClientFactory.class, StorageClientFactory.class})
public class StoreObjectGroupActionHandlerTest {

    private static final String CONTAINER_NAME = "aeaaaaaaaaaaaaabaa4quakwgip7nuaaaaaq";
    private static final String OBJECT_GROUP_GUID = "aeaaaaaaaaaam7myaaaamakxfgivuryaaaaq";
    StoreObjectGroupActionHandler handler;
    private static final String HANDLER_ID = "OG_STORAGE";
    private WorkspaceClient workspaceClient;
    private MetaDataClient metadataClient;
    private StorageClient storageClient;
    private HandlerIO action;
    private static final String OBJECT_GROUP = "storeObjectGroupHandler/aeaaaaaaaaaam7myaaaamakxfgivuryaaaaq.json";
    private final InputStream objectGroup;
    private static final String OBJ = "aeaaaaaaaaaam7myaaaamakxfgivuryaaaaq";

    public StoreObjectGroupActionHandlerTest() throws FileNotFoundException {
        objectGroup = PropertiesUtils.getResourceAsStream(OBJECT_GROUP);
    }

    @Before
    public void setUp() throws Exception {
        workspaceClient = mock(WorkspaceClient.class);
        metadataClient = mock(MetaDataClient.class);
        storageClient = mock(StorageClient.class);
        action = new HandlerIO("");
        action.addInput(PropertiesUtils.getResourceFile(OBJECT_GROUP));
        PowerMockito.mockStatic(WorkspaceClientFactory.class);
        PowerMockito.mockStatic(MetaDataClientFactory.class);
        PowerMockito.mockStatic(StorageClientFactory.class);
    }

    @Test
    public void givenWorkspaceErrorWhenExecuteThenReturnResponseKO() throws Exception {
        final StorageClientFactory storageClientFactory = PowerMockito.mock(StorageClientFactory.class);

        final WorkerParameters paramsObjectGroups =
            WorkerParametersFactory.newWorkerParameters().setWorkerGUID(GUIDFactory
                .newGUID()).setContainerName(CONTAINER_NAME).setUrlMetadata(OBJ).setUrlWorkspace(OBJ)
                .setObjectName(OBJECT_GROUP_GUID + ".json").setCurrentStep("Store ObjectGroup");

        when(MetaDataClientFactory.create(anyObject())).thenReturn(metadataClient);
        when(workspaceClient.getObject(CONTAINER_NAME, "ObjectGroup/aeaaaaaaaaaam7myaaaamakxfgivuryaaaaq.json"))
            .thenReturn(objectGroup);
        PowerMockito.when(WorkspaceClientFactory.create(Matchers.anyObject())).thenReturn(workspaceClient);

        Mockito.doThrow(new StorageServerClientException("Error storage")).when(storageClient)
            .storeFileFromWorkspace(anyObject(), anyObject(), anyObject(), anyObject(), anyObject());
        when(storageClientFactory.getStorageClient()).thenReturn(storageClient);
        when(StorageClientFactory.getInstance()).thenReturn(storageClientFactory);

        handler = new StoreObjectGroupActionHandler(storageClientFactory);
        assertEquals(StoreObjectGroupActionHandler.getId(), HANDLER_ID);

        final CompositeItemStatus response = handler.execute(paramsObjectGroups, action);
        assertEquals(StatusCode.KO, response.getGlobalStatus());
    }


    @Test
    public void givenWorkspaceExistWhenExecuteThenReturnResponseOK()
        throws Exception {
        final StorageClientFactory storageClientFactory = PowerMockito.mock(StorageClientFactory.class);

        final WorkerParameters paramsObjectGroups =
            WorkerParametersFactory.newWorkerParameters().setWorkerGUID(GUIDFactory
                .newGUID()).setContainerName(CONTAINER_NAME).setUrlMetadata(OBJ).setUrlWorkspace(OBJ)
                .setObjectName(OBJECT_GROUP_GUID + ".json").setCurrentStep("Store ObjectGroup");

        when(MetaDataClientFactory.create(anyObject())).thenReturn(metadataClient);
        when(workspaceClient.getObject(CONTAINER_NAME, "ObjectGroup/aeaaaaaaaaaam7myaaaamakxfgivuryaaaaq.json"))
            .thenReturn(objectGroup);

        PowerMockito.when(WorkspaceClientFactory.create(Matchers.anyObject())).thenReturn(workspaceClient);

        Mockito.doReturn(getStorageResult()).when(storageClient).storeFileFromWorkspace(anyObject(), anyObject(),
            anyObject(),
            anyObject(), anyObject());
        when(storageClientFactory.getStorageClient()).thenReturn(storageClient);
        when(StorageClientFactory.getInstance()).thenReturn(storageClientFactory);


        handler = new StoreObjectGroupActionHandler(storageClientFactory);
        assertEquals(StoreObjectGroupActionHandler.getId(), HANDLER_ID);

        final CompositeItemStatus response = handler.execute(paramsObjectGroups, action);
        assertEquals(StatusCode.OK, response.getGlobalStatus());
    }

    private StoredInfoResult getStorageResult() {
        final StoredInfoResult storedInfoResult = new StoredInfoResult();
        storedInfoResult.setInfo("Info");
        storedInfoResult.setId("obj");
        return storedInfoResult;
    }

}
