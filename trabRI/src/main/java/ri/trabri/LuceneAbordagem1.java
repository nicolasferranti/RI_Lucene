/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ri.trabri;

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
public class LuceneAbordagem1 {

    private StandardAnalyzer analyzer;
    private Directory index;
    private IndexWriterConfig config;
    private IndexWriter w;

    public LuceneAbordagem1() throws IOException {
        this.analyzer = new StandardAnalyzer(Version.LUCENE_40);
        // create the index
        this.index = new RAMDirectory();
        this.config = new IndexWriterConfig(Version.LUCENE_40, this.analyzer);
        w = new IndexWriter(index, config);
    }

    public String stemText(String word) {
        PorterStemmer stem = new PorterStemmer();
        stem.setCurrent(word);
        stem.stem();
        String result = stem.getCurrent();
        return result;
    }

    private ArrayList<String> geraTokens(String text) throws IOException {
        TokenStream stream = this.analyzer.tokenStream(null, new StringReader(text));
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

    public String getStemTokens(String text) throws IOException {
        ArrayList<String> words = geraTokens(text);
        String data = "";
        for (String word : words) {
            data += stemText(word) + " ";
        }
        return data;
    }

    /*
        Indexa os documentos de acordo com a primeira abordagem da equipe
    */
    public void Indexar(ArrayList<Documento> docums) throws IOException, ParseException {

        for (Documento d : docums) {
            for (Map.Entry<String, String> entry : d.atributos.entrySet()) {
                addDoc(w, getStemTokens(entry.getValue()), entry.getKey());
            }
        }
        this.w.close();
    }

    private void addDoc(IndexWriter w, String data, String id) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("data", data, Field.Store.YES));

        // use a string field for id because we don't want it tokenized
        doc.add(new StringField("id", id, Field.Store.YES));
        w.addDocument(doc);
    }

    
    
    
    
    public ArrayList<String> search(String querystr) throws IOException, ParseException {

        // the "data" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        Query q = new QueryParser(Version.LUCENE_40, "data", this.analyzer).parse(querystr);

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
            System.out.println((i + 1) + ". " + d.get("id"));
        }

        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();

        for (int i = 0; i < result.size(); ++i) {
            //tira os zeros a esquerda
            result.set(i, result.get(i).replaceFirst("^0+(?!$)", ""));
        }

        return result;
    }

    public void precisionAndRecall(ArrayList<String> result, ArrayList<String> relevantDocs) {
        int countRelevants = 0;
        for (int i = 0; i < result.size(); ++i) {

            if (relevantDocs.contains(result.get(i))) {
                ++countRelevants;
            }

        }
        System.out.println("precisao: " + (float) countRelevants / result.size());
        System.out.println("REcall: " + (float) countRelevants / relevantDocs.size());
    }
}
