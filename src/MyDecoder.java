import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

//this is video encoder as mentioned in the project paper
//usage: java MyDecoder file_name
public class MyDecoder {

	final static int DEFAULT = 0;
	final static int S420 = 1; //4:2:0 subsampling
	final static int S420Z = 2; //4:2:0 with zigzag
	final static int MC = 3; //motion compensation
	
	public static void main(String[] args) {
		double frameRate = 12;
		long time = 0;
		
		//if the number of parameter is incorrect, print the usage and terminate
		if (args.length != 1 && args.length != 2){
			System.out.println("Usage: java MyDecoder cmp_file");
			System.out.println("Usage: java MyDecoder video_raw_file cmp_file");
			System.out.println();
			System.exit(0);
		}
		
		//get parameters from command line
		String inputQuantizedFileName = (args.length==1)?args[0]:args[1];
		File inputQuantizedFile = new File(inputQuantizedFileName);
		
		byte[] bytesRaw = new byte[1];
		if(args.length==2){
			String inputRawFileName = args[0];
			File inputRawFile = new File(inputRawFileName);
			//copy the data in file into byte arrays
			try {
				bytesRaw = fileToBytes(inputRawFile);
			} catch (IOException e) {
				System.out.println("Unable to load data from the raw file");
				System.out.println("Please doublecheck the file name");
				System.exit(0);
			}
		}
		
		ObjectInputStream input;
		int width = 0;
		int height = 0;
		int n1 = 0;
		int n2 = 0;
		int[][][][] vectorsOfVideo = new int[1][][][];
		boolean[][][] layerOfVideo16 = new boolean[1][][];
		int[][] quantizedData = new int[1][];
		int option = 0;
		try {
			input = new ObjectInputStream(new FileInputStream(inputQuantizedFile));
			width = input.readInt();
			height = input.readInt();
			n1 = input.readInt();
			n2 = input.readInt();
			vectorsOfVideo = (int[][][][])input.readObject();
			layerOfVideo16 = (boolean[][][])input.readObject();
			quantizedData = (int[][])input.readObject();
			option = input.readInt();
		} catch (Exception e) {
			System.out.println("Unable to load data from the quantized file");
			System.out.println("Please doublecheck the file name");
			System.exit(0);
		}
		
		int frameSize = width * height;
		int frameDataSize = frameSize * 3; //number of bytes of a frame
		int numberOfFrames = vectorsOfVideo.length;
		
		byte[] bytesCmp = new byte[frameDataSize*numberOfFrames]; //store the video to play on the display window
		
		if(option==DEFAULT || option==S420 || option==S420Z){
			//do inverse-DCT and dequantization
			System.out.println("inverse-DCT and dequantization...");
			time = System.currentTimeMillis();			
			for(int index = 0; index <= numberOfFrames - 1; index++){
				boolean[][] layerOfImg8 = convert16To8(layerOfVideo16[index]);
				byte[]img = new byte[frameDataSize];
				if(option==DEFAULT){
					img = LayeredImageCodec.decoder(quantizedData[index], width, height, n1, n2, layerOfImg8);
				}
				else if(option==S420){
					img = LayeredImageCodec.decoder420(quantizedData[index], width, height, n1, n2, layerOfImg8);
				}
				else if(option==S420Z){
					img = LayeredImageCodec.decoder420z(quantizedData[index], width, height, n1, n2, layerOfImg8);
				}
				System.arraycopy(img, 0, bytesCmp, index*frameDataSize, frameDataSize);
			}
			time = System.currentTimeMillis() - time;
			System.out.println("Total time of inverse-DCT and dequantization: " + (time/1000) + " seconds");
		}
		else if(option==MC){
			System.out.println("Decoding...");
			time = System.currentTimeMillis();
			
			int numberOfIFrames = quantizedData.length;
			int gopSize = numberOfFrames/numberOfIFrames;
						
			byte[] decodedPreviousImg = new byte[frameDataSize];
			for(int index = 0; index <= numberOfFrames - 1; index++){
				if(index%gopSize==0){//I-Frame
					boolean[][] layerOfImg8 = convert16To8(layerOfVideo16[index]);
					decodedPreviousImg = LayeredImageCodec.decoder420z(quantizedData[index/gopSize], width, height, n1, n2, layerOfImg8);
					System.arraycopy(decodedPreviousImg, 0, bytesCmp, index*frameDataSize, frameDataSize);
				}
				else{//P-Frame
					decodedPreviousImg = Reconstruct.reconstruct(decodedPreviousImg, width, height, vectorsOfVideo[index]);
					System.arraycopy(decodedPreviousImg, 0, bytesCmp, index*frameDataSize, frameDataSize);
				}
			}		
			time = System.currentTimeMillis() - time;
			System.out.println("Total time of decoding: " + (time/1000) + " seconds");
		}
		
		if(args.length==1){
			bytesRaw = bytesCmp;
		}
		
		DisplayWindow window = new DisplayWindow(bytesRaw, bytesCmp, width, height, frameRate, vectorsOfVideo, layerOfVideo16);
		window.setTitle("Display Window: MyDecoder");
		window.setVisible(true);
		System.out.println("Done!");
		
	}//end main
	
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
	}//end fileToBytes
}//end MyDecoder
