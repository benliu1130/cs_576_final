//this class has a method "search"
//it takes two images as input, and then output a integer array, showing vectors
//the output is int[][][]
//first dimension means x or y. 0 = x and 1 = y
//second dimension means a block's order in the row
//third dimension means a block's order in the column

public class VectorSearch {
	//a search method using blockSize as argument
	private static int[][][] search(int[][][] previousArray, int[][][] currentArray, int kHorizontal, int kVertical, int blockSize){
		int width = previousArray[0].length;
		int height = previousArray[0][0].length;
		int blocksInRow = width/blockSize; //number of blocks in a row
		int blocksInColumn = height/blockSize; //number of blocks in a column
		previousArray = rgb_ycbcr(previousArray); //convert to YCbCr
		currentArray = rgb_ycbcr(currentArray); //convert to YCbCr
		int[][] previousArrayY = previousArray[0]; //Y channel only
		int[][] currentArrayY = currentArray[0]; //Y channel only
		
		int[][][] vectors = new int[2][blocksInRow][blocksInColumn];//an array to store output vectors
		
		for(int blockX = 0; blockX <= blocksInRow - 1; blockX++){
			for(int blockY = 0; blockY <= blocksInColumn - 1; blockY++){
				int[][] currentBlock = new int[blockSize][blockSize];
				for(int i = 0; i <= blockSize-1; i++){
					for(int j = 0; j <= blockSize-1; j++){
						currentBlock[i][j] = currentArrayY[blockX*blockSize+i][blockY*blockSize+j];
					}
				}
				int searchingLeftBound = (blockX*blockSize-kHorizontal >=0)?(blockX*blockSize-kHorizontal):0;
				int searchingUpBound = (blockY*blockSize-kVertical >=0)?(blockY*blockSize-kVertical):0;
				int searchingRightBound = (blockX*blockSize+kHorizontal+(blockSize-1) <= width-1)?(blockX*blockSize+kHorizontal):(width-1-(blockSize-1));
				int searchingLowBound = (blockY*blockSize+kVertical+(blockSize-1) <= height-1)?(blockY*blockSize+kVertical):(height-1-(blockSize-1));
				int min = 255*255*blockSize*blockSize; //record the minimum
				int minDistance = kHorizontal + kVertical;
				int minX = 0;
				int minY = 0;
				for(int x = searchingLeftBound; x <= searchingRightBound; x++){
					for(int y = searchingUpBound; y <= searchingLowBound; y++){
						int[][] previousBlock = new int[blockSize][blockSize];
						for(int i = 0; i <= blockSize-1; i++){
							for(int j = 0; j <= blockSize-1; j++){
								previousBlock[i][j] = previousArrayY[x+i][y+j];
							}
						}
						int distance = Math.abs(x-blockX*blockSize)+Math.abs(y-blockY*blockSize);
						int msd = msd(previousBlock, currentBlock, min);
						if (msd == min && distance < minDistance){
							minX = x;
							minY = y;
							minDistance = distance;
						}
						else if (msd < min){
							min = msd;
							minX = x;
							minY = y;
							minDistance = distance;
						}
					}
				}
				int vectorX = minX - blockX*blockSize;
				int vectorY = minY - blockY*blockSize;
				vectors[0][blockX][blockY] = vectorX;
				vectors[1][blockX][blockY] = vectorY;
			}
		}
		return vectors;
	}//search method with blockSize as argument
	
	//a search method not using blockSize as argument
	//use 16x16 as default block size
	public static int[][][] search(byte[] previousImg, byte[] currentImg, int width, int height, int kHorizontal, int kVertical){
		int blockSize = 16;
		int blocksInRow = width/blockSize; //number of blocks in a row
		int blocksInColumn = height/blockSize; //number of blocks in a row
		int[][][] vectors = new int[2][blocksInRow][blocksInColumn];//an array to store output vectors
		int[][][] previousArray = new int[3][width][height];
		int[][][] currentArray = new int[3][width][height];
		bytesToArray(previousImg, previousArray);
		bytesToArray(currentImg, currentArray);
		vectors = search(previousArray, currentArray, kHorizontal, kVertical, blockSize);
		return vectors;
	}
	
