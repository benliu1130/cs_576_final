//Nov. 6, 2009
//this class has two method: DCT and inverse DCT
//it uses two 1-dimensional DCT, rather than a 2-dimensional DCT
//DCT takes a 8x8 block as input and do DCT to the block, then output a 8x8 block 
public class DCT {
	
	public static double[][] dct(double[][] inputBlock){
		double piOver16 = Math.PI/16; 
		double[][] outputBlock = new double[8][8]; //output 8x8 block
		double[][] tempBlock = new double[8][8]; //F(u,y)
		for(int u = 0; u <= 7; u++){
			double cOfU = (u == 0) ? 1/Math.sqrt(2) : 1; //C(U)
			for(int y = 0; y <= 7; y++){
				double sum = 0;
				for(int x = 0; x <= 7; x++){
					sum += inputBlock[x][y] * Math.cos(piOver16 * u * (2*x+1));
				} 
				tempBlock[u][y] = 0.5 * cOfU * sum;
			}
		}
		for(int v = 0; v <= 7; v++){
			double cOfV = (v == 0) ? 1/Math.sqrt(2) : 1; //C(V)
			for(int u = 0; u <= 7; u++){
				double sum = 0;
				for(int y = 0; y <=7; y++){
					sum += tempBlock[u][y] * Math.cos(piOver16 * v * (2*y+1));
				}
				outputBlock[u][v] = (int)Math.round(0.5 * cOfV * sum);
			}
		} 
		return outputBlock;
	}//end dct method
	
	public static double[][] inverseDct(double[][] inputBlock){
		double piOver16 = Math.PI/16; 
		double[][] outputBlock = new double[8][8]; //output 8x8 block
		double[][] tempBlock = new double[8][8]; //F(u,y)
		for(int y = 0; y <= 7; y++){
			for(int u = 0; u <= 7; u++){
				double sum = 0;
				for(int v = 0; v <= 7; v++){
					double cOfV = (v == 0) ? 1/Math.sqrt(2) : 1; //C(V)
					sum += cOfV * inputBlock[u][v] * Math.cos(piOver16 * v * (2*y+1));
				} 
				tempBlock[u][y] = 0.5 * sum;
			}
		}
		for(int x = 0; x <= 7; x++){
			for(int y = 0; y <= 7; y++){
				double sum = 0;
				for(int u = 0; u <=7; u++){
					double cOfU = (u == 0) ? 1/Math.sqrt(2) : 1; //C(U)
					sum += cOfU * tempBlock[u][y] * Math.cos(piOver16 * u * (2*x+1));
				}
				outputBlock[x][y] = (int)Math.round(0.5 * sum);
			}
		} 
		return outputBlock;
	}//end inverseDct method
	
}// end DCT