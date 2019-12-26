package test;

import basic.util.StringSplitter;
import kale.struct.Matrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DataProcess {
    private Set<Integer> entitiesSet;
    private Set<Integer> relationsSet;
    private Map<String, Integer> entitiesMap;
    private Map<String, Integer> relationsMap;
    private Map<String, List<Integer>> rules;
    private Matrix matrixE;
    private Matrix matrixR;
    private List<String> testTriples;
    private Set<String> trainTripleSet;
    private Set<String> testTripleSet;

    public Set<Integer> getEntitiesSet() {
        return entitiesSet;
    }

    public Set<Integer> getRelationsSet() {
        return relationsSet;
    }

    public Map<String, Integer> getEntitiesMap() {
        return entitiesMap;
    }

    public Map<String, Integer> getRelationsMap() {
        return relationsMap;
    }

    public Matrix getMatrixE() {
        return matrixE;
    }

    public Matrix getMatrixR() {
        return matrixR;
    }

    public Map<String, List<Integer>> getRules() {
        return rules;
    }

    public List<String> getTestTriples() {
        return testTriples;
    }

    public Set<String> getTrainTripleSet() {
        return trainTripleSet;
    }

    public Set<String> getTestTripleSet() {
        return testTripleSet;
    }

    public void readEntities(String fnEntities) throws IOException {
        entitiesMap = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(fnEntities));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("\t");
            entitiesMap.put(tokens[1], Integer.parseInt(tokens[0]));
        }
        reader.close();
    }

    public void readRelations(String fnRelations) throws IOException {
        relationsMap = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(fnRelations));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("\t");
            relationsMap.put(tokens[1], Integer.parseInt(tokens[0]));
        }
        reader.close();
    }

    public void readTestTriple(String fnTestTriples) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fnTestTriples));
        entitiesSet = new HashSet<>();
        relationsSet = new HashSet<>();
        testTriples = new ArrayList<>();
        testTripleSet = new HashSet<>();
        String line;
        while ((line = reader.readLine()) != null) {
            testTriples.add(line);
            testTripleSet.add(line);
            String[] tokens = line.split("\t");
            int subject = Integer.parseInt(tokens[0]);
            int relation = Integer.parseInt(tokens[1]);
            int object = Integer.parseInt(tokens[2]);
            entitiesSet.add(subject);
            entitiesSet.add(object);
            relationsSet.add(relation);
        }
        reader.close();
        System.out.println("finished");
    }

    public void readRules(String fnRules) throws IOException {
        rules = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(fnRules));
        String line;
        while ((line = reader.readLine()) != null) {
            //rule type 1
            if (line.split("&&").length == 1 && line.endsWith("(x,y)")){
                String[] tokens = StringSplitter.RemoveEmptyEntries(StringSplitter
                        .split("=> ", line));
                int iFstRelation = relationsMap.get(tokens[0]);
                int iSndRelation = relationsMap.get(tokens[1]);
                String fstRelation = String.valueOf(iFstRelation);
                if(!rules.containsKey(fstRelation)){
                    List<Integer> sndRuleList = new ArrayList<>();
                    sndRuleList.add(iSndRelation);
                    rules.put(fstRelation, sndRuleList);
                }
                else{
                    rules.get(fstRelation).add(iSndRelation);
                }
            }
            //rule type 2
            else if(line.split("&&").length == 1 && line.endsWith("(y,x)")){
                String[] tokens = StringSplitter.RemoveEmptyEntries(StringSplitter
                        .split("=> ", line));
                int iFstRelation = relationsMap.get(tokens[0]);
                int iSndRelation = relationsMap.get(tokens[1]);
                String fstRelation = String.valueOf(iFstRelation);
                if(!rules.containsKey(fstRelation)){
                    List<Integer> sndRuleList = new ArrayList<>();
                    sndRuleList.add(iSndRelation);
                    rules.put(fstRelation, sndRuleList);
                }
                else{
                    rules.get(fstRelation).add(iSndRelation);
                }
            }
            // rule type 3
            else{
                String[] tokens = StringSplitter.RemoveEmptyEntries(StringSplitter
                        .split("=>& ", line));
                int iFstRelation = relationsMap.get(tokens[0]);
                int iSndRelation = relationsMap.get(tokens[1]);
                int iTrdRelation = relationsMap.get(tokens[2]);
                String relationKey = iFstRelation + "&&" + iSndRelation;
                if(!rules.containsKey(relationKey)){
                    List<Integer> sndRuleList = new ArrayList<>();
                    sndRuleList.add(iTrdRelation);
                    rules.put(relationKey, sndRuleList);
                }
                else{
                    rules.get(relationKey).add(iTrdRelation);
                }
            }
        }
        reader.close();
    }

    public void readMatrix(String fnMatrixE, String fnMatrixR, int factors) throws IOException {
        matrixE = new Matrix(entitiesMap.size(), factors);
        matrixR = new Matrix(relationsMap.size(), factors);
        matrixE.load(fnMatrixE);
        matrixR.load(fnMatrixR);
    }

    public void readTrainTripleSet(String fnTrainTriples) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fnTrainTriples));
        trainTripleSet = new HashSet<>();
        String line;
        while ((line = reader.readLine()) != null) {
            trainTripleSet.add(line);
        }
        reader.close();
        System.out.println("finished");

    }
}
