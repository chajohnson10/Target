package Search;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Search {
	static List<Map<String, List<Integer>>> mapList;
	static List<List<String>> texts;
	static List<String> names;
	public static void main(String[] args) {
		File dir = new File("sample_text/");
		mapList = new ArrayList<>();
		texts = new ArrayList<>();
		names = new ArrayList<>();
		for(File f : dir.listFiles()) {
			doPreprocess(f);
		}
		Scanner in = new Scanner(System.in);
		String term = "";
		String type = "";
		while(true) {
			//remove special chars --> will cause errors in regex
			System.out.println("Enter search term (all special characters are removed, enter # to exit): ");
			term = in.nextLine();
			while(term.equals("")) {
				System.out.println("Empty String not allowed, please enter a search term or # to exit:");
				term = in.nextLine();
			}
			if(term.equals("#")) {
				break;
			}
			System.out.println("Enter search type (1. String match, 2. Regular Expression 3. PreProcessed): ");
			type = in.next();
			while(!type.equals("1") && !type.equals("2") && !type.equals("3")) {
				in.nextLine();
				System.out.println("Enter a number (1-3):");
				type = in.next();
			}
			in.nextLine();
			term = term.replaceAll("[^a-zA-Z0-9\s]", "");
			term = term.toLowerCase();
			//normalize spaces in search term
			term = term.replaceAll("[\s]", " ");
			if(term.charAt(0) == ' ') {
				term = term.substring(1);
			}
			if(term.charAt(term.length() - 1) == ' '){
				term = term.substring(0, term.length() - 1);
			}
			String[] splitTerm = term.split("\s");
			long start = System.currentTimeMillis();
			if(type.equals("1")) {
				byWord(dir, splitTerm);
			}else if(type.equals("2")) {
				byRegex(dir, term);
			}else if(type.equals("3")) {
				byPreprocess(splitTerm);
			}
			System.out.println("Time Elapsed: " + (System.currentTimeMillis() - start) + "\n");
		}
		in.close();
	}
	
	//preprocess files
	public static void doPreprocess(File f) {
		Scanner fileRead = null;
		Map<String, List<Integer>> fileMap = new HashMap<>();
		List<String> text = new ArrayList<>();
		try {
			fileRead = new Scanner(f);
			int index = 0;
			while(fileRead.hasNext()) {
				//normalize text, add to text reference
				String s = fileRead.next().replaceAll("[^a-zA-Z0-9]", "");
				s = s.toLowerCase();
				text.add(s);
				List<Integer> indices;
				//add index of words for quick reference to text reference
				if(fileMap.containsKey(s)) {
					indices = fileMap.get(s);
				}else {
					indices = new ArrayList<>();
				}
				indices.add(index);
				fileMap.put(s, indices);
				index++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally {
			mapList.add(fileMap);
			texts.add(text);
			names.add(f.getName());
			fileRead.close();
		}
	}
	
	public static void byWord(File dir, String[] splitTerm){
		for(File f : dir.listFiles()) {
			Scanner fileRead = null;
			int count = 0;
			try {
				fileRead = new Scanner(f);
				while(fileRead.hasNext()) {
					//normalize words as they're read and iterate over them as they appear
					String s = fileRead.next().replaceAll("[^a-zA-Z0-9]", "");
					s = s.toLowerCase();
					for(int i = 0; i < splitTerm.length; i++) {
						if(!splitTerm[i].equals(s)) {
							break;
						}else if(splitTerm[i].equals(s)) {
							if(i == splitTerm.length - 1) {
								count++;
							}else {
								s = fileRead.next().replaceAll("[^a-zA-Z0-9]", "");
								s = s.toLowerCase();
							}
						}
					}
				}
			}catch(FileNotFoundException e) {
				e.printStackTrace();
			}finally {
				fileRead.close();
			}
			System.out.println(f.getName());
			System.out.println("Matches: " + count);
		}
	}
	public static void byRegex(File dir, String term){
		for(File f : dir.listFiles()){
			long count = 0;
			try {
				//grab file as string and normalize characters for pattern matching
				String fileString = Files.readString(Paths.get(f.getAbsolutePath()));
				fileString = fileString.toLowerCase();
				fileString = fileString.replaceAll("[^a-zA-Z0-9\s\n]", "");
				Pattern p = Pattern.compile("(?<!\\S)" + term + "(?!\\S)");
				Matcher m = p.matcher(fileString);
				count = m.results().count();
			}catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(f.getName());
			System.out.println("Matches: " + count);
		}
	}
	
	public static void byPreprocess(String[] splitTerm){
		for(int i = 0; i < mapList.size(); i++) {
			int count = 0;
			Map<String, List<Integer>> map = mapList.get(i);
			boolean contains = map.containsKey(splitTerm[0]);
			//if term is contained in file process
			if(contains) {
				List<Integer> indices = map.get(splitTerm[0]);
				//if term is only one word, return size (number) of instances
				if(splitTerm.length == 1) {
					System.out.println(names.get(i));
					System.out.println("Matches: " + indices.size());
				}else {
					//otherwise move to instance and iterate over subsequent words
					for(int index : indices) {
						for(int j = 1; j < splitTerm.length; j++) {
							String s = texts.get(i).get(index + j);
							if(!splitTerm[j].equals(s)) {
								break;
							}else if(splitTerm[j].equals(s) && j == splitTerm.length - 1) {
								count++;
							}
						}
					}
				}
			}
			if(splitTerm.length > 1 || !contains) {
				System.out.println(names.get(i));
				System.out.println("Matches: " + count);
			}
		}
	}
}
