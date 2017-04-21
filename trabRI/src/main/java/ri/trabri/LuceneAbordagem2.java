/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ri.trabri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.PorterStemmer;

/**
 *
 * @author nicolas
 */
public class LuceneAbordagem2 {
    
    static String output = "out.csv";
    static String[] typesGood = {"AU", "TI", "SO", "MJ", "MN", "AB", "EX"};

    public LuceneAbordagem2() throws IOException {

    }

    public static String stemText(String word) {

        PorterStemmer stem = new PorterStemmer();
        stem.setCurrent(word);
        stem.stem();
        String result = stem.getCurrent();
        return result;
    }

    public static ArrayList<String> geraTokens(StandardAnalyzer analyzer, String text) throws IOException {
        TokenStream stream = analyzer.tokenStream(null, new StringReader(text));
        ArrayList<String> words = new ArrayList<>();

        CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
        stream.reset();
        while (stream.incrementToken()) {
            //System.out.println(cattr.toString());
            words.add(cattr.toString());
        }
        stream.end();
        stream.close();
        return words;
    }

    public static String getStemTokens(StandardAnalyzer analyzer, String text) throws IOException {
        ArrayList<String> words = geraTokens(analyzer, text);
        String data = "";
        for (String word : words) {
            data += stemText(word) + " ";
        }
        return data;
    }

    public static void tester(ArrayList<Documento> docums) throws IOException, org.apache.lucene.queryparser.classic.ParseException {

        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);

        Directory index = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);

        IndexWriter w = new IndexWriter(index, config);
        for (Documento d : docums) {
            addDoc(w, d, analyzer);
        }

        w.close();
        readQueries(analyzer, index);

    }

    private static void addDoc(IndexWriter w, Documento d, StandardAnalyzer analyzer) throws IOException {
        Document doc = new Document();
        for (Map.Entry<String, String> entry : d.atributos.entrySet()) {

            TextField t = new TextField(entry.getKey(), getStemTokens(analyzer, entry.getValue()), Field.Store.YES);
            switch (entry.getKey()) {
                case "RN":
                    doc.add(new StringField(entry.getKey(), entry.getValue(), Field.Store.YES));
                    break;
                case "AU":
                 
                    break;
                case "TI":
                    t.setBoost(.8f);
                    break;
                case "SO":
                    break;
                case "MJ":
                    break;
                case "MN":
                    break;
                case "AB":
                    t.setBoost(.4f);
                    break;

                case "EX":
                    t.setBoost(.4f);
                    break;
            }
            if (!entry.getKey().equals("RN")) {
                doc.add(t);
            }
        }
        /*
        doc.add(new TextField("data", title, Field.Store.YES));

        // use a string field for id because we don't want it tokenized
        doc.add(new StringField("id", isbn, Field.Store.YES));*/

        w.addDocument(doc);
    }

    public static ArrayList<String> search(StandardAnalyzer analyzer, String querystr, Directory index, int n) throws IOException, ParseException {
        QueryParser queryParser;
        queryParser = new MultiFieldQueryParser(Version.LUCENE_40, typesGood, analyzer);

        Query q = queryParser.parse(querystr);
//        Query q = new QueryParser(Version.LUCENE_40, "TI", analyzer).parse(querystr);

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
            if (bestScore - hits[i].score < bestScore * 0.7) {
                count++;
                Document d = searcher.doc(docId);
                result.add(d.get("RN"));
                System.out.println((i + 1) + ". " + d.get("RN") + " |score :" + hits[i].score); //+ "\t" + d.get("data"));
            }
        }
        System.out.println("Found " + count + " hits.");

        reader.close();
        return result;
    }

    public static void readQueries(StandardAnalyzer analyzer, Directory index) throws FileNotFoundException, IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader("/home/nicolas/Documentos/RI_trab/cfc/cfquery"));
        StringBuilder sb = new StringBuilder();
        FileWriter pw = new FileWriter(new File(output));
        sb.append("id");
        sb.append(",");
        sb.append("precision");
        sb.append(",");
        sb.append("recall");
        sb.append(",");
        sb.append("fmeasure");
        sb.append("\n");
        pw.append(sb.toString());
        pw.close();

        String id = null;
        String nr = null;
        String line = br.readLine();
        String query = "";
        String docs = "";
        ArrayList<String> relevantDocs = new ArrayList<>();
        boolean controlRD = false;
        boolean controlQ = false;
        int cont = 1;
        while (line != null) {

            String[] columns = line.split(" ");
            if (columns.length > 1) {
                if (columns[0].equals("QN")) {
                    id = utils.tail(columns);
                }
                if (columns[0].equals("QU")) {
                    query += utils.tail(columns);
                    controlQ = true;
                } else if (!columns[0].equals("NR") && controlQ) {
                    query += utils.tail(columns);
                } else if (columns[0].equals("NR") && controlQ) {
                    controlQ = false;
                }
                if (columns[0].equals("NR")) {
                    nr = utils.tail(columns);
                }
                if (columns[0].equals("RD")) {
                    docs += utils.tail(columns);
                    controlRD = true;
                } else if ((!columns[0].equals("QN")) && controlRD) {
                    docs += utils.tail(columns);
                } else if (columns[0].equals("QN") && controlRD) {
                    //System.out.print(docs);

                    docs = docs.replaceAll("\\s{2,}", " ");
                    String[] a = docs.split(" ");
                    for (int i = 0; i < a.length; ++i) {
                        if (i % 2 != 0) {
                            System.out.println(a[i]);
                            relevantDocs.add(a[i]);
                        }

                    }
                    System.out.println(query);
                    query = query.replaceAll("\\?", "").replaceAll("\\/", " ");
                    ArrayList<String> result = search(analyzer, query, index, relevantDocs.size());
                    for (int i = 0; i < result.size(); ++i) {
                        //tira os zeros a esquerda
                        result.set(i, result.get(i).replaceFirst("^0+(?!$)", "").replaceAll(" ", ""));

                    }
                    precisionAndRecall(result, relevantDocs, cont);
                    ++cont;
                    query = "";
                    docs = "";
                    relevantDocs = new ArrayList<>();
                    controlRD = false;
                    controlQ = false;

                }
            }

            line = br.readLine();
        }

