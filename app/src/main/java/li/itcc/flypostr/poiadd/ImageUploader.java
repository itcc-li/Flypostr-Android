package li.itcc.flypostr.poiadd;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import li.itcc.flypostr.config.CloudEndpoint;
import li.itcc.flypostr.services.PoiServices;
import li.itcc.flypostr.util.StreamUtil;

/**
 * Created by Arthur on 19.09.2015.
 */
public class ImageUploader {

    private static final String BOUNDARY = "ayfQuegbhrmjYtdLwPdsfitergKyhbwjAM25z9AJuGSx7WG9dnD";
    private static final int JUNK_SIZE = 10000;
    private final PoiServices fServices;

    public ImageUploader(PoiServices services) {
        fServices = services;
    }

    public String uploadImage(File imageFile) throws Exception {
        if (!imageFile.exists() || !imageFile.canRead()) {
            return null;
        }
        long fileSize = imageFile.length();
        if (fileSize == 0) {
            return null;
        }
        InputStream inputStream = new FileInputStream(imageFile);
        return uploadImage(inputStream);
    }

    public String uploadImage(InputStream inputStream) throws Exception {
        // stream is ready, get url to upload
        String postURL = fServices.getImageUploadUrl().getUrl();
        // patch the url for local testing
        postURL = CloudEndpoint.dnsHack(postURL);
        URL url = new URL(postURL);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(60000);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        // stream the upload in junks so that we don't load the whole file in memory
        // this does not work
        //connection.setChunkedStreamingMode(JUNK_SIZE);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("X-AppEngine-BlobUpload", "true");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        OutputStream outBin = connection.getOutputStream();
        //attach post objects
        DataOutputStream outputStream = new DataOutputStream(outBin);
        try {
            //build the request body
            outputStream.writeBytes("--" + BOUNDARY + "\r\n" +
                    "Content-Disposition: form-data; name=\"image\"; filename=\"image.jpg\"\r\n" +
                    "Content-Type: image/jpeg\r\n" +
                    "\r\n");
            StreamUtil.pumpAllAndClose(inputStream, outputStream, false);
            // write final boundary
            outputStream.writeBytes("\r\n--" + BOUNDARY + "--\r\n");
            outputStream.flush();
        }
        finally {
            outputStream.close();
        }
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpsURLConnection.HTTP_OK) {
            String response = readResponse(connection);
            return response;
        }
        throw new IOException("Image upload failed with code " + responseCode);
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        StringBuffer response = new StringBuffer();
        InputStreamReader reader = new InputStreamReader(new BufferedInputStream(connection.getInputStream()), "utf-8");
        int ch;
        while ((ch = reader.read()) != -1) {
            response.append((char)ch);
        }
        return response.toString();
    }

}
