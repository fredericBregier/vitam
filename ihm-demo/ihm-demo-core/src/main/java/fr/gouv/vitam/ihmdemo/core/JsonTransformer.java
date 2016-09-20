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
package fr.gouv.vitam.ihmdemo.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.common.json.JsonHandler;

/**
 * THis class is used in order to make transformations on Json objects received from Vitam
 */
public final class JsonTransformer {

    private static final String MISSING_ID_ERROR_MSG = "Encountered Missing ID field in the given parents list.";
    private static final String MISSING_UP_ERROR_MSG = "Encountered Missing _up field in the given parents list.";
    private static final String INVALID_UP_FIELD_ERROR_MSG = "Encountered invalid _up field in the given parents list.";
    private static final String MISSING_UNIT_ID_ERROR_MSG = "The unit details is missing.";

    /**
     * This method transforms ResultObjects so thr IHM could display results
     * 
     * @param searchResult the Json to be transformed
     * @return the transformed JsonNode
     */
    public static JsonNode transformResultObjects(JsonNode searchResult) {
        ParametersChecker.checkParameter("Result cannot be empty", searchResult);
        final ObjectNode resultNode = JsonHandler.createObjectNode();
        long nbObjects = 0;
        JsonNode result = searchResult.get("$result").get(0);
        JsonNode qualifiers = result.get("_qualifiers");
        List<JsonNode> versions = qualifiers.findValues("versions");
        Map<String, Integer> usages = new HashMap<>();
        final ArrayNode arrayNode = JsonHandler.createArrayNode();
        for (JsonNode version : versions) {
            for (JsonNode object : version) {
                ObjectNode objectNode = JsonHandler.createObjectNode();
                objectNode.put("_id", object.get("_id").asText());
                String usage = object.get("DataObjectVersion").asText();
                if (usages.containsKey(usage)){
                    Integer rank = usages.get(usage) + 1;
                    objectNode.put("Rank", rank);
                } else {
                    usages.put(usage, 0);
                    objectNode.put("Rank", 0);
                }
                objectNode.put("DataObjectVersion", usage);
                objectNode.put("Size", object.get("Size").asText());
                if (object.get("FileInfo").get("LastModified") != null){
                    objectNode.put("LastModified", object.get("FileInfo").get("LastModified").asText());
                }
                if (object.get("FormatIdentification").get("FormatLitteral") != null){
                    objectNode.put("FormatLitteral", object.get("FormatIdentification").get("FormatLitteral").asText());
                }
                if (object.get("FileInfo").get("Filename") != null){
                    objectNode.put("FileName", object.get("FileInfo").get("Filename").asText());
                }
                objectNode.set("metadatas", object);
                arrayNode.add(objectNode);
                nbObjects++;
            }
        }
        resultNode.put("nbObjects", nbObjects);
        resultNode.set("versions", arrayNode);
        return resultNode;
    }

    /**
     * This method builds an ObjectNode based on list of JsonNode object
     * 
     * @param allParents list of JsonNode Objects used to build the referential
     * @return An ObjectNode where the key is the identifier and the value is the parent details (Title, Id, _up)
     * @throws VitamException
     */
    public static ObjectNode buildAllParentsRef(String unitId, JsonNode allParents) throws VitamException {
        ParametersChecker.checkParameter("Result cannot be empty", allParents);

        boolean hasUnitId = false;

        ObjectNode allParentsRef = JsonHandler.createObjectNode();
        for (JsonNode currentParentNode : allParents) {
            if (!currentParentNode.has(UiConstants.ID.getResultConstantValue())) {
                throw new VitamException(MISSING_ID_ERROR_MSG);
            }

            if (!currentParentNode.has(UiConstants.UNITUPS.getResultConstantValue())) {
                throw new VitamException(MISSING_UP_ERROR_MSG);
            }

            if (!currentParentNode.get(UiConstants.UNITUPS.getResultConstantValue()).isArray()) {
                throw new VitamException(INVALID_UP_FIELD_ERROR_MSG);
            }

            String currentParentId = currentParentNode.get(UiConstants.ID.getResultConstantValue()).asText();
            allParentsRef.set(currentParentId, currentParentNode);

            if (unitId.equalsIgnoreCase(currentParentId)) {
                hasUnitId = true;
            }
        }

        if (!hasUnitId) {
            throw new VitamException(MISSING_UNIT_ID_ERROR_MSG);
        }

        return allParentsRef;
    }
}