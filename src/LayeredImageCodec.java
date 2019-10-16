//Nov. 6, 2009
//this class has two method: encoder and decoder
//the input to encoder is an image, two parameters n1(foreground) and n2(background)
//and layer information
//layer information is a boolean array, true means foreground, false means background
//e.x. if layer[5][3] = true, it means the block at fifth column and third rows is foreground
//the output of encoder is a binary array
//the format is:
//coeff1 coeff2....coeff64 of block1 of Y channel
//coeff1 coeff2....coeff64 of block2 of Y channel
//...
//coeff1 coeff2....coeff64 of last block of Y channel
//then Cb and Cr channel (the number of blocks of Cb and Cr is less than Y in 4:2:0)
//4:2:0 subsampling is used in encoder420
//4:2:0 and zigzag ordering is used in encoder420z

public class LayeredImageCodec {
	
	//default
	public static int[] encoder(byte[] image, int width, int height, int n1, int n2, boolean[][] layer){
		int foregroundQ = (int)Math.round(Math.pow(2, n1)); //quantizer of foreground
		int backgroundQ = (int)Math.round(Math.pow(2, n2)); //quantizer of background
		int blocksInRow = width/8; //number of blocks in a row
		int blocksInColumn = height/8; //number of blocks in a column
		int numberOfBlocks = blocksInRow * blocksInColumn; //total number of blocks
				
		int[][][] imageArray = new int[3][width][height];
		bytesToArray(image, imageArray);		
		double[][][] imageArrayYCbCr = rgb_ycbcr(imageArray);//RGB to YCbCr
		
		double[][] yArray = imageArrayYCbCr[0];
		double[][] cbArray = imageArrayYCbCr[1];
		double[][] crArray = imageArrayYCbCr[2];
				
		//an output integer array of this method
		int[] output = new int[64 * numberOfBlocks * 3];
		
		//Y channel
		for (int y = 0; y <= blocksInColumn - 1; y++){
			for (int x = 0; x <= blocksInRow - 1; x++){
				int quantizer = layer[x][y] ? foregroundQ : backgroundQ;
				int blockOrder = blocksInRow * y + x; //the order of this block
				int offset = 64 * blockOrder;
				double[][] block = new double[8][8];
				//copy the block from the image to a 8x8 array
				for (int i = 0; i <= 7; i++){
					for (int j = 0; j <= 7; j++){
						block[i][j] = yArray[8*x+i][8*y+j];
					}
				}
				block = DCT.dct(block);
				
				int[][] blockQ = quantize(block, quantizer);
				//copy 64 coefficients
				for (int k = 0 ; k <= 63; k++){ 
					output[offset + k] = blockQ[k%8][k/8];
				}
			}
		}
		
		//Cb channel
		for (int y = 0; y <= blocksInColumn - 1; y++){
			for (int x = 0; x <= blocksInRow - 1; x++){
				int quantizer = layer[x][y] ? foregroundQ : backgroundQ;
				int blockOrder = blocksInRow * y + x; //the order of this block
				int offset = 64 * numberOfBlocks + 64 * blockOrder;
				double[][] block = new double[8][8];
				//copy the block from the image to a 8x8 array
				for (int i = 0; i <= 7; i++){
					for (int j = 0; j <= 7; j++){
						block[i][j] = cbArray[8*x+i][8*y+j];
					}
				}
				block = DCT.dct(block);
				int[][] blockQ = quantize(block, quantizer);
				//copy 64 coefficients
				for (int k = 0 ; k <= 63; k++){ 
					output[offset + k] = blockQ[k%8][k/8];
				}
			}
		}
		
		//Cr channel
		for (int y = 0; y <= blocksInColumn - 1; y++){
			for (int x = 0; x <= blocksInRow - 1; x++){
				int quantizer = layer[x][y] ? foregroundQ : backgroundQ;
				int blockOrder = blocksInRow * y + x; //the order of this block
				int offset = 64 * numberOfBlocks * 2 + 64 * blockOrder;
				double[][] block = new double[8][8];
				//copy the block from the image to a 8x8 array
				for (int i = 0; i <= 7; i++){
					for (int j = 0; j <= 7; j++){
						block[i][j] = crArray[8*x+i][8*y+j];
					}
				}
				block = DCT.dct(block);
				int[][] blockQ = quantize(block, quantizer);
				//copy 64 coefficients
				for (int k = 0 ; k <= 63; k++){ 
					output[offset + k] = blockQ[k%8][k/8];
				}
			}
		}
		
		//System.out.println("total time of DCT: " + totalTime);
		return output;
	}//end encoder method
	
