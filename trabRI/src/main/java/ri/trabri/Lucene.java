/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ri.trabri;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
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
 * @author nicolasferranti
 */
public class Lucene {

    public Lucene() throws IOException {

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
        // 0. Specify the analyzer for tokenizing text.
        //    The same analyzer should be used for indexing and searching

        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);

        // 1. create the index
        Directory index = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);

        IndexWriter w = new IndexWriter(index, config);
        for (Documento d : docums) {
            for (Map.Entry<String, String> entry : d.atributos.entrySet()) {
                addDoc(w, getStemTokens(analyzer, entry.getValue()), entry.getKey());
                //System.out.println("String trabalhada :" + getStemTokens(analyzer, entry.getValue()));
            }
        }
        //        addDoc(w, "Lucene in Action", "193398817");
        //        addDoc(w, "Lucene for Dummies", "55320055Z");
        //        addDoc(w, "Managing Gigabytes", "55063554A");
        //        addDoc(w, "The Art of Computer Science", "9900333X");
        w.close();
        readQueries(analyzer, index);
        // 2. query
        /* String querystr = "Is CF mucus abnormal";

        // the "data" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        Query q = new QueryParser(Version.LUCENE_40, "data", analyzer).parse(querystr);

        // 3. search
        int hitsPerPage = 1000;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        // 4. display results
        System.out.println("Found " + hits.length + " hits.");
        for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get("id")); //+ "\t" + d.get("data"));
        }
        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();*/
    }

    private static void addDoc(IndexWriter w, String title, String isbn) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("data", title, Field.Store.YES));

        // use a string field for id because we don't want it tokenized
        doc.add(new StringField("id", isbn, Field.Store.YES));
        w.addDocument(doc);
    }

    public static ArrayList<String> search(StandardAnalyzer analyzer, String querystr, Directory index, int n) throws IOException, ParseException {
        //String querystr = "Is CF mucus abnormal";

        // the "data" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        Query q = new QueryParser(Version.LUCENE_40, "data", analyzer).parse(querystr);

        int hitsPerPage = 1000;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;
        ArrayList<String> result = new ArrayList<>();
        // 4. display results
        System.out.println("Found " + hits.length + " hits.");
        for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            result.add(d.get("id"));
            System.out.println((i + 1) + ". " + d.get("id")); //+ "\t" + d.get("data"));
        }

        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();
        return result;
    }

    public static void readQueries(StandardAnalyzer analyzer, Directory index) throws FileNotFoundException, IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader("/home/eduardo/Documentos/trabRI/cfquery"));
        StringBuilder sb = new StringBuilder();
        String id = null;
        String nr = null;
        String line = br.readLine();
        String query = "";
        String docs = "";
        ArrayList<String> relevantDocs = new ArrayList<>();
        boolean controlRD = false;
        boolean controlQ = false;
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
                    controlRD = false;
                    break;
                }
            }

            line = br.readLine();
        }

        docs = docs.replaceAll("\\s{2,}", " ");
        String[] a = docs.split(" ");
        for (int i = 0; i < a.length; ++i) {
            if (i % 2 != 0) {
                System.out.println(a[i]);
                relevantDocs.add(a[i]);
            }

        }
        ArrayList<String> result = search(analyzer, query, index, relevantDocs.size());
        for (int i = 0; i < result.size(); ++i) {
            //tira os zeros a esquerda
            result.set(i, result.get(i).replaceFirst("^0+(?!$)", ""));

        }
       precisionAndRecall(result, relevantDocs);

        //System.out.println(result);
    }
    
    public static void precisionAndRecall(ArrayList<String> result, ArrayList<String> relevantDocs){
        int countRelevants = 0;
        for(int i = 0; i < result.size(); ++i){
            
            if(relevantDocs.contains(result.get(i))){
                ++countRelevants;
            }
            
        }
        System.out.println("precisao: " + (float)countRelevants/result.size());
        System.out.println("REcall: " + (float) countRelevants/relevantDocs.size());
    }
}
