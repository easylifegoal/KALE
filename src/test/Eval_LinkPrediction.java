package test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import kale.struct.Matrix;

public class Eval_LinkPrediction {
	public int iNumberOfEntities;
	public int iNumberOfRelations;
	public int iNumberOfFactors;
	
	public Matrix MatrixE = null;
	public Matrix MatrixR = null;
	List<Double> iFilterList = new ArrayList<>();
	List<Double> iRawList = new ArrayList<>();
	public HashMap<String, Boolean> lstTriples = null;
	
	
	public Eval_LinkPrediction(int iEntities, int iRelations, int iFactors) {
		iNumberOfEntities = iEntities;
		iNumberOfRelations = iRelations;
		iNumberOfFactors = iFactors;
	}
	
	public void LPEvaluation(
			String fnMatrixE, 
			String fnMatrixR, 
			String fnTrainTriples, 
			String fnValidTriples, 
			String fnTestTriples) throws Exception {
		preprocess(fnTrainTriples,fnValidTriples,fnTestTriples,fnMatrixE, fnMatrixR);
		evaluate(fnTestTriples);
	}
	
	public void preprocess(
			String fnTrainTriples, String fnValidTriples, String fnTestTriples, 
			String fnMatrixE, String fnMatrixR) throws Exception {
		MatrixE = new Matrix(iNumberOfEntities, iNumberOfFactors);
		MatrixE.load(fnMatrixE);
		
		MatrixR = new Matrix(iNumberOfRelations, iNumberOfFactors);
		MatrixR.load(fnMatrixR);
		
		BufferedReader train = new BufferedReader(new InputStreamReader(
				new FileInputStream(fnTrainTriples), StandardCharsets.UTF_8));
		BufferedReader valid = new BufferedReader(new InputStreamReader(
				new FileInputStream(fnValidTriples), StandardCharsets.UTF_8));
		BufferedReader test = new BufferedReader(new InputStreamReader(
				new FileInputStream(fnTestTriples), StandardCharsets.UTF_8));
		lstTriples = new HashMap<>();
		String line;
		while ((line = train.readLine()) != null) {
			if (!lstTriples.containsKey(line.trim())) {

					lstTriples.put(line.trim(), true);
				} 
		}
		while ((line = valid.readLine()) != null) {
			if (!lstTriples.containsKey(line.trim())) {

				lstTriples.put(line.trim(), true);
			} 
		}
		while ((line = test.readLine()) != null) {
			if (!lstTriples.containsKey(line.trim())) {

				lstTriples.put(line.trim(), true);
			} 

		}
		System.out.println("triples:"+lstTriples.size());
		valid.close();
		test.close();
		train.close();
	}
	
