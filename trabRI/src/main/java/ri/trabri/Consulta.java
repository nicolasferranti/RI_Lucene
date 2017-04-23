/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ri.trabri;

import java.util.ArrayList;

/**
 *
 * @author nicolas
 */
public class Consulta {
    public ArrayList<String> relevantDocs;
    public String query;
    public String id;
    
    public Consulta(){
        relevantDocs = new ArrayList<>();
    }
}
