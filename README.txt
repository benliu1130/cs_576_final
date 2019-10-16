To Compile:
   1. Use command javac *.java to compile all .java files.

To Run:
   1. Use command "java -Xmx512m MyEncoder input_video n1 n2" to encode video.
      n1: quantization step for foreground
      n2: quantization step for background
      p.s. quantization step 0 has best visual quality, when it goes higher, the visual quality becomes worse
           but use less bytes to store the pixels
      e.x. java -Xmx512m MyEncoder moving_camera3.576v 0 8
      When encoding is done, you will see a pop-up window, and the encoded video in the same folder as input_video with .cmp extension.
      Close the pop-up window to stop the program if you don't want to watch it now.
   2. Use command "java -Xmx256m MyDecoder input_video encoded_video" to play both.
      e.x. java -Xmx512m MyDecoder moving_camera3.576v moving_camera3.576v.cmp
      You will see a pop-up window, which shows you original video and encoded video side by side.

How to play the video on the display window:
   Use "Play/Pause" to play and pause the video.
   Use "Stop" to stop the video and go back to very beginning.
   Use ">>" to step forward when video is paused. (You can use "right" key to do this)
   Use "<<" to step backward when video is paused. (You can use "left" key to do this)

How to see motion vector:
   Use "Lock" to lock the original video (left) so it won't change. Then use ">>" to step forward the compressed video.
   By doing this, you can see n and n+1 frame at the same time.
   Move mouse to the right window, then you can see coordinates and motion vector of the block pointed by mouse.
   You will also see a white square in left window. This shows you the matching block.

How to see layers:
   Use "Layer View" to show the foreground blocks in white squares.
   Use "Print Vectors" to print all vectors of current frame into a csv file.

Extra Credit:
   There are several different encoder, using different encoding schemes: (Execute them the same way as MyEncoder)
   MyEncoder: The very basic one. Using DCT (discrete cosine transform) and quantization only.
   MyEncoder420: Using 4:2:0 subsampling.
   MyEncoder420z: Using 4:2:0 subsampling, then ordering coefficients in zigzag.
   MyEncoderMC: Using motion compensation whitout error image. There is one I-frame and two P-frames every three frames.
