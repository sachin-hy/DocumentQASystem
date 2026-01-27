package com.DocumentQ.ASystem.service.preprocessing;



import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;


@Component
public class DocumentChunk {

    @Autowired
    private EmbeddingModel embeddingModel;




    public List<Document>  breakText(String text)
    {
        BreakIterator it = BreakIterator.getSentenceInstance(Locale.ENGLISH);

        it.setText(text);

        int start = it.first();
        int end = it.next();

        List<Document> list = new ArrayList<>();

        while(end != BreakIterator.DONE)
        {
            list.add(new Document(text.substring(start, end)));
            start = end;
            end = it.next();
        }


        return list;
    }



    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }


    public List<String> createChunks(String text)
    {

        List<Document> sentences =   breakText(text);

        List<float[]> embeddings = new ArrayList<>();

        for(Document sentence : sentences)
        {
            embeddings.add(embeddingModel.embed(sentence.getText()));
        }


         System.out.println(embeddings.size());

        StringBuilder str = new StringBuilder(sentences.getFirst().getText());

        List<String> chunks = new ArrayList<>();

        for(int i=0; i < embeddings.size() - 1; i++)
        {
            double similarity = cosineSimilarity(embeddings.get(i), embeddings.get(i + 1));

            System.out.println(similarity);
            if(similarity < 0.5)
            {
                chunks.add(str.toString());
                str = new StringBuilder(sentences.get(i+1).getText());

            }else{
                str.append(" ").append(sentences.get(i+1).getText());
            }
        }

        chunks.add(str.toString());

        return  chunks;
    }

}
