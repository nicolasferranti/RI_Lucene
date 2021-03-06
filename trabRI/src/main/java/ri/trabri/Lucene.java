/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ri.trabri;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.PorterStemmer;

/**
 *
 * @author nicolas
 * classe genérica para representar abordagens distintas de indexação
 */
public abstract class Lucene {

    protected StandardAnalyzer analyzer;
    protected Directory index;
    protected IndexWriterConfig config;
    protected IndexWriter w;

    public Lucene() throws IOException {
        this.analyzer = new StandardAnalyzer(Version.LUCENE_40);
        // create the index
        this.index = new RAMDirectory();
        this.config = new IndexWriterConfig(Version.LUCENE_40, this.analyzer);
        w = new IndexWriter(index, config);
    }

    /*
        retorna palavra stemizada
    */
    protected String stemText(String word) {
        PorterStemmer stem = new PorterStemmer();
        stem.setCurrent(word);
        stem.stem();
        String result = stem.getCurrent();
        return result;
    }

    /*
        Cria Tokens a partir de um texto
    */
    protected ArrayList<String> geraTokens(String text) throws IOException {
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

    /*
        Pega um texto, gera os tokens e stemiza
    */
    protected String getStemTokens(String text) throws IOException {
        ArrayList<String> words = geraTokens(text);
        String data = "";
        for (String word : words) {
            data += stemText(word) + " ";
        }
        return data;
    }

    // cada classe deve implementar sua própria forma de indexação
    public abstract void Indexar(ArrayList<Documento> docums) throws IOException, ParseException;

    // sua própria forma de busca
    public abstract ArrayList<String> search(String querystr) throws IOException, ParseException;

}
