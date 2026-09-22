package com.DocumentQ.ASystem.service.preprocessing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Component;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentChunk {



    private final EmbeddingModel embeddingModel;




    // Creates semantic chunks using sentence embeddings.
    public List<String> createChunks(String text) {

        log.info("Starting semantic chunk generation.");

        List<Document> sentences = breakText(text);

        // If there is only one sentence
        if (sentences.size() == 1) {
            return List.of(sentences.getFirst().getText());
        }

        List<float[]> embeddings = new ArrayList<>();

        log.info("Generating embeddings.");
        for (Document sentence : sentences) {

            float[] embedding = embeddingModel.embed(sentence);

            if (embedding == null || embedding.length == 0) {
                throw new IllegalStateException("Failed to generate embedding for sentence.");
            }

            embeddings.add(embedding);
        }

        log.info("Generated {} embeddings.", embeddings.size());

        List<String> chunks = new ArrayList<>();

        StringBuilder currentChunk = new StringBuilder(sentences.getFirst().getText());

        final double THRESHOLD = 0.50;

        for (int i = 0; i < embeddings.size() - 1; i++) {

            double similarity = cosineSimilarity(embeddings.get(i), embeddings.get(i + 1));

            if (similarity < THRESHOLD) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder(sentences.get(i + 1).getText());
            } else {
                currentChunk.append(" ").append(sentences.get(i + 1).getText());
            }
        }

        chunks.add(currentChunk.toString().trim());

        log.info("Semantic chunking completed. Total chunks created: {}", chunks.size());

        return chunks;
    }









    // Breaks the document into sentences.

    public List<Document> breakText(String text) {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Document text is empty.");
        }

        BreakIterator it = BreakIterator.getSentenceInstance(Locale.ENGLISH);
        it.setText(text);

        int start = it.first();
        int end = it.next();

        List<Document> sentences = new ArrayList<>();

        while (end != BreakIterator.DONE) {

            String sentence = text.substring(start, end).trim();

            //System.out.println(sentence);

            if (!sentence.isEmpty()) {
                sentences.add(new Document(sentence));
            }

            start = end;
            end = it.next();
        }

        if (sentences.isEmpty()) {
            throw new IllegalArgumentException("No valid sentences found in document.");
        }

        log.info("Total sentences created: {}", sentences.size());

        return sentences;
    }




    //  Calculates cosine similarity between two embedding vectors.

    private double cosineSimilarity(float[] vectorA, float[] vectorB) {

        double dotProduct = 0;
        double normA = 0;
        double normB = 0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        if (normA == 0 || normB == 0) {
            return 0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }



}

