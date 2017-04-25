/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ri.trabri;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import static ri.trabri.LuceneAbordagem2.output;

/**
 *
 * @author nicolasferranti
 */
public class LuceneAbordagem1 extends Lucene {

    public LuceneAbordagem1() throws IOException {
        super();
    }

    /*
        Indexa os documentos de acordo com a primeira abordagem da equipe
     */
    @Override
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

    @Override
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
            result.set(i, result.get(i).replaceFirst("^0+(?!$)", "").replace(" ", ""));
        }

        return result;
    }

    public void precisionAndRecall(ArrayList<String> result, ArrayList<String> relevantDocs, String id) throws IOException {
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
