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
package fr.gouv.vitam.worker.client;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.client.DefaultClient;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.exception.VitamClientInternalException;
import fr.gouv.vitam.common.exception.VitamThreadAccessException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.model.ItemStatus;
import fr.gouv.vitam.common.thread.VitamThreadUtils;
import fr.gouv.vitam.worker.client.exception.WorkerNotFoundClientException;
import fr.gouv.vitam.worker.client.exception.WorkerServerClientException;
import fr.gouv.vitam.worker.common.DescriptionStep;

/**
 * WorkerClient implementation for production environment using REST API.
 */
class WorkerClientRest extends DefaultClient implements WorkerClient {
    private static final String WORKER_INTERNAL_SERVER_ERROR = "Worker Internal Server Error";
    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(WorkerClientRest.class);
    private static final String DATA_MUST_HAVE_A_VALID_VALUE = "data must have a valid value";

    WorkerClientRest(WorkerClientFactory factory) {
        super(factory);
    }

    @Override
    public ItemStatus submitStep(DescriptionStep step)
        throws WorkerNotFoundClientException, WorkerServerClientException {
        VitamThreadUtils.getVitamSession().checkValidRequestId();
        ParametersChecker.checkParameter(DATA_MUST_HAVE_A_VALID_VALUE, step);
        Response response = null;
        try {
            response =
                performRequest(HttpMethod.POST, "/" + "tasks", null, JsonHandler.toJsonNode(step),
                    MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_JSON_TYPE);
            return handleCommonResponseStatus(step, response, ItemStatus.class);
        } catch (final VitamClientInternalException e) {
            LOGGER.error(WORKER_INTERNAL_SERVER_ERROR, e);
            throw new WorkerServerClientException(WORKER_INTERNAL_SERVER_ERROR, e);
        } catch (final InvalidParseOperationException e) {
            LOGGER.error(WORKER_INTERNAL_SERVER_ERROR, e);
            throw new WorkerServerClientException("Step description incorrect", e);
        } finally {
            consumeAnyEntityAndClose(response);
        }
    }

    /**
     * Common method to handle status responses
     *
     * @param <R> response type parameter
     * @param step the current step
     * @param response the JAX-RS response from the server
     * @param responseType the type to map the response into
     * @return the Response mapped as an POJO
     * @throws VitamClientException the exception if any from the server
     */
    protected <R> R handleCommonResponseStatus(DescriptionStep step, Response response, Class<R> responseType)
        throws WorkerNotFoundClientException, WorkerServerClientException {
        final Response.Status status = Response.Status.fromStatusCode(response.getStatus());
        switch (status) {
            case OK:
                return response.readEntity(responseType);
            case NOT_FOUND:
                throw new WorkerNotFoundClientException(status.getReasonPhrase());
            default:
                try {
                    LOGGER.error(INTERNAL_SERVER_ERROR + " during execution of " +
                        VitamThreadUtils.getVitamSession().getRequestId() + " Request, stepname:  " +
                        step.getStep().getStepName() + " : " + status.getReasonPhrase());
                } catch (final VitamThreadAccessException e) {
                    LOGGER.error(
                        INTERNAL_SERVER_ERROR + " during execution of <unknown request id> Request, stepname:  " +
                            step.getStep().getStepName() + " : " + status.getReasonPhrase());
                }
                throw new WorkerServerClientException(INTERNAL_SERVER_ERROR);
        }
    }

}