	//default
	public static byte[] decoder(int[] coefficients, int width, int height, int n1, int n2, boolean[][] layer){
		int foregroundQ = (int)Math.round(Math.pow(2, n1)); //quantizer of foreground
		int backgroundQ = (int)Math.round(Math.pow(2, n2)); //quantizer of background
		int blocksInRow = width/8; //number of blocks in a row
		int blocksInColumn = height/8; //number of blocks in a column
		int numberOfBlocks = blocksInRow * blocksInColumn; //total number of blocks
		
		int imageSize = height * width;
		
		double[][] yArray = new double[width][height];
		double[][] cbArray = new double[width][height];
		double[][] crArray = new double[width][height];
		
		//Y channel
		for (int k = 0; k <= numberOfBlocks - 1; k++){ //from the first block to last block
			int offset = 64 * k;
			int x = k%blocksInRow; //the coordinate x of the block
			int y = k/blocksInRow; //the coordinate y of the block
			int quantizer = layer[x][y] ? foregroundQ : backgroundQ;
			
			int[][] blockQ = new int[8][8];
			//copy 64 coefficients
			for (int m = 0 ; m <= 63; m++){ 
				 blockQ[m%8][m/8] = coefficients[offset + m];
			}
			double[][] block = dequantize(blockQ, quantizer);
			block = DCT.inverseDct(block);
			//copy the block to image
			for (int i = 0; i <= 7; i++){
				for (int j = 0; j <= 7; j++){
					yArray[8*x+i][8*y+j] = block[i][j];
				}
			}
		}
		
		//Cb channel
		for (int k = 0; k <= numberOfBlocks - 1; k++){ //from the first block to last block
			int offset = 64 * numberOfBlocks + 64 * k;
			int x = k%blocksInRow; //the coordinate x of the block
			int y = k/blocksInRow; //the coordinate y of the block
			int quantizer = layer[x][y] ? foregroundQ : backgroundQ;
			
			int[][] blockQ = new int[8][8];
			//copy 64 coefficients
			for (int m = 0 ; m <= 63; m++){ 
				 blockQ[m%8][m/8] = coefficients[offset + m];
			}
			double[][] block = dequantize(blockQ, quantizer);
			block = DCT.inverseDct(block);
			//copy the block to image
			for (int i = 0; i <= 7; i++){
				for (int j = 0; j <= 7; j++){
					cbArray[8*x+i][8*y+j] = block[i][j];
				}
			}
		}
		
		//Cr channel
		for (int k = 0; k <= numberOfBlocks - 1; k++){ //from the first block to last block
			int offset = 64 * numberOfBlocks * 2 + 64 * k;
			int x = k%blocksInRow; //the coordinate x of the block
			int y = k/blocksInRow; //the coordinate y of the block
			int quantizer = layer[x][y] ? foregroundQ : backgroundQ;
			
			int[][] blockQ = new int[8][8];
			//copy 64 coefficients
			for (int m = 0 ; m <= 63; m++){ 
				 blockQ[m%8][m/8] = coefficients[offset + m];
			}
			double[][] block = dequantize(blockQ, quantizer);
			block = DCT.inverseDct(block);
			//copy the block to image
			for (int i = 0; i <= 7; i++){
				for (int j = 0; j <= 7; j++){
					crArray[8*x+i][8*y+j] = block[i][j];
				}
			}
		}
		
		double[][][] imageArrayYCbCr = new double[3][width][height];
		imageArrayYCbCr[0] = yArray;
		imageArrayYCbCr[1] = cbArray;		
		imageArrayYCbCr[2] = crArray;
		
		int[][][] imageArray = ycbcr_rgb(imageArrayYCbCr);//YCbCr to RGB
		
		//an output byte array of this method
		byte[] output = new byte[3 * imageSize];
		output = arrayToBytes(imageArray);
		
		//System.out.println("total time of inverse DCT: " + totalTime);
		return output;
	}//end decoder method
	
