package com.DocumentQ.ASystem.service.preprocessing;


import com.DocumentQ.ASystem.utils.URLDetector;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DocumentProcessor {

    @Autowired
    private URLDetector urlDetector;


    @Autowired
    private Tika tika;

    public String processURL(String url) throws TikaException, IOException {
        URLDetector.URLType urlType = urlDetector.detectURLType(url);

        System.out.println("type of url = " + urlType);

        switch(urlType)
        {
            case GOOGLE_DRIVE:
                return processGoogleDrive(url);
            case DROPBOX:
                return processDropBox(url);
            case DIRECT_DOCX:
                return processDirectDocx(url);
            case DIRECT_PDF:
                return processDirectPdf(url);
            case WEB_PAGE:
                return processWebPage(url);
            case ARXIV:
                return processArxiv(url);
            case UNKNOWN:
                return processUnknown(url);
        }

        return processUnknown(url);
    }

    private String extractFileId(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }

        // Common Google Drive file ID pattern
        String regex = "(?<=/d/|id=)[a-zA-Z0-9_-]{10,}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group();
        }

        throw new IllegalArgumentException("Invalid Google Drive URL: Unable to extract file ID");
    }


    private String processGoogleDrive(String url) throws IOException, TikaException {
        String fileId = extractFileId(url);
        String downloadUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
        return tika.parseToString(new URL(downloadUrl));
    }

    private String processDropBox(String url) throws IOException, TikaException {
        String directUrl = url.replace("dl=0", "dl=1");
        return tika.parseToString(new URL(directUrl));
    }

    private String processDirectDocx(String url) throws IOException, TikaException {
         return tika.parseToString(new URL(url));
    }

    private String processDirectPdf(String url) throws IOException, TikaException {
        return tika.parseToString(new URL(url));
    }


    private String processWebPage(String url) throws IOException, TikaException {
       Document doc = Jsoup.connect(url).get();
       return doc.body().text();
    }

    private String processArxiv(String url) throws IOException, TikaException {

        try {
            if (!url.contains("/pdf/")) {
                url = url.replace("/abs/", "/pdf/") + ".pdf";
            }
            System.out.println("value of changed url = " + url);
            String value = tika.parseToString(new URL(url));

            System.out.println(value);

            return value;
        }catch(Exception e)
        {
            System.out.println("Exception in procesarxiv method = " + e);
        }

        System.out.println("null is returned");
        return null;
    }

    private String processUnknown(String url){

        return "Invalid URL";
    }
}
