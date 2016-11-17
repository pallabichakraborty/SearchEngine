package search.words;

/*******************************************************************************************************************************
 * 
 * @author pallabichakraborty
 * Description: This class is used to store the details of the documents from the document index for a particular document id.
 *
 ********************************************************************************************************************************/

public class DocumentDetails {
	
	// URL for the document
	String url;
	// Word count of the document
	int wordCount;
	
	// Overriden constructor
	public DocumentDetails(String url, int wordCount) 
	{
		super();
		this.url = url;
		this.wordCount = wordCount;
	}

}
