package rest;

import com.google.gson.Gson;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import file.ExcelReader;
import file.PDFReader;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RestServer implements Rest {

    Gson gson = new Gson();

    public String analyzePDF(InputStream uploadedInputStream) {

        try {
            writeToFile(uploadedInputStream, "D:\\Estagio\\test.pdf");
            Map<String, List<String>> requirements = PDFReader.parsePDF("D:\\Estagio\\test.pdf");
            List<String> textList = new LinkedList<>();
            for(String key : requirements.keySet()) {
                StringBuilder str = new StringBuilder();
                requirements.get(key).forEach(line ->{
                    str.append(line + " ");
                });
                textList.add(str.toString());
            }
            String[] array = new String[textList.size()]; // return gson.toJson(ClassificationRequest.response(textList.toArray(array)));
            return gson.toJson(textList.toArray(array));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }



    public String analyzeExcel(InputStream uploadedInputStream) {
        try {
            writeToFile(uploadedInputStream, "D:\\Estagio\\test.xlsx");
            //String str = gson.toJson(ExcelReader.parseExcel(data));
            return gson.toJson(ExcelReader.parseExcel("D:\\Estagio\\test.xlsx"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void writeToFile(InputStream uploadedInputStream,
                             String uploadedFileLocation) {

        try {
            OutputStream out = new FileOutputStream(new File(
                    uploadedFileLocation));
            int read = 0;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        String URI_BASE = "http://localhost:9999/";
        System.out.println(URI_BASE);
        ResourceConfig config = new ResourceConfig();
        config.register(new RestServer());

        JdkHttpServerFactory.createHttpServer(URI.create(URI_BASE), config);

        System.err.println("Server ready....");
    }

}


