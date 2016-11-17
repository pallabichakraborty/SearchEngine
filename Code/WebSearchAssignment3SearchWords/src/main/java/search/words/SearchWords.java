package search.words;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/******************************************************************************************
 * 
 * Author: Pallabi Chakraborty
 * Description: This is the class containing the main class which takes as parameters the three 
 * 				index files and outputs the Conjunctive as well as Disjunctive Search Results
 * 
 ******************************************************************************************/
public class SearchWords {

	public static void main(String[] args) throws IOException {
		// Cache Size for the LRU Cache
		final int CACHE_SIZE=100;
		// If the required number of parameters are not passed during run then output an error
		if(args.length<3)
		{
			System.out.println("There are lesser number of arguments than expected");
		}
		else
		{
			//args[0]=/Users/pallabichakraborty/Desktop/test/doclist
			//args[1]=/Users/pallabichakraborty/Desktop/test/lexicon
			//args[2]=/Users/pallabichakraborty/Desktop/test/index
			// Parameter 1: Absolute path for the document index
			String documentIndexFileName=args[0];
			// Parameter 2: Absolute path for the lexicon index
			String lexiconIndexFile=args[1];
			// Paramter 3: Absolute path for the inverted index file
			String indexFile=args[2];
			
			System.out.println("Loading the document and lexicon files");
			//Load the document files
			Map<Integer,DocumentDetails> documentIndex=utilities.loadDocumentIndex(documentIndexFileName);
			
			//total number of documents in the collection
			int N=documentIndex.size();
			// the average length of documents in the collection;
			int davg=utilities.findAvgDocumentSize(documentIndex);
			
			//Load the lexicon files
			Map<String,TermDetails> lexiconIndex=utilities.loadLexiconFiles(lexiconIndexFile);
			
			System.out.println("Document and lexicon files loaded");
			
			//Generate an LRU Cache
			LinkedHashMap<String,TreeMap<Integer,DocumentURLScore>> LRUCache = new LinkedHashMap<String,TreeMap<Integer,DocumentURLScore>>(CACHE_SIZE,(float) 0.75,true){

				private static final long serialVersionUID = 1L;
				// Set the property that if the number of entries exceeds the Cache Size then delete the oldest accessed record
				protected boolean removeEldestEntry(Map.Entry<String,TreeMap<Integer,DocumentURLScore>>  eldest) {
		             return size() >  CACHE_SIZE;
		          }
		       };
			//Ask for the search terms
			while(true)
			{
				// Read the Query String
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				System.out.print("Enter the terms you want to search (In case of multiple words, enter all the words separated by a space):");
				String searchString = reader.readLine();
			    // Check for spaces
				String[]  searchwords=searchString.trim().split(" ");
				
				// Array to store all the document ID structures for all the search terms
				ArrayList<TreeMap<Integer,DocumentURLScore>> searchdata = new ArrayList<TreeMap<Integer,DocumentURLScore>>();
				//Output the search terms
				for(String word:searchwords)
				{
					//Variable to store the document details for a word initialized
					TreeMap<Integer,DocumentURLScore> docIdDetails=null;
					//Check if it available the cached data
					docIdDetails=LRUCache.get(word);
					//If not present then go ahead with fetching the data from the inverted list
					if(docIdDetails ==null)
					{
						// Fetch the position details for the term stored in the lexicon index
						TermDetails details=lexiconIndex.get(word);
						// If there is some data available in the lexicon index then get the document id details.
						if(details!=null)
						{
							// Get the document details.
							docIdDetails=utilities.getDocumentIds(word,details, indexFile, documentIndex, N, davg);
							// Put it in the LRU Cache
							LRUCache.put(word, docIdDetails);
							//Add all search results in the ArrayList
							searchdata.add(docIdDetails);
						}
					}	
					else
					{
						//Add all search results in the ArrayList
						searchdata.add(docIdDetails);
					}
					
				}	
				
				int count=0;
				// If the search string is empty then output a message stating that there is no search terms found
				if(searchwords.length==0)
				{
					System.out.println("No search term input found");
				}
				else 
				{
					// If there is no data retrieved for a search term then output a message stating that
					// the term is not found in the database
					if(searchdata.isEmpty())
					{
						System.out.println("Search Term not found in the database");
					}
					else
					{
						// If there is only one term then run the Disjunctive Search
						if(searchwords.length==1)
						{
							// Fetch the data for the disjunctive search for the term
							TreeMap<Double,Set<String>> disjunctivesearchResults=utilities.calculateDisjunctiveSearch(searchdata);
							
							//Output the  search results
							System.out.println("---------------------------------");
							System.out.println("Search Results");
							System.out.println("---------------------------------");
							
							count=0;
							
							// For disjunctive search output 100 search results
							for (Double bm25Score: disjunctivesearchResults.keySet())
							{ 
								for(String url:disjunctivesearchResults.get(bm25Score))
								{
									if(count>100)
									{
										break;
									}
									else
									{
										System.out.println(url+" "+String.valueOf(-1*bm25Score));
										count++;
									}
									
								}
			                } 
						}
						else
						{
							//Prepare Data for output on the basis of conjunctive and disjunctive search
							
							// Compute Disjunctive Search Results
							TreeMap<Double,Set<String>> disjunctivesearchResults=utilities.calculateDisjunctiveSearch(searchdata);
							
							System.out.println("---------------------------------");
							System.out.println("Disjunctive Search Results");
							System.out.println("---------------------------------");
							
							count=0;
							
							// Output disjunctive search results
							for (Double bm25Score: disjunctivesearchResults.keySet())
							{ 
								for(String url:disjunctivesearchResults.get(bm25Score))
								{
									if(count>100)
									{
										break;
									}
									else
									{
										System.out.println(url+" "+String.valueOf(-1*bm25Score));
										count++;
									}
								}
			                } 
							
							
							// Compute Conjunctive Search Results
							TreeMap<Double,Set<String>> conjunctivesearchResults=utilities.calculateConjunctiveSearch(searchdata);
							
							System.out.println("---------------------------------");
							System.out.println("Conjunctive Search Results");
							System.out.println("---------------------------------");
							
							count=0;
							
							// Output Conjunctive Search Results
							for (Double bm25Score: conjunctivesearchResults.keySet())
							{ 
								for(String url:conjunctivesearchResults.get(bm25Score))
								{
									if(count>10)
									{
										break;
									}
									else
									{
										System.out.println(url+" "+String.valueOf(-1*bm25Score));
										count++;
									}
								}
			                } 
			
						}
					}
				}
			}
		}
	}
}
