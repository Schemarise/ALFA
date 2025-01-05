package com.schemarise.alfa.generators.common;

import com.schemarise.alfa.compiler.utils.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


public class HttpDownloadUtil {
    private static final int BUFFER_SIZE = 4096;

    public static Path downloadFile(String fileURL, Path saveDir)
            throws IOException, AlfaHttpException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

        try {
            int responseCode = httpConn.getResponseCode();


            if (responseCode == HttpURLConnection.HTTP_OK) {
                String fileName = "";
                String disposition = httpConn.getHeaderField("Content-Disposition");

                if (disposition != null) {
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = TextUtils.removeQuotes(disposition.substring(index + 9));
                    }
                } else {
                    fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
                }

                InputStream inputStream = httpConn.getInputStream();
                Path saveFilePath = saveDir.resolve(fileName);

                Files.write(saveFilePath, new byte[0], StandardOpenOption.CREATE);

                int bytesRead = -1;
                byte[] buffer = new byte[BUFFER_SIZE];

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byte[] read = new byte[bytesRead];
                    System.arraycopy(buffer, 0, read, 0, bytesRead);

                    Files.write(saveFilePath, read, StandardOpenOption.APPEND);
                }

                inputStream.close();

                return saveFilePath;
            } else {
                String msg = "Download failed " + fileURL + ". Server replied with HTTP error code: " + responseCode + ".";
                throw new AlfaHttpException(msg);
            }
        } catch (Throwable t) {
            if (t instanceof AlfaHttpException)
                throw t;

            throw new AlfaHttpException("Download failed " + fileURL + " - " + t.getClass().getName() + " : " + t.getLocalizedMessage());

        } finally {
            httpConn.disconnect();
        }
    }
}