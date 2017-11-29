import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Arrays;


public class Process_Document {
	private ArrayList<String> listStopWord;
	
	
	public Process_Document(){
		listStopWord = new ArrayList<>();
		readFile();
	}
	
	private void readFile(){
		try {
			FileInputStream file = new FileInputStream("vietnamese-stopwords.txt");
			InputStreamReader isr = new InputStreamReader(file, "utf-8");
			BufferedReader reader = new BufferedReader(isr);
			String  line = null;	
			while((line = reader.readLine()) != null){
				listStopWord.add(line);
			}
			file.close();
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> process_RemoveStopWord(String data, boolean ner){
		String str = new String();
		str = data.toLowerCase().replaceAll("[$&+,:;=?@#|'<>.-^*()%!”“]", "").replaceAll("\\s\\s", " ");
		str = str.replaceAll("\"[^\"]*\"", "").replaceAll("\\s\\s", " ");
		String[] arrStr = str.split(" ");
		ArrayList<String> lstStrData = new ArrayList<>(Arrays.asList(arrStr));
		ArrayList<String> lst = new ArrayList<>();
		if (ner){
			for (String stopword : listStopWord) {
				for (String str1 : lstStrData){
					if (!is_StopWord(stopword, str1))
						lst.add(str1);
				}
			}
			return lst;
		} else {
			for (String stopword : listStopWord) {		
				if (lstStrData.contains(stopword)){
					lst.add(stopword);
					lstStrData.removeIf(stopword::equals);
				}
			}
		}
		return lstStrData;
	}
	
	private boolean is_StopWord(String strSW, String str){
		int lastIndexCharacter = str.lastIndexOf("_");
		return strSW.equals(str.substring(0, lastIndexCharacter));
	}
	
	public int[][] createSimilarity(ArrayList<ArrayList<Item>> lstItem){
		int[][] sim = new int[lstItem.size()][lstItem.size()];
		for(int i = 0; i < lstItem.size() - 1; i++){
			for (int j = i + 1; j < lstItem.size(); j++){
				sim[i][j] = simItem(lstItem.get(i), lstItem.get(j));
			}
		}
		return sim;
	}
	
	public double[][] createSimilarityMC_3(ArrayList<ArrayList<Item>> lstItem){
		double[][] sim = new double[lstItem.size()][lstItem.size()];
		
		for(int i = 0; i < lstItem.size() - 1; i++){
			for (int j = i + 1; j < lstItem.size(); j++){
				sim[i][j] = simItemMC_3(lstItem.get(i), lstItem.get(j));
			}
		}
		return sim;
	}
	
	private int simItem(ArrayList<Item> lstItem1, ArrayList<Item> lstItem2){
		int count = 0;
		for (Item i : lstItem1){
			for(Item j : lstItem2){
				if (i.getItem().equals(j.getItem())){
					count++;
					break;
				}
			}
		}
		return count;
	}
	
	private double simItemMC_3(ArrayList<Item> lstItem1, ArrayList<Item> lstItem2){
		int a11 = 0;
		int a10 = 0;
		boolean flag = false;
		for (Item i1 : lstItem1) {
			for (Item i2 : lstItem2){
				if(i1.getItem().equals(i2.getItem())){
					a11++;
					flag = true;
					break;
				}
			}
			if (!flag)
				a10++;
			flag = false;
		}
		
		int a01 = 0;
		for (Item i2 : lstItem2) {
			for (Item i1 : lstItem1){
				if(!i2.getItem().equals(i1.getItem())){
					flag = true;
					break;
				}
			}
			if (!flag)
				a01++;
			flag = false;
		}
		double reuult = ((double)a11/(a11+a10+a01));
		reuult = Math.round(reuult*100)/100D;
		return reuult;
	}
	
	
	public ArrayList<ArrayList<Item>> listArrItem_Base(ArrayList<Document> lstDocument, double minsupp, boolean ner){
		ArrayList<ArrayList<Item>> lstArrItem = new ArrayList<>();
		for(Document dc : lstDocument){
			dc.process_Data(minsupp, ner);
			lstArrItem.add(dc.getLstFreqItems());
		}
		return lstArrItem;
	}
	
	public ArrayList<ArrayList<Item>> listArrItem_TF_IDF(ArrayList<Document> lstDocument, double minsupp, boolean ner){
		ArrayList<ArrayList<Item>> lstArrItem = new ArrayList<>();
		ArrayList<Item> lstItemOfDataIDF = new ArrayList<>();
		for(Document dc : lstDocument){
			dc.process_TF(ner);
			lstItemOfDataIDF = findItemOfData_IDF(dc, lstItemOfDataIDF);
		}
		process_IDF(lstItemOfDataIDF, lstDocument.size());
		
		for(Document dc : lstDocument){
			ArrayList<Item> listItem = new ArrayList<>();
			for (Item i : dc.getLstFreqItems()){
				double tf_idf = i.getPerOfFreq()*findIDF(i, lstItemOfDataIDF);
				if(tf_idf >= minsupp){
					listItem.add(i);
				}
			}
			lstArrItem.add(listItem);
		}
		return lstArrItem;
	}
	
	
	
	private double findIDF(Item i, ArrayList<Item> lstItemOfDataIDF){
		for (Item fI : lstItemOfDataIDF){
			if(fI.getItem().equals(i.getItem())){
				return fI.getPerOfFreq();
			}
		}
		return 0;
	}
	
	private void process_IDF(ArrayList<Item> listItemOfData, int numOfDC){
		 for(Item i: listItemOfData){
			 i.setPerOfFreq((double)Math.log10(numOfDC/i.getFreq()));
		 }
	}
	
	private ArrayList<Item> findItemOfData_IDF(Document dc, ArrayList<Item> listItemOfData){		
		if (listItemOfData.size() == 0){
			for(Item iOfDC : dc.getLstFreqItems()){
				Item iNew = new Item(iOfDC.getItem(), 1);
				listItemOfData.add(iNew);
			}
		} else{
			ArrayList<Item> listNewItem = new ArrayList<>();
			boolean f = true;
			for(Item iOfDC : dc.getLstFreqItems()){
				for(Item i : listItemOfData){
					if(i.getItem().equals(iOfDC.getItem())){
						i.setFreq(i.getFreq() + 1);
						f = false;
						break;
					}
				}
				if (f){
					Item iNew = new Item(iOfDC.getItem(), 1);
					listNewItem.add(iNew);
				}
				f = true;
			}
			listItemOfData.addAll(listNewItem);
		}
		return listItemOfData;
	}
	
	public ArrayList<Item> getListAllItem(ArrayList<ArrayList<Item>> lstItem){
		ArrayList<Item> listAllItem = new ArrayList<>();
		for (ArrayList<Item> lstI : lstItem){
			for (Item i : lstI){
				boolean flagEqual = false;
				for (Item _i : listAllItem){
					if (_i.getItem().equals(i.getItem())){
						flagEqual = true;
						break;
					}
				}
				if (!flagEqual)
					listAllItem.add(i);
			}
		}
		return listAllItem;
	}
	
	
}
