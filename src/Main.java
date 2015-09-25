import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {

	private static Window window = new Window();
	private static String localPath = new File(".").getAbsolutePath();
	private static String gfxFileLocation = "";
	private static String lastSave = "";
	private static ArrayList<Integer> sheetIndex = new ArrayList<Integer>();
	
	public static void main(String[] args) throws IOException {
		//Load file paths
		localPath = localPath.substring(0,  localPath.length() - 1);
		loadConfig();

		if (!gfxFileLocation.equals("")) {
			loadGfxFile();
		}
	}
	
	public static void showButtons(boolean enable) {
		Window.cbFileNames.setEnabled(enable);
		Window.btnExport.setEnabled(enable);
		Window.btnImport.setEnabled(enable);
	}
		
	//The method performs all tasks  that occur when a .qda file is selected
	private static void loadGfxFile() throws IOException {
		if (!gfxFileLocation.equals("")) {
			showButtons(false);
			populateFileNames();
			populateSheetIndex();
			updatePreview(Window.cbFileNames.getSelectedIndex());
			showButtons(true);
		}
		
	}
	
	//Searches the file for sheets and saves their locations
	//Assumes gfxFileLocation is not empty
	public static void populateSheetIndex() throws IOException {
		InputStream is = null;
		try {
			is = new FileInputStream(new File(gfxFileLocation));
			sheetIndex.clear();
			
			int read = is.read();
			int byteCounter = 0;
			int byte1 = 0, byte2 = 0;
			
			while (read != -1) {
				
				if (byte1 == (int)'B' && byte2 == (int)'M' && read != (int)'P' && read != (int)'p') {
					//Found a sheet
					sheetIndex.add(byteCounter - 2);
					//System.out.println("" + (byteCounter - 2));
				}
				
				byte1 = byte2;
				byte2 = read;				
				read = is.read();
				
				byteCounter++;
			}
			
			sheetIndex.add(byteCounter);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally{
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {e.printStackTrace();}
			}
		}
	}
	
	public static void openQdaFile() throws IOException {
		File dialogPath = new File(gfxFileLocation);
		if (gfxFileLocation.equals("")) {
			dialogPath = new File(localPath);
		}
		
		JFileChooser chooser = new JFileChooser(dialogPath);
		FileFilter filter = new FileNameExtensionFilter("QDA file", "qda");
		chooser.setFileFilter(filter);
		
		int returnVal = chooser.showOpenDialog(chooser);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			showButtons(false);
			
			File gfxFile = chooser.getSelectedFile();
			
			gfxFileLocation = gfxFile.toString();
			
			saveConfig();
			loadGfxFile();
			
			showButtons(true);
		}
	}
	
	public static void exportImage() {
		File f = new File(lastSave + "\\" + Window.cbFileNames.getSelectedItem());
		JFileChooser chooser = new JFileChooser();
		chooser.setSelectedFile(f);
		int returnVal = chooser.showSaveDialog(chooser);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			showButtons(false);
			
			File chosenFile = chooser.getSelectedFile();
			lastSave = chosenFile.getParentFile().toString();
			saveConfig();
			
			int sheet = Window.cbFileNames.getSelectedIndex();
			
			int firstByte = sheetIndex.get(sheet),
				lastByte = sheetIndex.get(sheet + 1);
			//int read = 0;
			
			InputStream is = null;
			OutputStream os = null;
			
			try {
				is = new FileInputStream(new File(gfxFileLocation));
				os = new FileOutputStream(chosenFile);
				
				is.skip(firstByte);
				for (int i = lastByte - firstByte; i > 0; i--) {
					os.write(is.read());
				}
				
			}catch(Exception e){
				Window.showMessage("File Export Failed.");
			}finally{
				try {
					is.close();
					os.close();
				} catch (IOException e) { e.printStackTrace(); }
				Window.showMessage("File Export Complete.");
			}
			
			showButtons(true);
		}		
	}
	
	public static void importImage() throws IOException {
		
		File f = new File(lastSave + "\\" + Window.cbFileNames.getSelectedItem());
		JFileChooser chooser = new JFileChooser();
		chooser.setSelectedFile(f);
		int returnVal = chooser.showOpenDialog(chooser);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			
			showButtons(false);
			
			File chosenFile = chooser.getSelectedFile();
			lastSave = chosenFile.getParentFile().toString();
			saveConfig();
			
			InputStream is = null;
			OutputStream os = null;
			
			try {
				is = new FileInputStream(gfxFileLocation);
				ArrayList<Integer> newFile = new ArrayList<Integer>();
				int sheet = Window.cbFileNames.getSelectedIndex();
				
				for (int i = sheetIndex.get(sheet); i > 0; i--) {
					newFile.add(is.read());
				}
				is.close();
				
				//Import new file
				is = new FileInputStream(chosenFile);
				int read = is.read();
				while (read != -1) {
					newFile.add(read);
					read = is.read();
				}
				is.close();
				
				//Finish importing old file
				is = new FileInputStream(gfxFileLocation);
				is.skip(sheetIndex.get(sheet + 1));
				read = is.read();
				while (read != -1) {
					newFile.add(read);
					read = is.read();
				}
				is.close();
				
				//Output new file
				os = new FileOutputStream(gfxFileLocation);
				for (int i : newFile) {
					os.write(i);
				}
				os.close();
				
				updatePreview(sheet);
				Window.showMessage("File Import Complete.");
				
			}catch(Exception e){
			}finally{
				
			}
			
			showButtons(true);
		}
		
	}
	
	public static void updatePreview(int sheetind) {
		InputStream is = null;
		OutputStream os = null;
		ArrayList<Integer> previewBuffer = new ArrayList<Integer>();
		boolean end = false;
		int byte1 = 0, byte2 = 0;
		int read = 0;
		try {
			is = new FileInputStream(gfxFileLocation);
			os = new FileOutputStream(new File("preview.bmp"));
			is.skip(sheetIndex.get(sheetind));
			
			read = is.read();
			previewBuffer.add(read);
			read = is.read();
			
			while (end == false && read != -1) {
				
				previewBuffer.add(read);
				
				if (byte1 == (int)'B' && byte2 == (int)'M' && read != (int)'P' && read != (int)'p') {
					//Found a sheet
					end = true;
					previewBuffer.remove(previewBuffer.size() - 1);
					previewBuffer.remove(previewBuffer.size() - 1);
					previewBuffer.remove(previewBuffer.size() - 1);
				}
				
				byte1 = byte2;
				byte2 = read;				
				read = is.read();
			}
			
			//Write file
			for (int i : previewBuffer) {
				os.write(i);
			}
			
		}catch(Exception e){			
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
	
	private static void populateFileNames() {
		Window.clearFileNames();
		InputStream is = null;
		int thisByte;
		boolean end = false;
		int buffer[] = new int[2000];
		int buffindex = -1;
		
		try {			
			is = new FileInputStream(gfxFileLocation);
			
			while (end == false) {
				buffindex += 1;
				
				thisByte = is.read();
				buffer[buffindex] = thisByte;
				
				//Find '.bmp'
				if (buffindex > 4) {
					if ((thisByte == (int)'p' || thisByte == (int)'P') &&
						(buffer[buffindex - 1] == (int)'m' || buffer[buffindex - 1] == (int)'M') &&
						(buffer[buffindex - 2] == (int)'b' || buffer[buffindex - 2] == (int)'B') &&
						(buffer[buffindex - 3] == (int)'.'))
					{
						String filenameback = "";
						for (int i = 0; i < buffindex; i++) {
							if (buffer[buffindex - i] == 0) { //End stepback
								i = buffindex;
								buffindex = 0;
							}else{
								filenameback += (char)buffer[buffindex - i];
							}
						}
						String filename = "";
						for (int i = filenameback.length() - 1; i >= 0; i--) {
							filename += filenameback.charAt(i);
						}
						Window.addFileName(filename);
					}
				}
				
				//Find 'BM' but not 'BMP'
				if (buffindex > 3) {
					if (thisByte != (int)'P' && thisByte != (int)'p' && buffer[buffindex - 1] == (int)'M' &&
						buffer[buffindex - 2] == (int)'B')
					{
						end = true;
					}
				}
			}				
			
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			
			if (is!= null) {
				try {
					is.close();
				} catch (IOException e) {e.printStackTrace();}
			}
			
		}
		
	}
	
	private static void loadConfig() {
		File config = new File("config.txt");
		if (config.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader("config.txt"));
				gfxFileLocation = br.readLine();
				lastSave = br.readLine();
				br.close();
			}catch(Exception e){};
		}
		if (lastSave.equals("")) {
			lastSave = localPath;
		}
		if (gfxFileLocation.equals("")) {
			//gfxFileLocation = localPath;
		}
	}
	
	private static void saveConfig() {
		if (lastSave.equals("")) {
			lastSave = localPath;
		}
		if (gfxFileLocation.equals("")) {
			gfxFileLocation = localPath;
		}
		try {
			OutputStream os = new FileOutputStream(new File("config.txt"));
			PrintStream ps = new PrintStream(os);
			ps.println(gfxFileLocation);
			ps.println(lastSave);
			ps.close();
		}catch(Exception e){};
	}

}