//        docs = docs.replaceAll("\\s{2,}", " ");
//        String[] a = docs.split(" ");
//        for (int i = 0; i < a.length; ++i) {
//            if (i % 2 != 0) {
//                System.out.println(a[i]);
//                relevantDocs.add(a[i]);
//            }
//
//        }
//        ArrayList<String> result = search(analyzer, query, index, relevantDocs.size());
//        for (int i = 0; i < result.size(); ++i) {
//            //tira os zeros a esquerda
//            result.set(i, result.get(i).replaceFirst("^0+(?!$)", "").replaceAll(" ", ""));
//
//        }
//        precisionAndRecall(result, relevantDocs);
        //System.out.println(result);
    }

    public static void precisionAndRecall(ArrayList<String> result, ArrayList<String> relevantDocs, int index) throws FileNotFoundException, IOException {

        int countRelevants = 0;
//        for(String s : result)
//            System.out.println(s);
//        for(String s : relevantDocs)
//            System.out.println(s);
        for (int i = 0; i < result.size(); ++i) {
            if (relevantDocs.contains(result.get(i))) {
                System.out.println("oo");
                ++countRelevants;
            }

        }
        float precisao = (float) countRelevants / result.size();
        float recall = (float) countRelevants / relevantDocs.size();
        float fmeasure = 2 * ((float) (precisao * recall) / (precisao + recall));

        try (FileWriter pw = new FileWriter(output, true)) {
            StringBuilder sb = new StringBuilder();
            sb.append(Integer.toString(index));
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