	//use 4:2:0
	public static int[] encoder420(byte[] image, int width, int height, int n1, int n2, boolean[][] layer){
		int foregroundQ = (int)Math.round(Math.pow(2, n1)); //quantizer of foreground
		int backgroundQ = (int)Math.round(Math.pow(2, n2)); //quantizer of background
		int blocksInRowY = width/8; //number of blocks of Y channel in a row
		int blocksInColumnY = height/8; //number of blocks of Y channel in a column
		int numberOfBlocksY = blocksInRowY * blocksInColumnY; //total number of blocks of Y channel
		int blocksInRowC = width/16; //number of blocks of Cb channel in a row
		int blocksInColumnC = height/16; //number of blocks of Cb channel in a column
		int numberOfBlocksC = blocksInRowC * blocksInColumnC; //total number of blocks of Cb channel
		boolean[][] layerC = new boolean[layer.length/2][layer[0].length/2];//layer information for Cb and Cr
		for (int i = 0; i <= layerC.length - 1; i++){
			for (int j = 0; j <= layerC[0].length - 1; j++){
				layerC[i][j] = layer[i*2][j*2];
			}
		}
		
		int[][][] imageArray = new int[3][width][height];
		bytesToArray(image, imageArray);		
		double[][][] imageArrayYCbCr = rgb_ycbcr(imageArray);//RGB to YCbCr
		
		//do 4:2:0 subsampling
		double[][] yArray = imageArrayYCbCr[0];
		double[][] cbArray = new double[width/2][height/2];
		double[][] crArray = new double[width/2][height/2];
		for (int i = 0; i <= cbArray.length - 1; i++){
			for (int j = 0; j <= cbArray[0].length - 1; j++){
				cbArray[i][j] = imageArrayYCbCr[1][i*2][j*2];
			}
		}
		for (int i = 0; i <= crArray.length - 1; i++){
			for (int j = 0; j <= crArray[0].length - 1; j++){
				crArray[i][j] = imageArrayYCbCr[2][i*2][j*2];
			}
		}
		
		//an output integer array of this method
		int[] output = new int[64 * numberOfBlocksY + 64 * numberOfBlocksC * 2];
		
		//Y channel
		for (int y = 0; y <= blocksInColumnY - 1; y++){
			for (int x = 0; x <= blocksInRowY - 1; x++){
				int quantizer = layer[x][y] ? foregroundQ : backgroundQ;
				int blockOrder = blocksInRowY * y + x; //the order of this block
				int offset = 64 * blockOrder;
				double[][] block = new double[8][8];
				//copy the block from the image to a 8x8 array
				for (int i = 0; i <= 7; i++){
					for (int j = 0; j <= 7; j++){
						block[i][j] = yArray[8*x+i][8*y+j];
					}
				}
				block = DCT.dct(block);
				
				int[][] blockQ = quantize(block, quantizer);
				//copy 64 coefficients
				for (int k = 0 ; k <= 63; k++){ 
					output[offset + k] = blockQ[k%8][k/8];
				}
			}
		}
		
		//Cb channel (4:2:0 subsampling is used)
		for (int y = 0; y <= blocksInColumnC - 1; y++){
			for (int x = 0; x <= blocksInRowC - 1; x++){
				int quantizer = layerC[x][y] ? foregroundQ : backgroundQ;
				int blockOrder = blocksInRowC * y + x; //the order of this block
				int offset = 64 * numberOfBlocksY + 64 * blockOrder;
				double[][] block = new double[8][8];
				//copy the block from the image to a 8x8 array
				for (int i = 0; i <= 7; i++){
					for (int j = 0; j <= 7; j++){
						block[i][j] = cbArray[8*x+i][8*y+j];
					}
				}
				block = DCT.dct(block);
				int[][] blockQ = quantize(block, quantizer);
				//copy 64 coefficients
				for (int k = 0 ; k <= 63; k++){ 
					output[offset + k] = blockQ[k%8][k/8];
				}
			}
		}
		
		//Cr channel (4:2:0 subsampling is used)
		for (int y = 0; y <= blocksInColumnC - 1; y++){
			for (int x = 0; x <= blocksInRowC - 1; x++){
				int quantizer = layerC[x][y] ? foregroundQ : backgroundQ;
				int blockOrder = blocksInRowC * y + x; //the order of this block
				int offset = 64 * numberOfBlocksY + 64 * numberOfBlocksC + 64 * blockOrder;
				double[][] block = new double[8][8];
				//copy the block from the image to a 8x8 array
				for (int i = 0; i <= 7; i++){
					for (int j = 0; j <= 7; j++){
						block[i][j] = crArray[8*x+i][8*y+j];
					}
				}
				block = DCT.dct(block);
				int[][] blockQ = quantize(block, quantizer);
				//copy 64 coefficients
				for (int k = 0 ; k <= 63; k++){ 
					output[offset + k] = blockQ[k%8][k/8];
				}
			}
		}
		
		//System.out.println("total time of DCT: " + totalTime);
		return output;
	}//end encoder420 method
	
