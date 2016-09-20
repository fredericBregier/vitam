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
package fr.gouv.vitam.access.core;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.access.common.exception.AccessExecutionException;
import fr.gouv.vitam.access.config.AccessConfiguration;
import fr.gouv.vitam.api.exception.MetaDataDocumentSizeException;
import fr.gouv.vitam.api.exception.MetaDataExecutionException;
import fr.gouv.vitam.api.exception.MetadataInvalidSelectException;
import fr.gouv.vitam.client.MetaDataClient;
import fr.gouv.vitam.client.MetaDataClientFactory;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.junit.JunitHelper;
import fr.gouv.vitam.logbook.common.exception.LogbookClientNotFoundException;
import fr.gouv.vitam.logbook.lifecycles.client.LogbookLifeCycleClient;
import fr.gouv.vitam.logbook.operations.client.LogbookClient;
import fr.gouv.vitam.storage.engine.client.StorageClient;
import fr.gouv.vitam.storage.engine.client.exception.StorageServerClientException;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.mockito.Mockito;

import javax.ws.rs.ProcessingException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccessModuleImplTest {

    private static String HOST = "http:\\localhost:";

    private AccessConfiguration conf;

    private AccessModuleImpl accessModuleImpl;

    private MetaDataClientFactory metaDataClientFactory;

    private MetaDataClient metaDataClient;

    private LogbookClient logbookOperationClient;
    private LogbookLifeCycleClient logbookLifeCycleClient;

    private static JunitHelper junitHelper;
    private static int serverPort;

    private static final String SAMPLE_OBJECTGROUP_FILENAME = "sample_objectGroup_document.json";
    private static JsonNode sampleObjectGroup;

    private static final String QUERY =
        "{\"$queries\": [{ \"$path\": \"aaaaa\" }],\"$filter\": { },\"$projection\": {}}";
    private static final String FAKE_METADATA_RESULT = "{$result:[{'_id':123}]}";
    private static final String FAKE_METADATA_MULTIPLE_RESULT = "{$result:[{'_id':123}, {'_id':124}]}";



    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        junitHelper = new JunitHelper();
        serverPort = junitHelper.findAvailablePort();
        HOST += serverPort;
        sampleObjectGroup = JsonHandler.getFromFile(PropertiesUtils.findFile(SAMPLE_OBJECTGROUP_FILENAME));
    }

    @AfterClass
    public static void tearDownAfterClass() {
        junitHelper.releasePort(serverPort);
    }

    private static final String ID = "identifier1";

    /**
     * @param query
     * @return
     * @throws InvalidParseOperationException
     */
    public JsonNode FromStringToJson(String query) throws InvalidParseOperationException {
        return JsonHandler.getFromString(query);

    }

    @Before
    public void setUp() {
        metaDataClient = mock(MetaDataClient.class);
        metaDataClientFactory = mock(MetaDataClientFactory.class);
        logbookLifeCycleClient = mock(LogbookLifeCycleClient.class);
        logbookOperationClient = mock(LogbookClient.class);
        conf = new AccessConfiguration();
        conf.setUrlMetaData(HOST);
    }

    @Test
    public void given_correct_dsl_When_select_thenOK()
        throws Exception {
        when(metaDataClient.selectUnits(anyObject())).thenReturn(JsonHandler.createObjectNode());
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectUnit(FromStringToJson(QUERY));
    }

    @Test(expected = InvalidParseOperationException.class)
    public void given_empty_dsl_When_select_thenTrows_IllegalArgumentException()
        throws Exception {
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectUnit(FromStringToJson(""));
    }



    @Test(expected = IllegalArgumentException.class)
    public void given_test_AccessExecutionException()
        throws Exception {
        when(metaDataClientFactory.create(HOST)).thenReturn(metaDataClient);
        Mockito.doThrow(new IllegalArgumentException("")).when(metaDataClient).selectUnits(QUERY);
        accessModuleImpl = new AccessModuleImpl(conf);
        accessModuleImpl.selectUnit(FromStringToJson(QUERY));
    }


    @Ignore
    @Test(expected = InvalidParseOperationException.class)
    public void given_empty_DSLWhen_select_units_ThenThrows_InvalidParseOperationException()
        throws Exception {
        when(metaDataClient.selectUnits(anyObject())).thenReturn(JsonHandler.createObjectNode());
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        Mockito.doThrow(new InvalidParseOperationException("")).when(metaDataClient).selectUnits(QUERY);
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectUnit(FromStringToJson(QUERY));


    }


    @Ignore
    @Test(expected = MetadataInvalidSelectException.class)
    public void given__DSLWhen_select_units_ThenThrows_MetadataInvalidSelectException()
        throws Exception {
        when(metaDataClient.selectUnits(anyObject())).thenReturn(JsonHandler.createObjectNode());
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        Mockito.doThrow(new InvalidParseOperationException("")).when(metaDataClient).selectUnits(QUERY);
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectUnit(FromStringToJson(QUERY));
    }



    @Ignore
    @Test(expected = MetaDataDocumentSizeException.class)
    public void given_DSLWhen_select_units_ThenThrows_MetaDataDocumentSizeException()
        throws Exception {
        when(metaDataClient.selectUnits(anyObject())).thenReturn(JsonHandler.createObjectNode());
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        Mockito.doThrow(new MetaDataDocumentSizeException("")).when(metaDataClient).selectUnits(QUERY);
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectUnit(FromStringToJson(QUERY));
    }


    @Ignore
    @Test(expected = AccessExecutionException.class)
    public void given_DSL_When_select_units_ThenThrows_MetaDataExecutionException()
        throws Exception {
        when(metaDataClient.selectUnits(anyObject())).thenReturn(JsonHandler.createObjectNode());
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        Mockito.doThrow(new MetaDataExecutionException("")).when(metaDataClient).selectUnits(QUERY);
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectUnit(FromStringToJson(QUERY));
    }


    @Test(expected = AccessExecutionException.class)
    public void given_clientProblem_When_select_units_ThenThrows_AccessExecutionException()
        throws Exception {
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        Mockito.doThrow(new ProcessingException("")).when(metaDataClient).selectUnits(anyObject());
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectUnit(FromStringToJson(QUERY));
    }

    // select Unit Id
    @Test
    public void given_correct_dsl_When_selectunitById_thenOK()
        throws Exception {
        when(metaDataClient.selectUnitbyId(anyObject(), anyObject())).thenReturn(JsonHandler.createObjectNode());
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectUnitbyId(FromStringToJson(QUERY), ID);
    }

    @Test(expected = InvalidParseOperationException.class)
    public void given_empty_dsl_When_selectunitById_thenTrows_IllegalArgumentException()
        throws Exception {
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectUnitbyId(FromStringToJson(""), ID);
    }



    @Test(expected = IllegalArgumentException.class)
    public void given_test_AccessExecutionException_unitById()
        throws Exception {
        when(metaDataClientFactory.create(HOST)).thenReturn(metaDataClient);
        Mockito.doThrow(new IllegalArgumentException("")).when(metaDataClient).selectUnitbyId(QUERY, ID);
        accessModuleImpl = new AccessModuleImpl(conf);
        accessModuleImpl.selectUnitbyId(FromStringToJson(QUERY), ID);
    }


    @Ignore
    @Test(expected = InvalidParseOperationException.class)
    public void given_empty_DSLWhen_select_unitById_ThenThrows_InvalidParseOperationException()
        throws Exception {
        when(metaDataClient.selectUnitbyId(anyObject(), anyObject())).thenReturn(JsonHandler.createObjectNode());
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        Mockito.doThrow(new InvalidParseOperationException("")).when(metaDataClient).selectUnitbyId(QUERY, ID);
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectUnitbyId(FromStringToJson(QUERY), ID);


    }

    @Ignore
    @Test(expected = MetadataInvalidSelectException.class)
    public void given__DSLWhen_select_unitById_ThenThrows_MetadataInvalidSelectException()
        throws Exception {
        // when(metaDataClient.selectUnitbyId(anyObject(), anyObject())).thenReturn(JsonHandler.createObjectNode());
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        Mockito.doThrow(new MetadataInvalidSelectException("")).when(metaDataClient).selectUnits(QUERY);
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectUnitbyId(FromStringToJson(QUERY), ID);
    }


    @Ignore
    @SuppressWarnings("unchecked")
    @Test(expected = AccessExecutionException.class)
    public void given_DSLWhen_select_unitById_ThenThrows_MetaDataDocumentSizeException()
        throws Exception {
        // when(metaDataClient.selectUnitbyId(anyObject(), anyObject())).thenReturn(JsonHandler.createObjectNode());
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        // Mockito.doThrow(new MetaDataDocumentSizeException("")).when(metaDataClient).selectUnitbyId(QUERY, ID);

        when(metaDataClient.selectUnitbyId(anyObject(), anyObject())).thenThrow(MetaDataDocumentSizeException.class);
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectUnitbyId(FromStringToJson(QUERY), ID);
    }


    @Ignore
    @Test(expected = AccessExecutionException.class)
    public void given_DSL_When_select_unitById_ThenThrows_MetaDataExecutionException()
        throws Exception {
        when(metaDataClient.selectUnitbyId(anyObject(), anyObject())).thenReturn(JsonHandler.createObjectNode());
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        Mockito.doThrow(new MetaDataExecutionException("")).when(metaDataClient).selectUnitbyId(QUERY, ID);
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectUnitbyId(FromStringToJson(QUERY), ID);
    }


    @Test(expected = IllegalArgumentException.class)
    public void given_null_params_conf_When_select_unitById_ThenThrows_AccessExecutionException()
        throws Exception {
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void given_nullFactory_when_selectUnitById_then_instanciateNewFactory() throws Exception {
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        accessModuleImpl = new AccessModuleImpl(null, conf);
        accessModuleImpl.selectUnitbyId(FromStringToJson(QUERY), ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void given_emptyOrNullIdUnit_when_selectUnitbyId_thenthrows_IllegalArgumentException() throws Exception {
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        Mockito.doThrow(new IllegalArgumentException("")).when(metaDataClient)
            .selectUnitbyId(FromStringToJson(QUERY).toString(), "");
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectUnitbyId(FromStringToJson(QUERY), "");
    }

    @Test(expected = InvalidParseOperationException.class)
    public void given_empty_DSLWhen_selectUnitById_ThenThrows_InvalidParseOperationException()
        throws Exception {
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        Mockito.doThrow(new InvalidParseOperationException("")).when(metaDataClient)
            .selectUnitbyId(FromStringToJson(QUERY).toString(), ID);
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectUnitbyId(FromStringToJson(QUERY), ID);
    }

    @Test(expected = InvalidParseOperationException.class)
    public void given_empty_DSLWhen_selectUnit_ThenThrows_InvalidParseOperationException()
        throws Exception {
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        Mockito.doThrow(new InvalidParseOperationException("")).when(metaDataClient)
            .selectUnits(FromStringToJson(QUERY).toString());
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectUnit(FromStringToJson(QUERY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void given_nullFactory_when_selectOGById_then_instanciateNewFactory() throws Exception {
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        accessModuleImpl = new AccessModuleImpl(null, conf);
        accessModuleImpl.selectObjectGroupById(FromStringToJson(QUERY), ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void given_emptyOrNullIdUnit_when_selectOGbyId_thenthrows_IllegalArgumentException() throws Exception {
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        Mockito.doThrow(new IllegalArgumentException("")).when(metaDataClient)
            .selectObjectGrouptbyId(FromStringToJson(QUERY).toString(), "");
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectObjectGroupById(FromStringToJson(QUERY), "");
    }

    @Test(expected = AccessExecutionException.class)
    public void given_metadataAccessProblem_throw_AccessExecutionException() throws Exception {
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        Mockito.doThrow(new ProcessingException("Fake error")).when(metaDataClient).selectObjectGrouptbyId
            (FromStringToJson(QUERY).toString(), "ds");
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectObjectGroupById(FromStringToJson(QUERY), "ds");
    }

    @Test
    public void given_selectObjectGroupById_OK()
        throws Exception {
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        when(metaDataClient.selectObjectGrouptbyId(FromStringToJson(QUERY).toString(), ID)).thenReturn
            (sampleObjectGroup);
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        JsonNode result = accessModuleImpl.selectObjectGroupById(FromStringToJson(QUERY), ID);
        assertEquals(sampleObjectGroup, result);
    }

    @Test(expected = InvalidParseOperationException.class)
    public void given_empty_DSLWhen_selectOGById_ThenThrows_InvalidParseOperationException()
        throws Exception {
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        Mockito.doThrow(new InvalidParseOperationException("")).when(metaDataClient)
            .selectObjectGrouptbyId(FromStringToJson(QUERY)
                .toString(), ID);
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.selectObjectGroupById(FromStringToJson(QUERY), ID);
    }

    //update by id - start
    @Test
    public void given_correct_dsl_When_updateUnitById_thenOK()
        throws Exception {

        Mockito.doNothing().when(logbookOperationClient).update(anyObject());
        Mockito.doNothing().when(logbookLifeCycleClient).update(anyObject());
        when(metaDataClient.selectUnitbyId(anyObject(), anyObject())).thenReturn(JsonHandler.createObjectNode());
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        accessModuleImpl =
            new AccessModuleImpl(metaDataClientFactory, conf, logbookOperationClient, logbookLifeCycleClient);
        accessModuleImpl.updateUnitbyId(FromStringToJson(QUERY), ID);
    }

    @Test(expected = InvalidParseOperationException.class)
    public void given_empty_dsl_When_updateUnitById_thenTrows_IllegalArgumentException()
        throws Exception {
        Mockito.doNothing().when(logbookOperationClient).update(anyObject());
        Mockito.doNothing().when(logbookLifeCycleClient).update(anyObject());
        accessModuleImpl =
            new AccessModuleImpl(metaDataClientFactory, conf, logbookOperationClient, logbookLifeCycleClient);
        accessModuleImpl.updateUnitbyId(FromStringToJson(""), ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void given_test_AccessExecutionException_updateUnitById()
        throws Exception {
        Mockito.doNothing().when(logbookOperationClient).update(anyObject());
        Mockito.doNothing().when(logbookLifeCycleClient).update(anyObject());
        when(metaDataClientFactory.create(HOST)).thenReturn(metaDataClient);
        Mockito.doThrow(new IllegalArgumentException("")).when(metaDataClient)
            .updateUnitbyId(FromStringToJson(QUERY).toString(), ID);
        accessModuleImpl =
            new AccessModuleImpl(metaDataClientFactory, conf, logbookOperationClient, logbookLifeCycleClient);
        accessModuleImpl.updateUnitbyId(FromStringToJson(QUERY), ID);
    }

    @Test(expected = InvalidParseOperationException.class)
    public void given_empty_DSLWhen_updateUnitById_ThenThrows_InvalidParseOperationException()
        throws Exception {
        Mockito.doNothing().when(logbookOperationClient).update(anyObject());
        Mockito.doNothing().when(logbookLifeCycleClient).update(anyObject());
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        Mockito.doThrow(new InvalidParseOperationException("")).when(metaDataClient)
            .updateUnitbyId(FromStringToJson(QUERY).toString(), ID);
        accessModuleImpl =
            new AccessModuleImpl(metaDataClientFactory, conf, logbookOperationClient, logbookLifeCycleClient);
        accessModuleImpl.updateUnitbyId(FromStringToJson(QUERY), ID);
    }

    @Test(expected = AccessExecutionException.class)
    public void given_DSLWhen_updateUnitById_ThenThrows_MetaDataDocumentSizeException()
        throws Exception {
        Mockito.doNothing().when(logbookOperationClient).update(anyObject());
        Mockito.doNothing().when(logbookLifeCycleClient).update(anyObject());
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        when(metaDataClient.updateUnitbyId(anyObject(), anyObject())).thenThrow(MetaDataDocumentSizeException.class);
        accessModuleImpl =
            new AccessModuleImpl(metaDataClientFactory, conf, logbookOperationClient, logbookLifeCycleClient);
        accessModuleImpl.updateUnitbyId(FromStringToJson(QUERY), ID);
    }


    @Test(expected = AccessExecutionException.class)
    public void given_DSL_When_updateUnitById_ThenThrows_MetaDataExecutionException()
        throws Exception {
        Mockito.doNothing().when(logbookOperationClient).update(anyObject());
        Mockito.doNothing().when(logbookLifeCycleClient).update(anyObject());
        when(metaDataClient.updateUnitbyId(anyObject(), anyObject())).thenReturn(JsonHandler.createObjectNode());
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        Mockito.doThrow(new MetaDataExecutionException("")).when(metaDataClient)
            .updateUnitbyId(FromStringToJson(QUERY).toString(), ID);
        accessModuleImpl =
            new AccessModuleImpl(metaDataClientFactory, conf, logbookOperationClient, logbookLifeCycleClient);
        accessModuleImpl.updateUnitbyId(FromStringToJson(QUERY), ID);
    }


    @Test(expected = AccessExecutionException.class)
    public void given_LogbookProblem_When_updateUnitById_ThenThrows_AccessExecutionException()
        throws Exception {
        Mockito.doThrow(new LogbookClientNotFoundException("")).when(logbookOperationClient).update(anyObject());
        Mockito.doNothing().when(logbookLifeCycleClient).update(anyObject());
        when(metaDataClient.updateUnitbyId(anyObject(), anyObject())).thenReturn(JsonHandler.createObjectNode());
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        Mockito.doThrow(new MetaDataExecutionException("")).when(metaDataClient)
            .updateUnitbyId(FromStringToJson(QUERY).toString(), ID);
        accessModuleImpl =
            new AccessModuleImpl(metaDataClientFactory, conf, logbookOperationClient, logbookLifeCycleClient);
        accessModuleImpl.updateUnitbyId(FromStringToJson(QUERY), ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void given_null_params_conf_When_updateUnitById_ThenNotThrowAnyException()
        throws Exception {
        accessModuleImpl =
            new AccessModuleImpl(metaDataClientFactory, null, logbookOperationClient, logbookLifeCycleClient);
    }

    @Test(expected = IllegalArgumentException.class)
    public void given_emptyOrNullIdUnit_when_updateUnitbyId_thenthrows_IllegalArgumentException() throws Exception {
        Mockito.doNothing().when(logbookOperationClient).update(anyObject());
        Mockito.doNothing().when(logbookLifeCycleClient).update(anyObject());
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        Mockito.doThrow(new IllegalArgumentException("")).when(metaDataClient)
            .updateUnitbyId(FromStringToJson(QUERY).toString(), "");
        accessModuleImpl =
            new AccessModuleImpl(metaDataClientFactory, conf, logbookOperationClient, logbookLifeCycleClient);
        accessModuleImpl.updateUnitbyId(FromStringToJson(QUERY), "");
    }


    @Test(expected = IllegalArgumentException.class)
    public void given_nullFactory_when_updateUnitById_then_instanciateNewFactory() throws Exception {
        Mockito.doNothing().when(logbookOperationClient).update(anyObject());
        Mockito.doNothing().when(logbookLifeCycleClient).update(anyObject());
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        accessModuleImpl = new AccessModuleImpl(null, conf, logbookOperationClient, logbookLifeCycleClient);
        accessModuleImpl.updateUnitbyId(FromStringToJson(QUERY), ID);
    }

    @Test
    public void testGetOneObjectFromObjectGroup_OK() throws Exception {
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        when(metaDataClient.selectObjectGrouptbyId(anyObject(), anyString())).thenReturn
            (FromStringToJson(FAKE_METADATA_RESULT));
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        InputStream binaryMaster =
            accessModuleImpl.getOneObjectFromObjectGroup(ID, FromStringToJson(QUERY), "BinaryMaster", 0, "0");
        assertNotNull(binaryMaster);
        InputStream stream2 = IOUtils.toInputStream("Vitam test");
        assertTrue(IOUtils.contentEquals(binaryMaster, stream2));
    }

    @Test(expected = AccessExecutionException.class)
    public void testGetOneObjectFromObjectGroup_With_Multiple_Result() throws Exception {
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        when(metaDataClient.selectObjectGrouptbyId(anyObject(), anyString())).thenReturn
            (FromStringToJson(FAKE_METADATA_MULTIPLE_RESULT));
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.getOneObjectFromObjectGroup(ID, FromStringToJson(QUERY), "BinaryMaster", 0, "0");
    }

    @Test(expected = AccessExecutionException.class)
    public void testGetOneObjectFromObjectGroup_With_Result_Null() throws Exception {
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        when(metaDataClient.selectObjectGrouptbyId(anyObject(), anyString())).thenReturn(null);
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf);
        accessModuleImpl.getOneObjectFromObjectGroup(ID, FromStringToJson(QUERY), "BinaryMaster", 0, "0");
    }

    @Test(expected = AccessExecutionException.class)
    public void testGetOneObjectFromObjectGroup_With_StorageClient_Error() throws Exception {
        StorageClient mockStorageClient = mock(StorageClient.class);
        when(metaDataClientFactory.create(anyObject())).thenReturn(metaDataClient);
        when(metaDataClient.selectObjectGrouptbyId(anyObject(), anyString())).thenReturn(FromStringToJson
            (FAKE_METADATA_RESULT));
        when(mockStorageClient.getContainerObject(anyString(), anyString(), anyString())).thenThrow(new
            StorageServerClientException("Test wanted exception"));
        accessModuleImpl = new AccessModuleImpl(metaDataClientFactory, conf, mockStorageClient);
        accessModuleImpl.getOneObjectFromObjectGroup(ID, FromStringToJson(QUERY), "BinaryMaster", 0, "0");
    }

}