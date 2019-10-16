import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

//this is video encoder as mentioned in the project paper
//usage: java MyEncoder file_name n1 n2
public class MyEncoder420z {
	
	final static int DEFAULT = 0;
	final static int S420 = 1; //4:2:0 subsampling
	final static int S420Z = 2; //4:2:0 with zigzag
	final static int MC = 3; //motion compensation
	
	public static void main(String[] args) {
		
		//assuming the size of all sample video is 480 x 368
		int width = 480;
		int height = 368;
		int kHStatic = 48; //horizontal searching range k for static video
		int kVStatic = 12; //vertical searching range k for static video
		int kHMoving = 48; //horizontal searching range k for moving video
		int kVMoving = 12; //vertical searching range k for moving video
		int fastSearchLevel = 1; //how many levels when use hierarchical fast searching
		long time = 0; //used to calculate time
		
		//if the number of parameter is incorrect, print the usage and terminate
		if (args.length != 3){
			System.out.println("Usage: java MyEncoder video_raw_file n1 n2");
			System.exit(0);
		}
		
		//get parameters from command line
		String inputFileName = args[0];
		String outputFileName = inputFileName + ".cmp";
		int n1 = Integer.parseInt(args[1]);
		int n2 = Integer.parseInt(args[2]);
		File inputFile = new File(inputFileName);
		File outputFile = new File(outputFileName);
		
		byte[] bytes = new byte[(int)inputFile.length()];
		int frameSize = width * height;
		int frameDataSize = frameSize * 3; //number of bytes of a frame
		int numberOfFrames = bytes.length/frameDataSize;
		
		//copy the data in file into a byte array
		try {
			bytes = fileToBytes(inputFile);
		} catch (IOException e) {
			System.out.println("Unable to load data from the file");
			System.out.println("Please doublecheck the file name");
			System.exit(0);
		}
		
		int[][][][] vectorsOfVideo = new int[numberOfFrames][2][width/16][height/16]; //an array to store vectors for the whole video
		boolean[][][] layerOfVideo16 = new boolean[numberOfFrames][width/16][height/16];//layer information for the whole video(16x16 blocks)
		
		//check if the video is static or not
		if(CheckStatic.check(bytes, width, height)){
			System.out.println("The background is static");
			
			//searching motion vectors
			System.out.println("Searching motion vectors...");
			time = System.currentTimeMillis();
			byte[] imgTarget = new byte[frameDataSize]; //the target image(usually frame n)
			byte[] imgCurrent = new byte[frameDataSize]; //the current image(usually frame n+1)
			//the vectors of frame0 (I-frame) is different
			for (int index = 0; index <= 0; index++){
				System.arraycopy(bytes, (index+1)*frameDataSize, imgTarget, 0, frameDataSize);
				System.arraycopy(bytes, index*frameDataSize, imgCurrent, 0, frameDataSize);
				vectorsOfVideo[index] = VectorSearch.fastSearch(imgTarget, imgCurrent, width, height, kHStatic, kVStatic, fastSearchLevel);
			}			
			//the vectors of frame 1 to last frame (P-frame)
			for (int index = 1; index <= numberOfFrames - 1; index++){
				System.arraycopy(bytes, (index-1)*frameDataSize, imgTarget, 0, frameDataSize);
				System.arraycopy(bytes, index*frameDataSize, imgCurrent, 0, frameDataSize);
				vectorsOfVideo[index] = VectorSearch.fastSearch(imgTarget, imgCurrent, width, height, kHStatic, kVStatic, fastSearchLevel);
			}
			time = System.currentTimeMillis()-time;
			System.out.println("Total time of vectors searching: " + (time/1000) + " seconds");
			
			
			//separating layers
			System.out.println("Separating layers...");
			boolean[][][] backgroundOfVideo = new boolean[numberOfFrames][width/16][height/16];//background information for the whole video
			for (int index = 0; index <= numberOfFrames - 1; index++){
				backgroundOfVideo[index] = LayerSeparation.findBackground(vectorsOfVideo[index]);
			}
			
			//make sure there is no temporally discontinuous foreground
			boolean[][][] foregroundOfVideo = invert(backgroundOfVideo);
			foregroundOfVideo = LayerSeparation.eliminateDiscontinuousForeground(foregroundOfVideo);
			layerOfVideo16 = foregroundOfVideo;
			
		}//end static video
		else{//else if the background is moving
			System.out.println("The background is moving");
			
			//searching motion vectors
			System.out.println("Searching motion vectors...");
			time = System.currentTimeMillis();
			byte[] imgTarget = new byte[frameDataSize]; //the target image(usually frame n)
			byte[] imgCurrent = new byte[frameDataSize]; //the current image(usually frame n+1)
			//the vectors of frame0 (I-frame) is different
			for (int index = 0; index <= 0; index++){
				System.arraycopy(bytes, (index+1)*frameDataSize, imgTarget, 0, frameDataSize);
				System.arraycopy(bytes, index*frameDataSize, imgCurrent, 0, frameDataSize);
				vectorsOfVideo[index] = VectorSearch.fastSearch(imgTarget, imgCurrent, width, height, kHMoving, kVMoving, 1);
			}			
			//the vectors of frame 1 to last frame (P-frame)
			for (int index = 1; index <= numberOfFrames - 1; index++){
				System.arraycopy(bytes, (index-1)*frameDataSize, imgTarget, 0, frameDataSize);
				System.arraycopy(bytes, index*frameDataSize, imgCurrent, 0, frameDataSize);
				vectorsOfVideo[index] = VectorSearch.fastSearch(imgTarget, imgCurrent, width, height, kHMoving, kVMoving, 1);
			}
			time = System.currentTimeMillis()-time;
			System.out.println("Total time of vectors searching: " + (time/1000) + " seconds");
			
			//separating layers
			System.out.println("Separating layers...");
			boolean[][] border = CheckBorder.check(bytes, width, height);
			
			//find background first
			boolean[][][] backgroundOfVideo = new boolean[numberOfFrames][width/16][height/16];//background information for the whole video
			for (int index = 0; index <= numberOfFrames - 1; index++){
				backgroundOfVideo[index] = LayerSeparation.findBackground(vectorsOfVideo[index], border);
			}
			
			//make sure there is no temporally discontinuous foreground
			boolean[][][] foregroundOfVideo = invert(backgroundOfVideo);
			foregroundOfVideo = LayerSeparation.eliminateDiscontinuousForeground(foregroundOfVideo);
			backgroundOfVideo = invert(foregroundOfVideo);
			
			//find foreground
			for (int index = 0; index <= numberOfFrames - 1; index++){
				foregroundOfVideo[index] = LayerSeparation.findForeground(vectorsOfVideo[index], backgroundOfVideo[index]);
			}
			
			//make sure there is no temporally discontinuous foreground
			foregroundOfVideo = LayerSeparation.eliminateDiscontinuousForeground(foregroundOfVideo);
			
			layerOfVideo16 = foregroundOfVideo;
			
		}//end moving video
		
		//do DCT and quantization using 4:2:0 and Zigzag
		System.out.println("DCT and quantization...");
		time = System.currentTimeMillis();
		int[][] quantizedData = new int[numberOfFrames][];
		for(int index = 0; index <= numberOfFrames - 1; index++){
			byte[] img = new byte[frameDataSize];
			System.arraycopy(bytes, index*frameDataSize, img, 0, frameDataSize); //image frame# = index
			boolean[][] layerOfImg8 = convert16To8(layerOfVideo16[index]); //convert layer info to 8x8
			quantizedData[index] = LayeredImageCodec.encoder420z(img, width, height, n1, n2, layerOfImg8);
		}
		time = System.currentTimeMillis() - time;
		System.out.println("Total time of DCT and quantization: " + (time/1000) + " seconds");
		
		//write data to output file
		try {
			writeToFile(outputFile, width, height, n1, n2, vectorsOfVideo, layerOfVideo16, quantizedData, S420Z);
		} catch (IOException e) {
			System.out.println("Cannot write to an output file");
			System.exit(0);
		}
		
		DisplayWindow window = new DisplayWindow(bytes, bytes, width, height, 12, vectorsOfVideo, layerOfVideo16);
		window.setTitle("Display Window: MyEncoder420z");
		window.setVisible(true);
		
		System.out.println("Done!");
	}//end main

