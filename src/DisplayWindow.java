import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.JFrame;

//Nov. 6, 2009
//this class will take two binary streams as input
//then it will display these two videos in two screens side by side
//the window has some buttons to control the videos, like play/pause, stop, etc...
//
//it has a button "Lock" to lock the left video
//once it is locked, press step forward " >> ", you can see i and i+1 frame at the same time
//move mouse on the screen of video 2,(i+1 frame) it will show coordinate of the block which is pointed by mouse
//
//it will also show the matching block in left video (i frame), once motion vector information is provided
//
//if layer information is provided, there will be a "Layer View" button, which can show foreground

public class DisplayWindow extends JFrame {
	private int width;
	private int height;
	private int frameSize; //how many pixels each frame
	private int frameDataSize; //how many bytes each frame
	private double frameRate;
	private int interval; //time interval between frames in milliseconds
	private byte[] videoBytes1; //this byte array store data of video 1
	private byte[] videoBytes2; //this byte array store data of video 2
	private int numberOfFrames1; //number of frames in video 1
	private int numberOfFrames2; //number of frames in video 2
	private byte[] imgBytes1; //this byte array store data of image being displayed at screen of video 1
	private byte[] imgBytes2; //this byte array store data of image being displayed at screen of video 2
	private int index = 0; //the index of the frame being displayed
	private JPanel videoPanel1 = new JPanel(); //a panel for video 1
	private JPanel videoPanel2 = new JPanel(); //a panel for video 2
	private JPanel videoPanelAll = new JPanel(); //a panel for both
	private JPanel textPanelLayerVector = new JPanel(); //a panel to show foreground/background vectors
	private JPanel textPanelBlock = new JPanel(); //a panel to show coordinate of the block pointed by mouse
	private JPanel textPanelBlockVector =  new JPanel(); //a panel to show motion vector of the block pointed by mouse
	private JPanel textPanelWhatGround = new JPanel(); //a panel to show the block pointed by mouse is foreground or background
	private JPanel textPanelFrameNumber = new JPanel(); //a panel to show frame number
	private JPanel textPanelMiddle = new JPanel(); //a panel to contain Block coordinate and motion vector
	private JPanel textPanelAll = new JPanel(); //a panel for all text
	private JPanel buttonPanel1 = new JPanel(); //a panel for buttons on left
	private JPanel buttonPanel2 = new JPanel(); //a panel for buttons on center
	private JPanel buttonPanel3 = new JPanel(); //a panel for buttons on right
	private JPanel buttonPanelAll = new JPanel(); //a panel for all buttons
	private JLabel textLayerVector = new JLabel(); //a text to show foreground/background vectors
	private JLabel textBlock = new JLabel(); //a text to show coordinate of the block pointed by mouse
	private JLabel textBlockVector = new JLabel(); //a text to show motion vector of the block pointed by mouse
	private JLabel textWhatGround = new JLabel(); //a text to show motion vector of the block pointed by mouse
	private JLabel textFrameNumber = new JLabel(); //a text to show frame number
	private JButton playButton = new JButton(); // a button to play/pause
	private JButton restartButton = new JButton(); // a button to restart
	private JButton forwardButton = new JButton(); // a button to step forward
	private JButton backwardButton = new JButton(); // a button to step backward
	private JButton lockButton = new JButton(); // a button to lock video 1
	private JButton layerButton = new JButton(); // a button to show blocks on foreground
	private JButton csvButton = new JButton(); // a button to output vectors to a csv file
	private JButton groupButton = new JButton(); // a button to show whole group in blocks
	private boolean lock; //determine if video 1 is locked
	private boolean fix; //determine if the displayed block is fixed
	private boolean layerView; //determine if in layer view mode{foreground & background}
	private boolean showGroup; //determine if show grouping information(show the whole group when a block pointed by mouse)
	private int mouseX; //the mouse location in video 2 screen
	private int mouseY; //the mouse location in video 2 screen
	private int[][][][] vectors; //the vectors of each block
	private boolean[][][] layer; //an array to show layer information
	private BlockSet[][][] groupInfo; //an array to show grouping
	
	private Timer timer = new Timer(0, new ActionListener(){
		public void actionPerformed(ActionEvent e){
			if(index < Math.min(numberOfFrames1, numberOfFrames2) - 1){
				index++;
				updateFrameNumber();
				updateVideo2();
				if(!lock){
					updateVideo1();
				}
			}
			else{
				timer.stop();
			}
		}
	});//end timer
	
