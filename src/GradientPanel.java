import javax.swing.*;
import java.awt.*;

public class GradientPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();
        Color skyBlue = new Color(135, 206, 235);
        Color violet = new Color(138, 43, 226);
        GradientPaint gradient = new GradientPaint(0, 0, skyBlue, width, height, violet);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
    }
}