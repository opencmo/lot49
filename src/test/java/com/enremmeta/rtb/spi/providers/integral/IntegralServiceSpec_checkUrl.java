package com.enremmeta.rtb.spi.providers.integral;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.enremmeta.rtb.LogUtils;
import com.enremmeta.rtb.spi.providers.integral.result.dto.IntegralAllResponse;
import com.enremmeta.rtb.spi.providers.integral.result.dto.IntegralScoresDto;
import com.enremmeta.rtb.spi.providers.integral.result.dto.ViewabilityDto;
import com.google.gson.Gson;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CloseableHttpAsyncClient.class, LogUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*", "javax.net.ssl.*"})
public class IntegralServiceSpec_checkUrl {

    @Test
    public void negativeFlow_throwNPEOnNullUrl() {
        IntegralConfig integralConfig = new IntegralConfig();
        IntegralService is = new IntegralService(integralConfig);

        IntegralInfoReceived iir = new IntegralInfoReceived();
        String url = null;
        try {
            is.checkUrl(url, iir);
            fail("NPE expected but not happen");
        } catch (Exception e) {
            assertEquals(NullPointerException.class, e.getClass());
        }

    }

    @Test
    public void negativeFlow_integralClientSendFailed() {
        IntegralConfig integralConfig = new IntegralConfig();
        IntegralService is = new IntegralService(integralConfig);

        IntegralInfoReceived iir = new IntegralInfoReceived();
        String url = "";

        is.checkUrl(url, iir);

        assertTrue(iir.getErrorMsg().contains("Integral: could not get info for url"));
        assertTrue(iir.isCompleted());
    }

    @Test
    public void negativeFlow_wrongJsonInResponse() {
        IntegralConfig integralConfig = new IntegralConfig();
        IntegralService is = new IntegralService(integralConfig);

        IntegralInfoReceived iir = new IntegralInfoReceived();
        String url = "";

        IntegralClient2 icSpy = (IntegralClient2) Whitebox.getInternalState(is, "integralClient");
        CloseableHttpAsyncClient httpAsyncClientSpy =
                        PowerMockito.spy((CloseableHttpAsyncClient) Whitebox.getInternalState(icSpy,
                                        "httpAsyncClient"));
        PowerMockito.doReturn(Mockito.mock(Future.class)).when(httpAsyncClientSpy)
                        .execute(Mockito.any(HttpGet.class), Mockito.any(FutureCallback.class));

        Whitebox.setInternalState(icSpy, "httpAsyncClient", httpAsyncClientSpy);

        is.checkUrl(url, iir);

        ArgumentCaptor<FutureCallback> responseCaptor =
                        ArgumentCaptor.forClass(FutureCallback.class);
        verify(httpAsyncClientSpy, times(1)).execute(Mockito.any(HttpGet.class),
                        responseCaptor.capture());
        FutureCallback<HttpResponse> responseFuture = responseCaptor.getValue();

        HttpResponse httpResponseMock = Mockito.mock(HttpResponse.class);
        Mockito.when(httpResponseMock.getStatusLine()).thenReturn(testStatusLine);
        Mockito.when(httpResponseMock.getEntity()).thenReturn(new TestHttpEntity(){

            @Override
            public InputStream getContent() throws IOException, IllegalStateException {
                InputStream stream = new ByteArrayInputStream(
                                "WRONG_JSON_RESPONSE".getBytes(StandardCharsets.UTF_8));
                return stream;
            }
            
        });

        PowerMockito.mockStatic(LogUtils.class);
        PowerMockito.doNothing().when(LogUtils.class);
        LogUtils.error(anyString());
        
        // simulate successful response from the server
        responseFuture.completed(httpResponseMock);

        assertTrue(iir.getErrorMsg().contains("Integral: could not get info for url"));
        assertTrue(iir.isCompleted());
    }

