import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class Window extends JFrame implements ActionListener, ItemListener {

	public static JFrame frame;
	public static JComboBox cbFileNames;
	public static Preview canPreview;
	public static JButton btnExport;
	public static JButton btnImport;

	public Window() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 588, 473);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Hydra Castle Graphics Manager v1.0");
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
								
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
				
		JMenuItem mntmOpenBmpqda = new JMenuItem("Open Bmp.qda...");
		mntmOpenBmpqda.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				try {
					Main.openQdaFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		mnFile.add(mntmOpenBmpqda);
		
		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				System.exit(0);
			}
		});
		mnFile.add(mntmQuit);
		frame.getContentPane().setLayout(null);
		
		cbFileNames = new JComboBox();
		cbFileNames.setEnabled(false);
		cbFileNames.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				frame.validate();
				frame.repaint();
				Main.updatePreview(cbFileNames.getSelectedIndex());
			}
		});
		cbFileNames.setBounds(416, 11, 146, 20);
		frame.getContentPane().add(cbFileNames);
		
		btnExport = new JButton("Export...");
		btnExport.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
				if (btnExport.isEnabled() == true) {
					Main.exportImage();
				}
				
			}
		});
		btnExport.setBounds(416, 42, 146, 23);
		frame.getContentPane().add(btnExport);
		
		btnImport = new JButton("Import...");
		btnImport.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
				if (btnImport.isEnabled() == true) {
					try {
						Main.importImage();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		});
		btnImport.setBounds(416, 76, 146, 23);
		frame.getContentPane().add(btnImport);
		
		
		canPreview = new Preview();
		canPreview.setBackground(Color.WHITE);
		canPreview.setBounds(10, 11, 400, 400);
		frame.getContentPane().add(canPreview);
		
		frame.setVisible(true);
	}
	
	public static void showMessage(String mess) {
		JOptionPane.showMessageDialog(frame, mess);
	}
	
	public static void updatePreview() {
		canPreview.repaint();
	}
	
	public static void addFileName(String fn) {
		cbFileNames.addItem(fn);
	}

	public static void clearFileNames() {
		cbFileNames.removeAllItems();
	}
	
	@Override
	public void itemStateChanged(ItemEvent arg0) {}

	@Override
	public void actionPerformed(ActionEvent arg0) {}
	
}