	//use 4:2:0
	public static byte[] decoder420(int[] coefficients, int width, int height, int n1, int n2, boolean[][] layer){
		int foregroundQ = (int)Math.round(Math.pow(2, n1)); //quantizer of foreground
		int backgroundQ = (int)Math.round(Math.pow(2, n2)); //quantizer of background
		int blocksInRowY = width/8; //number of blocks of Y channel in a row
		int blocksInColumnY = height/8; //number of blocks of Y channel in a column
		int numberOfBlocksY = blocksInRowY * blocksInColumnY; //total number of blocks of Y channel
		int blocksInRowC = width/16; //number of blocks of Cb channel in a row
		int blocksInColumnC = height/16; //number of blocks of Cb channel in a column
		int numberOfBlocksC = blocksInRowC * blocksInColumnC; //total number of blocks of Cb channel
		boolean[][] layerC = new boolean[layer.length/2][layer[0].length/2];//layer information for Cb and Cr
		for (int i = 0; i <= layerC.length - 1; i++){
			for (int j = 0; j <= layerC[0].length - 1; j++){
				layerC[i][j] = layer[i*2][j*2];
			}
		}
		
		int imageSize = height * width;
		
		double[][] yArray = new double[width][height];
		double[][] cbArray = new double[width/2][height/2];
		double[][] crArray = new double[width/2][height/2];
		
		//Y channel
		for (int k = 0; k <= numberOfBlocksY - 1; k++){ //from the first block to last block
			int offset = 64 * k;
			int x = k%blocksInRowY; //the coordinate x of the block
			int y = k/blocksInRowY; //the coordinate y of the block
			int quantizer = layer[x][y] ? foregroundQ : backgroundQ;
			
			int[][] blockQ = new int[8][8];
			//copy 64 coefficients
			for (int m = 0 ; m <= 63; m++){ 
				 blockQ[m%8][m/8] = coefficients[offset + m];
			}
			double[][] block = dequantize(blockQ, quantizer);
			block = DCT.inverseDct(block);
			//copy the block to image
			for (int i = 0; i <= 7; i++){
				for (int j = 0; j <= 7; j++){
					yArray[8*x+i][8*y+j] = block[i][j];
				}
			}
		}
		
		//Cb channel (4:2:0 subsampling is used)
		for (int k = 0; k <= numberOfBlocksC - 1; k++){ //from the first block to last block
			int offset = 64 * numberOfBlocksY + 64 * k;
			int x = k%blocksInRowC; //the coordinate x of the block
			int y = k/blocksInRowC; //the coordinate y of the block
			int quantizer = layerC[x][y] ? foregroundQ : backgroundQ;
			
			int[][] blockQ = new int[8][8];
			//copy 64 coefficients
			for (int m = 0 ; m <= 63; m++){ 
				 blockQ[m%8][m/8] = coefficients[offset + m];
			}
			double[][] block = dequantize(blockQ, quantizer);
			block = DCT.inverseDct(block);
			//copy the block to image
			for (int i = 0; i <= 7; i++){
				for (int j = 0; j <= 7; j++){
					cbArray[8*x+i][8*y+j] = block[i][j];
				}
			}
		}
		
		//Cr channel (4:2:0 subsampling is used)
		for (int k = 0; k <= numberOfBlocksC - 1; k++){ //from the first block to last block
			int offset = 64 * numberOfBlocksY + 64 * numberOfBlocksC + 64 * k;
			int x = k%blocksInRowC; //the coordinate x of the block
			int y = k/blocksInRowC; //the coordinate y of the block
			int quantizer = layerC[x][y] ? foregroundQ : backgroundQ;
			
			int[][] blockQ = new int[8][8];
			//copy 64 coefficients
			for (int m = 0 ; m <= 63; m++){ 
				 blockQ[m%8][m/8] = coefficients[offset + m];
			}
			double[][] block = dequantize(blockQ, quantizer);
			block = DCT.inverseDct(block);
			//copy the block to image
			for (int i = 0; i <= 7; i++){
				for (int j = 0; j <= 7; j++){
					crArray[8*x+i][8*y+j] = block[i][j];
				}
			}
		}
		
		//desubsampling 4:2:0
		double[][][] imageArrayYCbCr = new double[3][width][height];
		imageArrayYCbCr[0] = yArray;
		for (int i = 0; i <= width - 1; i++){
			for (int j = 0; j <= height - 1; j++){
				imageArrayYCbCr[1][i][j] = cbArray[i/2][j/2];
			}
		}
		for (int i = 0; i <= width - 1; i++){
			for (int j = 0; j <= height - 1; j++){
				imageArrayYCbCr[2][i][j] = crArray[i/2][j/2];
			}
		}
		
		int[][][] imageArray = ycbcr_rgb(imageArrayYCbCr);//YCbCr to RGB
		
		//an output byte array of this method
		byte[] output = new byte[3 * imageSize];
		output = arrayToBytes(imageArray);
		
		//System.out.println("total time of inverse DCT: " + totalTime);
		return output;
	}//end decoder420 method
	
