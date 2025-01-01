package de.cubeattack.api.util;


import de.cubeattack.api.logger.LogManager;
import okhttp3.*;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class RestAPIUtils {
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();


    public Response request(String methode, String url, RequestBody requestBody){
        if(methode.startsWith("GET")){
            return callRequest(defaultBuilder().url(url).build());
        }else if(methode.startsWith("POST")) {
            return callRequest(defaultBuilder().url(url).post(requestBody).build());
        }else {
            return callRequest(defaultBuilder().url(url).delete().build());
        }
    }

    protected Response callRequest(Request request){
        try {
            return client.newCall(request).execute();
        }catch (UnknownHostException | SocketTimeoutException | SocketException connectionException){
            LogManager.getLogger().error( request + " failed cause (" + connectionException + ")");
            return new Response.Builder().request(request).protocol(Protocol.HTTP_2).code(500).message(connectionException.getMessage()).build();
        } catch (Exception exception){
            exception.printStackTrace();
        }
        return null;
    }

    protected Request.Builder defaultBuilder(){
        return new Request.Builder()
                .addHeader("accept", "*/*")
                .addHeader("Content-Type", "application/json");
    }
}
