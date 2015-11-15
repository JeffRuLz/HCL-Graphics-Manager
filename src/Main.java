import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {

	private static Window window = new Window();
	private static String localPath = new File(".").getAbsolutePath();
	private static String gfxFileLocation = "";
	private static String lastExport = "";
	private static String lastImport = "";
	private static ArrayList<Integer> sheetIndex = new ArrayList<Integer>();
	private static ArrayList<Integer> sheetSize = new ArrayList<Integer>();
	
	public static void main(String[] args) throws IOException {
		//Load file paths
		localPath = localPath.substring(0,  localPath.length() - 1);
		loadConfig();

		showButtons(false);
		if (getFileExtension(gfxFileLocation).equals("qda")) {
			if (loadGfxFile() == true) {
				showButtons(true);
			}
		}
	}
	
	public static void showButtons(boolean enable) {
		Window.cbFileNames.setEnabled(enable);
		Window.btnExport.setEnabled(enable);
		Window.btnImport.setEnabled(enable);
	}
		
	//The method performs all tasks that occur when a .qda file is selected
	private static boolean loadGfxFile() throws IOException {
		boolean result = true;
		
		File f = new File(gfxFileLocation);
		InputStream is = new FileInputStream(f);		
		
		//Get number of sheets
		is.skip(8);
		int numOfSheets = is.read();
		
		is.skip(247);
		
		sheetIndex.clear();
		sheetSize.clear();
		
		Window.clearFileNames();
		//Get data from sheet headers
		for (int i = 0; i < numOfSheets; i++) {
			byte[] buf = new byte[268];
			is.read(buf);
			
			int bmpPos = (buf[0] & 0xff) + ((buf[1] & 0xff) * 256) + ((buf[2] & 0xff) * 65536) + ((buf[3] & 0xff) * 16777216);
			int bmpSize = (buf[4] & 0xff) + ((buf[5] & 0xff) * 256) + ((buf[6] & 0xff) * 65536) + ((buf[7] & 0xff) * 16777216);
			
			String fname = "";
			for (int a = 0; a < 256; a++) {
				fname += (char)(buf[12 + a] & 0xff);
			}
			
			//Trim filename			
			for (int a = 255; a > 0; a--) {
				if (fname.charAt(a) == 0) {
					fname = fname.substring(0, a);
				}else{
					a = 0;
				}
			}
			
			//Save data
			sheetIndex.add(bmpPos);
			sheetSize.add(bmpSize);
			Window.cbFileNames.addItem(fname);
			
			/*
			System.out.println(bmpPos);
			System.out.println(bmpSize);
			System.out.println(fname);
			*/
		}

		is.close();
		return result;
	}
		
	public static String getFileExtension(String path) {
		String result = "";
		
		for (int i = path.length() - 1; i > 0; i--) {
			if (path.charAt(i) != '.') {
				result = path.charAt(i) + result;
			}else{
				i = 0;
			}
		}
		
		return result;
	}
	
	public static File openFile(String fpath, String description, String extensions) {
		File dialogPath = new File(fpath);
		JFileChooser chooser = new JFileChooser(dialogPath);
		FileFilter filter = new FileNameExtensionFilter(description, extensions);
		chooser.setFileFilter(filter);
		
		int returnVal = chooser.showOpenDialog(chooser);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		
		return null;
	}
	
	//Quickly return a single byte in a file
	public static char checkByte(File f, int pos) throws IOException {
		char result;
		InputStream is = new FileInputStream(f);
		
		is.skip(pos);
		result = (char)is.read();
		is.close();
		
		return result;
	}
	
	public static void openQdaFile() throws IOException {
		//Start at last opened QDA file...
		String startPath = gfxFileLocation;
		//...otherwise start at local directory
		File check = new File(gfxFileLocation);
		if (!check.exists()) {
			startPath = localPath;
		}
		
		File gfxFile = openFile(startPath, "QDA file", "qda");		
		if (gfxFile != null) {
			//Check to see if it is a valid qda file
			if (checkByte(gfxFile, 4) == 'Q' && checkByte(gfxFile, 5) == 'D' &&
				checkByte(gfxFile, 6) == 'A' && checkByte(gfxFile, 7) == '0')
			{
				gfxFileLocation = gfxFile.toString();				
				//System.out.println("Suckfest " + (int)checkByte(gfxFile, 8));
				
				if (loadGfxFile() == true) {
					saveConfig();					
					showButtons(true);
				}
			}else{
				Window.showMessage("Please select a valid .QDA file.");
			}
		}
	}
	
	public static File saveFile(String fpath) {
		File dialogPath = new File(fpath);
		
		JFileChooser chooser = new JFileChooser();
		chooser.setSelectedFile(dialogPath);
		int returnVal = chooser.showSaveDialog(chooser);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		
		return null;
	}
	
	public static void exportImage() {
		showButtons(false);
		File f = saveFile(lastExport + "\\" + Window.cbFileNames.getSelectedItem());
		
		if (f != null) {
			lastExport = f.getParentFile().toString();
			saveConfig();
			
			int sheet = Window.cbFileNames.getSelectedIndex();
			InputStream is = null;
			OutputStream os = null;
			try {
				is = new FileInputStream(new File(gfxFileLocation));
				os = new FileOutputStream(f);
				is.skip(sheetIndex.get(sheet));
				
				byte[] buf = new byte[sheetSize.get(sheet)];
				
				is.read(buf);
				os.write(buf, 0, sheetSize.get(sheet));
			}catch(Exception e){
				Window.showMessage("File Export Failed.");
			}finally{
				try {
					is.close();
					os.close();
				} catch (IOException e) { e.printStackTrace(); }
				Window.showMessage("File Export Complete.");
			}
		}
		showButtons(true);	
		
	}
	
	public static void writeLittleEndian(OutputStream os, long val, int spaces) {
		long v = val;
		for (int ii = 0; ii < spaces; ii++) {
			try {
				if (ii == 0) {
					os.write((int)val % (256));
				}else{
					int r = (int)Math.floor((int)val / Math.pow(256,ii));
					//System.out.println(r);
					os.write(r);
				}
			} catch (IOException e) {e.printStackTrace();}
		}
	}
	
	public static void importImage() throws IOException {
		showButtons(false);
		
		File f = openFile(lastImport, "BMP File", "bmp");
		File newFile = new File("temp.qda");
		
		if (f != null) {
			//Check to see if the selected file is a BMP file
			if (checkByte(f, 0) == 'B' && checkByte(f, 1) == 'M') {
				lastImport = f.getParentFile().toString();
				System.out.println(lastImport);
				saveConfig();
				
				long byteTracker = 0;
				int numOfSheets = 0;
				
				int sheet = Window.cbFileNames.getSelectedIndex();
				long filesize = f.length();
				String filename;/* = f.getName();*/
				filename = Window.cbFileNames.getSelectedItem().toString();
				
				byte[] buf;
				
				long iscounter = 0;
				InputStream is = null;
				OutputStream os = null;
				
				try {
					is = new FileInputStream(gfxFileLocation);
					os = new FileOutputStream(newFile);
					
					//Copy the first header
					buf = new byte[256];
					is.read(buf);
					iscounter += 256;
					os.write(buf, 0, 256);
					
					//Get number of sheets
					numOfSheets = buf[8] & 0xff + ((buf[9] & 0xff) * 256);
					//System.out.println(numOfSheets);
					byteTracker = 256 + (268 * numOfSheets);
					
					//Directly copy the preceding sheet headers
					for (int i = 0; i < sheet; i++) {
						buf = new byte[268];
						iscounter += 268;
						is.read(buf);
						os.write(buf, 0 ,268);
	
						byteTracker += sheetSize.get(i);
						System.out.println(byteTracker);
					}
					
					//Write the new sheet's header
					writeLittleEndian(os, byteTracker, 4);
					writeLittleEndian(os, filesize, 4);
					writeLittleEndian(os, filesize, 4);
					
					for (int i = 0; i < 256; i++) {
						if (i < filename.length()) {
							os.write(filename.charAt(i));
						}else{
							os.write(0);
						}
					}
					
					is.skip(268);
					iscounter += 268;
					byteTracker += filesize;
					
					//Finish writing the old headers
					for (int i = sheet + 1; i < numOfSheets; i++) {
						writeLittleEndian(os, byteTracker, 4);
						is.skip(4);
						iscounter += 4;
						
						buf = new byte[264];
						iscounter += 264;
						is.read(buf);
						os.write(buf, 0, 264);
	
						byteTracker += sheetSize.get(i);
					}
					
					//Write sheets
					for (int i = 0; i < numOfSheets; i++) {
						int bufferSize;
						InputStream newis = new FileInputStream(f);
						if (i == sheet) {
							
							bufferSize = (int)f.length();
							buf = new byte[bufferSize];
							newis.read(buf);
							
						}else{
							bufferSize = sheetSize.get(i);
							buf = new byte[bufferSize];
							is.skip(sheetIndex.get(i) - iscounter);
							iscounter += sheetIndex.get(i) - iscounter;
							iscounter += bufferSize;
							is.read(buf);
						}
						os.write(buf, 0, bufferSize);
						//System.out.println(bufferSize);
						newis.close();
					}
					
					is.close();
					os.close();
					
					//Delete old file, and replace with the new one
					File old = new File(gfxFileLocation);
					old.delete();
					newFile.renameTo(old);
										
					//Reset the combo box selection
					int si = Window.cbFileNames.getSelectedIndex();
					loadGfxFile();
					Window.cbFileNames.setSelectedIndex(si);
					updatePreview(Window.cbFileNames.getSelectedIndex());
					
					Window.showMessage("File import complete successfully!");
					
				}catch(Exception e){
					System.out.println(e.toString());
				}finally{
					
				}
			}else{
				Window.showMessage("The file selected was not a BMP file. Please only import 8-bit BMP files.");
			}
		}else{
			newFile.delete();
		}
				
		showButtons(true);		
	}
	
	public static void updatePreview(int sheetind) {
		InputStream is = null;
		OutputStream os = null;
		
		try {
			is = new FileInputStream(gfxFileLocation);
			os = new FileOutputStream(new File("preview.bmp"));
			is.skip(sheetIndex.get(sheetind));
			
			byte[] buf = new byte[sheetSize.get(sheetind)];
			
			is.read(buf);
			os.write(buf, 0, sheetSize.get(sheetind));
			BufferedImage bi = ImageIO.read(new File("preview.bmp"));
			Window.canPreview.setSize(new Dimension(bi.getWidth(), bi.getHeight()));
			//Window.scrollPane.setSize(new Dimension(bi.getWidth(), bi.getHeight()));
		}catch(Exception e){
			System.out.println(e.toString());
		}finally{
			if (is != null) {
				try {
					is.close();
					os.close();
					Window.updatePreview();
				} catch (IOException e) { e.printStackTrace(); }
			}
		}
		
	}
	
	private static void loadConfig() {
		File config = new File("config.txt");
		if (config.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader("config.txt"));
				gfxFileLocation = br.readLine();
				lastExport = br.readLine();
				lastImport = br.readLine();
				br.close();
			}catch(Exception e){};
		}
		if (lastExport.equals("")) {
			lastExport = localPath;
		}
		if (lastImport.equals("")) {
			lastImport = localPath;
		}
		
		File gfx = new File(gfxFileLocation);
		if (!gfx.exists()) {
			gfxFileLocation = "";
		}
		
		/*
		System.out.println(gfxFileLocation);
		System.out.println(lastImport);
		System.out.println(lastExport);
		*/
	}
	
	private static void saveConfig() {
		if (lastExport.equals("")) {
			lastExport = localPath;
		}
		if (lastImport.equals("")) {
			lastImport = localPath;
		}
		if (gfxFileLocation.equals("")) {
			gfxFileLocation = "";
		}
		try {
			OutputStream os = new FileOutputStream(new File("config.txt"));
			PrintStream ps = new PrintStream(os);
			ps.println(gfxFileLocation);
			ps.println(lastExport);
			ps.println(lastImport);
			ps.close();
		}catch(Exception e){};
		
		/*
		System.out.println(gfxFileLocation);
		System.out.println(lastImport);
		System.out.println(lastExport);
		*/
	}

}
