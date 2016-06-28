package com.secdec.codedx.api.client;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by samuelj on 3/27/15.
 */
public class CodeDxRepeatingClient extends CodeDxClient {
    private PrintStream logger;

    public CodeDxRepeatingClient(CodeDxClient oldClient, PrintStream logger) {
        super(oldClient.url, oldClient.key, oldClient.httpClientBuilder);
        this.logger = logger;
    }

    /**
     * A generic get that will marshal JSON data into some type.
     * @param path Append this to the URL
     * @param typeOfT
     * @param experimental If this request is part of the experimental API
     * @return Something of type T
     * @throws ClientProtocolException
     * @throws IOException
     * @throws CodeDxClientException
     */
    protected <T> T doGet(String path, Type typeOfT, boolean experimental) throws IOException, CodeDxClientException {
        try {
            int fails = 0;
            while (fails < 3) {
                try {
                    return super.doGet(path, typeOfT, experimental);
                } catch (CodeDxClientException clientException) {
                    fails++;
                    logger.println("Attempt " + fails + " " + clientException.getMessage() + " response code: " + clientException.getHttpCode());
                    switch (fails) {
                        case 1:
                            logger.println("Trying again after 1 second");
                            Thread.sleep(1000);
                            break;
                        case 2:
                            logger.println("Trying again after 5 seconds");
                            Thread.sleep(5000);
                            break;
                        case 3:
                            logger.println("Trying again after 30 seconds");
                            Thread.sleep(30000);
                            break;
                        default:
                            throw clientException;
                    }
                } catch (SocketException socketException) {
                    fails++;
                    logger.println("Attempt " + fails + " " + socketException.getMessage());
                    switch (fails) {
                        case 1:
                            logger.println("Trying again after 1 second");
                            Thread.sleep(1000);
                            break;
                        case 2:
                            logger.println("Trying again after 5 seconds");
                            Thread.sleep(5000);
                            break;
                        case 3:
                            logger.println("Trying again after 30 seconds");
                            Thread.sleep(30000);
                            break;
                        default:
                            throw socketException;
                    }
                }

            }
        } catch (InterruptedException i) {
            logger.println("Thread was interrupted while waiting to re-attempt GET");
            throw new CodeDxClientException("GET", path, "Thread was interrupted. Unabled to finish GET", -1, "");
        }
        //This shouldn't happen, but we all know how that assumption turns out.
        throw new CodeDxClientException("GET", path, "GET was unsuccessful for " + path, -1, "");
    }
}
