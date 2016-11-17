package merge.wordlists;

import me.lemire.integercompression.differential.IntegratedIntCompressor;

/*******************************************************************************************************
 * Author: Pallabi Chakraborty
 * Description: Class containing the methods to compress the integers
 * Reference https://github.com/lemire/JavaFastPFOR/blob/master/example.java
 ********************************************************************************************************/
	
public class IntegerCompression {
	
	// Method to compress an array of integer data
	public static int[] compressBlockData(int[] blockData)
	{
		// Group of document Ids pertaining to one term are compressed together 
		IntegratedIntCompressor iic = new IntegratedIntCompressor();
        int[] compressed = iic.compress(blockData);
		return compressed;
	}
	
}
