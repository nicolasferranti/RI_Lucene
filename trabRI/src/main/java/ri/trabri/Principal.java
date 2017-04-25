/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ri.trabri;

/**
 *
 * @author nicolas
 */
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Principal {

    public static void main(String[] args) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
        if (args.length > 1) {
            args[0] = "/home/eduardo/Documentos/trabRI/";
        }

        // todos os tipos e os tipos mais relevantes 
        ArrayList<String> typesGood = new ArrayList<>(Arrays.asList("RN", "AU", "TI", "SO", "MJ", "MN", "AB", "EX"));
        ArrayList<String> types = new ArrayList<>(Arrays.asList("PN", "AN", "RF", "CT", "RN", "AU", "TI", "SO", "MJ", "MN", "AB", "EX"));

        ArrayList<Documento> docs = null;

        // Lê o arquivo guardando o id do doc e todos os campos relevantes em um unico atributo
        // ABORDAGEM 1 (INDEXA TODAS AS TAGS COMO UMA SÓ)
        /*
        docs = getAllTagsTogether(args[0], typesGood);
        LuceneAbordagem1 lc = new LuceneAbordagem1();
        lc.Indexar(docs);
        ArrayList<Consulta> cn = readQueries(args[0]);
        for (Consulta c : cn) {
            ArrayList<String> result = lc.search(c.query);
            lc.precisionAndRecall(result, c.relevantDocs, c.id);
        }
        */
        
        
        docs = getTagsByField(args[0], typesGood, types);
        LuceneAbordagem2 lc = new LuceneAbordagem2();
        lc.Indexar(docs);
        ArrayList<Consulta> cn = readQueries(args[0]);
        for (Consulta c : cn) {
            ArrayList<String> result = lc.search(c.query);
            lc.precisionAndRecall(result, c.relevantDocs, c.id);
        }

        // Lê o arquivo guardando os pares de tag e valor dentre os considerados relevantes
        //        docs = getTagsByField(args[0], typesGood,types);
        //        LuceneAbordagem2.tester(docs);
    }

    /*
        Primeira abordagem, lê o arquivo de entrada e monta documento concatenando todos os campos
        considerados relevantes em um só
    */
    public static ArrayList<Documento> getTagsByField(String args, ArrayList<String> typesGood, ArrayList<String> types) {
        ArrayList<Documento> docs = new ArrayList<>();
        for (int count = 4; count < 10; count++) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(args + "/cf7" + count));
                try {
                    //int cont = 0;

                    StringBuilder sb = new StringBuilder();
                    String line = br.readLine();

                    Documento di = null;

                    boolean util = false;
                    String data = "";
                    String tag = null;
                    while (line != null) {

                        // quebra a linha em colunas
                        String[] columns = line.split(" ");

                        // se começa um novo documento
                        if (columns[0].equals("RN")) {
                            if (di != null) {
                                docs.add(di);
                                tag = null;
                                data = "";
                            }

                            di = new Documento();
                        }

                        if (typesGood.contains(columns[0])) {
                            tag = columns[0];
                            data = utils.tail(columns);

                            line = br.readLine();
                            String[] col = line.split(" ");
                            while (!types.contains(col[0])) {
                                data += " " + utils.tail(col);

                                line = br.readLine();
                                col = line.split(" ");
                            }
                            di.add(tag, data);
                        } else {
                            line = br.readLine();
                        }

                    }
                    //System.out.println(docs.get(0).atributos.toString());
                } catch (FileNotFoundException ex) {
                    System.out.println("Arquivo nao encontrado");
                    Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        br.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (FileNotFoundException ex) {
                System.out.println("Arquivo nao encontrado");
                Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return docs;
    }
    
    /*
        Segunda abordagem, lê o arquivo e cria documentos onde as tags relevantes são
        armazenadas separadamente
    */
    public static ArrayList<Documento> getAllTagsTogether(String args, ArrayList<String> typesGood) {
        // args[0] <- pasta onde os arquivos se encontram

        ArrayList<String> typesBad = new ArrayList<>(Arrays.asList("PN", "AN", "RF", "CT"));

        ArrayList<Documento> docs = new ArrayList<>();

        for (int count = 4; count < 10; count++) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(args + "/cf7" + count));

                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                Documento di = null;

                boolean util = false;
                String data = "";
                String RN = null;
                while (line != null) {

                    // quebra a linha em colunas
                    String[] columns = line.split(" ");

                    // se começa um novo documento
                    if (columns[0].equals("PN")) {
                        if (di != null) {
                            di.atributos.put(RN, data);
                            RN = null;
                            data = "";
                            docs.add(di);
                        }

                        di = new Documento();
                    }

                    if (typesGood.contains(columns[0])) {
                        util = true;
                        if (columns[0].equals("RN")) {
                            RN = columns[1];

                        } else {
                            data += utils.tail(columns);
                        }
                    } else {
                        if (typesBad.contains(columns[0])) {
                            util = false;
                        }
                        if (util) {
                            data += utils.tail(columns);
                        }
                    }

                    line = br.readLine();

                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return docs;
    }

    public static ArrayList<Consulta> readQueries(String arg) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(arg + "/cfquery"));
        StringBuilder sb = new StringBuilder();
        ArrayList<Consulta> consultas = new ArrayList<>();
        String id = null;
        String nr = null;
        String line = br.readLine();
        String query = "";
        String docs = "";
        ArrayList<String> relevantDocs = new ArrayList<>();
        Consulta cn = null;

        boolean controlRD = false;
        boolean controlQ = false;
        int cont = 1;

        do {

            String[] columns = line.split(" ");

            if (columns.length > 1) {
                if (columns[0].equals("QN")) {
                    if (cn != null) {
                        docs = docs.replaceAll("\\s{2,}", " ");
                        String[] a = docs.split(" ");
                        for (int i = 0; i < a.length; ++i) {
                            if (i % 2 != 0) {
                                System.out.println(a[i]);
                                cn.relevantDocs.add(a[i]);
                            }

                        }

                        query = query.replaceAll("\\?", "").replaceAll("\\/", " ");
                        cn.query = query;
                        cn.id = id;
                        consultas.add(cn);
                        ++cont;
                        query = "";
                        docs = "";
                        relevantDocs = new ArrayList<>();

                        controlRD = false;
                        controlQ = false;
                    }
                    id = utils.tail(columns);
                    cn = new Consulta();

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
                }
            }

            line = br.readLine();
        } while (line != null);
        if (cn != null) {
            docs = docs.replaceAll("\\s{2,}", " ");
            String[] a = docs.split(" ");
            for (int i = 0; i < a.length; ++i) {
                if (i % 2 != 0) {
                    System.out.println(a[i]);
                    cn.relevantDocs.add(a[i]);
                }

            }

            query = query.replaceAll("\\?", "").replaceAll("\\/", " ");
            cn.query = query;
            cn.id = id;
            consultas.add(cn);
        }
        return consultas;

        //System.out.println(result);
    }
}