    @Test
    public void positiveFlow_correctJsonInResponse() {
       
        IntegralAllResponse integralAllResponse = constructGraphOfPOJOs();
        Gson gson = new Gson();

        String test_json = gson.toJson(integralAllResponse);

        IntegralConfig integralConfig = new IntegralConfig();
        IntegralService is = new IntegralService(integralConfig);

        IntegralInfoReceived iir = new IntegralInfoReceived();
        String url = "";

        IntegralClient2 icSpy = (IntegralClient2) Whitebox.getInternalState(is, "integralClient");
        CloseableHttpAsyncClient httpAsyncClientSpy =
                        PowerMockito.spy((CloseableHttpAsyncClient) Whitebox.getInternalState(icSpy,
                                        "httpAsyncClient"));
        PowerMockito.doReturn(Mockito.mock(Future.class)).when(httpAsyncClientSpy)
                        .execute(Mockito.any(HttpGet.class), Mockito.any(FutureCallback.class));

        Whitebox.setInternalState(icSpy, "httpAsyncClient", httpAsyncClientSpy);

        is.checkUrl(url, iir);

        ArgumentCaptor<FutureCallback> responseCaptor =
                        ArgumentCaptor.forClass(FutureCallback.class);
        verify(httpAsyncClientSpy, times(1)).execute(Mockito.any(HttpGet.class),
                        responseCaptor.capture());
        FutureCallback<HttpResponse> responseFuture = responseCaptor.getValue();

        HttpResponse httpResponseMock = Mockito.mock(HttpResponse.class);
        Mockito.when(httpResponseMock.getStatusLine()).thenReturn(testStatusLine);
        Mockito.when(httpResponseMock.getEntity()).thenReturn(new TestHttpEntity() {

            @Override
            public InputStream getContent() throws IOException, IllegalStateException {
                InputStream stream = new ByteArrayInputStream(
                                test_json.getBytes(StandardCharsets.UTF_8));
                return stream;
            }

        });

        // simulate successful response from the server
        responseFuture.completed(httpResponseMock);

        assertNull(iir.getErrorMsg());
        assertEquals(expectedJson, iir.getResponseJson());
        assertTrue(iir.isCompleted());
    }

    private IntegralAllResponse constructGraphOfPOJOs() {
        
        IntegralScoresDto integralScoresDto = new IntegralScoresDto();
        
        try {
            Class c = Class.forName("com.enremmeta.rtb.spi.providers.integral.result.dto.IntegralScoresDto");
            Method m[] = c.getMethods();
            for (int i = 0; i < m.length; i++)
             if (m[i].getName().startsWith("set"))
                 if(m[i].getGenericParameterTypes()[0].equals(java.lang.Integer.class))
                     m[i].invoke(integralScoresDto, 101);
            for (int i = 0; i < m.length; i++)
                if (m[i].getName().startsWith("get"))
                    if(m[i].getReturnType().equals(java.lang.Integer.class))
                        assertEquals(101, m[i].invoke(integralScoresDto));
         }
         catch (Throwable e) {
            fail("IntegralScoresDto fixture error: " + e.toString());
         }
        
        ViewabilityDto viewabilityDto = new ViewabilityDto();
        
        try {
            Class c = Class.forName("com.enremmeta.rtb.spi.providers.integral.result.dto.ViewabilityDto");
            Method m[] = c.getMethods();
            for (int i = 0; i < m.length; i++)
             if (m[i].getName().startsWith("set"))
                 if(m[i].getGenericParameterTypes()[0].equals(java.lang.Integer.class))
                     m[i].invoke(viewabilityDto, 202);
            for (int i = 0; i < m.length; i++)
                if (m[i].getName().startsWith("get"))
                    if(m[i].getReturnType().equals(java.lang.Integer.class))
                        assertEquals(202, m[i].invoke(viewabilityDto));
         }
         catch (Throwable e) {
            fail("ViewabilityDto fixture error: " + e.toString());
         }
        
        IntegralAllResponse integralAllResponse = new IntegralAllResponse();
        
        try {
            Class c = Class.forName("com.enremmeta.rtb.spi.providers.integral.result.dto.IntegralAllResponse");
            Method m[] = c.getMethods();
            for (int i = 0; i < m.length; i++)
             if (m[i].getName().startsWith("set")){
                 if(m[i].getGenericParameterTypes()[0].equals(java.lang.Integer.class))
                     m[i].invoke(integralAllResponse, 10101);
                 if(m[i].getGenericParameterTypes()[0].equals(java.lang.String.class))
                     m[i].invoke(integralAllResponse, "TEST_VLUE");
             }
            for (int i = 0; i < m.length; i++)
                if (m[i].getName().startsWith("get")){
                    if(m[i].getReturnType().equals(java.lang.Integer.class))
                        assertEquals(10101, m[i].invoke(integralAllResponse));
                    if(m[i].getReturnType().equals(java.lang.String.class))
                        assertEquals("TEST_VLUE", m[i].invoke(integralAllResponse));
                }
         }
         catch (Throwable e) {
            fail("IntegralAllResponse fixture error: " + e.toString());
         }
        
        integralAllResponse.setScores(integralScoresDto);
        integralAllResponse.setUem(viewabilityDto);
        
        return integralAllResponse;
    }

