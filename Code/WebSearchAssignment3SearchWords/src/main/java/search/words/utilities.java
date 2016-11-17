package search.words;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/******************************************************************************************
 * 
 * Author: Pallabi Chakraborty
 * Description: This class contains the methods used by the SearchWords class to generate
 * 				search results for the Search String passed through command line.
 * 
 * ******************************************************************************************/
 
public class utilities {

	//Load the document index file into memory
	public static Map<Integer,DocumentDetails> loadDocumentIndex(String documentFileIndex)
	{
		// Dictionary to store Document index data from the document index file
		Map<Integer,DocumentDetails> documentIndex=new HashMap<Integer,DocumentDetails>();
		
		// Read the file and load the details like URL, wordcount etc.
		try 
		{
			String sCurrentLine;
			
			// Reading the document index file
			BufferedReader in = new BufferedReader(new FileReader(documentFileIndex));
			// Read the file till there exists any unread line
			while ((sCurrentLine = in.readLine()) != null) 
			{
				// Split the line
				String[] words=sCurrentLine.split(" ");
				// document id of the URL
				int docId=Integer.parseInt(words[0]);
				// URL
				String url=words[1];
				// Number of words in the document
				int wordCount=Integer.parseInt(words[2]);
				
				// Store the data into the Map
				DocumentDetails details=new DocumentDetails(url,wordCount);

				documentIndex.put(docId,details);

			}

			in.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return documentIndex;
	}

	//Load the lexicon file
	public static Map<String,TermDetails> loadLexiconFiles(String lexiconFileName)
	{
		// Hashmap for storing the lexicon data
		Map<String,TermDetails> lexicon=new HashMap<String,TermDetails>();
		
		// Read the lexicon file and extract corresponding storage details of the inverted index
		try 
		{
			String sCurrentLine;
			// Reading the lexicon index file
			BufferedReader in = new BufferedReader(new FileReader(lexiconFileName));
			// Read till there exists some unread line
			while ((sCurrentLine = in.readLine()) != null) 
			{
				String[] words=sCurrentLine.split(" ");
				
				//Search term
				String term=words[0].trim();
				// Numer of documents containing the word
				int numOfDocuments=Integer.parseInt(words[1]);
				// Start position in the inverted index
				long startPosition=Long.parseLong(words[2]);
				// End position in the inverted index
				long endPosition=Long.parseLong(words[3]);
				// Store the data into the map object
				TermDetails details=new TermDetails(numOfDocuments,startPosition,endPosition);

				lexicon.put(term, details);
			}

			in.close();

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return lexicon;
	}

	// Get the document ids corresponding to the terms from the inverted index
	public static TreeMap<Integer,DocumentURLScore> getDocumentIds(String term,TermDetails details,String invertedIndexFile,Map<Integer,DocumentDetails> documentIndex,int N,int davg) throws IOException
	{
		// Read the string containing the data for a particular document ID
		String docDetails=readFromFile(invertedIndexFile,details.startPosition,(int)(details.endPosition-details.startPosition));

		// Separate the different blocks
		String[] blockArrays=docDetails.trim().split("\\^");

		// Dictionary to store the document details corresponding to a word
		TreeMap<Integer,DocumentURLScore> documentlist=new TreeMap<Integer,DocumentURLScore>();

		for(String blockarray:blockArrays)
		{
			//Split the blocks to get the start position
			String[] blockdata=blockarray.split("\\|");
			//Get Metadata
			String metadata=blockdata[0];
			//Get docIdData
			String docIdData=blockdata[1];

			//Get Metadata Details
			String[] metadataArray=metadata.trim().split(" ");
			int startPosition=Integer.parseInt(metadataArray[1]);

			// Get the size of the compressed integer array
			int numberOfCompressedValues=Integer.parseInt(metadataArray[2]);
			//As the data is separated by spaces thus split it
			String[] docIdDataArray=docIdData.split(" ");
			
			// Get all the values of the compressed array
			int[] compressedData=new int[numberOfCompressedValues];
			for(int i=0;i<numberOfCompressedValues;i++)
			{
				compressedData[i]=Integer.parseInt(docIdDataArray[i]);
			}

			// Decompress the compressed array
			int[] decompressedData=IntegerCompression.decompressBlockData(compressedData);

			// Get the actual number of documents ids which were compressed
			int numberOfDocIds=Integer.parseInt(docIdDataArray[0]);

			// add the start position integer to all decompressed values to get the actual document ids
			for(int i=0;i<numberOfDocIds;i++)
			{
				// Add the start position to the decompressed data
				int docId=decompressedData[i]+startPosition;
				// Add the document ids pertaining to the word in the dictionary
				DocumentDetails docdetails = documentIndex.get(docId);
				documentlist.put(docId, new DocumentURLScore(docdetails.url.intern(),utilities.calculateBM25Score(N, details.numOfDocuments, Integer.parseInt(docIdDataArray[numberOfCompressedValues+i])+1, docdetails.wordCount, davg)));
			}

		}
		return documentlist;
	}

	//Read from the index file by directly jumping to a particular position
	public static String readFromFile(String filePath, long position, int size) throws IOException {

		// Open the file
		RandomAccessFile file = new RandomAccessFile(filePath, "r");
		// Traverse to the position
		file.seek(position);
		// Read the "size" number of data
		byte[] bytes = new byte[size];
		file.read(bytes);
		file.close();
		//FileNumbers String generated and returned
		String fileNumbers=new String(bytes, "UTF-8");

		return fileNumbers;

	}

	//Find the average length of documents in the collection;
	public static int findAvgDocumentSize(Map<Integer,DocumentDetails> documentIndex)
	{
		// Initialize the variables
		int avgsize=0;
		int sum=0;

		// Average out the size of the documents' word count to get the average size
		for (Integer docId: documentIndex.keySet())
		{
			sum=sum+documentIndex.get(docId).wordCount;
		}

		avgsize=(int) sum/documentIndex.size();

		return avgsize;
	}

	//Calculate BM25 Score 
	public static double calculateBM25Score(int docNum,int docNumContainterm,int freqOfTermInDoc,int docSize,int avgDocSize)
	{
		
		double bm25Score=0;

		// Hardcoded values
		double K1=1.2;
		double B=0.75;
		
		// Compute K
		double K=K1 *((1-B)+B*((double)docSize/(double)docSize));

		// Compute BM25 Score
		bm25Score=Math.log10((docNum-docNumContainterm+0.5)/(docNumContainterm+0.5)) * ((double)((K1+1)*freqOfTermInDoc)/(double)(K+freqOfTermInDoc));

		return bm25Score;
	}

	//Calculate conjunctive search on search terms
	public static TreeMap<Double,Set<String>> calculateConjunctiveSearch(ArrayList<TreeMap<Integer,DocumentURLScore>> searchdata)
	{
		TreeMap<Double,Set<String>> conjunctionSearchedData=new TreeMap<Double,Set<String>>();

		Set<Integer> commonDocIds = new HashSet<Integer>();
		int count=0;
		
		//Fetch all the common document ids
		for(TreeMap<Integer,DocumentURLScore> docDetails:searchdata)
		{
			// For the first dictionary add all the keys, for the subsequent ones only retain the common ones and delete the others
			if(count==0)
			{
				commonDocIds.addAll(docDetails.keySet());
				count++;
			}
			else
			{
				commonDocIds.retainAll(docDetails.keySet());
			}

		}
		//Get all the data into a TreeMap
		conjunctionSearchedData=utilities.prepareData(commonDocIds, searchdata);

		return conjunctionSearchedData;
	}

	//Calculate disjunctive search on search terms
	public static TreeMap<Double,Set<String>> calculateDisjunctiveSearch(ArrayList<TreeMap<Integer,DocumentURLScore>> searchdata)
	{
		// Dictionary to get all the document ids for all the search terms 
		TreeMap<Integer,DocumentURLScore> commonDocIds = new TreeMap<Integer,DocumentURLScore>();
		//Fetch all the common document ids
		for(TreeMap<Integer,DocumentURLScore> docDetails:searchdata)
		{
			for(Map.Entry<Integer,DocumentURLScore> entry : docDetails.entrySet())
			{
				Integer docId=entry.getKey();
				DocumentURLScore docScore=entry.getValue();
				String url=docScore.url;
				Double bm25Score=docScore.bm25Score;
				
				// Compute a cumulative BM25 Score
				if(commonDocIds.containsKey(docId))
				{
					Double totalbm25Score=commonDocIds.get(docId).bm25Score+bm25Score;
					commonDocIds.put(docId,new DocumentURLScore(url, totalbm25Score));
				}
				else
				{
					commonDocIds.put(docId, docScore);
				}
			}
		}
		
		//Get all the data into a TreeMap
		TreeMap<Double,Set<String>> sortedByBM25Data=new TreeMap<Double,Set<String>>();
		//Put the data into a TreeMap with the sorted BM25Scores.
		for(Map.Entry<Integer,DocumentURLScore> entry : commonDocIds.entrySet())
		{
			String url=entry.getValue().url;
			Double bm25Score=entry.getValue().bm25Score;
			
			if(sortedByBM25Data.containsKey(-1*bm25Score))
			{
				Set<String> s=sortedByBM25Data.get(-1*bm25Score);
				s.add(url);
				sortedByBM25Data.put(-1*bm25Score, s);
			}
			else
			{
				Set<String> s=new HashSet<String>();
				s.add(url);
				sortedByBM25Data.put(-1*bm25Score, s);
			}
		}

		return sortedByBM25Data;
	}


	//Extract data from ArrayList for the key data present in a Set and return in a TreeMap
	public static TreeMap<Double,Set<String>> prepareData(Set<Integer> docIds,ArrayList<TreeMap<Integer,DocumentURLScore>> searchdata)
	{
		TreeMap<Integer,DocumentURLScore> preparedData=new TreeMap<Integer,DocumentURLScore>();

		// Get the URL and cumulative BM25 scores for the common URLs
		for(TreeMap<Integer,DocumentURLScore> data:searchdata)
		{
			for(Integer docId:data.keySet())
			{
				if(docIds.contains(docId))
				{
					if(preparedData.containsKey(docId))
					{
						DocumentURLScore docURLScore=preparedData.get(docId);
						Double bm25Score=docURLScore.bm25Score+data.get(docId).bm25Score;
						preparedData.put(docId, new DocumentURLScore(docURLScore.url,bm25Score));
					}
					else
					{
						preparedData.put(docId, data.get(docId));
					}
				}
			}
		}

		TreeMap<Double,Set<String>> sortedByBM25Data=new TreeMap<Double,Set<String>>();
		//Put the data into a TreeMap with the sorted BM25Scores.
		for(Integer docId:preparedData.keySet())
		{
			DocumentURLScore docURLScore=preparedData.get(docId);
			if(sortedByBM25Data.containsKey(-1*docURLScore.bm25Score))
			{
				Set<String> s=sortedByBM25Data.get(-1*docURLScore.bm25Score);
				s.add(docURLScore.url);
				sortedByBM25Data.put(-1*docURLScore.bm25Score, s);
			}
			else
			{
				Set<String> s=new HashSet<String>();
				s.add(docURLScore.url);
				sortedByBM25Data.put(-1*docURLScore.bm25Score, s);
			}
		}

		return sortedByBM25Data;
	}


}