	//use 4:2:0 and zigzag
	public static int[] encoder420z(byte[] image, int width, int height, int n1, int n2, boolean[][] layer){
		int foregroundQ = (int)Math.round(Math.pow(2, n1)); //quantizer of foreground
		int backgroundQ = (int)Math.round(Math.pow(2, n2)); //quantizer of background
		int blocksInRowY = width/8; //number of blocks of Y channel in a row
		int blocksInColumnY = height/8; //number of blocks of Y channel in a column
		int numberOfBlocksY = blocksInRowY * blocksInColumnY; //total number of blocks of Y channel
		int blocksInRowC = width/16; //number of blocks of Cb channel in a row
		int blocksInColumnC = height/16; //number of blocks of Cb channel in a column
		int numberOfBlocksC = blocksInRowC * blocksInColumnC; //total number of blocks of Cb channel
		boolean[][] layerC = new boolean[layer.length/2][layer[0].length/2];//layer information for Cb and Cr
		for (int i = 0; i <= layerC.length - 1; i++){
			for (int j = 0; j <= layerC[0].length - 1; j++){
				layerC[i][j] = layer[i*2][j*2];
			}
		}
		
		int[][][] imageArray = new int[3][width][height];
		bytesToArray(image, imageArray);		
		double[][][] imageArrayYCbCr = rgb_ycbcr(imageArray);//RGB to YCbCr
		
		//do 4:2:0 subsampling
		double[][] yArray = imageArrayYCbCr[0];
		double[][] cbArray = new double[width/2][height/2];
		double[][] crArray = new double[width/2][height/2];
		for (int i = 0; i <= cbArray.length - 1; i++){
			for (int j = 0; j <= cbArray[0].length - 1; j++){
				cbArray[i][j] = imageArrayYCbCr[1][i*2][j*2];
			}
		}
		for (int i = 0; i <= crArray.length - 1; i++){
			for (int j = 0; j <= crArray[0].length - 1; j++){
				crArray[i][j] = imageArrayYCbCr[2][i*2][j*2];
			}
		}
		
		//an output integer array of this method
		int[] output = new int[64 * numberOfBlocksY + 64 * numberOfBlocksC * 2];
		
		//Y channel
		for (int y = 0; y <= blocksInColumnY - 1; y++){
			for (int x = 0; x <= blocksInRowY - 1; x++){
				int quantizer = layer[x][y] ? foregroundQ : backgroundQ;
				int blockOrder = blocksInRowY * y + x; //the order of this block
				int offset = 64 * blockOrder;
				double[][] block = new double[8][8];
				//copy the block from the image to a 8x8 array
				for (int i = 0; i <= 7; i++){
					for (int j = 0; j <= 7; j++){
						block[i][j] = yArray[8*x+i][8*y+j];
					}
				}
				block = DCT.dct(block);
				
				int[][] blockQ = quantize(block, quantizer);
				//copy 64 coefficients using zigzag order
				Zigzag zigzag = new Zigzag();
				for (int k = 0 ; k <= 63; k++){ 
					output[offset + k] = blockQ[zigzag.getX()][zigzag.getY()];
					zigzag.go();
				}
			}
		}
		
		//Cb channel (4:2:0 subsampling is used)
		for (int y = 0; y <= blocksInColumnC - 1; y++){
			for (int x = 0; x <= blocksInRowC - 1; x++){
				int quantizer = layerC[x][y] ? foregroundQ : backgroundQ;
				int blockOrder = blocksInRowC * y + x; //the order of this block
				int offset = 64 * numberOfBlocksY + 64 * blockOrder;
				double[][] block = new double[8][8];
				//copy the block from the image to a 8x8 array
				for (int i = 0; i <= 7; i++){
					for (int j = 0; j <= 7; j++){
						block[i][j] = cbArray[8*x+i][8*y+j];
					}
				}
				block = DCT.dct(block);
				int[][] blockQ = quantize(block, quantizer);
				//copy 64 coefficients using zigzag order
				Zigzag zigzag = new Zigzag();
				for (int k = 0 ; k <= 63; k++){ 
					output[offset + k] = blockQ[zigzag.getX()][zigzag.getY()];
					zigzag.go();
				}
			}
		}
		
		//Cr channel (4:2:0 subsampling is used)
		for (int y = 0; y <= blocksInColumnC - 1; y++){
			for (int x = 0; x <= blocksInRowC - 1; x++){
				int quantizer = layerC[x][y] ? foregroundQ : backgroundQ;
				int blockOrder = blocksInRowC * y + x; //the order of this block
				int offset = 64 * numberOfBlocksY + 64 * numberOfBlocksC + 64 * blockOrder;
				double[][] block = new double[8][8];
				//copy the block from the image to a 8x8 array
				for (int i = 0; i <= 7; i++){
					for (int j = 0; j <= 7; j++){
						block[i][j] = crArray[8*x+i][8*y+j];
					}
				}
				block = DCT.dct(block);
				int[][] blockQ = quantize(block, quantizer);
				//copy 64 coefficients using zigzag order
				Zigzag zigzag = new Zigzag();
				for (int k = 0 ; k <= 63; k++){ 
					output[offset + k] = blockQ[zigzag.getX()][zigzag.getY()];
					zigzag.go();
				}
			}
		}
		
		//System.out.println("total time of DCT: " + totalTime);
		return output;
	}//end encoder420z method
	
