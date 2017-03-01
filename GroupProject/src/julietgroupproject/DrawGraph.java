package julietgroupproject;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;

@SuppressWarnings("serial")
public class DrawGraph {
   private static final float MAX_FIT = 200;
   private static final int MAX_SCORE = 10000;
   private static final int BORDER_GAP = 30;
   //private static final int BORDER_GAP = 10;
   private static final int PREF_W = 800;
   private static final int PREF_H = 650;
   private static final Color GRAPH_BACKGROUND_COLOUR = Color.WHITE;
   private static final float GRAPH_BACKGROUND_ALPHA = 0.4f;
   private static final Color GRAPH_AXES_COLOUR = Color.BLACK;
   private static final Color GRAPH_COLOR = Color.green;
   private static final Color GRAPH_POINT_COLOR = new Color(150, 50, 50, 180);
   private static final Stroke GRAPH_STROKE = new BasicStroke(3f);
   private static final int GRAPH_POINT_WIDTH = 12;
   private static final int Y_HATCH_CNT = 10;

   private DrawGraph() {}
   
   public static List<Integer> convertToInt(List<Float> fScores) {

       List<Integer> iScores = new ArrayList<Integer>();
       for (int i = 0; i<fScores.size(); i++){
           float fraction = (fScores.get(i)/MAX_FIT)*MAX_SCORE;
           int rounded = Math.round(fraction);
           iScores.add(rounded);
       }
       return iScores;
   }
   
   public static BufferedImage plotGraph(List<Float> inputScores) {
       return plotGraph(inputScores, PREF_W, PREF_H);
   }
   
   /**
    * Plotting a graph based on given training scores.
    * 
    * @param inputScores List of scores (in Doubles)
    * @param width width of image
    * @param height height of image
    * @return a BufferedImage object containing the graph
    */
   public static BufferedImage plotGraph(List<Float> inputScores, int width, int height) {
      BufferedImage bi = new BufferedImage(width, height,BufferedImage.TYPE_4BYTE_ABGR);
      Graphics g = bi.getGraphics();
      List<Integer> scores = convertToInt(inputScores);
      int maxIntScore = Collections.max(scores);
      int minIntScore = Collections.min(scores);
      
      Graphics2D g2 = (Graphics2D)g;
      float[] bgc = new float[3];
      float bgAlpha = GRAPH_BACKGROUND_ALPHA;
      GRAPH_BACKGROUND_COLOUR.getColorComponents(bgc);
      Color transparentBG = new Color(ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB),bgc,bgAlpha);
      g2.setColor(transparentBG);
      g2.fillRect(0, 0, width, height);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(GRAPH_AXES_COLOUR);
      
      double xScale = ((double) width - 2 * BORDER_GAP) / (scores.size() - 1);
      if (scores.size() < 2) { xScale = (width - 2 * BORDER_GAP) / 2; }
      double yScale = ((double) height - 2 * BORDER_GAP) / (double)(maxIntScore + 1 - minIntScore);
      if (scores.size() < 2) { yScale = (height - 2 * BORDER_GAP) / 2; }
      //System.out.println("xScale:" + xScale + ", yScale:" + yScale);

      List<Point> graphPoints = new ArrayList<Point>();
      for (int i = 0; i < scores.size(); i++) {
         int x1 = (int) (i * xScale + BORDER_GAP);
         int y1 = (int) ((maxIntScore - scores.get(i)) * yScale + BORDER_GAP);
         graphPoints.add(new Point(x1, y1));
      }

      // create x and y axes 
      g2.drawLine(BORDER_GAP, height - BORDER_GAP, BORDER_GAP, BORDER_GAP);
      g2.drawLine(BORDER_GAP, height - BORDER_GAP, width - BORDER_GAP, height - BORDER_GAP);
      
      // create hatch marks for y axis. 
      for (int i = 0; i < Y_HATCH_CNT; i++) {
         int x0 = BORDER_GAP;
         int x1 = GRAPH_POINT_WIDTH + BORDER_GAP;
         int y0 = height - (((i + 1) * (height - BORDER_GAP * 2)) / Y_HATCH_CNT + BORDER_GAP);
         int y1 = y0;
         g2.drawLine(x0, y0, x1, y1);
      }

      Stroke oldStroke = g2.getStroke();
      g2.setColor(GRAPH_COLOR);
      g2.setStroke(GRAPH_STROKE);
      for (int i = 0; i < graphPoints.size() - 1; i++) {
         int x1 = graphPoints.get(i).x;
         int y1 = graphPoints.get(i).y;
         int x2 = graphPoints.get(i + 1).x;
         int y2 = graphPoints.get(i + 1).y;
         g2.drawLine(x1, y1, x2, y2);         
      }

      g2.setStroke(oldStroke);      
      g2.setColor(GRAPH_POINT_COLOR);
      for (int i = 0; i < graphPoints.size(); i++) {
         int x = graphPoints.get(i).x - GRAPH_POINT_WIDTH / 2;
         int y = graphPoints.get(i).y - GRAPH_POINT_WIDTH / 2;
         int ovalW = GRAPH_POINT_WIDTH;
         int ovalH = GRAPH_POINT_WIDTH;
         g2.fillOval(x, y, ovalW, ovalH);
      }
      g2.dispose();
      return bi;
   }
   
   /*
   private static void makePanelImage(Component panel)
    {
        Dimension size = panel.getSize();
        BufferedImage image = new BufferedImage(
                    size.width, size.height
                              , BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        panel.paint(g2);
        try
        {
            ImageIO.write(image, "png", new File(file));
            
            System.out.println("Panel saved as Image.");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
*/

}