	//Fast search using hierarchical search
	//level means how many levela of hierarchy
	//use 16x16 as default block size
	public static int[][][] fastSearch(byte[] previousImg, byte[] currentImg, int width, int height, int kHorizontal, int kVertical, int level){
		int blockSize = 16;
		int[][][] previousArray = new int[3][width][height];
		int[][][] currentArray = new int[3][width][height];
		bytesToArray(previousImg, previousArray);
		bytesToArray(currentImg, currentArray);
		int[][][][] previousArrayHierarchy = new int[level+1][][][];//a hierarchy,0=level1,1=level2,...
		int[][][][] currentArrayHierarchy = new int[level+1][][][];//a hierarchy,0=level1,1=level2,...
		previousArrayHierarchy[0] = previousArray;
		currentArrayHierarchy[0] = currentArray;
		for(int k = 1; k <= level; k++){
			previousArrayHierarchy[k] = subsample(previousArrayHierarchy[k-1]);
			currentArrayHierarchy[k] = subsample(currentArrayHierarchy[k-1]);
			kHorizontal = kHorizontal/2;
			kVertical = kVertical/2;
			blockSize = blockSize/2;
		}
		int[][][][] vectorHierarchy = new int[level+1][][][];
		vectorHierarchy[level] = search(previousArrayHierarchy[level], currentArrayHierarchy[level], kHorizontal, kVertical, blockSize);
		for(int k = level-1; k >=0; k--){
			vectorHierarchy[k] = doubleVectors(vectorHierarchy[k+1]);
			refine(vectorHierarchy[k], previousArrayHierarchy[k], currentArrayHierarchy[k]);
		}
		return vectorHierarchy[0];
	}

	//this method do MAD, it stop when mad bigger than min
	private static int mad(int[][] previousBlock, int[][] currentBlock, int min){
		int mad = 0;
		for (int i = 0; i <= previousBlock.length - 1 && mad <= min; i++){
			for (int j = 0; j <= previousBlock[0].length - 1 && mad <= min; j++){
				mad += Math.abs(previousBlock[i][j] - currentBlock[i][j]);
			}
		}
		return mad;
	}//end mad

	//this method do MSD, it stop when msd bigger than min
	private static int msd(int[][] previousBlock, int[][] currentBlock, int min){
		int msd = 0;
		for (int i = 0; i <= previousBlock.length - 1 && msd <= min; i++){
			for (int j = 0; j <= previousBlock[0].length - 1 && msd <= min; j++){
				msd += (previousBlock[i][j] - currentBlock[i][j])*(previousBlock[i][j] - currentBlock[i][j]);
			}
		}
		return msd;
	}//end mad
	
	//this method convert RGB to YCbCr
	private static int[][][] rgb_ycbcr(int[][][] inputImageArray){
		int width = inputImageArray[0].length;
		int height = inputImageArray[0][0].length;
		int[][][] outputImageArray = new int[3][width][height];
		for(int i = 0; i <= width - 1; i++){
			for(int j = 0; j <= height - 1; j++){
				int r = inputImageArray[0][i][j];
				int g = inputImageArray[1][i][j];
				int b = inputImageArray[2][i][j];
				outputImageArray[0][i][j] = (int)Math.round(r*77/256.0 + g*150/256.0 + b*29/256.0); //Y channel
				outputImageArray[1][i][j] = (int)Math.round(r*(-44)/256.0 + g*(-87)/256.0 + b*131/256.0 + 128); //Cb channel
				outputImageArray[2][i][j] = (int)Math.round(r*131/256.0 + g*(-110)/256.0 + b*(-21)/256.0 + 128); //Cr channel
			}
		}
		return outputImageArray;
	}//end rgb_ycbcr

	//subsampling an image by 2
	//the width and height of the input image must be dividable by 2
	private static int[][][] subsample(int[][][] inputImgArray){
		int width = inputImgArray[0].length;
		int height = inputImgArray[0][0].length;
		int[][][] outputImgArray = new int[3][width/2][height/2];
		for(int x = 0; x<=width/2-1; x++){
			for(int y = 0; y<=height/2-1; y++){
				outputImgArray[0][x][y] = inputImgArray[0][x*2][y*2];
				outputImgArray[1][x][y] = inputImgArray[1][x*2][y*2];
				outputImgArray[2][x][y] = inputImgArray[2][x*2][y*2];
			}
		}
		return outputImgArray;
	}//end subsample

