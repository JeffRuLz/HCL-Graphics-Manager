import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Preview extends Canvas {

	public void paint (Graphics g) {
		BufferedImage bi;
		try {
			File preview = new File("preview.bmp");
			if (preview.exists()) {
				preview.deleteOnExit();
				bi = ImageIO.read(preview);
				Graphics2D g2;
		        g2 = (Graphics2D) g;
		        g2.drawImage(bi, 0, 0, null);
		        g2.dispose();
			}
		} catch (IOException e) {}
		
     }	
}
