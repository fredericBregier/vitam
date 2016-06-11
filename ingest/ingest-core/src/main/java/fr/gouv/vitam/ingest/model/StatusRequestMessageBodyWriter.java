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
package fr.gouv.vitam.ingest.model;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Created by bsui on 10/05/16.
 */
@Produces("application/json")
public class StatusRequestMessageBodyWriter implements MessageBodyWriter<StatusRequestDTO> {

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return type == StatusRequestDTO.class;
    }

    @Override
    public long getSize(StatusRequestDTO statusRequestDTO, Class<?> aClass, Type type, Annotation[] annotations,
        MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(StatusRequestDTO statusRequestDTO, Class<?> aClass, Type type, Annotation[] annotations,
        MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream)
        throws IOException, WebApplicationException {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(StatusRequestDTO.class);

            // serialize the entity myBean to the entity output stream
            jaxbContext.createMarshaller().marshal(statusRequestDTO, outputStream);
        } catch (final JAXBException jaxbException) {
            throw new ProcessingException("Error serializing a MyBean to the output stream", jaxbException);
        }
    }
}