    private StatusLine testStatusLine = new StatusLine() {

        @Override
        public int getStatusCode() {
            return 200;
        }

        @Override
        public String getReasonPhrase() {
            return null;
        }

        @Override
        public ProtocolVersion getProtocolVersion() {
            return null;
        }
    };
    
    private abstract class TestHttpEntity implements HttpEntity {

        @Override
        public void writeTo(OutputStream arg0) throws IOException {}

        @Override
        public boolean isStreaming() {
            return false;
        }

        @Override
        public boolean isRepeatable() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isChunked() {
            return false;
        }

        @Override
        public Header getContentType() {
            return null;
        }

        @Override
        public long getContentLength() {
            return 0;
        }

        @Override
        public Header getContentEncoding() {
            return null;
        }

        @Override
        public abstract InputStream getContent() throws IOException, IllegalStateException;

        @Override
        public void consumeContent() throws IOException {}
    }
    
    private final String expectedJson = 
                    "{\"country\":\"TEST_VLUE\",\"dma\":\"TEST_VLUE\",\"lang\":\"TEST_VLUE\","
                    + "\"clu\":10101,\"action\":\"TEST_VLUE\",\"risk\":\"TEST_VLUE\",\"si\":\"TEST_VLUE\","
                    + "\"state\":\"TEST_VLUE\",\"traq\":10101,\"scores\":{\"adt\":101,\"alc\":101,\"arf\":101,"
                    + "\"dlm\":101,\"drg\":101,\"hat\":101,\"iab_business\":101,\"iab_news\":101,"
                    + "\"iab_religion\":101,\"iab_travel\":101,\"iv2\":101,\"iv3\":101,\"iviab\":101,"
                    + "\"iviab_160x600\":101,\"iviab_300x250\":101,\"iviab_728x90\":101,\"ivl\":101,"
                    + "\"ivl_160x600\":101,\"ivl_300x250\":101,\"ivl_728x90\":101,\"ivp\":101,"
                    + "\"ivp_160x600\":101,\"ivp_300x250\":101,\"ivp_728x90\":101,\"ivt\":101,\"ivu\":101,"
                    + "\"jdl0426\":101,\"jha0420\":101,\"jof0427\":101,\"lang\":101,\"niv\":101,\"off\":101,"
                    + "\"off1220\":101,\"pac\":101,\"par\":101,\"pol\":101,\"pro\":101,\"rsa\":101,"
                    + "\"sam\":101,\"top\":101,\"trq\":101,\"ugb\":101,\"ugc\":101,\"ugd\":101,\"ugf\":101,"
                    + "\"ugm\":101,\"ugs\":101,\"ugt\":101,\"v_ap\":101,\"v_c\":101,\"v_h250\":101,"
                    + "\"v_r6_5\":101,\"v_s1\":101,\"v_w300\":101,\"vio\":101,\"vio1029\":101,"
                    + "\"visibility\":101,\"viv2\":101,\"webmail\":101,\"zacc\":101,\"zboa\":101,"
                    + "\"zcke\":101,\"zcmt\":101,\"zfos\":101,\"zgp\":101,\"zibm\":101,\"zmer\":101,"
                    + "\"ztraf\":101,\"zult\":101,\"zver\":101,\"zvzn\":101},\"uem\":{\"iviab\":202,"
                    + "\"ivl\":202,\"ivp\":202,\"ivt\":202,\"ivu\":202,\"niv\":202,\"top\":202}}";   
}
