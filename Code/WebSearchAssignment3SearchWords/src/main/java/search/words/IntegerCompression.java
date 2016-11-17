package search.words;

import me.lemire.integercompression.differential.IntegratedIntCompressor;

/*******************************************************************************************************
 * Author: Pallabi Chakraborty
 * Description: Class containing the methods to decompress the integers
 * Reference https://github.com/lemire/JavaFastPFOR/blob/master/example.java
 *******************************************************************************************************/

public class IntegerCompression {
	
	// Method to decompress an compressed array of compressed integers compressed using PFOR
	public static int[] decompressBlockData(int[] compressedblockData)
	{
		// Create an object for the Compressor class
		IntegratedIntCompressor iic = new IntegratedIntCompressor();
		// Decompress the compressed array
        int[] recov = iic.uncompress(compressedblockData);
		return recov;
	}


}
