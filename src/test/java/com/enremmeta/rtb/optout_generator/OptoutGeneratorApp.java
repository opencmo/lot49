package com.enremmeta.rtb.optout_generator;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;

public class OptoutGeneratorApp {

    public static void getHTML(String urlToRead, Integer time) throws Exception {
        File file = new File("optout_response_" + time.toString() + "ms.dat");
        if (!file.exists()) {
            file.createNewFile();
        }

        urlToRead = urlToRead + "?time=" + time.toString();

        URL url = new URL(urlToRead);
        // HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // conn.setRequestMethod("POST");

        String data = "";

        Socket socket = new Socket(url.getHost(), url.getPort());

        String path = url.getPath() + "?" + url.getQuery();
        BufferedWriter wr = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
        wr.write("POST " + path + " HTTP/1.0\r\n");
        wr.write("Content-Length: " + data.length() + "\r\n");
        wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
        wr.write("\r\n");
        wr.write(data);
        wr.flush();

        try (FileOutputStream stream = new FileOutputStream(file)) {
            int nRead;
            byte[] buf = new byte[16384];

            while ((nRead = socket.getInputStream().read(buf, 0, buf.length)) != -1) {
                stream.write(buf, 0, nRead);
            }

            stream.flush();
        }
        wr.close();
    }

    public static void main(String[] args) throws Exception {
        getHTML("http://localhost:10000/auction/adx_optout", 20);
    }

}