	//use 4:2:0 and zigzag
	public static byte[] decoder420z(int[] coefficients, int width, int height, int n1, int n2, boolean[][] layer){
		int foregroundQ = (int)Math.round(Math.pow(2, n1)); //quantizer of foreground
		int backgroundQ = (int)Math.round(Math.pow(2, n2)); //quantizer of background
		int blocksInRowY = width/8; //number of blocks of Y channel in a row
		int blocksInColumnY = height/8; //number of blocks of Y channel in a column
		int numberOfBlocksY = blocksInRowY * blocksInColumnY; //total number of blocks of Y channel
		int blocksInRowC = width/16; //number of blocks of Cb channel in a row
		int blocksInColumnC = height/16; //number of blocks of Cb channel in a column
		int numberOfBlocksC = blocksInRowC * blocksInColumnC; //total number of blocks of Cb channel
		boolean[][] layerC = new boolean[layer.length/2][layer[0].length/2];//layer information for Cb and Cr
		for (int i = 0; i <= layerC.length - 1; i++){
			for (int j = 0; j <= layerC[0].length - 1; j++){
				layerC[i][j] = layer[i*2][j*2];
			}
		}
		
		int imageSize = height * width;
		
		double[][] yArray = new double[width][height];
		double[][] cbArray = new double[width/2][height/2];
		double[][] crArray = new double[width/2][height/2];
		
		//Y channel
		for (int k = 0; k <= numberOfBlocksY - 1; k++){ //from the first block to last block
			int offset = 64 * k;
			int x = k%blocksInRowY; //the coordinate x of the block
			int y = k/blocksInRowY; //the coordinate y of the block
			int quantizer = layer[x][y] ? foregroundQ : backgroundQ;
			
			int[][] blockQ = new int[8][8];
			//copy 64 coefficients using zigzag order
			Zigzag zigzag = new Zigzag();
			for (int m = 0 ; m <= 63; m++){ 
				 blockQ[zigzag.getX()][zigzag.getY()] = coefficients[offset + m];
				zigzag.go();
			}
			double[][] block = dequantize(blockQ, quantizer);
			block = DCT.inverseDct(block);
			//copy the block to image
			for (int i = 0; i <= 7; i++){
				for (int j = 0; j <= 7; j++){
					yArray[8*x+i][8*y+j] = block[i][j];
				}
			}
		}
		
		//Cb channel (4:2:0 subsampling is used)
		for (int k = 0; k <= numberOfBlocksC - 1; k++){ //from the first block to last block
			int offset = 64 * numberOfBlocksY + 64 * k;
			int x = k%blocksInRowC; //the coordinate x of the block
			int y = k/blocksInRowC; //the coordinate y of the block
			int quantizer = layerC[x][y] ? foregroundQ : backgroundQ;
			
			int[][] blockQ = new int[8][8];
			//copy 64 coefficients using zigzag order
			Zigzag zigzag = new Zigzag();
			for (int m = 0 ; m <= 63; m++){ 
				 blockQ[zigzag.getX()][zigzag.getY()] = coefficients[offset + m];
				zigzag.go();
			}
			double[][] block = dequantize(blockQ, quantizer);
			block = DCT.inverseDct(block);
			//copy the block to image
			for (int i = 0; i <= 7; i++){
				for (int j = 0; j <= 7; j++){
					cbArray[8*x+i][8*y+j] = block[i][j];
				}
			}
		}
		
		//Cr channel (4:2:0 subsampling is used)
		for (int k = 0; k <= numberOfBlocksC - 1; k++){ //from the first block to last block
			int offset = 64 * numberOfBlocksY + 64 * numberOfBlocksC + 64 * k;
			int x = k%blocksInRowC; //the coordinate x of the block
			int y = k/blocksInRowC; //the coordinate y of the block
			int quantizer = layerC[x][y] ? foregroundQ : backgroundQ;
			
			int[][] blockQ = new int[8][8];
			//copy 64 coefficients using zigzag order
			Zigzag zigzag = new Zigzag();
			for (int m = 0 ; m <= 63; m++){ 
				 blockQ[zigzag.getX()][zigzag.getY()] = coefficients[offset + m];
				zigzag.go();
			}
			double[][] block = dequantize(blockQ, quantizer);
			block = DCT.inverseDct(block);
			//copy the block to image
			for (int i = 0; i <= 7; i++){
				for (int j = 0; j <= 7; j++){
					crArray[8*x+i][8*y+j] = block[i][j];
				}
			}
		}
		
		//desubsampling 4:2:0
		double[][][] imageArrayYCbCr = new double[3][width][height];
		imageArrayYCbCr[0] = yArray;
		for (int i = 0; i <= width - 1; i++){
			for (int j = 0; j <= height - 1; j++){
				imageArrayYCbCr[1][i][j] = cbArray[i/2][j/2];
			}
		}
		for (int i = 0; i <= width - 1; i++){
			for (int j = 0; j <= height - 1; j++){
				imageArrayYCbCr[2][i][j] = crArray[i/2][j/2];
			}
		}
		
		int[][][] imageArray = ycbcr_rgb(imageArrayYCbCr);//YCbCr to RGB
		
		//an output byte array of this method
		byte[] output = new byte[3 * imageSize];
		output = arrayToBytes(imageArray);
		
		//System.out.println("total time of inverse DCT: " + totalTime);
		return output;
	}//end decoder420z method
	
