package search.words;

/*************************************************************************************************************************
 * 
 * @author pallabichakraborty
 * Description: This class is used to store the URL for a website along with its BM25 Score for a particular word
 *
 **************************************************************************************************************************/

public class DocumentURLScore {
	// URL of the website
	String url;
	// BM25 Score of the URL for a particular word
	double bm25Score;
	
	// Overriden Constructor with the variables added
	public DocumentURLScore(String url, double bm25Score) {
		super();
		this.url = url;
		this.bm25Score = bm25Score;
	}

}
