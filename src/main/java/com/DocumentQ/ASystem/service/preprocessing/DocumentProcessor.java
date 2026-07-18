package com.DocumentQ.ASystem.service.preprocessing;

import com.DocumentQ.ASystem.utils.URLDetector;
import lombok.extern.slf4j.Slf4j;
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



@Slf4j
@Component
public class DocumentProcessor {

    @Autowired
    private URLDetector urlDetector;

    @Autowired
    private Tika tika;

    public String processURL(String url) throws IOException, TikaException {

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL is empty.");
        }

        URLDetector.URLType urlType = urlDetector.detectURLType(url);

        log.info("Detected URL type: {}", urlType);

        switch (urlType) {
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

            default:
                throw new IllegalArgumentException("Unsupported or invalid URL.");
        }
    }




    private String extractFileId(String url) {

        String regex = "(?<=/d/|id=)[a-zA-Z0-9_-]{10,}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {return matcher.group();}

        throw new IllegalArgumentException("Invalid Google Drive URL.");
    }

    private String processGoogleDrive(String url) throws IOException, TikaException {

        log.info("Processing Google Drive document.");

        String fileId = extractFileId(url);

        String downloadUrl = "https://drive.google.com/uc?export=download&id=" + fileId;

        return tika.parseToString(new URL(downloadUrl));
    }

    private String processDropBox(String url) throws IOException, TikaException {

        log.info("Processing Dropbox document.");

        String directUrl = url.replace("dl=0", "dl=1");

        return tika.parseToString(new URL(directUrl));
    }



    private String processDirectDocx(String url) throws IOException, TikaException {

        log.info("Processing DOCX document.");

        return tika.parseToString(new URL(url));
    }

    private String processDirectPdf(String url) throws IOException, TikaException {

        log.info("Processing PDF document.");

        return tika.parseToString(new URL(url));
    }



    private String processWebPage(String url) throws IOException {

        log.info("Processing web page.");

        Document document = Jsoup.connect(url).get();

        return document.body().text();
    }



    private String processArxiv(String url) throws IOException, TikaException {

        log.info("Processing Arxiv paper.");

        if (!url.contains("/pdf/")) {
            url = url.replace("/abs/", "/pdf/") + ".pdf";
        }

        return tika.parseToString(new URL(url));
    }

}

