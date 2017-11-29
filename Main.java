import java.util.ArrayList;
import java.util.Scanner;

public class Main {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);
		System.out.println("-------- Maximum Capturing -------\n");
		Reader reader = new Reader();
		System.out.println("Reading data ...");
		ArrayList<Document> lstDC = reader.readFile();
		Process_Document pDC = new Process_Document();

		double minsupp = Double.parseDouble(args[1]);
		System.out.println("Processing document ... \n");
		
		String execute = args[2];
		boolean ner = execute.equals("ner") ? true : false;
		String method = args[3];
		ArrayList<ArrayList<Item>> lstArrItem = new ArrayList<>();
		if (method.equals("-base-line-method")){
			lstArrItem = pDC.listArrItem_Base(lstDC, minsupp, ner);
		} else if(method.equals("-tf-idf-method")){
			lstArrItem = pDC.listArrItem_TF_IDF(lstDC, minsupp, ner);
		}
		System.out.println("Creating similarity matrix ...\n");
		double[][] sim = pDC.createSimilarityMC_3(lstArrItem);
		MC mc1 = new MC(sim, lstArrItem.size());
		System.out.println("Clustering text ...\n");
		mc1.cluster();
		
		int numOfCluster = Integer.parseInt(args[5]);
		if(numOfCluster > 0){
			System.out.println("Normalizing ...\n");
			mc1.normalize_New(numOfCluster);
			mc1.setTopic(lstArrItem);
		}
		System.out.println("Writing data output....\n");
		mc1.writeOutPut(lstDC, minsupp);
		System.out.println("----- DONE -----\n");
		
	}
	
}