	//double the value of vectors
	//go from level n back to level n-1
	private static int[][][] doubleVectors(int[][][] vectors){
		int blocksInRow = vectors[0].length; //number of blocks in a row
		int blocksInColumn = vectors[0][0].length; //number of blocks in a column
		int[][][] output = new int[2][blocksInRow][blocksInColumn];
		for(int x = 0; x<=blocksInRow-1; x++){
			for(int y = 0; y<=blocksInColumn-1; y++){
				output[0][x][y] = 2*vectors[0][x][y];
				output[1][x][y] = 2*vectors[1][x][y];
			}
		}
		return output;
	}//end doubleVectors
	
	private static void refine(int[][][] vectors, int[][][] previousArray, int[][][] currentArray){
		int width = previousArray[0].length;
		int height = previousArray[0][0].length;
		int blocksInRow = vectors[0].length;
		int blocksInColumn = vectors[0][0].length;
		int blockSize = width/blocksInRow;
		previousArray = rgb_ycbcr(previousArray); //convert to YCbCr
		currentArray = rgb_ycbcr(currentArray); //convert to YCbCr
		int[][] previousArrayY = previousArray[0]; //Y channel only
		int[][] currentArrayY = currentArray[0]; //Y channel only
		
		for(int blockX = 0; blockX <= blocksInRow - 1; blockX++){
			for(int blockY = 0; blockY <= blocksInColumn - 1; blockY++){
				int[][] currentBlock = new int[blockSize][blockSize];
				for(int i = 0; i <= blockSize-1; i++){
					for(int j = 0; j <= blockSize-1; j++){
						currentBlock[i][j] = currentArrayY[blockX*blockSize+i][blockY*blockSize+j];
					}
				}
				int targetX = blockX*blockSize + vectors[0][blockX][blockY];
				int targetY = blockY*blockSize + vectors[1][blockX][blockY];
				int searchingLeftBound = (targetX-1 >=0)?(targetX-1):0;
				int searchingUpBound = (targetY-1 >=0)?(targetY-1):0;
				int searchingRightBound = (targetX+1+(blockSize-1) <= width-1)?(targetX+1):(width-1-(blockSize-1));
				int searchingLowBound = (targetY+1+(blockSize-1) <= height-1)?(targetY+1):(height-1-(blockSize-1));
				int min = 255*255*blockSize*blockSize; //record the minimum
				int minDistance = vectors[0][blockX][blockY]+vectors[1][blockX][blockY]+2;
				int minX = targetX;
				int minY = targetY;
				for(int x = searchingLeftBound; x <= searchingRightBound; x++){
					for(int y = searchingUpBound; y <= searchingLowBound; y++){
						int[][] previousBlock = new int[blockSize][blockSize];
						for(int i = 0; i <= blockSize-1; i++){
							for(int j = 0; j <= blockSize-1; j++){
								previousBlock[i][j] = previousArrayY[x+i][y+j];
							}
						}
						int distance = Math.abs(x-blockX*blockSize)+Math.abs(y-blockY*blockSize);
						int mad = mad(previousBlock, currentBlock, min);
						if (mad == min && distance < minDistance){
							minX = x;
							minY = y;
							minDistance = distance;
						}
						else if (mad < min){
							min = mad;
							minX = x;
							minY = y;
							minDistance = distance;
						}
					}
				}
				int vectorX = minX - blockX*blockSize;
				int vectorY = minY - blockY*blockSize;
				vectors[0][blockX][blockY] = vectorX;
				vectors[1][blockX][blockY] = vectorY;
			}
		}
		
	}//end refine


	//this method copy the data in a byte array into a 3-dimensional integer array
	//first dimension means color, (RGB) 0=R, 1=G, 2=B
	//second dimension means column, 0=first column, 1=second column, and so on
	//third dimension means row, 0=first row, 1=second row, and so on
	//the values range from 0 to 255. 0 means most dark, 255 means most bright
	private static void bytesToArray(byte[] bytes, int[][][] imageArray){
		int width = imageArray[0].length;
		int height = imageArray[0][0].length;
		int index = 0;
	    for (int color = 0; color <= 2; color++){
	    	for (int y = 0; y <= height - 1; y++){
	    		for (int x = 0; x <= width - 1; x++){
	    			int k = bytes[index];
	    			imageArray[color][x][y] = (k + ((k < 0)?256:0)); //k = k + 256 if x < 0
	    			index++;
	    		}
	    	}
	    }
	}//end bytesToArray
}
