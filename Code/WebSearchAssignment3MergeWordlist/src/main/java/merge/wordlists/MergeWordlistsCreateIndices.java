package merge.wordlists;

/*******************************************************************************************************
*
* Author: Pallabi Chakraborty
* Description: Class which generates the inverted index and lexicon index from sorted word postings
*
*******************************************************************************************************/

import merge.wordlists.utilties;

public class MergeWordlistsCreateIndices {

	public static void main(String[] args) {
		// If the number of arguments is lesser than expected then throw an error
		if(args.length<3)
		{
			System.out.println("Arguments specified are missing or less than expected");
		}
		else
		{
			// Method to generate the lexicon and inverted index
			// Input parameters: Absolute path for the sorted word posting
			//                   Absolute path where the inverted index file should get generated
			//                   Absolute path where the lexicon list should get generated
			//args[0]=/Users/pallabichakraborty/Desktop/WebSearchEngines/Homeworks/Homework3/index/sort_wordlist_0
			//args[1]=/Users/pallabichakraborty/Desktop/WebSearchEngines/Homeworks/Homework3/index/inverted_list
			//args[2]=/Users/pallabichakraborty/Desktop/WebSearchEngines/Homeworks/Homework3/index/lexicon_list
			utilties.generateIndexFiles(args[0], args[1], args[2]);
			
		}
		
		

	}

}
