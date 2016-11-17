package merge.wordlists;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/*******************************************************************************************************
*
* Author: Pallabi Chakraborty
* Description: Contains the required methods required for generating the index files
*
********************************************************************************************************/

public class utilties {
	
	// Block Size set to 128
	// The blocks are separated for different words, there will be no overlapping blocks where a single block contains the data for multiple words together
	static int BLOCK_SIZE=128;
	
	// Method to generate index files from the sorted word posting file
	public static void generateIndexFiles(String sortedWordList,String indexFileName,String lexiconFileName)
	{
		// Read through the sorted word posting file
		try (BufferedReader br = new BufferedReader(new FileReader(sortedWordList)))
		{
			// Intialize the variables
			String sCurrentLine;
			String prevword="";

			// Variable to store the document ids for a single word
			SortedSet<Integer> docIdSet=new TreeSet<Integer>();
			// Map storing the document details along with frequency of the search term in it
			Map<Integer, Integer> dictionary = new HashMap<Integer, Integer>();
			
			//Index File Initialization.
			File indexfile = new File(indexFileName);
			// If index file exists then delete the file
			if(indexfile.exists())
			{
				indexfile.delete();
			}
			// Generate the parent directory along with the dependencies so that there is no issues in creating the file
			indexfile.getParentFile().mkdirs();
			//true = append file
			PrintWriter invertedIndexOut = new PrintWriter(new FileOutputStream(indexfile, true /* append = true */));
			
			
			//Lexicon File Initialization
			File lexiconfile = new File(lexiconFileName);
			// If the lexicon file exists then delete it
			if(lexiconfile.exists())
			{
				lexiconfile.delete();
			}
			// Generate the parent directory along with the dependencies so that there is no issues in creating the file
			lexiconfile.getParentFile().mkdirs();
			//true = append file
			//Prepping the lexicon list file
			PrintWriter termOut = new PrintWriter(new FileOutputStream(lexiconfile, true /* append = true */));

			// Initializing variable to keep the positioning details
			long offset=0;
			// Till there is some unread data in the sorted word list file
			while ((sCurrentLine = br.readLine()) != null) 
			{
				// Split each line by space
				String[] words=sCurrentLine.split(" ");
				// Word fetched
				String word=words[0];
				// Document ID containing the word fetched
				int docId=Integer.parseInt(words[1]);
				
				// If there is a word change, then dump the data for the earlier word into the inverted list as well as the lexicon file
				if(!prevword.equals("") && !prevword.equals(word))
				{
					
					try
					{
						// Initialize inverted list file
						invertedIndexOut = new PrintWriter(new FileOutputStream(indexfile, true /* append = true */));
						// Initialize lexicon file
						termOut = new PrintWriter(new FileOutputStream(lexiconfile, true /* append = true */));
						
						// Initialize the variables
						String docIdlist="";
						String freqlist="";
						String metadata="";
						int docIdCount=0;
						int firstValueBlock=0;
						String invString="";
						int blockNumber=0;
						int[] compressed;
						
						// Assign the ending offset of the previous word to the starting offset for the new word
						long startOffset=offset;
						
						// Loop through all the documents corresponding to the word
						for (Integer s : docIdSet) {
							// If the document ID is the first document id for a particular block then record that, this is used to perform compression
							if(docIdCount==0)
							{
								firstValueBlock=s;
							}
							
							// Each document ID added by subtracting the first value from it reducing the size of the integer
							docIdlist=docIdlist+" "+ String.valueOf(s-firstValueBlock);
							// Add the corresponding word frequency 
							freqlist=freqlist+" "+String.valueOf(dictionary.get(s));
							// Increment the document count corresponding to the word
							docIdCount++;
							
							// If the document ID Count is equal to block size then dump the data into the inverted index file
							if(docIdCount==BLOCK_SIZE)
							{
								// Increase the block number corresponding to the word
								blockNumber++;
								// Compress the integer block using PFOR
								compressed=IntegerCompression.compressBlockData(StringToArray(docIdlist.trim()));
								// Add the string format of the compresssed document list along with the frequencies to the inverted list
								invString=invString+arrayToString(compressed)+freqlist+"^"+" ";
								// Meta data in the beginning of the block to contain the Block number First document id in the block and the length of the compressed integer array
								metadata=metadata+" "+firstValueBlock+" "+compressed.length+" ";
								metadata=String.valueOf(blockNumber)+" "+metadata.trim();
								// Write the data into the inverted index file
								invertedIndexOut.write(metadata+"|"+invString);
								// Update the position data to get the proper position data in the inverted index
								offset=offset+(metadata+"|"+invString).length();
								// Reinitialize the variables
								docIdlist="";
								freqlist="";
								docIdCount=0;
								firstValueBlock=0;
								metadata="";
								invString="";
							}
						}
						// If the document list is not empty, this is when there is data left after all the document ids are taken count of and the number of document ids is lesset than the block size and thus does not get picked up
						if(!docIdlist.equals(""))
						{
							// Increment the block number
							blockNumber++;
							// Generate the compressed array of integers
							compressed=IntegerCompression.compressBlockData(StringToArray(docIdlist.trim()));
							// Generate the metadata and the inverted index data
							metadata=metadata+" "+firstValueBlock+" "+compressed.length+" ";
							invString=invString+arrayToString(compressed)+freqlist+"^"+" ";
						}
						
						// Write to the lexicon file
						metadata=String.valueOf(blockNumber)+" "+metadata.trim();
						invertedIndexOut.write(metadata+"|"+invString);
						offset=offset+(metadata+"|"+invString).length();
						termOut.write(prevword+" "+String.valueOf(docIdSet.size())+" "+String.valueOf(startOffset)+" "+String.valueOf(offset)+"\n");
						

					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					finally
					{
						invertedIndexOut.close();
						termOut.close();
					}
					
					// Reinitialize the document ID Set as well as the dictionary containing the document 
					docIdSet=new TreeSet<Integer>();
					dictionary = new HashMap<Integer, Integer>();
				}
				
			
				//If the word shows up once then the frequency will be 0.
				// Update the frequency in the dictionary if the word already exists in the set else add it in the set as well as dictionary
				if(docIdSet.contains(docId))
				{
					dictionary.put(docId, dictionary.get(docId) + 1);
				}
				else
				{
					docIdSet.add(docId);
					dictionary.put(docId,0);
				}
				// Assign the word being processed to the previous word variable
				prevword=word;
			}
		} 
		catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	
	// Converts an integer array to String
	public static String arrayToString(int[] intArray)
	{
		String intArraytoString="";
		
		for(int i:intArray)
		{
			intArraytoString=intArraytoString+" "+String.valueOf(i);
		}
		
		return intArraytoString.trim();
	}
	
	// Converts a string to integer array
	public static int[] StringToArray(String valueString)
	{
		String[] valueArray=valueString.split(" ");
		
		int[] intArray=new int[valueArray.length];
		
		for(int i=0;i<valueArray.length;i++)
		{
			intArray[i]=Integer.parseInt(valueArray[i]);
		}
		
		return intArray;
	}


}