	//DisplayWindow constructor
	public DisplayWindow(byte[] bytes1, byte[] bytes2, int videoWidth, int videoHeight, double fRate){
		width = videoWidth;
		height = videoHeight;
		frameSize = width * height;
		frameDataSize = frameSize * 3; //number of bytes of a frame
		frameRate = fRate;
		interval = (int)(1/frameRate * 1000);
		timer.setDelay(interval);
		videoBytes1 = bytes1;
		videoBytes2 = bytes2;
		numberOfFrames1 = videoBytes1.length/frameDataSize;
		numberOfFrames2 = videoBytes2.length/frameDataSize;
		imgBytes1 = new byte[frameDataSize];
		imgBytes2 = new byte[frameDataSize];
		
		//arrange the location of two screens
	    videoPanelAll.setLayout(new GridLayout(1,2,5,0));
	    videoPanelAll.add(videoPanel1);
	    videoPanelAll.add(videoPanel2);
	    updateVideo1();
	    updateVideo2();
	    setLayout(new BorderLayout());
	    add(videoPanelAll, BorderLayout.NORTH); //put two video on north
	    
	    //arrange text panel
	    updateFrameNumber();
	    textPanelLayerVector.add(textLayerVector);
	    textPanelBlock.add(textBlock);
	    textPanelBlockVector.add(textBlockVector);
	    textPanelWhatGround.add(textWhatGround);
	    textPanelFrameNumber.add(textFrameNumber);	    
	    textPanelMiddle.add(textPanelBlock);
	    textPanelMiddle.add(textPanelBlockVector);
	    textPanelMiddle.add(textPanelWhatGround);
	    textPanelAll.setLayout(new BorderLayout());
	    textPanelAll.add(textPanelLayerVector, BorderLayout.WEST);
	    textPanelAll.add(textPanelMiddle, BorderLayout.CENTER);
	    textPanelAll.add(textPanelFrameNumber, BorderLayout.EAST);
	    add(textPanelAll, BorderLayout.CENTER); //put text on center

	    //detect mouse location on video 2 screen
	    videoPanel2.addMouseMotionListener(new MouseMotionAdapter(){
	    	public void mouseMoved(MouseEvent e){
	    		if(!fix && !timer.isRunning()){ //if video stop and the block is not fixed
	    			mouseX = e.getX();
		    		mouseY = e.getY();
		    		updateVideo2Block();	    		
		    		updateBlockCoordinate();
	    		}	    		
	    	}
	    });	    
	    
	    //fix (or release) the block when mouse is clicked
	    videoPanel2.addMouseListener(new MouseAdapter(){
	    	public void mouseClicked(MouseEvent e){
	    		if(!timer.isRunning()){
	    			mouseX = e.getX();
		    		mouseY = e.getY();
		    		updateVideo2Block();	    		
		    		updateBlockCoordinate();
		    		if(fix){
		    			fix = false;	
		    		}
		    		else if(!fix){
		    			fix = true;
		    		}
	    		}
	    	}
	    });
	    
	    //a button to play/pause
	    playButton.setText("Play/Pause");
	    playButton.setFont(new Font("Arial", Font.PLAIN, 20));
	    playButton.setFocusable(false);
	    playButton.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
	    		//if timer is not running
	    		if(!timer.isRunning()){
	    			//if not at the end of the video, play video
	    			if (index < Math.min(numberOfFrames1, numberOfFrames2) - 1){
	    				timer.start();
	    			}
	    		}
	    		//if timer is running, stop it
	    		else{
	    			timer.stop();
	    		}
	    	}
	    });//end playButton.addActionListener
	    
	    //a button to restart the video
	    restartButton.setText("Stop");
	    restartButton.setFont(new Font("Arial", Font.PLAIN, 20));
	    restartButton.setFocusable(false);
	    restartButton.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
	    		timer.stop();
	    		index = 0; //back to first frame
	    		lock = false; //unlock
    			lockButton.setText(" Lock ");
	    		updateFrameNumber(); //update frame#
	    		updateVideo1();
	    		updateVideo2();
	    	}
	    });//end restartButton.addActionListener
	    
	    //a button to step forward
	    forwardButton.setText(">>");
	    forwardButton.setFont(new Font("Arial", Font.PLAIN, 20));
	    forwardButton.setFocusable(false);
	    forwardButton.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
	    		if(index < Math.min(numberOfFrames1, numberOfFrames2) - 1){
	    			index++;
	    			updateFrameNumber(); //update frame#
	    			updateVideo2();
	    			if(!lock){//if video 1 is not locked
						updateVideo1();
					}
	    		}
	    	}
	    });//end forwardButton.addActionListener
	    
	    //a button to step backward
	    backwardButton.setText("<<");
	    backwardButton.setFont(new Font("Arial", Font.PLAIN, 20));
	    backwardButton.setFocusable(false);
	    backwardButton.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
	    		if(index > 0){
	    			index--;
	    			updateFrameNumber(); //update frame#
		    		updateVideo2();
		    		if(!lock){//if video 1 is not locked
						updateVideo1();
					}
	    		}
	    	}
	    });//end backwardButton.addActionListener
	    
	    //a button to lock video 1
	    lockButton.setText(" Lock ");
	    lockButton.setFont(new Font("Arial", Font.PLAIN, 20));
	    lockButton.setFocusable(false);
	    lockButton.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
	    		if (!lock){ //if not locked yet, change to lock
	    			lock = true;
	    			lockButton.setText("Unlock");
	    		}
	    		else if (lock){
	    			lock = false;
	    			lockButton.setText(" Lock ");
	    			updateVideo1(); //update video 1 when unlocked
	    		}
	    	}
	    });//end lockButton.addActionListener

	    //a button to show blocks on foreground of video 2
	    layerButton.setText("Layer View");
	    layerButton.setFont(new Font("Arial", Font.PLAIN, 20));
	    layerButton.setFocusable(false);
	    layerButton.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
	    		if (!layerView){ //if not layer view yet, change to layer view mode
	    			layerView = true;
	    			updateVideo2();
	    		}
	    		else if (layerView){
	    			layerView = false;
	    			updateVideo2();
	    		}
	    	}
	    });//end lockButton.addActionListener
	    
	    //a button to output vectors of a certain frame to a csv file
	    csvButton.setText("Print Vectors");
	    csvButton.setFont(new Font("Arial", Font.PLAIN, 20));
	    csvButton.setFocusable(false);
	    csvButton.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
	    		JFileChooser fileChooser = new JFileChooser();
	    		File file = new File("vectors.csv");
	    		fileChooser.setSelectedFile(file);
	    		if(fileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
	    			File outputFile = fileChooser.getSelectedFile();
	    			try {
						PrintWriter output = new PrintWriter(outputFile);
						for(int y = 0; y <= vectors[0][0][0].length-1; y++){
							for(int x = 0; x <= vectors[0][0].length-1; x++){
								output.print("\"(" + vectors[index][0][x][y] + "," + vectors[index][1][x][y] + ")\",");
							}
							output.println();
						}						
						output.close();
					} catch (FileNotFoundException e1) {
						System.out.println("Cannot output csv file");
					} 
	    		}
	    	}
	    });//end lockButton.addActionListener
	    
	    //a button to show group info
	    groupButton.setText("Show Group");
	    groupButton.setFont(new Font("Arial", Font.PLAIN, 20));
	    groupButton.setFocusable(false);
	    groupButton.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
	    		if (!showGroup){ //if not show group yet, show group
	    			showGroup = true;
	    			updateVideo2Block();
	    		}
	    		else if (showGroup){
	    			showGroup = false;
	    			updateVideo2Block();
	    		}
	    	}
	    });//end lockButton.addActionListener
	    
	    //use keyboard to forward
	    addKeyListener(new KeyAdapter(){
	    	public void keyPressed(KeyEvent e){
	    		if(e.getKeyCode()==KeyEvent.VK_RIGHT){
	    			if(index < Math.min(numberOfFrames1, numberOfFrames2) - 1){
		    			index++;
		    			updateFrameNumber(); //update frame#
		    			updateVideo2();
		    			if(!lock){//if video 1 is not locked
							updateVideo1();
						}
		    		}
	    		}
	    		else if(e.getKeyCode()==KeyEvent.VK_LEFT){
	    			if(index > 0){
		    			index--;
		    			updateFrameNumber(); //update frame#
			    		updateVideo2();
			    		if(!lock){//if video 1 is not locked
							updateVideo1();
						}
		    		}
	    		}
	    	}
	    });
	    
	    buttonPanelAll.setLayout(new BorderLayout());
	    buttonPanel1.add(lockButton);
	    buttonPanel2.add(backwardButton);
	    buttonPanel2.add(restartButton);
	    buttonPanel2.add(playButton);
	    buttonPanel2.add(forwardButton);
	    buttonPanelAll.add(buttonPanel1, BorderLayout.WEST);
	    buttonPanelAll.add(buttonPanel2, BorderLayout.CENTER);
	    buttonPanelAll.add(buttonPanel3, BorderLayout.EAST);
	    add(buttonPanelAll, BorderLayout.SOUTH); //put all buttons on south
	    setResizable(false);
	    pack();
	    setTitle("Display Window");
	    setLocationRelativeTo(null);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setFocusable(true);
		setVisible(true);
	    
	}//end DisplayWindow constructor
	
	//DisplayWindow constructor with motion vector	
	//the format of motion vectors array is: motionVectors[a][b][c][d]
	//a means frame number, 0 = first frame(I frame), 1 = second frame(first P frame), and so on
	//b means x or y. 0=x, 1=y
	//c means the order of block in row.
	//d means the order of block in column
	//for example, motionVectors[1][1][5][6] means the y value of the block on 6th column and 7th row of the second frame(first P-frame)
	public DisplayWindow(byte[] bytes1, byte[] bytes2, int videoWidth, int videoHeight, double fRate, int[][][][] motionVectors){
		this(bytes1, bytes2, videoWidth, videoHeight, fRate);//use the 5 arguments constructor first
		//detect mouse location on video 2 screen
	    vectors = motionVectors;
		videoPanel2.addMouseMotionListener(new MouseMotionAdapter(){
	    	public void mouseMoved(MouseEvent e){
	    		if(!fix && !timer.isRunning()){ //if video stop and the block is not fixed
	    			mouseX = e.getX();
		    		mouseY = e.getY();
		    		updateBlockVector();
		    		updateVideo1Block();
	    		}	    		
	    	}
	    });	    
	    //fix (or release) the block when mouse is clicked
	    videoPanel2.addMouseListener(new MouseAdapter(){
	    	public void mouseClicked(MouseEvent e){
	    		mouseX = e.getX();
	    		mouseY = e.getY();
	    		updateBlockVector();
	    		updateVideo1Block();
	    	}
	    });
	    buttonPanel3.add(csvButton);
	}//end DisplayWindow constructor with motion vectors

	//DisplayWindow constructor with motion vector and layer information
	//the format of layer information array is: boolean[a][b][c]
	//a means frame number, 0 = first frame(I frame), 1 = second frame(first P frame), and so on
	//b means the order of block in row.
	//c means the order of block in column
	//for example, layer[1][5][6] shows if the block on 6th column and 7th row of the second frame(first P-frame) is foreground(true) or background(false)
	public DisplayWindow(byte[] bytes1, byte[] bytes2, int videoWidth, int videoHeight, double fRate, int[][][][] motionVectors, boolean[][][] layerInfo){
		this(bytes1, bytes2, videoWidth, videoHeight, fRate, motionVectors);//use the 6 arguments constructor first
		//detect mouse location on video 2 screen
	    buttonPanel3.add(layerButton); //add layer view button
		layer = layerInfo;
		videoPanel2.addMouseMotionListener(new MouseMotionAdapter(){
	    	public void mouseMoved(MouseEvent e){
	    		if(!fix && !timer.isRunning()){ //if video stop and the block is not fixed
	    			mouseX = e.getX();
		    		mouseY = e.getY();
		    		updateLayerInfo();
	    		}	    		
	    	}
	    });	    
	    //fix (or release) the block when mouse is clicked
	    videoPanel2.addMouseListener(new MouseAdapter(){
	    	public void mouseClicked(MouseEvent e){
	    		mouseX = e.getX();
	    		mouseY = e.getY();
	    		updateLayerInfo();
	    	}
	    });
	}//end DisplayWindow constructor with motion vectors
	
	//DisplayWindow constructor with grouping information
	//the format of grouping information array is: BlockSet[a][b][c]
	//a means frame number
	//b means the order of block in row.
	//c means the order of block in column.
	//for example, blocks[3][1][5] shows group information of the block on 2th column and 6th row of 4th frame
	//for more information about class BlockSet, read the comment in BlockSet
	public DisplayWindow(byte[] bytes1, byte[] bytes2, int videoWidth, int videoHeight, double fRate, int[][][][] motionVectors, BlockSet[][][] groups){
		this(bytes1, bytes2, videoWidth, videoHeight, fRate, motionVectors);//use the 6 arguments constructor first
		//detect mouse location on video 2 screen
		buttonPanel3.add(groupButton);
		groupInfo = groups;
	}//end DisplayWindow constructor with motion vectors
	
	public DisplayWindow(byte[] bytes1, byte[] bytes2, int videoWidth, int videoHeight, double fRate, int[][][][] motionVectors, BlockSet[][][] groups, boolean[][][] layerInfo){
		this(bytes1, bytes2, videoWidth, videoHeight, fRate, motionVectors, groups);
		buttonPanel3.add(layerButton); //add layer view button
		layer = layerInfo;
		videoPanel2.addMouseMotionListener(new MouseMotionAdapter(){
	    	public void mouseMoved(MouseEvent e){
	    		if(!fix && !timer.isRunning()){ //if video stop and the block is not fixed
	    			mouseX = e.getX();
		    		mouseY = e.getY();
		    		updateLayerInfo();
	    		}	    		
	    	}
	    });	    
	    //fix (or release) the block when mouse is clicked
	    videoPanel2.addMouseListener(new MouseAdapter(){
	    	public void mouseClicked(MouseEvent e){
	    		mouseX = e.getX();
	    		mouseY = e.getY();
	    		updateLayerInfo();
	    	}
	    });
	}
	
	//update video 1
	private void updateVideo1(){
		System.arraycopy(videoBytes1, index*frameDataSize, imgBytes1, 0, frameDataSize);
		BufferedImage img1 = bytesToImg(imgBytes1, height, width);
		JLabel label = new JLabel(new ImageIcon(img1));
		videoPanel1.removeAll();
		videoPanel1.add(label);
		videoPanel1.validate();
	}
	
	//update video 2
	private void updateVideo2(){
		fix = false;
		System.arraycopy(videoBytes2, index*frameDataSize, imgBytes2, 0, frameDataSize);
		BufferedImage img2 = bytesToImg(imgBytes2, height, width);
		if(layerView){ //display blocks on foreground
			for(int x = 0; x <= width/16 - 1; x++){
				for(int y = 0; y <= height/16 - 1; y++){
					if(layer[index][x][y]){ //the block is foreground
						//draw a square
						int pix = 0xffffffff; //white color
						for (int k = x*16; k <= x*16 + 15; k++){
							img2.setRGB(k, y*16, pix);
						}
						for (int k = x*16; k <= x*16 + 15; k++){
							img2.setRGB(k, y*16+15, pix);
						}
						for (int k = y*16; k <= y*16 + 15; k++){
							img2.setRGB(x*16, k, pix);
						}
						for (int k = y*16; k <= y*16 + 15; k++){
							img2.setRGB(x*16+15, k, pix);
						}
					}
				}
			}
		}
		JLabel label = new JLabel(new ImageIcon(img2));
		videoPanel2.removeAll();
		videoPanel2.add(label);
		videoPanel2.validate();
	}//end updateVideo2
	
	//update the block shown on the video 2
	private void updateVideo2Block(){
		BufferedImage img2 = bytesToImg(imgBytes2, height, width);
		if(layerView){ //display blocks on foreground
			for(int x = 0; x <= width/16 - 1; x++){
				for(int y = 0; y <= height/16 - 1; y++){
					if(layer[index][x][y]){ //the block is foreground
						//draw a square
						int pix = 0xffffffff; //white color
						for (int k = x*16; k <= x*16 + 15; k++){
							img2.setRGB(k, y*16, pix);
						}
						for (int k = x*16; k <= x*16 + 15; k++){
							img2.setRGB(k, y*16+15, pix);
						}
						for (int k = y*16; k <= y*16 + 15; k++){
							img2.setRGB(x*16, k, pix);
						}
						for (int k = y*16; k <= y*16 + 15; k++){
							img2.setRGB(x*16+15, k, pix);
						}
					}
				}
			}
		}
		int blockX = (mouseX/16 > width/16-1)?width/16-1:mouseX/16; //x coordinate of the block pointed by mouse
		int blockY = (mouseY/16 > height/16-1)?height/16-1:mouseY/16; //y coordinate of the block pointed by mouse
		
		//draw a square around the block pointed by mouse
		int white = 0xffffffff; //white color
		for(int k = blockX*16; k <= blockX*16 + 15; k++){
			img2.setRGB(k, blockY*16, white);
		}
		for(int k = blockX*16; k <= blockX*16 + 15; k++){
			img2.setRGB(k, blockY*16 + 15, white);
		}
		for(int k = blockY*16; k <= blockY*16 + 15; k++){
			img2.setRGB(blockX*16, k, white);
		}
		for(int k = blockY*16; k <= blockY*16 + 15; k++){
			img2.setRGB(blockX*16 + 15, k, white);
		}
		
		if(showGroup){//show groups information
			Block[] blocks = groupInfo[index][blockX][blockY].getBlocksArray();
			int yellow = 0xffffff00;
			for(int m = 0; m <= blocks.length-1; m++){ //draw blocks one by one
				int x = blocks[m].getX(); //block coordinate x
				int y = blocks[m].getY(); //block coordinate y
				for(int k = x*16; k <= x*16 + 15; k++){
					img2.setRGB(k, y*16, yellow);
				}
				for(int k = x*16; k <= x*16 + 15; k++){
					img2.setRGB(k, y*16 + 15, yellow);
				}
				for(int k = y*16; k <= y*16 + 15; k++){
					img2.setRGB(x*16, k, yellow);
				}
				for(int k = y*16; k <= y*16 + 15; k++){
					img2.setRGB(x*16 + 15, k, yellow);
				}
			}
		}
		JLabel label = new JLabel(new ImageIcon(img2));
		videoPanel2.removeAll();
		videoPanel2.add(label);
		videoPanel2.validate();
	}//end updateVideo2Block
	
	//update the block shown on the video 1
	private void updateVideo1Block(){
		BufferedImage img1 = bytesToImg(imgBytes1, height, width);
		int blockX = (mouseX/16 > width/16-1)?width/16-1:mouseX/16; //x coordinate of the block pointed by mouse
		int blockY = (mouseY/16 > height/16-1)?height/16-1:mouseY/16; //y coordinate of the block pointed by mouse
		
		int x = blockX*16 + vectors[index][0][blockX][blockY];
		int y = blockY*16 + vectors[index][1][blockX][blockY];
		//draw a square around the block pointed by mouse
		int pix = 0xffffffff; //white color
		for(int k = x; k <= x + 15; k++){//top
			img1.setRGB(k, y, pix);
		}
		for(int k = x; k <= x + 15; k++){//bottom
			img1.setRGB(k, y + 15, pix);
		}
		for(int k = y; k <= y + 15; k++){//left
			img1.setRGB(x, k, pix);
		}
		for(int k = y; k <= y + 15; k++){//right
			img1.setRGB(x + 15, k, pix);
		}
		JLabel label = new JLabel(new ImageIcon(img1));
		videoPanel1.removeAll();
		videoPanel1.add(label);
		videoPanel1.validate();
	}//end updateVideo1Block
	
	//update block coordinate
	private void updateBlockCoordinate(){
		int blockX = (mouseX/16 > width/16-1)?width/16-1:mouseX/16; //x coordinate of the block pointed by mouse
		int blockY = (mouseY/16 > height/16-1)?height/16-1:mouseY/16; //y coordinate of the block pointed by mouse
		//update text of block coordinate
		textBlock.setText("Block: (" + blockX + ", " + blockY + ")");
	}
	
	//update block's motion vector
	private void updateBlockVector(){
		int blockX = (mouseX/16 > width/16-1)?width/16-1:mouseX/16; //x coordinate of the block pointed by mouse
		int blockY = (mouseY/16 > height/16-1)?height/16-1:mouseY/16; //y coordinate of the block pointed by mouse
		//update text of motion vector
		textBlockVector.setText("Vector: (" + vectors[index][0][blockX][blockY] + ", " + vectors[index][1][blockX][blockY] + ")");
	}
	
	//update layer information
	private void updateLayerInfo(){
		int blockX = (mouseX/16 > width/16-1)?width/16-1:mouseX/16; //x coordinate of the block pointed by mouse
		int blockY = (mouseY/16 > height/16-1)?height/16-1:mouseY/16; //y coordinate of the block pointed by mouse
		//update text of layer information
		if(layer[index][blockX][blockY]){
			textWhatGround.setText("Foreground");
		}
		else{
			textWhatGround.setText("Background");
		}
	}
	
	//update frame number
	private void updateFrameNumber(){
		textFrameNumber.setText("Frame#:    " + index);
	}
	
	//this method create a BufferedImage object from a byte array
	private static BufferedImage bytesToImg(byte[] bytes, int height, int width){
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	   	int offsetOfG = height * width;
	   	int offsetOfB = 2 * height * width;
		int index = 0;
		for(int y = 0; y <= height - 1; y++){
			for(int x = 0; x <= width - 1; x++){
				byte r = bytes[index];
				byte g = bytes[offsetOfG + index];
				byte b = bytes[offsetOfB + index]; 
				
				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				img.setRGB(x,y,pix);
				index++;
			}
		}
		return img;
	}// end bytesToImg
	
}//end DisplayWindow
