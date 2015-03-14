package com.enremmeta.rtb.proto.rawtest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.amazonaws.util.StringInputStream;
import com.enremmeta.rtb.api.proto.openrtb.OpenRtbResponse;
import com.enremmeta.rtb.constants.Lot49Constants;
import com.enremmeta.rtb.jersey.protobuf.ProtobufMessageReader;
import com.enremmeta.rtb.jersey.protobuf.ProtobufMessageWriter;
import com.google.protobuf.Message;
import com.google.protos.adx.NetworkBid;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;


public class RawRequestLogTool {

    Map<String, Builder> builders = new HashMap<String, Builder>();

    int urlIndex = 2;
    int jsonIndex = 5;
    String baseUrl = "http://localhost:10000" + Lot49Constants.ROOT_PATH_AUCTIONS + "/";
    int countRequests = 0;

    public int reuseRawRequestLog(String rawRequestLogPath, int countThreads)
                    throws InterruptedException {

        if (rawRequestLogPath != null) {

            Map<String, List<String>> requests = new HashMap<String, List<String>>();

            try {

                BufferedReader br = new BufferedReader(new FileReader(rawRequestLogPath));

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("raw")) {

                        String[] array = line.split("\t");

                        String url = array[urlIndex];
                        final String requestType = url.substring(url.lastIndexOf("/") + 1);

                        String json = array[jsonIndex].trim();

                        if (requestType != null && !requestType.isEmpty() && json != null
                                        && !json.isEmpty()) {
                            if (!requests.containsKey(requestType)) {
                                List<String> requestsList = new ArrayList<String>();
                                requests.put(requestType, requestsList);
                            }
                            requests.get(requestType).add(json);
                        }
                    }
                }

                br.close();
            } catch (IOException e) {

            }

            ExecutorService pool = Executors.newFixedThreadPool(countThreads);

            long currentTime = System.currentTimeMillis();

            for (String requestType : requests.keySet()) {
                for (String json : requests.get(requestType)) {
                    pool.submit(new Runnable() {
                        @Override
                        public void run() {
                            sendRequest(requestType, json);
                        }
                    });
                }
            }

            pool.shutdown();
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

            long periodSec = (System.currentTimeMillis() - currentTime) / 1000;
            if (countRequests > 0) {
                System.out.println("Requests: " + countRequests);
                System.out.println("QPS: " + countRequests / periodSec);
            }
        }

        return countRequests;
    }

    private void sendRequest(String requestType, String json) {
        switch (requestType) {
            case "adx":
                sendAdxRequest(json);
                break;
            case "pubmatic":
                sendPabmaticRequest(json);
                break;
            default:
                // sendOtherRequest(url, json);
        }
    }

    private void sendPabmaticRequest(String json) {
        try {

            Builder builder = builders.get(Lot49Constants.EXCHANGE_PUBMATIC);

            if (builder == null) {
                Client client = ClientBuilder.newClient();

                String url = baseUrl + Lot49Constants.EXCHANGE_PUBMATIC;

                builder = client.target(url).request(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON);
            }

            OpenRtbResponse resp = builder.post(Entity.entity(json, MediaType.APPLICATION_JSON),
                            OpenRtbResponse.class);

            countRequests++;

        } catch (BadRequestException bre) {
            MultivaluedMap<String, Object> headers = bre.getResponse().getHeaders();
            for (String key : headers.keySet()) {
                System.out.println(key + ": " + headers.get(key));
            }
        }
    }

    private void sendAdxRequest(String json) {

        try {
            json = clearString(json, ", \"54\"");
            json = clearString(json, ", \"53\"");
            json = clearString(json, ", \"52\"");
            json = clearString(json, ", \"52\"");
            json = clearString(json, ", \"25\"");
            json = clearString(json, ", \"23\"");

            if (json.indexOf(",\"deals\":[]") >= 0) {
                json = json.replace(",\"deals\":[]", "");
            }

            Message.Builder requestBuilder = NetworkBid.BidRequest.newBuilder();
            JsonFormat jf = new JsonFormat();
            jf.merge(new StringInputStream(json), requestBuilder);
            NetworkBid.BidRequest req = (NetworkBid.BidRequest) requestBuilder.build();

            Builder builder = builders.get(Lot49Constants.EXCHANGE_ADX);

            if (builder == null) {
                Client client = ClientBuilder.newClient().register(ProtobufMessageReader.class)
                                .register(ProtobufMessageWriter.class);

                String url = baseUrl + Lot49Constants.EXCHANGE_ADX;

                builder = client.target(url).request(MediaType.APPLICATION_OCTET_STREAM)
                                .accept(MediaType.APPLICATION_OCTET_STREAM)
                                .header("X-OPENRTB-VERSION", "2.1");

            }

            NetworkBid.BidResponse resp =
                            builder.post(Entity.entity(req, MediaType.APPLICATION_OCTET_STREAM),
                                            NetworkBid.BidResponse.class);
            countRequests++;

        } catch (ParseException e) {
            // System.out.println("ParseException: " + e.getMessage() + "\n JSON: " + json);
            // e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String clearString(String str, String removedStr) {
        int pos = str.indexOf(removedStr);
        if (pos >= 0) {
            str = str.replace(str.substring(pos, str.indexOf("]", pos) + 1), "");
        }
        return str;
    }
}
