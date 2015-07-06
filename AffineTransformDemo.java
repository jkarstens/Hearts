import java.applet.Applet;
import javax.swing.JFrame;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

public class AffineTransformDemo extends Applet implements AdjustmentListener {
  private Thread thread;
  private Graphics dbGraphics;
  private Image dbImage;
  private int WIDTH, HEIGHT;
  private AffineTransform at;

  private Image image, model;
  private int cx, cy; // center coordinates of image
  private Scrollbar translatex, translatey, rotateAmt, rotateAnchorx, rotateAnchory;

  public void init() {
    WIDTH = 600;
    HEIGHT = 600;
    setSize(WIDTH, HEIGHT);
    setPreferredSize(new Dimension(WIDTH, HEIGHT));
    setBackground(new Color(255, 228, 181));
    at = new AffineTransform(); // identity transform

    image = Toolkit.getDefaultToolkit().createImage("images/s12.png").getScaledInstance(110, 160, Image.SCALE_SMOOTH);
    model = Toolkit.getDefaultToolkit().createImage("images/s122.png").getScaledInstance(110, 160, Image.SCALE_SMOOTH);
    cx = WIDTH / 2 - 55;
    cy = HEIGHT / 2 - 80;
    at.translate(cx, cy); // center image
    translatex = new Scrollbar(Scrollbar.HORIZONTAL, cx, 5, 0, WIDTH + 5);
    translatex.setBackground(new Color(200, 255, 200));
    translatex.addAdjustmentListener(this);
    add(translatex);
    translatey = new Scrollbar(Scrollbar.VERTICAL, cy, 5, 0, HEIGHT + 5);
    translatey.setBackground(new Color(200, 255, 200));
    translatey.addAdjustmentListener(this);
    add(translatey);
    rotateAmt = new Scrollbar(Scrollbar.HORIZONTAL, 0, 5, 0, 365);
    rotateAmt.setBackground(new Color(255, 200, 200));
    rotateAmt.addAdjustmentListener(this);
    add(rotateAmt);
    rotateAnchorx = new Scrollbar(Scrollbar.HORIZONTAL, 0, 5, 0, WIDTH + 5);
    rotateAnchorx.setBackground(new Color(250, 200, 200));
    rotateAnchorx.addAdjustmentListener(this);
    add(rotateAnchorx);
    rotateAnchory = new Scrollbar(Scrollbar.VERTICAL, 0, 5, 0, HEIGHT + 5);
    rotateAnchory.setBackground(new Color(250, 200, 200));
    rotateAnchory.addAdjustmentListener(this);
    add(rotateAnchory);
  }

  public void paint(Graphics g) {
    g.drawImage(model, 0, 0, this);
    Graphics2D g2 = (Graphics2D) g;
    g2.drawImage(image, at, this);
    setAWTComponentBounds();
    drawStrings(g);
  }

  private void drawStrings(Graphics g) {
    g.drawString("Translate X: " + translatex.getValue(), 450, 575);
    g.drawString("Translate Y: " + translatey.getValue(), 510, 475);
    g.drawString("Rotate Amount: " + rotateAmt.getValue() + " degrees", 240, 575);
    g.drawString("Rotate Anchor X: " + rotateAnchorx.getValue(), 50, 575);
    g.drawString("Rotate Anchor Y: " + rotateAnchory.getValue(), 0, 475);
    g.fillOval(rotateAnchorx.getValue() - 8, rotateAnchory.getValue() - 8, 16, 16);
    g.fillOval(55, 540, 16, 16);
  }

  private void setAWTComponentBounds() {
    translatex.setBounds(450, 580, 100, 15);
    translatey.setBounds(550, 480, 15, 100);
    rotateAmt.setBounds(200, 580, 210, 15);
    rotateAnchorx.setBounds(50, 580, 100, 15);
    rotateAnchory.setBounds(30, 480, 15, 100);
  }

  public void update(Graphics g) {
    if (dbImage == null) {
      dbImage = createImage(WIDTH, HEIGHT);
      dbGraphics = dbImage.getGraphics();
    }
    dbGraphics.setColor(getBackground());
    dbGraphics.fillRect(0, 0, WIDTH, HEIGHT);
    dbGraphics.setColor(getForeground());
    paint(dbGraphics);
    g.drawImage(dbImage, 0, 0, this);
  }

  public void adjustmentValueChanged(AdjustmentEvent e) {
    at.setToIdentity();
    at.translate(translatex.getValue(), translatey.getValue());
    at.rotate(Math.PI * rotateAmt.getValue() / 180, rotateAnchorx.getValue(), rotateAnchory.getValue());
    repaint();
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Affine Tranform Demo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Applet applet = new AffineTransformDemo();
    frame.add(applet, BorderLayout.CENTER);
    applet.init();
    applet.start();
    frame.pack();
    frame.setVisible(true);
  }
}
