package com.DocumentQ.ASystem.utils;


import org.springframework.stereotype.Component;

@Component
public class URLDetector {

    public enum URLType{
        GOOGLE_DRIVE,
        DROPBOX,
        DIRECT_PDF,
        DIRECT_DOCX,
        WEB_PAGE,
        ARXIV,
        UNKNOWN
    }


    public URLType detectURLType(String url){

        String lowerCaseURL = url.toLowerCase();

        if(lowerCaseURL.contains("drive.google.com")){
            return URLType.GOOGLE_DRIVE;
        }
        else if (lowerCaseURL.contains("dropbox.com")) {
            return URLType.DROPBOX;
        }
        else if(lowerCaseURL.contains("arxiv.org"))
        {
            return URLType.ARXIV;
        }else if(lowerCaseURL.endsWith(".pdf"))
        {
            return URLType.DIRECT_PDF;
        }else if (lowerCaseURL.endsWith(".docx") || lowerCaseURL.endsWith(".doc")) {
            return URLType.DIRECT_DOCX;
        }

        //check is pdf is in between the url
        if (lowerCaseURL.contains(".pdf?") || lowerCaseURL.contains(".pdf#")) {
            return URLType.DIRECT_PDF;
        }

        //check for web page
        if (lowerCaseURL.startsWith("http://") || lowerCaseURL.startsWith("https://")) {
            return URLType.WEB_PAGE;
        }

        return URLType.UNKNOWN;

    }
}