	//this method convert RGB to YCbCr
	private static double[][][] rgb_ycbcr(int[][][] inputImageArray){
		int width = inputImageArray[0].length;
		int height = inputImageArray[0][0].length;
		double[][][] outputImageArray = new double[3][width][height];
		for(int i = 0; i <= width - 1; i++){
			for(int j = 0; j <= height - 1; j++){
				double r = inputImageArray[0][i][j];
				double g = inputImageArray[1][i][j];
				double b = inputImageArray[2][i][j];
				outputImageArray[0][i][j] = Math.round(r*77/256 + g*150/256 + b*29/256); //Y channel
				outputImageArray[1][i][j] = Math.round(r*(-44)/256 + g*(-87)/256 + b*131/256 + 128); //Cb channel
				outputImageArray[2][i][j] = Math.round(r*131/256 + g*(-110)/256 + b*(-21)/256 + 128); //Cr channel
			}
		}
		return outputImageArray;
	}//end rgb_ycbcr
	
	//this method convert YCbCr to RGB
	private static int[][][] ycbcr_rgb(double[][][] inputImageArray){
		int width = inputImageArray[0].length;
		int height = inputImageArray[0][0].length;
		int[][][] outputImageArray = new int[3][width][height];
		for(int i = 0; i <= width - 1; i++){
			for(int j = 0; j <= height - 1; j++){
				double y = inputImageArray[0][i][j];
				double cb = inputImageArray[1][i][j];
				double cr = inputImageArray[2][i][j];
				outputImageArray[0][i][j] = (int)Math.round(y + 1.371*(cr-128)); //R channel
				outputImageArray[1][i][j] = (int)Math.round(y - 0.698*(cr-128) - 0.336*(cb-128)); //G channel
				outputImageArray[2][i][j] = (int)Math.round(y + 1.732*(cb-128)); //B channel
				for (int k = 0; k <=2; k++){
					if(outputImageArray[k][i][j] > 255){
						outputImageArray[k][i][j] = 255;
					}
					else if(outputImageArray[k][i][j] < 0){
						outputImageArray[k][i][j] = 0;
					}
				}				
			}
		}
		return outputImageArray;
	}//end ycbcr_rgb
		
