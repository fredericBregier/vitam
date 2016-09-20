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
package fr.gouv.vitam.common.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.google.common.collect.ObjectArrays;

import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.exception.VitamException;

/**
 * SSL Configuration
 */
public class SSLConfiguration {

    private static final String PARAMETERS = "SSLConfiguration parameters";
    private List<SSLKey> truststore;
    private List<SSLKey> keystore;

    /**
     * Empty SSLConfiguration constructor for YAMLFactory
     */

    public SSLConfiguration() {
        // Empty
    }

    /**
     * SSLConfiguration Constructor
     * 
     * @param trustConfigs
     * @param keyConfig
     * @throws IllegalArgumentException if keystore/truststore is null or empty
     */
    public SSLConfiguration(List<SSLKey> keystore, List<SSLKey> truststore) {
        ParametersChecker.checkParameter(PARAMETERS, truststore, keystore);
        this.truststore = truststore;
        this.keystore = keystore;
    }

    /**
     * @return SSL Context
     * @throws VitamException
     */
    public SSLContext createSSLContext() throws VitamException {

        // TODO use JKS Keystore
        KeyManager[] keyManagers = null;
        if (keystore != null) {
            keyManagers = readKeyManagers();
        }
        TrustManager[] trustManagers = null;
        if (truststore != null) {
            trustManagers = readTrustManagers();
        } else {
            trustManagers = loadTrustManagers();
        }
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, new java.security.SecureRandom());
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new VitamException(e.getMessage(), e);
        }


    }



    /**
     * Converts pwd string to a new character array.
     * 
     * @param pwd
     * @return character array
     * @throws VitamException
     * @throws IllegalArgumentException if pwd is null or empty
     */
    private char[] readPassword(String pwd) throws VitamException {
        ParametersChecker.checkParameter(PARAMETERS, pwd);
        return pwd.toCharArray();
    }

    /**
     * get the File associated with this filename, trying in this order: as fullpath, as in Vitam Config Folder, as
     * Resources file
     * 
     * @param filePath
     * @return the File if found
     * @throws VitamException
     * @throws IllegalArgumentException if filePath is null or empty
     */
    private InputStream readInputStream(String filePath) throws VitamException {
        ParametersChecker.checkParameter(PARAMETERS, filePath);
        try {
            return new FileInputStream(PropertiesUtils.findFile(filePath));
        } catch (FileNotFoundException e) {
            throw new VitamException(e.getMessage(), e);
        }
    }


    /**
     * load Key Managers
     * 
     * @param filePath
     * @param pwd
     * @return key managers
     * @throws VitamException
     * @throws IllegalArgumentException if filePath/pwd is null or empty
     */
    private KeyManager[] loadKeyManagers(String filePath, String pwd) throws VitamException {
        ParametersChecker.checkParameter(PARAMETERS, filePath, pwd);
        char[] password = readPassword(pwd);
        try (InputStream keyInputStream = readInputStream(filePath)) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keyInputStream, password);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, password);
            return kmf.getKeyManagers();
        } catch (IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException |
            KeyStoreException e) {
            throw new VitamException(e.getMessage(), e);
        }
    }

    /**
     * load Trust Managers
     * 
     * @param filePath
     * @param pwd
     * @return Trust Managers
     * @throws VitamException
     * @throws IllegalArgumentException if filePath/pwd is null or empty
     */
    private TrustManager[] loadTrustManagers(String filePath, String pwd) throws VitamException {

        ParametersChecker.checkParameter(PARAMETERS, filePath, pwd);
        char[] password = readPassword(pwd);
        try (InputStream trustInputStream = readInputStream(filePath)) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(trustInputStream, password);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            return tmf.getTrustManagers();
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new VitamException(e.getMessage(), e);
        }

    }


    /**
     * load Trust Managers
     * 
     * @return Trust Managers
     * @throws VitamException
     */
    private TrustManager[] loadTrustManagers() throws VitamException {
        TrustManager[] trustManager = new TrustManager[] {new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] arg0,
                String arg1) throws CertificateException {}

            public void checkServerTrusted(X509Certificate[] arg0,
                String arg1) throws CertificateException {}

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};

        return trustManager;
    }

    /**
     * read Trust Managers
     * 
     * @return Trust Managers
     * @throws VitamException
     */
    private TrustManager[] readTrustManagers() throws VitamException {
        TrustManager[] result = ObjectArrays.newArray(TrustManager.class, 0);
        for (SSLKey key : truststore) {
            result = ObjectArrays.concat(result, loadTrustManagers(key.getKeyPath(), key.getKeyPassword()),
                TrustManager.class);
        }
        return result;
    }

    /**
     * read Key Managers
     * 
     * @return Key Managers
     * @throws VitamException
     */
    private KeyManager[] readKeyManagers() throws VitamException {
        KeyManager[] result = ObjectArrays.newArray(KeyManager.class, 0);
        for (SSLKey key : keystore) {
            result =
                ObjectArrays.concat(result, loadKeyManagers(key.getKeyPath(), key.getKeyPassword()), KeyManager.class);
        }
        return result;
    }

    /**
     * @return the truststore
     */
    public List<SSLKey> getTruststore() {
        return truststore;
    }

    /**
     * @return the keystore
     */
    public List<SSLKey> getKeystore() {
        return keystore;
    }

    /**
     * @param truststore the truststore to set
     *
     * @return this
     */
    public SSLConfiguration setTruststore(List<SSLKey> truststore) {
        this.truststore = truststore;
        return this;
    }

    /**
     * @param keystore the keystore to set
     *
     * @return this
     */
    public SSLConfiguration setKeystore(List<SSLKey> keystore) {
        this.keystore = keystore;
        return this;
    }

}