package com.secdec.codedx.api.client;

import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
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

    @Override
    protected <T> T doHttpRequest(HttpRequestBase request, String path, boolean isXApi, Type responseType, Object requestBody) throws IOException, CodeDxClientException {
        try {
            int fails = 0;
            while (fails < 3) {
                try {
                    try {
                        // use a clone to avoid reusing the request object
                        HttpRequestBase clonedRequest = (HttpRequestBase) request.clone();
                        return super.doHttpRequest(clonedRequest, path, isXApi, responseType, requestBody);
                    } catch (CloneNotSupportedException e){
                        throw new IOException("Could not clone request body entity: " + requestBody);
                    }
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
