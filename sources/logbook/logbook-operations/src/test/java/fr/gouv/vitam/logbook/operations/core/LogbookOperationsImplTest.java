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
package fr.gouv.vitam.logbook.operations.core;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.logbook.common.parameters.LogbookOperationParameters;
import fr.gouv.vitam.logbook.common.parameters.LogbookParametersFactory;
import fr.gouv.vitam.logbook.common.server.LogbookDbAccess;
import fr.gouv.vitam.logbook.common.server.exception.LogbookAlreadyExistsException;
import fr.gouv.vitam.logbook.common.server.exception.LogbookDatabaseException;
import fr.gouv.vitam.logbook.common.server.exception.LogbookNotFoundException;

public class LogbookOperationsImplTest {

    private LogbookOperationsImpl logbookOperationsImpl;
    private LogbookDbAccess mongoDbAccess;
    private LogbookOperationParameters logbookParameters;

    @Before
    public void setUp() {
        mongoDbAccess = mock(LogbookDbAccess.class);
        logbookParameters = LogbookParametersFactory.newLogbookOperationParameters();
    }

    @Test(expected = LogbookDatabaseException.class)
    public void givenCreateOperationWhenErrorInMongoThenThrowLogbookException() throws Exception {
        Mockito.doThrow(LogbookDatabaseException.class).when(mongoDbAccess).createLogbookOperation(anyObject());

        logbookOperationsImpl = new LogbookOperationsImpl(mongoDbAccess);
        logbookOperationsImpl.create(logbookParameters);
    }

    @Test(expected = LogbookDatabaseException.class)
    public void givenUpdateOperationWhenErrorInMongoThenThrowLogbookException() throws Exception {
        Mockito.doThrow(LogbookDatabaseException.class).when(mongoDbAccess).updateLogbookOperation(anyObject());

        logbookOperationsImpl = new LogbookOperationsImpl(mongoDbAccess);
        logbookOperationsImpl.update(logbookParameters);
    }

    @Test(expected = LogbookNotFoundException.class)
    public void givenSelectOperationWhenErrorInMongoThenThrowLogbookException() throws Exception {
        Mockito.doThrow(LogbookDatabaseException.class).when(mongoDbAccess)
            .getLogbookOperations(JsonHandler.createObjectNode(), true);
        logbookOperationsImpl = new LogbookOperationsImpl(mongoDbAccess);
        logbookOperationsImpl.select(null);
    }

    @Test(expected = LogbookAlreadyExistsException.class)
    public void givenCreateOperationWhenOperationAlreadyExistsThenThrowLogbookException() throws Exception {
        Mockito.doThrow(LogbookAlreadyExistsException.class).when(mongoDbAccess).createLogbookOperation(anyObject());

        logbookOperationsImpl = new LogbookOperationsImpl(mongoDbAccess);
        logbookOperationsImpl.create(logbookParameters);
    }

    @Test(expected = LogbookNotFoundException.class)
    public void givenUpdateOperationWhenOperationNotExistsThenThrowLogbookException() throws Exception {
        Mockito.doThrow(LogbookNotFoundException.class).when(mongoDbAccess).updateLogbookOperation(anyObject());

        logbookOperationsImpl = new LogbookOperationsImpl(mongoDbAccess);
        logbookOperationsImpl.update(logbookParameters);
    }

    @Test(expected = LogbookNotFoundException.class)
    public void givenSelectOperationWhenOperationNotExistsThenThrowLogbookException() throws Exception {
        Mockito.doThrow(LogbookNotFoundException.class).when(mongoDbAccess)
            .getLogbookOperations(JsonHandler.createObjectNode(), true);
        logbookOperationsImpl = new LogbookOperationsImpl(mongoDbAccess);
        logbookOperationsImpl.select(null);
    }

}
