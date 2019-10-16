//Nov. 6, 2009
//a point which goes zig-zag in a 8x8 block
//starting at (0,0)
//method go() can make the point move one step forward
public class Zigzag {
	private int x;
	private int y;
	private boolean upRight; //determine which direction to go, up-right or down-left
	
	public Zigzag(){
		x = 0;
		y = 0;
		upRight = true;
	}
	
	public void go(){
		if (x == 7 && y == 7){ //stop going
		}
		else if(upRight && y == 0){ //top boundary
			x++;
			upRight = false;
		}
		else if(upRight && x == 7){ //right boundary
			y++;
			upRight = false;
		}
		else if(!upRight && y == 7){ //bottom boundary
			x++;
			upRight = true;
		}
		else if(!upRight && x == 0){ //left boundary
			y++;
			upRight = true;
		}
		else if(upRight){
			x++;
			y--;
		}
		else if(!upRight){
			x--;
			y++;
		}
	}//end go
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
}
