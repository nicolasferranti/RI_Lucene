/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ri.trabri;

import java.io.FileNotFoundException;

import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import org.apache.lucene.util.Version;

/**
 *
 * @author nicolas
 */
public class LuceneAbordagem2 extends Lucene {

    static String output = "out.csv";
    static String[] typesGood = {"AU", "TI", "SO", "MJ", "MN", "AB", "EX"};

    public LuceneAbordagem2() throws IOException {
        super();
    }

    @Override
    public void Indexar(ArrayList<Documento> docums) throws IOException, org.apache.lucene.queryparser.classic.ParseException {

        for (Documento d : docums) {
            addDoc(w, d);
        }

        w.close();

    }

    /*
        Customização da adição de um documento, identificando as tags e alterando
        o boost de alguns atributos para teste de desempenho
    */
    private void addDoc(IndexWriter w, Documento d) throws IOException {
        Document doc = new Document();
        for (Map.Entry<String, String> entry : d.atributos.entrySet()) {

            TextField t = new TextField(entry.getKey(), getStemTokens(entry.getValue()), Field.Store.YES);
            switch (entry.getKey()) {
                case "RN":
                    doc.add(new StringField(entry.getKey(), entry.getValue(), Field.Store.YES));
                    break;
                case "AU":

                    break;
                case "TI":
                    t.setBoost(.4f);
                    break;
                case "SO":
                    break;
                case "MJ":
                    t.setBoost(.8f);
                    break;
                case "MN":
                    t.setBoost(.8f);
                    break;
                case "AB":
                    t.setBoost(.6f);
                    break;

                case "EX":
                    t.setBoost(.6f);
                    break;
            }
            if (!entry.getKey().equals("RN")) {
                doc.add(t);
            }
        }

        w.addDocument(doc);
    }

    @Override
    /*
        Busca Multicampos implementada limitando o resultado baseado em sua diferença
        para o primeiro(maior score)
    */
    public ArrayList<String> search(String querystr) throws IOException, ParseException {
        QueryParser queryParser;
        queryParser = new MultiFieldQueryParser(Version.LUCENE_40, typesGood, analyzer);

        Query q = queryParser.parse(querystr);

        int hitsPerPage = 1000;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;
        ArrayList<String> result = new ArrayList<>();
        float bestScore = hits[0].score;
        // 4. display results
        int count = 0;
        for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            if (bestScore - hits[i].score < bestScore * 0.9) {
                count++;
                Document d = searcher.doc(docId);
                result.add(d.get("RN"));
                System.out.println((i + 1) + ". " + d.get("RN") + " |score :" + hits[i].score); //+ "\t" + d.get("data"));
            }
        }
        System.out.println("Found " + count + " hits.");

        reader.close();
        for (int i = 0; i < result.size(); ++i) {
            //tira os zeros a esquerda
            result.set(i, result.get(i).replaceFirst("^0+(?!$)", "").replaceAll(" ", ""));
        }
        return result;
    }

    public void precisionAndRecall(ArrayList<String> result, ArrayList<String> relevantDocs, String id) throws FileNotFoundException, IOException {

        int countRelevants = 0;
        for (int i = 0; i < result.size(); ++i) {
            if (relevantDocs.contains(result.get(i))) {
                ++countRelevants;
            }

        }
        float precisao = (float) countRelevants / result.size();
        float recall = (float) countRelevants / relevantDocs.size();
        float fmeasure = 2 * ((float) (precisao * recall) / (precisao + recall));

        try (FileWriter pw = new FileWriter(output, true)) {
            StringBuilder sb = new StringBuilder();
            sb.append(id);
            sb.append(",");
            sb.append(precisao);
            sb.append(",");
            sb.append(recall);
            sb.append(",");
            sb.append(fmeasure);
            sb.append("\n");

            pw.append(sb.toString());
        }

        System.out.println("precisao: " + precisao);
        System.out.println("REcall: " + recall);
        System.out.println("F-measure: " + fmeasure);
    }

}
