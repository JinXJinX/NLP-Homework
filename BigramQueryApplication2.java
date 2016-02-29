package HW1;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.omg.PortableInterceptor.DISCARDING;

import HW1.LanguageModelBuilder2.bigram;

public class BigramQueryApplication2 {
	
	static Map<String, bigram> dictionary = new HashMap<String, bigram>(); // String key, bigram value
	static int N = 0; // total number of words in train file
	static int V = 0; // numbers of bigram

	public static void main(String[] args) {

		Scanner input = new Scanner(System.in);
		
		System.out.println("language model txt file path:          e.g.  /Users/jx/Documents/workspace/git/CSE390HomeWork/language_model_file.txt");//     /Users/jx/Documents/workspace/git/CSE390HomeWork/language_model_file.txt
		String fileName = input.next();
		
		//String fileName = "/Users/jx/Documents/workspace/git/CSE390HomeWork/language_model_file.txt";
		readLanguageModelFileToDictionary(fileName);
		System.out.println("choose estimate desired: 1. MLE   2. Laplace   3.Katz backoff 4.UnigramAD (use number only)");
		int estimationMethodsPrefer = input.nextInt();
		if (estimationMethodsPrefer>4 ||estimationMethodsPrefer <1){ System.out.println("invild input");}
		
		System.out.println("type in a pair of words (x, y). separate by \",\"");
		String twoWords = input.next();	
		if(twoWords.contains(",")){
			String[] w1w2 = twoWords.trim().split(",");
			String x = w1w2[0];
			String y = w1w2[1];

			System.out.println(probiblity(x,y,estimationMethodsPrefer));
		}else{
			System.out.println("invalid input!");
		}
		//System.out.println(dictionary.size());
		//System.out.println("N: " +N +" V: " + V);	

	}
	
	static void readLanguageModelFileToDictionary(String fileName){
		String content = "";
		try {
			content = LanguageModelBuilder2.readFile(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] elements = content.split(" \n");
		
		for (int i = 1; i < elements.length; i++) {
			String[] tmp = elements[i].split(" ");
			
			String xxx = tmp[0];
			int num = num(xxx);
			//N+= num;
			String Xword = word(xxx);
			dictionary.put(Xword, new bigram(Xword, num));
			
			for (int j = 1; j < tmp.length; j++) {
				//V++;
				String yyy = tmp[j];
				num = num(yyy);
				String word = word(yyy);
				try{
					dictionary.get(Xword).addYYY(word, num);
				}catch(NullPointerException e){
					System.out.println("NullPointerException");
				}
				
			}
		}	
		N = getN(); V = getV();
	}
	
	private static int num(String str){
		return Integer.parseInt(str.substring(0,str.indexOf("|")));
	}
	private static String word(String str){
		return str.substring(str.indexOf("|")+1);
	}
	
	public static double probiblity(String x, String y, int p){
		switch(p){
		case 1: return MLE(x,y);
		case 2: return Laplace(x,y);
		case 3: return Katz(x,y);
		case 4: return unigramAD(x);
		}
		return -1;
		
	}
	
	static double MLE(String x, String y){
		if(!(dictionary.containsKey(x)))
			return 0.0;
		if(!(dictionary.containsKey(y)))
			return 0.0;
		if(!dictionary.get(x).getYhelper().contains(y))
			return 0.0;
		return (double)dictionary.get(x).getNumOfXY(y)/dictionary.get(x).getNumOfX();
	}
	
	static double Laplace(String x, String y){
		if(!(dictionary.containsKey(x))){
			return 1.0/  (V + 1.0);
		}else if(!(dictionary.get(x).getYhelper().contains(y))){
			bigram tmp = dictionary.get(x);
			LinkedList<String> listOfY	= tmp.getYhelper();
			double pr = 1.0;
			for(String yWord:listOfY){
				pr-= Laplace(x, yWord);
			}
			return pr;
		}

		return ((double)dictionary.get(x).getNumOfXY(y) + 1.0)/ (dictionary.get(x).getNumOfX() + V);
	}
	
	static double Katz(String x, String y){
		double D = 0.5;
		if(!dictionary.containsKey(x) || !dictionary.get(x).getYhelper().contains(y)){ // if count(x,y)  = 0
			return alpha(x) * beta(x, y) ;
		}
		return bigramAD( x, y) ; 
	
	}

	static double bigramAD(String x, String y){
		double D = 0.5;
		
		if(!(dictionary.containsKey(x))){
			return 1.0/ V ;
		}else if(!(dictionary.get(x).getYhelper().contains(y))){  // 1 - sum( all pr ( x, y ))
			bigram tmp = dictionary.get(x);
			LinkedList<String> listOfY	= tmp.getYhelper();
			double pr = 1.0;
			for(String yWord:listOfY){
				pr-= bigramAD(x, yWord);
			}
			return pr;
		}
		bigram tmp = dictionary.get(x);		
		return ( tmp.getNumOfXY(y) - D ) / tmp.getNumOfX();	
	}
	
	
	static double unigramAD(String x){
		if(!dictionary.containsKey(x)){
			return 1.0 / V ;
		}
		double D = 0.5;
		return ( dictionary.get(x).getNumOfX() - D ) / N;
	}
	
	static double alpha(String x){
		double result = 1.0;
		if(dictionary.containsKey(x)){
			bigram tmp = dictionary.get(x);
			LinkedList<String>  listOfY = tmp.getYhelper();
			for(String yWord : listOfY){
				result -= bigramAD(x, yWord);	
			}
		}else{
			result = unigramAD(x);
		}
		return result;	
	}
	
	static double beta(String x, String y){
		double denominator = 0;
		Set<String> Xset = dictionary.keySet();
		if(dictionary.containsKey(x)){
			
			LinkedList<String> listOfY = dictionary.get(x).getYhelper();
			for(String w :Xset){
				if(!listOfY.contains(w))
					denominator += unigramAD(w);
			}
		}else{
			for(String w :Xset){
				denominator += unigramAD(w);
			}
		}
		return unigramAD(y) / denominator;
	}
	
	public static int getN(){
		Set<String> tmp = dictionary.keySet();
		int sum = 0;
		for(String x :tmp)
			sum += dictionary.get(x).getNumOfX();
		return sum;
	}
	public static int getV(){
		Set<String> tmp = dictionary.keySet();
		int sum = 0;
		for(String x :tmp)
			sum += dictionary.get(x).getY().size();
		return sum;
	}
	
}
