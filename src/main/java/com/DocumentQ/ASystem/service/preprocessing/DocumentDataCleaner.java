package com.DocumentQ.ASystem.service.preprocessing;


import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.text.Normalizer;

@Component
public class DocumentDataCleaner {

    public String clean(String data) {
        if (data == null) return "";


        data = Jsoup.parse(data).text();

        data = Normalizer.normalize(data, Normalizer.Form.NFKC);

        data = data.replaceAll("(?<![.!?])\\n", " ");

        data = data.replaceAll("\\p{C}", "");

        data = data.replaceAll("\\s+", " ").trim();

        return data;
    }

}
