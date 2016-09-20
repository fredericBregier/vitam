/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@culture.gouv.fr
 * <p>
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.function.administration.rules.core;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.eq;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.bson.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.junit.JunitHelper;
import fr.gouv.vitam.common.server.application.configuration.DbConfigurationImpl;
import fr.gouv.vitam.functional.administration.common.FileRules;
import fr.gouv.vitam.functional.administration.common.exception.ReferentialException;

public class RulesManagerFileImplTest {
    String FILE_TO_TEST_OK = "jeu_donnees_OK_regles_CSV.csv";
    String FILE_TO_TEST_KO = "jeu_donnees_KO_regles_CSV_Parameters.csv";

    File rulesFile = null;

    static MongodExecutable mongodExecutable;
    static MongodProcess mongod;
    static MongoClient mongoClient;
    static JunitHelper junitHelper;
    static final String DATABASE_HOST = "localhost";
    static final String DATABASE_NAME = "vitamtest";
    static final String COLLECTION_NAME = "FileRules";
    static int port;
    static RulesManagerFileImpl rulesFileManager;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        final MongodStarter starter = MongodStarter.getDefaultInstance();
        junitHelper = new JunitHelper();
        port = junitHelper.findAvailablePort();
        mongodExecutable = starter.prepare(new MongodConfigBuilder()
            .version(Version.Main.PRODUCTION)
            .net(new Net(port, Network.localhostIsIPv6()))
            .build());
        mongod = mongodExecutable.start();
        rulesFileManager = new RulesManagerFileImpl(
            new DbConfigurationImpl(DATABASE_HOST, port, DATABASE_NAME));

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        mongod.stop();
        mongodExecutable.stop();
        junitHelper.releasePort(port);
    }

    @Test
    public void testRulesFileCSV() throws ReferentialException, IOException {
        rulesFileManager.checkFile(new FileInputStream(PropertiesUtils.findFile(FILE_TO_TEST_OK)));
    }

    @Test(expected = ReferentialException.class)
    public void testRulesFileCSVKO() throws ReferentialException, IOException {
        rulesFileManager.checkFile(new FileInputStream(PropertiesUtils.findFile(FILE_TO_TEST_KO)));
    }

    @Test
    public void testimportAndDeleteRulesFile() throws Exception {
        rulesFileManager.importFile(new FileInputStream(PropertiesUtils.findFile(FILE_TO_TEST_OK)));
        MongoClient client = new MongoClient(new ServerAddress(DATABASE_HOST, port));
        MongoCollection<Document> collection = client.getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME);
        assertEquals(22, collection.count());
        Select select = new Select();
        select.setQuery(eq("RuleId", "APP-00005"));
        List<FileRules> fileList = rulesFileManager.findDocuments(select.getFinalSelect());
        String id = fileList.get(0).getString("RuleId");
        FileRules file = rulesFileManager.findDocumentById(id);
        // assertEquals(file, fileList.get(0));
        rulesFileManager.deleteCollection();
        assertEquals(0, collection.count());
        client.close();
    }
}