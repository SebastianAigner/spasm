package io.sebi.spasm;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * Created by Sebastian Aigner
 */

/**
 * The BargraphDiagram is a simple way to display a bargraph as a JPanel. It takes a list of integer values. The bars
 * will automatically resize in height and width based on the amount of data and the size of the window. By default,
 * the Bargraph will be blank.
 */
public class BargraphDiagram extends JPanel {
    private List<Integer> values;
    //Consider supporting different colors

    /**
     * Draws the bargraph as a JPanel.
     * @param g Graphics
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int panelWidth = this.getWidth();
        int panelHeight = this.getHeight();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLUE);
        int maximum;
        if (values == null) {
            return;
        }
        maximum = Collections.max(values);
        int resolution = values.size();
        for (int i = 0; i < values.size(); ++i) {
            int startX = (int) (panelWidth / (double) resolution * i);
            int width = (int) (panelWidth / (double) resolution);
            int height = (int) (values.get(i) / (double) maximum * panelHeight);
            int startY = panelHeight - height;
            g2d.fillRect(startX, startY, width, height);
        }
    }

    /**
     * Get the current values that the bargraph represents
     * @return current values
     */
    public List<Integer> getValues() {
        return values;
    }

    /**
     * Set the values that should be displayed by the diagram. Changes will not apply until the diagram is repainted.
     * @param values values to be drawn
     */
    public void setValues(List<Integer> values) {
        this.values = values;
    }


}