	//this method convert true to false and false to true
	private static boolean[][][] invert(boolean[][][] input){
		boolean[][][] output = new boolean[input.length][input[0].length][input[0][0].length];
		for(int x = 0; x <= input.length-1; x++){
			for(int y = 0; y <= input[0].length-1; y++){
				for(int z = 0; z <= input[0][0].length-1; z++){
					output[x][y][z] = !input[x][y][z];
				}
			}
		}
		return output;
	}//end invert
	
	//Since DCT use 8x8 blocks but motion vectors use 16x16 blocks
	//the layer information needs to be converted
	private static boolean[][] convert16To8(boolean[][] input16){
		boolean[][] output8 = new boolean[input16.length*2][input16[0].length*2];
		for(int i = 0; i <= output8.length-1; i++){
			for(int j = 0; j <= output8[0].length-1; j++){
				output8[i][j] = input16[i/2][j/2];
			}
		}
		return output8;
	}//end convert16To8
	
	//this method write n1, n2, layer information, and 
	private static void writeToFile(File outputFile, int width, int height, int n1, int n2, int[][][][] vectorsOfVideo, boolean[][][] layerOfVideo16, int[][] quantizedData, int option)throws IOException{
		ObjectOutputStream output;
		output = new ObjectOutputStream(new FileOutputStream(outputFile));
		output.writeInt(width);
		output.writeInt(height);
		output.writeInt(n1);
		output.writeInt(n2);
		output.writeObject(vectorsOfVideo);
		output.writeObject(layerOfVideo16);
		output.writeObject(quantizedData);
		output.writeInt(option);
		output.close();
	}

	//this method take a file as an input 
	//and output the data of the file into a byte array
	private static byte[] fileToBytes(File file) throws IOException{
		InputStream is = new FileInputStream(file);
		byte[] bytes = new byte[(int)file.length()];
		int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length && 
	    		(numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }
	    return bytes;
	}//end fileToBytes//end main
}//end MyEncoder420z
