package HW1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageModelBuilder2 {
	
	static Map<String, bigram> dictionary = new HashMap<String, bigram>(); // String key, bigram value
	static int N = 0; // total number of words in train file
	static int V = 0; // numbers of bigram
	static List<TOP> top20MLE = new ArrayList<TOP>();
	static List<TOP> top20Laplace = new ArrayList<TOP>();

	public static void main(String[] args) {
		System.out.println("type in file path:     e.g.   /Users/jx/Desktop/2016SP/CSE390/train.txt");
		
		Scanner input = new Scanner(System.in); //     /Users/jx/Desktop/2016SP/CSE390/train.txt
		
		String fileName = input.next();
		//String fileName = "/Users/jx/Desktop/2016SP/CSE390/train.txt"; // use this one for testing
		
		String[] sentence = null;
		try {
			sentence = contentToSentence(readFile(fileName));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		arrayToDictionary(sentence);

		create_language_model_file();
		
		try {
			create_top_20_probability_file();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	

	public static class bigram{
		//private int empId;
		private String x;
		private String y;
		private LinkedList<String> listOfY = new LinkedList<String>();
		private LinkedList<String> subListOfY = new LinkedList<String>();
		private int numOfx;
		
		public bigram(String x, String y) {
			this.x = x;
			this.y = y;
			listOfY.add("1|" + y);
			subListOfY.add(y);
			numOfx++;
			if(!(x.equals("<s>")||y.equals("</s>"))){V++; N++; }
				
		}
		//for use language model file to create dictionary
		public bigram(String x, int numOfX) {
			this.x = x;
			this.numOfx = numOfX;
			//N+=numOfX;
		}
			// set values on attributes
		
		public String getX(){ return x; }
		public LinkedList<String> getY(){ return listOfY; }
		public LinkedList<String> getYhelper(){ return subListOfY; }
		public int getNumOfX(){
			return numOfx;
		}
		
		public void addY(String y){

			int idxOfY = -1;
			if(subListOfY.contains(y)){
				idxOfY = subListOfY.indexOf(y);
				
				listOfY.add(idxOfY, addOneToY(listOfY.get(idxOfY)));
				listOfY.remove(idxOfY+1);
				
			}else{
				subListOfY.add(y);
				listOfY.add("1|" + y);
			}
			numOfx++;
		}
		public void addYYY(String y, int numOfY){
			listOfY.add(numOfY + "|" +y);
			subListOfY.add(y);
			//System.out.println("added");
		}
		
		String addOneToY(String y){
			int idx = -1;
			idx = y.indexOf("|");
			int num = Integer.parseInt(y.substring(0,idx));
			String word = y.substring(idx+1);
			//System.out.println("num is: " +(num + 1) +" word is: " +word); 
			return (num + 1) +"|" +word;
		}
		
		public String toString(){
			String result = x;
			for(String y: listOfY)
				result += (" " + y);
			return result;
		}
		
		public int getNumOfXY(String yWord){
			if(subListOfY.contains(yWord)){
				String str = listOfY.get(subListOfY.indexOf(yWord));
				int idx = -1;
				idx = str.indexOf("|"); 
				return Integer.parseInt(str.substring(0,idx));
			}else{ return -1;  }//cant find
		
		}

	}
	
	// read train file. return a String
	static String readFile(String fileName) throws FileNotFoundException, IOException{
		
		String content = "";
		try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line + "\n");
		        //sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    content = sb.toString();
		}catch(FileNotFoundException e){
			System.out.println("File Not Found Exception, restar this application and use CORRECT absolute path!");
		}catch(IOException e){
			System.out.println(" IO Exception, restar this application!");
		}
		return content;
	}
	
	// split train file to sentence
	static String[] contentToSentence(String content){
		String[] paragraph = content.split("\n{2,}");
		
		StringBuilder s = new StringBuilder();
		
		int pLength = paragraph.length;	
		String rowData = "";
		
		for(int i=0; i<pLength; i++){
			String p = paragraph[i];
			p = p.replace("\n", " ");
			//System.out.println(p);
			String[] tmp = p.split("[;.?!]");//split paragraph to sentence. by "." or ";" or "?" or "!"
			int l = tmp.length;
			
			for(int j = 0; j<l; j++){
				
				StringBuilder editedData = new StringBuilder();// add <S> at the begin of sentence, and </S> at end of sentence
				
				rowData = tmp[j].trim(); //single sentence
				//System.out.println(rowData);
				Pattern ppp=Pattern.compile("[A-Za-z][A-Za-z']*"); 
				Matcher m=ppp.matcher(rowData); 
				s.append("<s> ");
				while(m.find()) { 
					s.append(m.group().toLowerCase() + " "); 
				}
					s.append("</s> <end>"); // add 5 "|". and use this to split string later		
			}
		}
		//System.out.println(s.toString());
		String[] tmp = s.toString().split(" <end>");
		List<String> list = new ArrayList<String>();

		for(String element : tmp) {
			if(element.length() > 8 ) {
				list.add(element);
			}
		}
		tmp = list.toArray(new String[list.size()]);
		
		return tmp;
	}	
	//read string array and put every bigrams to dictionary
	static void arrayToDictionary(String[] sentence){
		for(String str: sentence){
			String[] words = str.split(" ");
			for (int i = 0; i < words.length-1; i++) {
				String x = words[i];
				String y = words[i+1];
				if(dictionary.containsKey(x))
					dictionary.get(x).addY(y);
				else
					dictionary.put(x, new bigram(x,y));
			}
			
		}
	
	}
	
	public static class TOP implements Comparable<TOP>{
		//private int empId;
		private String word;
		private double Pr;
		
		public TOP(String word, double Pr) {
			this.word = word;
			this.Pr = Pr;
		}
		public String getWord(){ return word; }
		
		public double getPr(){ return Pr; }

		@Override
		public int compareTo(TOP o) {
			// TODO Auto-generated method stub
			return (int)(this.getPr() * 100000000) - (int)(o.getPr() * 100000000);
		}
	}
	private static void create_language_model_file(){
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream("language_model_file.txt"), "utf-8"))) {
			
						writer.write("N: " +getN() + "V: " + getV() + " \n");
						Set<String> dicKeySet = dictionary.keySet();
						
						for (String Xword:dicKeySet) {
							
							bigram tmp = dictionary.get(Xword);
							LinkedList<String> YwordsHelper = tmp.getYhelper();
							LinkedList<String> Ywords = tmp.getY();
							
							writer.write(tmp.numOfx + "|" + Xword + " ");
							for(String xxx:Ywords)
								writer.write(xxx + " ");
							writer.write("\n");
							for(String Yword:YwordsHelper){
								if(!(Xword.equals("<s>") || Yword.equals("</s>") )){
									String name = Xword+"," +Yword;
									double numOfxy = (double)tmp.getNumOfXY(Yword);
									double MLEpr = numOfxy / N ;
									double LaplacePr = (numOfxy + 1.0)/ ( N + Math.pow(V, 2));
									top20MLE.add(new TOP(name,MLEpr));
									top20Laplace.add(new TOP(name,LaplacePr));
								}
							}
						}				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("language_model_file.txt done!");	
	}
	private static void create_top_20_probability_file() throws UnsupportedEncodingException, FileNotFoundException, IOException{
		
		Collections.sort(top20MLE); // sort method
		Collections.sort(top20Laplace);
		//System.out.println("EmpId\tName\tAge");
		int MLElen = top20MLE.size(), Laplacelen = top20Laplace.size();
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream("Top_20_Probability.txt"), "utf-8"))) {
			
						writer.write("**********\ntop 20 MLE\n**********\n");
						for (int i = 1; i <= 20; i++) {
							TOP e = top20MLE.get(MLElen - i);
							writer.write("#" + i +"\n");
							writer.write(e.getWord()  + " " + e.getPr() + "\n");
						}
	
						writer.write("**********\ntop 20 Laplace\n**********\n");
						for (int i = 1; i <= 20; i++) {
							TOP e = top20Laplace.get(Laplacelen - i);
							writer.write("#" + i +"\n");
							writer.write(e.getWord()  + " " + e.getPr() + "\n");
						}
		}
	
		System.out.println("Top_20_Probability.txt done!");
	}

}
