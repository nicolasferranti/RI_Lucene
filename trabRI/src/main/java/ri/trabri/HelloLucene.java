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
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

public class HelloLucene {

    public static void main(String[] args) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
        //tester();
        ArrayList<String> typesGood = new ArrayList<>(Arrays.asList("RN", "AU", "TI", "SO", "MJ", "MN", "AB", "EX"));
        ArrayList<String> typesBad = new ArrayList<>(Arrays.asList("PN", "AN", "RF", "CT"));
        
        ArrayList<Documento> docs = new ArrayList<>();
        
        for (int count = 4; count < 10; count++) {
            BufferedReader br = new BufferedReader(new FileReader("/home/nicolasferranti/Documentos/RI/RI_Lucene/cfc/cf7"+count));

            try {
                //int cont = 0;

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
//                            System.out.println(docs.get(cont).atributos.toString());
//                            if (cont == 2) {
//                                Lucene.tester(docs);
//                                break;
//                            }
//                            cont++;
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
            } finally {
                br.close();
            }
        }
        Lucene.tester(docs);
    }

}
