package my.img;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GeneralPathRect {
	/** * @param args */
	public static void main(String[] args) {
		new GeneralPathRectFrame();
	}
}

class GeneralPathRectFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private GeneralPath path = new GeneralPath();
	BufferedImage image;
	JFileChooser chooser = new JFileChooser();
	JMenuBar bar = new JMenuBar();
	JPanel panel = new drawPanel();

	public GeneralPathRectFrame() {
		setSize(400, 400);
		JMenu file = new JMenu("file");
		JMenuItem openItem = new JMenuItem("导入图片");
		openItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				chooser.setCurrentDirectory(new File("."));
				String[] suffiex = ImageIO.getReaderFileSuffixes();
				chooser.setFileFilter(new FileNameExtensionFilter("Image file",
						suffiex));
				int ans = chooser.showOpenDialog(GeneralPathRectFrame.this);
				if (ans == JFileChooser.APPROVE_OPTION) {
					try {
						image = ImageIO.read(chooser.getSelectedFile());
						repaint();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		file.add(openItem);
//		panel.insertComponent(); 
		add(panel);
		bar.add(file);
		setJMenuBar(bar);
		setVisible(true);
	}

	class drawPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			if (image != null) {
				int width = image.getWidth(), height = image.getHeight();
				RoundRectangle2D rect = new RoundRectangle2D.Double(0, 0,
						width, height, 20, 20);
				path.append(rect, false);
				g2.setClip(path);
				g2.drawImage(image, 0, 0, null);
			} else
				g.drawString("nihoa", 0, 0);
		}
	}
}
