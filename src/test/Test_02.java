import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


public abstract class Test_02 {

	/**
	 * test DCT
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		File file = new File("C:\\temp\\test.txt");
		Scanner input = new Scanner(file);
		ArrayList<int[]> arrayList_x = new ArrayList<int[]>(); //an array for whole table
		while (input.hasNext()){
			String line = input.nextLine();
			ArrayList<Integer> arrayList_y = new ArrayList<Integer>(); //an array for only one line
			int index = 0;
			while(index <= line.length()-1){
				if (Character.isDigit(line.charAt(index))){
					int value = Integer.parseInt(line.substring(index, index+1));
					index++;
					while (index <= line.length()-1 && Character.isDigit(line.charAt(index))){
						value = 10 * value + Integer.parseInt(line.substring(index, index+1));
						index++;
					}
					arrayList_y.add(value);
				}
				else {
					index++;
				}					
			}
			//copy the values in arrayList_y into array_y
			int[] array_y = new int[arrayList_y.size()];
			for (int i = 0; i <= arrayList_y.size()-1; i++){
				array_y[i] = arrayList_y.get(i);
			}
			arrayList_x.add(array_y);
		}
		double[][] array = new double[8][8];
		for (int x = 0; x <= 7; x++){
			for (int y = 0; y <= 7 ; y++){
				array[x][y] = arrayList_x.get(y)[x];
			}
		}
		array = DCT.dct(array);
		array = DCT.inverseDct(array);
		System.out.println("done");
	}
}
