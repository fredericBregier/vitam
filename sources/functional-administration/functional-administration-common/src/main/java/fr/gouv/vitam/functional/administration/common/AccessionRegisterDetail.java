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
package fr.gouv.vitam.functional.administration.common;

import org.bson.Document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.gouv.vitam.common.database.server.mongodb.VitamDocument;

/**
 * Accession Register Detail document
 */
public class AccessionRegisterDetail extends VitamDocument<AccessionRegisterDetail> {

    private static final long serialVersionUID = 3439757375656161919L;
    private static final String ORIGINATING_AGENCY = "OriginatingAgency";
    private static final String SUBMISSION_AGENCY = "SubmissionAgency";
    private static final String START_DATE = "StartDate";
    private static final String END_DATE = "EndDate";
    private static final String LAST_UPDATE = "LastUpdate";
    private static final String TOTAL_UNITS = "TotalUnits";
    private static final String TOTAL_OBJECTGROUPS = "TotalObjectGroups";
    private static final String TOTAL_OBJECTS = "TotalObjects";
    private static final String OBJECT_SIZE = "ObjectSize";
    private static final String STATUS = "Status";
    private static final String TENANT = "_tenant";

    /**
     * Empty Constructor
     */
    public AccessionRegisterDetail() {
        // Empty
        // FIXME P1
        append(TENANT, 0);
    }

    /**
     * Constructor
     *
     * @param document
     */
    public AccessionRegisterDetail(Document document) {
        super(document);
        // FIXME P1
        append(TENANT, 0);
    }

    /**
     * @param content
     */
    public AccessionRegisterDetail(JsonNode content) {
        super(content);
        // FIXME P1
        append(TENANT, 0);
    }

    /**
     * @param content
     */
    public AccessionRegisterDetail(String content) {
        super(content);
        // FIXME P1
        append(TENANT, 0);
    }

    /**
     * @param id
     * @return AccessionRegisterDetail
     */
    public AccessionRegisterDetail setId(String id) {
        append(VitamDocument.ID, id);
        return this;
    }

    /**
     * @param orgAgency
     * @return AccessionRegisterDetail
     */
    public AccessionRegisterDetail setOriginatingAgency(String orgAgency) {
        append(ORIGINATING_AGENCY, orgAgency);
        return this;
    }

    /**
     * @param orgAgency
     * @return String
     */
    public String getOriginatingAgency() {
        return getString(ORIGINATING_AGENCY);
    }

    /**
     * @param subAgency
     * @return this
     */
    public AccessionRegisterDetail setSubmissionAgency(String subAgency) {
        append(SUBMISSION_AGENCY, subAgency);
        return this;
    }

    /**
     * @param startDate
     * @return AccessionRegisterDetail
     */
    public AccessionRegisterDetail setStartDate(String startDate) {
        append(START_DATE, startDate);
        return this;
    }

    /**
     * @param endDate
     * @return AccessionRegisterDetail
     */
    public AccessionRegisterDetail setEndDate(String endDate) {
        append(END_DATE, endDate);
        return this;
    }

    /**
     * @return String
     */
    public String getEndDate() {
        return getString(END_DATE);
    }

    /**
     * @param lastUpdate
     * @return AccessionRegisterDetail
     */
    public AccessionRegisterDetail setLastUpdate(String lastUpdate) {
        append(LAST_UPDATE, lastUpdate);
        return this;
    }

    /**
     * @param totalUnits
     * @return AccessionRegisterDetail
     */
    public AccessionRegisterDetail setTotalUnits(RegisterValueDetail totalUnits) {
        append(TOTAL_UNITS, totalUnits);
        return this;
    }

    /**
     * @return String
     */
    public RegisterValueDetail getTotalUnits() {
        return new ObjectMapper().convertValue(this.get(TOTAL_UNITS), RegisterValueDetail.class);
    }

    /**
     * @param totalObjectGroups
     * @return AccessionRegisterDetail
     */
    public AccessionRegisterDetail setTotalObjectGroups(RegisterValueDetail totalObjectGroups) {
        append(TOTAL_OBJECTGROUPS, totalObjectGroups);
        return this;
    }

    /**
     * @return String
     */
    public RegisterValueDetail getTotalObjectGroups() {
        return new ObjectMapper().convertValue(this.get(TOTAL_OBJECTGROUPS), RegisterValueDetail.class);
    }

    /**
     * @param total
     * @return this
     */
    public AccessionRegisterDetail setTotalObjects(RegisterValueDetail total) {
        append(TOTAL_OBJECTS, total);
        return this;
    }

    /**
     * @return String
     */
    public RegisterValueDetail getTotalObjects() {
        return new ObjectMapper().convertValue(this.get(TOTAL_OBJECTS), RegisterValueDetail.class);
    }

    /**
     * @param objectSize
     * @return AccessionRegisterDetail
     */
    public AccessionRegisterDetail setObjectSize(RegisterValueDetail objectSize) {
        append(OBJECT_SIZE, objectSize);
        return this;
    }

    /**
     * @return String
     */
    public RegisterValueDetail getTotalObjectSize() {
        return new ObjectMapper().convertValue(this.get(OBJECT_SIZE), RegisterValueDetail.class);
    }

    /**
     * @param status
     * @return AccessionRegisterDetail
     */
    public AccessionRegisterDetail setStatus(AccessionRegisterStatus status) {
        append(STATUS, status.name());
        return this;
    }

}
