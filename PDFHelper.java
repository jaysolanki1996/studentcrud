package my.airo.roboadvisor.infra.helper;

import java.awt.Color;

import org.springframework.stereotype.Component;

import com.lowagie.text.Chunk;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;

@Component
public class PDFHelper extends AbstractHelper {

    private static final int UNDEFINED = -1;

    public Paragraph addContentParagraph(String paragraph, float fontSize, int fontStyle) {
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        return new Paragraph(paragraph, font);

    }

    public Paragraph addLabelParagraph(String paragraph, float fontSize, int fontStyle) {
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        return new Paragraph(getContextMessage(paragraph), font);
    }

    public Paragraph addParagraph(String paragraph, String seperator, String appendString, float fontSize, int fontStyle) {
        paragraph = getContextMessage(paragraph) + seperator + appendString;
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        return new Paragraph(paragraph, font);

    }
    
    public Paragraph addParagraph(String paragraph, String seperator, String appendString, float fontSize, int fontStyle, Color fontColor) {
        paragraph = getContextMessage(paragraph) + seperator + appendString;
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        font.setColor(fontColor);
        return new Paragraph(paragraph, font);

    }
    
    public Paragraph addLabelParagraph(String paragraph, String seperator, String appendLocale, float fontSize, int fontStyle) {
        paragraph = getContextMessage(paragraph) + seperator + getContextMessage(appendLocale);
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        return new Paragraph(paragraph, font);

    }    

    public Paragraph addContentParagraph(String paragraph, float fontSize) {
        Font font = new Font(UNDEFINED, fontSize);
        return new Paragraph(paragraph, font);

    }

    public Paragraph addLabelParagraph(String paragraph, float fontSize) {
        Font font = new Font(UNDEFINED, fontSize);
        return new Paragraph(getContextMessage(paragraph), font);

    }

    public Paragraph addLabelParagraph(String paragraph, Object[] args, float fontSize) {
        Font font = new Font(UNDEFINED, fontSize);
        return new Paragraph(getContextMessage(paragraph, args), font);

    }

    public Paragraph addEmptyLine(int emptyLine) {
        Paragraph paragraph = new Paragraph();
        for (int i = 0; i < emptyLine; i++) {
            paragraph.add(new Paragraph(""));
        }
        return paragraph;
    }