	public void evaluate(String fnTestTriples) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fnTestTriples), StandardCharsets.UTF_8));
		String line;
		int iCnt = 0;
		double dTotalMeanRank_filter = 0.0;
		double dTotalMRR_filter = 0.0;
		int iTotalHits1_filter = 0;
		int iTotalHits3_filter = 0;
		int iTotalHits5_filter = 0;
		int iTotalHits10_filter = 0;
		double dMedian_filter;
		
		double dTotalMeanRank_raw = 0.0;
		double dTotalMRR_raw = 0.0;
		int iTotalHits1_raw = 0;
		int iTotalHits3_raw = 0;
		int iTotalHits5_raw = 0;
		int iTotalHits10_raw = 0;
		double dMedian_raw;

		int isTrue = 0;
		
		while ((line = reader.readLine()) != null) {
			System.out.println("triple:" + iCnt/2);
			String[] tokens = line.split("\t");
			int iRelationID = Integer.parseInt(tokens[1]);
			int iSubjectID = Integer.parseInt(tokens[0]);
			int iObjectID = Integer.parseInt(tokens[2]);
			double dTargetValue = 0.0;
			for (int p = 0; p < iNumberOfFactors; p++) {
				dTargetValue -= Math.abs(MatrixE.get(iSubjectID, p) + MatrixR.get(iRelationID, p) - MatrixE.get(iObjectID, p));
			}
			
			int iLeftRank_filter = 1;
			int iLeftIdentical_filter = 0;
			int iLeftRank_raw = 1;
			int iLeftIdentical_raw = 0;
			
			for (int iLeftID = 0; iLeftID < iNumberOfEntities; iLeftID++) {
				double dValue = 0.0;
				String negTriple = iLeftID + "\t" + iRelationID + "\t" +iObjectID;
				for (int p = 0; p < iNumberOfFactors; p++) {
					dValue -= Math.abs(MatrixE.get(iLeftID, p) + MatrixR.get(iRelationID, p) - MatrixE.get(iObjectID, p));
				}
				if(!lstTriples.containsKey(negTriple)){
					if (dValue > dTargetValue) {
						iLeftRank_filter++;
					}
					if (dValue == dTargetValue) {
						iLeftIdentical_filter++;
					}
				}	
				if (dValue > dTargetValue) {
					iLeftRank_raw++;
				}
				if (dValue == dTargetValue) {
					iLeftIdentical_raw++;
				}
			}

			double dLeftRank_filter = iLeftRank_filter;
			double dLeftRank_raw = (2.0 * iLeftRank_raw + iLeftIdentical_raw -1.0) / 2.0;
			int iLeftHitsAt1_filter = 0, iLeftHitsAt3_filter = 0, iLeftHitsAt5_filter = 0, iLeftHitsAt10_filter = 0;
			int iLeftHitsAt1_raw = 0,iLeftHitsAt3_raw = 0,iLeftHitsAt5_raw = 0,iLeftHitsAt10_raw = 0;
			if (dLeftRank_filter <= 1.0) {
				iLeftHitsAt1_filter = 1;
			}
			if (dLeftRank_filter <= 3.0) {
				iLeftHitsAt3_filter = 1;
			}
			if (dLeftRank_filter <= 5.0) {
				iLeftHitsAt5_filter = 1;
			}
			if (dLeftRank_filter <= 10.0) {
				iLeftHitsAt10_filter = 1;
			}
			
			if (dLeftRank_raw <= 1.0) {
				iLeftHitsAt1_raw = 1;
			}
			if (dLeftRank_raw <= 3.0) {
				iLeftHitsAt3_raw = 1;
			}
			if (dLeftRank_raw <= 5.0) {
				iLeftHitsAt5_raw = 1;
			}
			if (dLeftRank_raw <= 10.0) {
				iLeftHitsAt10_raw = 1;
			}
		
			dTotalMeanRank_filter += dLeftRank_filter;
			dTotalMRR_filter += 1.0/ dLeftRank_filter;
			iTotalHits1_filter += iLeftHitsAt1_filter;
			iTotalHits3_filter += iLeftHitsAt3_filter;
			iTotalHits5_filter += iLeftHitsAt5_filter;
			iTotalHits10_filter += iLeftHitsAt10_filter;
			iFilterList.add(dLeftRank_filter);
			
			dTotalMeanRank_raw += dLeftRank_raw;
			dTotalMRR_raw += 1.0/(double)dLeftRank_raw;
			iTotalHits1_raw += iLeftHitsAt1_raw;
			iTotalHits3_raw += iLeftHitsAt3_raw;
			iTotalHits5_raw += iLeftHitsAt5_raw;
			iTotalHits10_raw += iLeftHitsAt10_raw;
			iRawList.add(dLeftRank_raw);
			iCnt++;
			
			int iRightRank_filter = 1;
			int iRightIdentical_filter = 0;
			int iRightRank_raw = 1;
			int iRightIdentical_raw = 0;
			for (int iRightID = 0; iRightID < iNumberOfEntities; iRightID++) {
				double dValue = 0.0;
				String negTriple = iSubjectID + "\t" + iRelationID + "\t" +iRightID;
				for (int p = 0; p < iNumberOfFactors; p++) {
					dValue -= Math.abs(MatrixE.get(iSubjectID, p) + MatrixR.get(iRelationID, p) - MatrixE.get(iRightID, p));
				}
				if(!lstTriples.containsKey(negTriple)){
					if (dValue > dTargetValue) {
						iRightRank_filter++;
					}
					if (dValue == dTargetValue) {
						iRightIdentical_filter++;
					}					
				}
				if (dValue > dTargetValue) {
					iRightRank_raw++;						
				}
				if (dValue == dTargetValue) {
					iRightIdentical_raw++;
				}	
			}
			
			double dRightRank_filter = iRightRank_filter;
			double dRightRank_raw = (2.0 * iRightRank_raw + iRightIdentical_raw -1.0) / 2.0;
			int iRightHitsAt1_filter = 0, iRightHitsAt3_filter = 0, iRightHitsAt5_filter = 0, iRightHitsAt10_filter = 0;
			int iRightHitsAt1_raw = 0,iRightHitsAt3_raw = 0,iRightHitsAt5_raw= 0,iRightHitsAt10_raw = 0;
			if (dRightRank_filter <= 1.0) {
				iRightHitsAt1_filter = 1;
			}
			if (dRightRank_filter <= 3.0) {
				iRightHitsAt3_filter = 1;
			}
			if (dRightRank_filter <= 5.0) {
				iRightHitsAt5_filter = 1;
			}
			if (dRightRank_filter <= 10.0) {
				iRightHitsAt10_filter = 1;
			}
			
			if (dRightRank_raw <= 1.0) {
				iRightHitsAt1_raw = 1;
			}
			if (dRightRank_raw <= 3.0) {
				iRightHitsAt3_raw = 1;
			}
			if (dRightRank_raw <= 5.0) {
				iRightHitsAt5_raw = 1;
			}
			if (dRightRank_raw <= 10.0) {
				iRightHitsAt10_raw = 1;
			}
			
			dTotalMeanRank_filter += dRightRank_filter;
			dTotalMRR_filter += 1.0/ dRightRank_filter;
			iTotalHits1_filter += iRightHitsAt1_filter;
			iTotalHits3_filter += iRightHitsAt3_filter;
			iTotalHits5_filter += iRightHitsAt5_filter;
			iTotalHits10_filter += iRightHitsAt10_filter;
			iFilterList.add(dRightRank_filter);
			
			dTotalMeanRank_raw += dRightRank_raw;
			dTotalMRR_raw += 1.0/ dRightRank_raw;
			iTotalHits1_raw += iRightHitsAt1_raw;
			iTotalHits3_raw += iRightHitsAt3_raw;
			iTotalHits5_raw += iRightHitsAt5_raw;
			iTotalHits10_raw += iRightHitsAt10_raw;
			iRawList.add(dRightRank_raw);
			iCnt++;	
			if (iLeftHitsAt1_filter == 1 && iRightHitsAt1_filter == 1) {
				isTrue++;
				System.out.println(iSubjectID + "\t" + iRelationID + "\t" + iObjectID + " is True");
			}
		}
		System.out.println("rate is " + (isTrue / (iCnt / 2.0)));
		Collections.sort(iFilterList);
		int indx= iFilterList.size()/2;
		if (iFilterList.size()%2==0) {
			dMedian_filter = (iFilterList.get(indx-1)+ iFilterList.get(indx))/2.0;
		}
		else {
			dMedian_filter = iFilterList.get(indx);
		}
		
		Collections.sort(iRawList);
		indx=iRawList.size()/2;
		if (iRawList.size()%2==0) {
			dMedian_raw = (iRawList.get(indx-1)+iRawList.get(indx))/2.0;
		}
		else {
			dMedian_raw = iRawList.get(indx);
		}
		
		System.out.println("Filter setting:");
		System.out.println("MeanRank: "+(dTotalMeanRank_filter / (double)iCnt) + "\n"
				+ "MRR: "+(dTotalMRR_filter / (double)iCnt) + "\n"
				+ "Median: " + dMedian_filter + "\n"
				+ "Hit@1: "+((double) iTotalHits1_filter / (double)iCnt) + "\n"
				+ "Hit@3: " + ((double)iTotalHits3_filter / (double)iCnt) + "\n"
				+ "Hit@5: " +((double) iTotalHits5_filter / (double)iCnt)+ "\n"
				+ "Hit@10: " +((double) iTotalHits10_filter / (double)iCnt)+ "\n");
		
		System.out.println("Raw setting:");
		System.out.println("MeanRank: "+(dTotalMeanRank_raw / (double)iCnt) + "\n"  
				+ "MRR: "+(dTotalMRR_raw / (double)iCnt) + "\n" 
				+ "Median: " + dMedian_raw + "\n" 
				+ "Hit@1: "+((double)iTotalHits1_raw / (double)iCnt) + "\n" 
				+ "Hit@3: " + ((double)iTotalHits3_raw / (double)iCnt) + "\n" 
				+ "Hit@5: " +((double)iTotalHits5_raw / (double)iCnt)+ "\n"
				+ "Hit@10: " +((double)iTotalHits10_raw / (double)iCnt)+ "\n");
		reader.close();
	}
	
	public static void main(String[] args) throws Exception {
		int iEntities = 40943;
		int iRelations = 18;
		int iFactors = 50;
		String fnMatrixE = "MatrixE.real.best";
		String fnMatrixR = "MatrixR.real.best";
		String fnTrainTriples = "datasets\\wn18\\train.txt";
		String fnValidTriples = "datasets\\wn18\\valid.txt";
		String fnTestTriples = "datasets\\wn18\\test.txt";

//		int iEntities = Integer.parseInt(args[0]);
//		int iRelations = Integer.parseInt(args[1]);
//		int iFactors = Integer.parseInt(args[2]);
//		String fnMatrixE = args[3];
//		String fnMatrixR = args[4];
//		String fnTrainTriples = args[5];
//		String fnValidTriples = args[6];
//		String fnTestTriples = args[7];

		
		Eval_LinkPrediction eval = new Eval_LinkPrediction(iEntities, iRelations, iFactors);
		eval.LPEvaluation(fnMatrixE, fnMatrixR, 
				fnTrainTriples, fnValidTriples, fnTestTriples);
	}
}
