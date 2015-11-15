import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.awt.Component;

public class Window extends JFrame implements ActionListener, ItemListener {

	public static JFrame frame;
	public static JComboBox cbFileNames;
	public static JButton btnExport;
	public static JButton btnImport;
	public static Preview canPreview;

	public Window() {
		initialize();
	}

	private void initialize() {
		//JFrame.setDefaultLookAndFeelDecorated(true);
		frame = new JFrame();
		frame.getContentPane().setLayout(null);
		frame.setPreferredSize(new Dimension(600, 480));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Hydra Castle Graphics Manager v1.4");
		
	//Menubar
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, 594, 20);
		
		JMenu mnFile = new JMenu("File");	
		
		JMenuItem mntmOpenBmpqda = new JMenuItem("Open Bmp.qda...");
		mntmOpenBmpqda.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				try {
					Main.openQdaFile();
				} catch (IOException e) {e.printStackTrace();}
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
		menuBar.add(mnFile);	
		frame.getContentPane().add(menuBar);
		
		JPanel panel = new JPanel();
		panel.setBounds(10, 31, 145, 407);
		frame.getContentPane().add(panel);
		
		cbFileNames = new JComboBox();
		cbFileNames.setLocation(0, 0);
		cbFileNames.setAlignmentX(Component.LEFT_ALIGNMENT);
		cbFileNames.setSize(new Dimension(145, 20));
		cbFileNames.setEnabled(false);
		cbFileNames.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				frame.validate();
				frame.repaint();
				Main.updatePreview(cbFileNames.getSelectedIndex());
			}
		});
		panel.setLayout(null);
		panel.add(cbFileNames);
		
		btnExport = new JButton("Export...");
		btnExport.setBounds(0, 31, 95, 23);
		btnExport.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				
				if (btnExport.isEnabled() == true) {
					Main.exportImage();
				}
				
			}
		});
		panel.add(btnExport);
		
		btnImport = new JButton("Import...");
		btnImport.setBounds(0, 65, 95, 23);
		btnImport.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				
				if (btnImport.isEnabled() == true) {
					try {
						Main.importImage();
					} catch (IOException e) {e.printStackTrace();}
				}
				
			}
		});
		panel.add(btnImport);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(165, 32, 417, 406);
		frame.getContentPane().add(panel_1);
		
		canPreview = new Preview();
		panel_1.add(canPreview);
		canPreview.setBackground(Color.WHITE);		
		
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
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