    public PdfPCell addContentCell(String text, float fontSize, int fontStyle) {
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    public PdfPCell addLabelCell(String text, float fontSize, int fontStyle) {
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        PdfPCell cell = new PdfPCell(new Paragraph(getContextMessage(text), font));
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    
    public PdfPCell addLabelCell(String text, Object args[], float fontSize, int fontStyle) {
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        PdfPCell cell = new PdfPCell(new Paragraph(getContextMessage(text, args), font));
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    public PdfPCell addContentCell(String text, float fontSize) {
        Font font = new Font(UNDEFINED, fontSize);
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    public PdfPCell addLabelCell(String text, float fontSize) {
        Font font = new Font(UNDEFINED, fontSize);
        PdfPCell cell = new PdfPCell(new Paragraph(getContextMessage(text), font));
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    public PdfPCell addEmptyCell() {
        PdfPCell cell = new PdfPCell(new Paragraph(""));
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    public PdfPCell addEmptyCellWithBorder(Color borderColor) {
        PdfPCell cell = new PdfPCell(new Paragraph(""));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(borderColor);
        return cell;
    }

    public PdfPCell addEmptyCell(Color backgroundcolor) {
        PdfPCell cell = new PdfPCell(new Paragraph(""));
        cell.setBackgroundColor(backgroundcolor);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    public PdfPCell addContentCell(String text, float fontSize, int fontStyle, int alignment, Color backgroundColor) {
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBackgroundColor(backgroundColor);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    public PdfPCell addContentCell(String text, float fontSize, int alignment, Color backgroundColor) {
        Font font = new Font(UNDEFINED, fontSize);
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBackgroundColor(backgroundColor);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }
    
    public PdfPCell addContentCell(String text, float fontSize, int fontStyle, int alignment, Color backgroundColor, int paddingTop, int paddingBottom) {
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setPaddingBottom(paddingBottom);
        cell.setPaddingTop(paddingTop);
        cell.setBackgroundColor(backgroundColor);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    public PdfPCell addLabelCell(String text, float fontSize, int fontStyle, int alignment, Color fontColor) {
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        font.setColor(fontColor);
        PdfPCell cell = new PdfPCell(new Paragraph(getContextMessage(text), font));
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    public PdfPCell addLabelCell(String text, float fontSize, int fontStyle, int alignment, Color backgroundColor, int paddingTop, int paddingBottom) {
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        PdfPCell cell = new PdfPCell(new Paragraph(getContextMessage(text), font));
        cell.setPaddingBottom(paddingBottom);
        cell.setPaddingTop(paddingTop);
        cell.setBackgroundColor(backgroundColor);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }
    
    public PdfPCell addContentCell(String text, float fontSize, int fontStyle, int alignment, int cellPadding) {
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        PdfPCell cell = new PdfPCell(new Paragraph(getContextMessage(text), font));
        cell.setPaddingBottom(cellPadding);
        cell.setPaddingTop(cellPadding);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }
    
    public PdfPCell addContentCell(String text, float fontSize, int fontStyle, Color fontColor, int alignment, int cellPadding) {
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        font.setColor(fontColor);
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setPaddingBottom(cellPadding);
        cell.setPaddingTop(cellPadding);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    public PdfPCell addImageCellWithBorder(Image image, int padding, Color borderColor) {
        PdfPCell cell = new PdfPCell(image);
        cell.setPaddingBottom(padding);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(borderColor);
        return cell;
    }

    public PdfPCell addImageCellWithBorder(Image image, int padding, Color borderColor, int alignment) {
        PdfPCell cell = new PdfPCell(image);
        cell.setPaddingBottom(padding);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(borderColor);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    public PdfPCell addContentCellWithBorder(String text, float fontSize, int fontStyle, int alignment, Color borderColor) {
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setPaddingTop(6);
        cell.setPaddingBottom(6);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(borderColor);
        return cell;
    }
    
    public PdfPCell addContentCellWithBorder(String text, float fontSize, int fontStyle, int alignment, Color borderColor, Color fontColor) {
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        font.setColor(fontColor);
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setPaddingTop(6);
        cell.setPaddingBottom(6);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(borderColor);
        return cell;
    }

    public PdfPCell addLabelCellWithBorder(String text, float fontSize, int fontStyle, int alignment, Color borderColor) {
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        PdfPCell cell = new PdfPCell(new Paragraph(getContextMessage(text), font));
        cell.setPaddingTop(6);
        cell.setPaddingBottom(6);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(borderColor);
        return cell;
    }
    
    public PdfPCell addLabelCellWithBorder(String text, float fontSize, int fontStyle, Color fontColor, int alignment, Color borderColor) {
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        font.setColor(fontColor);
        PdfPCell cell = new PdfPCell(new Paragraph(getContextMessage(text), font));
        cell.setPaddingTop(6);
        cell.setPaddingBottom(6);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(borderColor);
        return cell;
    }
    
    public PdfPCell addParagraphCell(Paragraph paragraph, int alignment, Color borderColor) {
        PdfPCell cell = new PdfPCell(paragraph);
        cell.setPaddingTop(6);
        cell.setPaddingBottom(6);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(borderColor);
        return cell;
    }

    public PdfPCell addLabelCellWithBorder(String localeText, String seperator, String appendText, float fontSize, int fontStyle, int alignment, Color borderColor) {
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        PdfPCell cell = new PdfPCell(new Paragraph(getContextMessage(localeText) + seperator + appendText, font));
        cell.setPaddingTop(6);
        cell.setPaddingBottom(6);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(borderColor);
        return cell;
    }

    public PdfPCell addLabelCellWithBorder(String localeText, String seperator, String appendText, float fontSize, int alignment, Color borderColor) {
        Font font = new Font(UNDEFINED, fontSize);
        PdfPCell cell = new PdfPCell(new Paragraph(getContextMessage(localeText) + seperator + appendText, font));
        cell.setPaddingTop(6);
        cell.setPaddingBottom(6);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(borderColor);
        return cell;
    }
    
    public PdfPCell addLabelCellWithBorder(String localeText, String seperator, Chunk appendText, float fontSize, int fontStyle, int alignment, Color borderColor, Color fontColor) {
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        font.setColor(fontColor);
        Paragraph paragraph = new Paragraph(getContextMessage(localeText) + seperator,font);
        paragraph.add(appendText);
        PdfPCell cell = new PdfPCell(paragraph);
        cell.setPaddingTop(6);
        cell.setPaddingBottom(6);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(borderColor);
        return cell;
    }
    
    public PdfPCell addLabelCellWithBorder(String text, String[] args, float fontSize, int fontStyle, int alignment, Color borderColor) {
        Font font = new Font(UNDEFINED, fontSize, fontStyle);
        PdfPCell cell = new PdfPCell(new Paragraph(getContextMessage(text, args), font));
        cell.setPaddingTop(6);
        cell.setPaddingBottom(6);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(borderColor);
        return cell;
    }
}
