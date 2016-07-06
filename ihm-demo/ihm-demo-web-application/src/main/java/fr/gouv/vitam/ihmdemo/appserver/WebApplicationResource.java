/**
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
 */
package fr.gouv.vitam.ihmdemo.appserver;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.access.common.exception.AccessClientNotFoundException;
import fr.gouv.vitam.access.common.exception.AccessClientServerException;
import fr.gouv.vitam.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.ihmdemo.core.DslQueryHelper;
import fr.gouv.vitam.ihmdemo.core.UiConstants;
import fr.gouv.vitam.ihmdemo.core.UserInterfaceTransactionManager;
import fr.gouv.vitam.logbook.common.exception.LogbookClientException;
import fr.gouv.vitam.logbook.operations.client.LogbookClient;
import fr.gouv.vitam.logbook.operations.client.LogbookClientFactory;

/**
 * Web Application Resource class
 */
@Path("/v1/api")
public class WebApplicationResource {

	private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(WebApplicationResource.class);
	private static final String BAD_REQUEST_EXCEPTION_MSG = "Bad request Exception";
	private static final String ACCESS_CLIENT_NOT_FOUND_EXCEPTION_MSG = "Access client unavailable";
	private static final String ACCESS_SERVER_EXCEPTION_MSG = "Access Server exception";
	private static final String INTERNAL_SERVER_ERROR_MSG = "INTERNAL SERVER ERROR";
	private static final String SEARCH_CRITERIA_MANDATORY_MSG = "Search criteria payload is mandatory";

	/**
	 * @param criteria
	 *            criteria search for units
	 * @return Reponse
	 */
	@POST
	@Path("/archivesearch/units")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getArchiveSearchResult(String criteria) {
		ParametersChecker.checkParameter(SEARCH_CRITERIA_MANDATORY_MSG, criteria);
		try {
			Map<String, String> criteriaMap = JsonHandler.getMapStringFromString(criteria);

			String preparedQueryDsl = DslQueryHelper.createSelectDSLQuery(criteriaMap);
			JsonNode searchResult = UserInterfaceTransactionManager.searchUnits(preparedQueryDsl);
			return Response.status(Status.OK).entity(searchResult).build();

		} catch (InvalidCreateOperationException | InvalidParseOperationException e) {
			LOGGER.error(BAD_REQUEST_EXCEPTION_MSG, e);
			return Response.status(Status.BAD_REQUEST).build();
		} catch (AccessClientServerException e) {
			LOGGER.error(ACCESS_SERVER_EXCEPTION_MSG, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} catch (AccessClientNotFoundException e) {
			LOGGER.error(ACCESS_CLIENT_NOT_FOUND_EXCEPTION_MSG, e);
			return Response.status(Status.NOT_FOUND).build();
		} catch (Exception e) {
			LOGGER.error(INTERNAL_SERVER_ERROR_MSG, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * @param unitId
	 *            archive unit id
	 * @return archive unit details
	 */
	@GET
	@Path("/archivesearch/unit/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getArchiveUnitDetails(@PathParam("id") String unitId) {
		ParametersChecker.checkParameter(SEARCH_CRITERIA_MANDATORY_MSG, unitId);

		// Prepare required map
		Map<String, String> selectUnitIdMap = new HashMap<String, String>();
		selectUnitIdMap.put(UiConstants.SELECT_BY_ID.toString(), unitId);

		try {
			String preparedQueryDsl = DslQueryHelper.createSelectDSLQuery(selectUnitIdMap);
			JsonNode archiveDetails = UserInterfaceTransactionManager.getArchiveUnitDetails(preparedQueryDsl, unitId);

			return Response.status(Status.OK).entity(archiveDetails).build();
		} catch (InvalidCreateOperationException | InvalidParseOperationException e) {
			LOGGER.error(BAD_REQUEST_EXCEPTION_MSG, e);
			return Response.status(Status.BAD_REQUEST).build();
		} catch (AccessClientServerException e) {
			LOGGER.error(ACCESS_SERVER_EXCEPTION_MSG, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} catch (AccessClientNotFoundException e) {
			LOGGER.error(ACCESS_CLIENT_NOT_FOUND_EXCEPTION_MSG, e);
			return Response.status(Status.NOT_FOUND).build();
		} catch (Exception e) {
			LOGGER.error(INTERNAL_SERVER_ERROR_MSG, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * @param search
	 *            options
	 * @return Response
	 * @throws InvalidParseOperationException
	 */
	@POST
	@Path("/logbook/operations")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLogbookResult(String options) throws InvalidParseOperationException {
		ParametersChecker.checkParameter("Search criteria payload is mandatory", options);
		JsonNode result = JsonHandler.getFromString("{}");
		String query = "";
		try {
			Map<String, String> optionsMap = JsonHandler.getMapStringFromString(options);
			query = DslQueryHelper.createLogBookSelectDSLQuery(optionsMap);
			LogbookClient logbookClient = LogbookClientFactory.getInstance().getLogbookOperationClient();
			result = logbookClient.selectOperation(query);
		} catch (InvalidCreateOperationException | InvalidParseOperationException e) {
			LOGGER.error("Bad request Exception ", e);
			return Response.status(Status.BAD_REQUEST).build();
		} catch (LogbookClientException e) {
			LOGGER.error("Logbook Client NOT FOUND Exception ", e);
			return Response.status(Status.NOT_FOUND).build();
		} catch (Exception e) {
			LOGGER.error(INTERNAL_SERVER_ERROR_MSG, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return Response.status(Status.OK).entity(result).build();
	}

	/**
	 * @param operationId
	 *            id of operation
	 * @param search
	 *            options
	 * @return Response
	 * @throws InvalidParseOperationException
	 */
	@POST
	@Path("/logbook/operations/{idOperation}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLogbookResultById(@PathParam("idOperation") String operationId, String options)
			throws InvalidParseOperationException {
		ParametersChecker.checkParameter("Search criteria payload is mandatory", options);
		JsonNode result = JsonHandler.getFromString("{}");
		try {
			LogbookClient logbookClient = LogbookClientFactory.getInstance().getLogbookOperationClient();
			result = logbookClient.selectOperationbyId(operationId);
		} catch (LogbookClientException e) {
			LOGGER.error("Logbook Client NOT FOUND Exception ", e);
			return Response.status(Status.NOT_FOUND).build();
		} catch (Exception e) {
			LOGGER.error("INTERNAL SERVER ERROR", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return Response.status(Status.OK).entity(result).build();
	}

	/**
	 * Return a response status
	 *
	 * @return Response
	 */
	@Path("status")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response status() {
		return Response.status(Status.OK).build();
	}
}
