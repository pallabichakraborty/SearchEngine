package search.words;

/*******************************************************************************************************
 * 
 * @author pallabichakraborty
 * Description: This class is used to store the data pertaining to details stored with every word in the lexicon index
 *
 ********************************************************************************************************/

public class TermDetails {
	// Number of Documents containing the word
	int numOfDocuments;
	// Starting position of the document id blocks in the inverted index
	long startPosition;
	// Ending position of the document id blocks in the inverted index
	long endPosition;
	
	// Overidden constructor with the variables added
	public TermDetails(int numOfDocuments, long startPosition, long endPosition) {
		super();
		this.numOfDocuments = numOfDocuments;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}
}
