package build.wordlists;

import java.io.IOException;

/******************************************************************************************************
 * 
 * Author: Pallabi Chakraborty
 * Description: This program is used to generate words posting extracted from the data from the GZIP files
 *              from the Common Crawl database. This class contains the main method which calls the method
 *              which does the extractions
 *
 *******************************************************************************************************/

public class CreateProcessWordlists {

	public static void main(String[] args) throws IOException {
		// Check for the number of arguments supplied to the code
		if(args.length<3)
		{
			System.out.println("Arguments specified are missing or less than expected");
		}
		else
		{
			//args[0] = "/Users/pallabichakraborty/Desktop/WebSearchEngines/Homeworks/Homework3/data"
			//args[1] = "/Users/pallabichakraborty/Desktop/WebSearchEngines/Homeworks/Homework3/index"
			//args[2] = "/Users/pallabichakraborty/Desktop/WebSearchEngines/Homeworks/Homework3/documentIdSequence"
			//Extract the data from the GZIP files and generate word postings
			utilities.extractDataFromFolder(args[0], args[1],args[2] );
		}
		
	}

}
