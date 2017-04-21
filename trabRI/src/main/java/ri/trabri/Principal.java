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
        if (args[0] == null) {
            args[0] = "/home/nicolasferranti/Documentos/RI/RI_Lucene/cfc";
        }

        // todos os tipos e os tipos mais relevantes 
        ArrayList<String> typesGood = new ArrayList<>(Arrays.asList("RN", "AU", "TI", "SO", "MJ", "MN", "AB", "EX"));
        ArrayList<String> types = new ArrayList<>(Arrays.asList("PN", "AN", "RF", "CT", "RN", "AU", "TI", "SO", "MJ", "MN", "AB", "EX"));

        ArrayList<Documento> docs = null;
        
        // Lê o arquivo guardando o id do doc e todos os campos relevantes em um unico atributo
        /* ABORDAGEM 1 (INDEXA TODAS AS TAGS COMO UMA SÓ)
        docs = getAllTagsTogether(args[0], typesGood);
        LuceneAbordagem1 lc = new LuceneAbordagem1();
        lc.Indexar(docs);
        Consulta cn = readQueries(args[0]);
        ArrayList<String> result = lc.search(cn.query);
        lc.precisionAndRecall(result, cn.relevantDocs);
        */
        
        // Lê o arquibo guardando os pares de tag e valor dentre os considerados relevantes
        docs = getTagsByField(args[0], typesGood,types);
        LuceneAbordagem2.tester(docs);
    }

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

    public static Consulta readQueries(String arg) throws FileNotFoundException, IOException{
        BufferedReader br = new BufferedReader(new FileReader(arg+"/cfquery"));
        StringBuilder sb = new StringBuilder();
        String id = null;
        String nr = null;
        String line = br.readLine();
        String query = "";
        String docs = "";
        
        Consulta cn = new Consulta();
        
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
                cn.relevantDocs.add(a[i]);
            }

        }
        cn.query = query;
        return cn;

        
        //System.out.println(result);
    }
}
