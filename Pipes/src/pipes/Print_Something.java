/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pipes;

/**
 *
 * @author Gary
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.print.*;

public class Print_Something implements Printable {

    private String ar[] = null;
    int[] pageBreaks;  // array of page break line positions.

    /* Synthesise some sample lines of text */
    String[] textLines = null;

    //****************************************************
    public Print_Something(String arr[]) {
        /*
        Object is created with a parameter which contains the lines of the whole print job
        i.e. each element in array represents a line of text.
        ar (global variable) then points to arr to enable all methods access to the print job.
        It does not take an empty string as a parameter	
         */
        ar = arr;
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        boolean ok = job.printDialog();
        if (ok) {
            try {
                job.print();
            } catch (PrinterException ex) {
                /* The job did not successfully complete */
            }
        }
    }

    //****************************************************
    private void initTextLines() {
        if (textLines == null) {
            /*
            set numLines to the nr of elements in the array - each element represents a line to be 	printed	    
             */
            int numLines = ar.length;
            textLines = new String[numLines];
            for (int i = 0; i < numLines; i++) {
                textLines[i] = ar[i];
            }
        }
    }

    //****************************************************
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        //Font font = new Font ("Serif", Font.PLAIN, 10);
        Font font = new Font("Serif", Font.PLAIN, 8);
        FontMetrics metrics = g.getFontMetrics(font);
        int lineHeight = metrics.getHeight();

        if (pageBreaks == null) {
            initTextLines();
            /*
            nr of lines printed per page can be determined by the formula below - takes height of text in account.
            OR linesPerPage is forced to be 60 as below - which means that the elements in the array 	must be grouped in 60's (except for the last group) to ensure correct output to the paper
             */
            //int linesPerPage = (int) (pf.getImageableHeight () / lineHeight);
            int linesPerPage = 60;
            int numBreaks = (textLines.length - 1) / linesPerPage;
            pageBreaks = new int[numBreaks];
            for (int b = 0; b < numBreaks; b++) {
                pageBreaks[b] = (b + 1) * linesPerPage;
            }
        }

        if (pageIndex > pageBreaks.length) {
            return NO_SUCH_PAGE;
        }

        /* User (0,0) is typically outside the imageable area, so we must
         * translate by the X and Y values in the PageFormat to avoid clipping
         * Since we are drawing text we
         */
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        /* Draw each line that is on this page.
         * Increment 'y' position by lineHeight for each line.
         */
        int y = 0;
        int start = (pageIndex == 0) ? 0
                : pageBreaks[pageIndex - 1];
        int end = (pageIndex == pageBreaks.length)
                ? textLines.length
                : pageBreaks[pageIndex];
        for (int line = start; line < end; line++) {
            y += lineHeight;
            g.drawString(textLines[line], 0, y);
        }

        /* tell the caller that this page is part of the printed document */
        return PAGE_EXISTS;
    }
}
