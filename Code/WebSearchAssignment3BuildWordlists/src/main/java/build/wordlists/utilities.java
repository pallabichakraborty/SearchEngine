package build.wordlists;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/******************************************************************************************************
 * 
 * Author: Pallabi Chakraborty
 * Description: Contains the methods which are used for the extraction process
 * 
 ******************************************************************************************************/
public class utilities {
	
	// Method to extract data from the files in a folder
	// Input Parameter : readFolderName : The folder containing the gzip files containing the HTML page data
	// writeFolderName : destination folder name where the word list files are to be generated
	// sequenceFileName: Source file name from which the next document identifier number is to be fetched
	public static void extractDataFromFolder(String readFolderName,String writeFolderName,String sequenceFileName)
	{
		// Initialize the file number to 1 so that we are able to track the number of files being processed
		int fileNumber = 1;
		// Loop through the folder which contains the GZIP files which have the HTML stripped off
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(readFolderName))) {
        	// Loop through the folder
            for (Path path : directoryStream) {
            	// Read the file only if the extension of the file is GZ
            	if(path.toString().substring(path.toString().length()-2).equals("gz"))
            	{
            		// Method to extract data from the file with the naming of the word lists files which will store the word postings along with the document index files.
            		// This method will also generate the document identifier which will be used in the document index
            		utilities.extractDataFromFiles(path.toString(), writeFolderName+"/wordlist_"+String.valueOf(fileNumber/10), writeFolderName+"/doclist",sequenceFileName);
            		// Increment the file number processed by the program
            		fileNumber++;
            	}     
            }
        } 
        catch (Exception ex) 
        {
        	ex.printStackTrace();
        }
        
	}
	
	// Method to extract data from the file
	// Input parameter: readfileName: The file name which contains the text containing the words in the HTML page
	// writefileName: The file name which will store the word postings
	// doclistfileName: The file name which will store the document index
	// sequenceFileName: The File Name which contains the sequence number for generation of document identifier for document index
	public static void extractDataFromFiles(String readfileName,String writefileName,String doclistfileName,String sequenceFileName) throws IOException
	{
		// Initializing the encoding for the data
		String encoding="UTF-8";
		
		// Declaring and Initializing the variables
		InputStream fileStream=null;
		BufferedReader buffered= null;
		Reader decoder=null;
		InputStream gzipStream=null;

		FileWriter fileWriter;
		BufferedWriter bufferWriter;

		FileWriter docfileWriter;
		BufferedWriter docbufferWriter;

		// Fetch the document identifier from the file containing the next sequence number
		int docIdentifier=utilities.getDocumentIDSeq(sequenceFileName);

		try {

			//Variables for Reading the GZ file
			fileStream = new FileInputStream(readfileName);
			gzipStream = new GZIPInputStream(fileStream);
			decoder = new InputStreamReader(gzipStream, encoding);
			buffered = new BufferedReader(decoder);
			
			//Intialize the variables
			String sCurrentLine;

			String url = "";
			long docId=0;
			long lineNumber=0;
			long wordCount=0;

			//Variables for Writing into the wordlist file
			File docfile = new File(doclistfileName);
			docfile.getParentFile().mkdirs();
			//true = append file
			docfileWriter = new FileWriter(doclistfileName,true);
			docbufferWriter = new BufferedWriter(docfileWriter);

			//Variables for Writing into the wordlist file
			File file = new File(writefileName);
			file.getParentFile().mkdirs();
			//true = append file
			fileWriter = new FileWriter(writefileName,true);
			bufferWriter = new BufferedWriter(fileWriter);

			// Loop till there is some data left in the file
			while ((sCurrentLine = buffered.readLine()) != null) 
			{	
				//Increment the line number being read
				lineNumber++;

				// Ignore the first 15 lines which contains the document identifier for every file
				if(lineNumber>15)
				{
					// Retrieve the URL for the HTML being read
					if(sCurrentLine.contains("WARC-Target-URI:"))
					{
						url=sCurrentLine.replace("WARC-Target-URI:", "").trim();
						docId=docIdentifier++;
						System.out.println("Doc Number:"+String.valueOf(docId));
					}
					// Detect end of the HTML file
					else if(sCurrentLine.contains("WARC/1.0"))
					{
						// If the memory contains the data for some HTML page then dump it into the document list index file
						if(!(url.contains("") && docId==0))
						{
							// Entry of the form <document id> <URL> <Word Count of the HTML page>
							docbufferWriter.write(String.valueOf(docId)+" "+url+" "+String.valueOf(wordCount)+"\n");
						}
						
						// Reinitialize the variables
						url=null;
						docId=0;
						wordCount=0;
					}
					// Fetch data only if the line contains some data and the data line contains ASCII characters
					else if(!sCurrentLine.equals("") && sCurrentLine.matches("\\A\\p{ASCII}*\\z"))
					{
						// Ignore the header information
						if(!(sCurrentLine.contains("WARC//1.0")
								||sCurrentLine.contains("WARC-Type")
								||sCurrentLine.contains("WARC-Target-URI")
								||sCurrentLine.contains("WARC-Date")
								||sCurrentLine.contains("WARC-Record-ID")
								||sCurrentLine.contains("WARC-Refers-To")
								||sCurrentLine.contains("WARC-Block-Digest")
								||sCurrentLine.contains("Content-Type")
								||sCurrentLine.contains("Content-Length")))
						{

							// Loop through the words in the line after replacing common signs
							for (String words : sCurrentLine.replace("&"," ").replace("#"," ").replace("|"," ").replace(")"," ").replace("(", " ").replace("\""," ").replace("//"," ").replace(">>", " ").replace(",", " ").replace("?", " ").replace(":", " ").replace("[", " ").replace("]", " ").replace("$", " ").replace("{", " ").replace("}", " ").replace("!", " ").split(" "))
							{
								// If the last charcter of the word is . then substring it to exclude the .
								if(words.length()>1 && words.substring(words.length()-1).equals("."))
								{
									words=words.substring(0, words.length()-1).trim().toLowerCase();
								}
								// If the word contains something other than the common symbols then go ahead with adding the word into the word postings
								if(!(words.trim().length()==0 || words.trim().equals("-") || words.trim().equals("--")|| words.trim().equals(".")))
								{
									// Remove all the unicode data
									// Format <word> <Document idenfier containing the word>
									String data=words.replaceAll("\\p{Cc}", "").toLowerCase()+" "+String.valueOf(docId)+"\n";
									// Increment the word Count for the file
									wordCount++;
									// Write the data into the word list file
									bufferWriter.write(data);
								}
							}
						}
					}
				}
			}
			// Final entry to the document index file for the data which is left for the last HTML file
			docbufferWriter.write(String.valueOf(docId)+" "+url+" "+String.valueOf(wordCount)+"\n");
			bufferWriter.close();
			docbufferWriter.close();

		} catch (IOException e) {

			e.printStackTrace();
		}
		finally
		{
			// Update the document Identifier Sequence number
			utilities.updateDocumentIdSeq(docIdentifier,sequenceFileName);
			buffered.close();
			decoder.close();
			gzipStream.close();
			fileStream.close();
		}

	}
	
	//Get the maximum Sequence ID for the documents
		public static int getDocumentIDSeq(String sequenceFileName)
		{
			//Read the file with the next sequence number for the docId to be associated with the document
			int seqNumber=0;
			// Read from the sequence containing file
			try {
				
			    BufferedReader in = new BufferedReader(new FileReader(sequenceFileName));
			    seqNumber=Integer.parseInt(in.readLine());
			    in.close();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
			return seqNumber;
		}
		//Update back the latest sequence id for the documents
		public static void updateDocumentIdSeq(int seq,String sequenceFileName)
		{
			//Update back the document id into the file containing the sequence number
			File file = new File(sequenceFileName);
			FileWriter writer;
			// Write the sequence number into the file containing the previous latest document identifier
			try {
				writer = new FileWriter(file, false);
				writer.write(String.valueOf(seq));
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
		}

}