	//this method quantize a 8x8 block by quantizer
	//(divide all the values by quantizer)
	private static int[][] quantize(double[][] block, int quantizer){
		int[][] output = new int[block.length][block[0].length];
		for(int i = 0; i <= block.length - 1; i++){
			for(int j = 0; j <= block[0].length - 1; j++){
				output[i][j] = (int)Math.round(block[i][j]/quantizer);
			}
		}
		return output;
	}
	
	//this method dequantize a 8x8 block by quantizer
	//(multiply all the values by quantizer)
	private static double[][] dequantize(int[][] block, int quantizer){
		double[][] output = new double[block.length][block[0].length];
		for(int i = 0; i <= block.length - 1; i++){
			for(int j = 0; j <= block[0].length - 1; j++){
				output[i][j] = block[i][j] * quantizer;
			}
		}
		return output;
	}
	
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

	//this method convert a 3-dimensional image array into a byte array
	private static byte[] arrayToBytes(int[][][] imageArray){
		int width = imageArray[0].length;
		int height = imageArray[0][0].length;
		byte[] bytes = new byte[3*height*width];
		int index = 0;
	    for (int color = 0; color <= 2; color++){
	    	for (int y = 0; y <= height - 1; y++){
	    		for (int x = 0; x <= width - 1; x++){
	    			bytes[index] = (byte)imageArray[color][x][y];
	    			index++;
	    		}
	    	}
	    }
	    return bytes;
	}//end arrayToBytes
}//end LayeredImageCodec
