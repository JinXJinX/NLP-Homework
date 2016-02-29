package HW1;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import HW1.LanguageModelBuilder2.bigram;

public class LanguageModelEvaluator2 {
	
	static Map<String, bigram> NewDictionary = new HashMap<String, bigram>(); // String key, bigram value
	static int newN = 0; // total number of words in train file
	static int newV = 0; // numbers of bigram

	public static void main(String[] args) {

		Scanner input = new Scanner(System.in);
		
		System.out.println(" language Model file path:       e.g. /Users/xxx/Documents/workspace/language_model_file.txt");
		String languageModelFile = input.next(); 
		
		BigramQueryApplication2.readLanguageModelFileToDictionary(languageModelFile);
		
		System.out.println(" test file path:      "); 
		String testFileName = input.next();
		
		String[] testFileSentence = null;
		try {
			testFileSentence = LanguageModelBuilder2.contentToSentence(LanguageModelBuilder2.readFile(testFileName));
		} catch (IOException e1) {

			e1.printStackTrace();
		}
		
		testArrayToNewDictionary(testFileSentence);

		System.out.println("choose estimate desired: 1. MLE   2. Laplace   3.Katz backoff 4.unigram(use number only)");
		int estimationMethodsPrefer = input.nextInt(); //because the order is "y(0) numOfy(1) jointProbability(2) MLE(3) Laplace(4) Katz(5)"
		input.nextLine();
		
		double H = H(testFileSentence,estimationMethodsPrefer);
		
		if(H != -1){
		int PP = (int) Math.ceil(Math.pow(2, -H));
		
		System.out.println("PP is: " + PP);
		}else{
			System.out.println("PP is: infinite");
		}
	}
	
	static void testArrayToNewDictionary(String[] sentence){
		
		NewDictionary = BigramQueryApplication2.dictionary;
		newN = BigramQueryApplication2.N; newV = BigramQueryApplication2.V + 1;
		
		if(!NewDictionary.keySet().contains("<unk>"))
			NewDictionary.put("<unk>", new bigram("<unk>", "<unk>"));
		Set<String> dictionayKeySet = NewDictionary.keySet();
		
		for(String str: sentence){
			String[] words = str.split(" ");
			for (int i = 0; i < words.length-1; i++) {
				String x = words[i];
				String y = words[i+1];
				if(!dictionayKeySet.contains(x))
					x = "<unk>";
				if(!dictionayKeySet.contains(y))
					y = "<unk>";
				
				if(NewDictionary.containsKey(x)){
					NewDictionary.get(x).addY(y);
					newN++;
				}else{
					NewDictionary.put(x, new bigram(x,y));
				}
			}	
		}
	}
	
	static double H(String[] test, int method){
		int totalWordsInCorpus = 0;
		double sum = 0;
		if(method == 2){ // laplace
			BigramQueryApplication2.dictionary = NewDictionary;
			BigramQueryApplication2.N = getN();
			BigramQueryApplication2.V = getV();
		}
		for(String sentence:test){

			String[] wordsFromSentence = sentence.split(" ");
			int lengthOfStr = wordsFromSentence.length;
			totalWordsInCorpus += lengthOfStr - 2;
			double totalProbability = 1;
			
			//System.out.println(" N: " + BigramQueryApplication2.N + " V: " + getV());
			for(int i = 0; i< lengthOfStr -1 ; i++){// counter <s> </s>
			//for(int i = 1; i< lengthOfStr - 2; i++){//not counter <s> </s>
				double tmp = Math.log(BigramQueryApplication2.probiblity(wordsFromSentence[i], wordsFromSentence[i+1],method)) / Math.log(2);
				// log[e] x  / log[e] 2 = log[2] x
				if(Double.isInfinite(tmp)){ return -1;}
				if(!Double.isNaN(tmp) ){ sum += tmp; }
			}	
			
			//sum += Math.pow(totalProbability, 1.0/lengthOfStr);
		}
		//System.out.println(" sum is: " +sum + "  total w is: " +totalWordsInCorpus		);
		//System.out.println("N is:" + BigramQueryApplication2.N);
		
		return sum / totalWordsInCorpus;
	}
	
	public static int getN(){
		Set<String> tmp = NewDictionary.keySet();
		int sum = 0;
		for(String x :tmp)
			sum += NewDictionary.get(x).getNumOfX();
		return sum;
	}
	public static int getV(){
		Set<String> tmp = NewDictionary.keySet();
		int sum = 0;
		for(String x :tmp)
			sum += NewDictionary.get(x).getY().size();
		return sum;
	}

}
