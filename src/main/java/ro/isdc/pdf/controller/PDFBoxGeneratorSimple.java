package ro.isdc.pdf.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;

import ro.isdc.pdf.pdfbox.Column;
import ro.isdc.pdf.pdfbox.ColumnAlignment;

public class PDFBoxGeneratorSimple {
	private static Column[] columns = new Column[6];
	private static float[] rightEdgePos = new float[6];
	static {
		columns[0] = new Column("EmpNo", 50, ColumnAlignment.RIGHT);
		columns[1] = new Column("BirthDate", 70);
		columns[2] = new Column("FirstName", 75, ColumnAlignment.RIGHT);
		columns[3] = new Column("LastName", 150);
		columns[4] = new Column("Gender", 50);
		columns[5] = new Column("HireDate", 70);
		float crtEdge=0f;
		for (int i=0; i<6; i++) {
			crtEdge += columns[i].getWidth();
			rightEdgePos[i] = crtEdge;
		}
	}

	// Page configuration
	private static final PDRectangle PAGE_SIZE = PDRectangle.A4;
	private static final boolean IS_LANDSCAPE = false;
	private static final float MARGIN = 20;
	private static final float tableTopY = IS_LANDSCAPE ? PAGE_SIZE.getWidth() - MARGIN : PAGE_SIZE.getHeight() - MARGIN;
	// Font configuration
	private static final PDFont TEXT_FONT = PDType1Font.HELVETICA;
	private static final float FONT_SIZE = 10;
	private static final float TEXT_LINE_HEIGHT = TEXT_FONT.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * FONT_SIZE;
	// Table configuration
	private static final float ROW_HEIGHT = 15;
	private static final float CELL_MARGIN = 2;

	public ByteArrayOutputStream generateReport(final int lineCount, final int columnCount, List<String[]> tableContent) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Calculate center alignment for text in cell considering font height
		final float startTextY = tableTopY - (ROW_HEIGHT / 2) - (TEXT_LINE_HEIGHT / 4);
		PDDocument doc = new PDDocument();
		PDPageContentStream pageStream = addPageToDoc(doc);
		float crtLineY = tableTopY;
		float crtTextY = startTextY;
		// draw the header
		float rowHeight = drawTableHeader(pageStream, columnCount, crtTextY,crtLineY);
		crtLineY -= rowHeight;
		crtTextY -= rowHeight;

		// draw the lines
		for (String[] crtLine : tableContent) {
			if (crtTextY <= MARGIN) {
				drawVerticalLines(pageStream, columnCount, crtLineY);
				pageStream.close();
				// start new Page
				pageStream = addPageToDoc(doc);
				crtLineY = tableTopY;
				crtTextY = startTextY;
				rowHeight = drawTableHeader(pageStream, columnCount, crtTextY,crtLineY);
				crtLineY -= rowHeight;
				crtTextY -= rowHeight;
			}
			rowHeight = drawTableRow(pageStream, crtLine, columnCount, crtTextY,crtLineY);
			crtLineY -= rowHeight;
			crtTextY -= rowHeight;
		}
		
		drawVerticalLines(pageStream, columnCount, crtLineY);
		pageStream.close();
		doc.save(baos);
		doc.close();
		return baos;
	}
	
	/**Draw vertical separator lines between cells.
	 * @throws IOException */
	private void drawVerticalLines(PDPageContentStream pageStream, int columnCount,float crtLineY) throws IOException{
		pageStream.addLine(MARGIN, tableTopY, MARGIN, crtLineY);
		for (int i = 0; i < columnCount; i++) {
			float crtY = MARGIN + rightEdgePos[i];
			pageStream.addLine(crtY, tableTopY, crtY, crtLineY);
		}
		pageStream.closeAndStroke();
	}
	
	/**
	 * Draw the Table header.
	 * @param crtLineY 
	 * 
	 * @return
	 */
	private float drawTableHeader(PDPageContentStream pageStream, int columnCount, float startTextY, float crtLineY) throws IOException {
		// Position cursor to start drawing content
		float crtX = MARGIN + CELL_MARGIN;
		float maxHeight = 0;
		
		pageStream.drawLine(MARGIN, crtLineY, MARGIN + rightEdgePos[columnCount-1], crtLineY);
		
		for (int i = 0; i < columnCount; i++) {
			String text = columns[i].getName();
			float cellHeight = writeCellContent(i, text, pageStream, crtX, startTextY);
			if (cellHeight > maxHeight) {
				maxHeight = cellHeight;
			}
			crtX += columns[i].getWidth();
		}
		
		pageStream.drawLine(MARGIN, crtLineY-maxHeight, MARGIN + rightEdgePos[columnCount-1], crtLineY-maxHeight);
		return maxHeight;
	}

	/**
	 * Draw the Line content.
	 * @param crtLineY 
	 * 
	 * @return
	 */
	private float drawTableRow(PDPageContentStream pageStream, String[] lineContent, int columnCount, float startTextY, float crtLineY) throws IOException {
		float crtX = MARGIN + CELL_MARGIN;
		float maxHeight = 0;

		for (int i = 0; i < columnCount; i++) {
			float cellHeight = writeCellContent(i, lineContent[i], pageStream, crtX, startTextY);
			if (cellHeight > maxHeight) {
				maxHeight = cellHeight;
			}
			crtX += columns[i].getWidth();
		}
		pageStream.drawLine(MARGIN, crtLineY-maxHeight, MARGIN + rightEdgePos[columnCount-1], crtLineY-maxHeight);
		return maxHeight;
	}

	private float writeCellContent(int colIndex, String text, PDPageContentStream pageStream, float crtX, float startTextY) throws IOException {
		float cellHeight = ROW_HEIGHT;
		float cellInnerWidth = columns[colIndex].getWidth() - 2 * CELL_MARGIN;
		float textWidth = TEXT_FONT.getStringWidth(text) / 1000 * FONT_SIZE;
		if (textWidth <= cellInnerWidth) {// line fits in cell
			pageStream.beginText();
			if (columns[colIndex].getAlignment() == ColumnAlignment.LEFT) {
				pageStream.newLineAtOffset(crtX, startTextY);
			} else {
				float textLeftPos = crtX + cellInnerWidth - textWidth;
				pageStream.newLineAtOffset(textLeftPos, startTextY);
			}
			pageStream.showText(text != null ? text : "");
			pageStream.endText();
		} else {//line must be wrapped

			
			int start = 0;
			int end = 0;
			float crtCellY = startTextY;
			String currentToken="";
			for (int i : possibleWrapPoints(text)) {
				float width = TEXT_FONT.getStringWidth(text.substring(start, i)) / 1000 * FONT_SIZE;
				if (start < end && width > cellInnerWidth) {
					currentToken=text.substring(start, end);
					// Draw partial text and increase height
					pageStream.beginText();
					if (columns[colIndex].getAlignment() == ColumnAlignment.LEFT) {
						pageStream.newLineAtOffset(crtX, crtCellY);
					}else{
						float currentTokenWidth = TEXT_FONT.getStringWidth(currentToken) / 1000 * FONT_SIZE;
						float textLeftPos = crtX + cellInnerWidth - currentTokenWidth;
						pageStream.newLineAtOffset(textLeftPos, crtCellY);
					}
					pageStream.showText(currentToken);
					pageStream.endText();
					crtCellY -= TEXT_LINE_HEIGHT;
					start = end;
					cellHeight+=TEXT_LINE_HEIGHT;
				}
				end = i;
			}
			// Last piece of text
			pageStream.beginText();
			currentToken=text.substring(start);
			if (columns[colIndex].getAlignment() == ColumnAlignment.LEFT) {
				pageStream.newLineAtOffset(crtX, crtCellY);
			}else{
				float currentTokenWidth = TEXT_FONT.getStringWidth(currentToken) / 1000 * FONT_SIZE;
				float textLeftPos = crtX + cellInnerWidth - currentTokenWidth;
				pageStream.newLineAtOffset(textLeftPos, crtCellY);
			}
			pageStream.showText(currentToken);
			pageStream.endText();
		}

		return cellHeight;
	}

	int[] possibleWrapPoints(String text) {
		String[] split = text.split("(?<=\\W)");
		int[] ret = new int[split.length];
		ret[0] = split[0].length();
		for (int i = 1; i < split.length; i++)
			ret[i] = ret[i - 1] + split[i].length();
		return ret;
	}

	private PDPageContentStream addPageToDoc(PDDocument doc) throws IOException {
		PDPage page = new PDPage();
		page.setMediaBox(PAGE_SIZE);
		page.setRotation(IS_LANDSCAPE ? 90 : 0);
		doc.addPage(page);
		PDPageContentStream contentStream = new PDPageContentStream(doc, page, false, false);
		// User transformation matrix to change the reference when drawing.
		// This is necessary for the landscape position to draw correctly
		if (IS_LANDSCAPE) {
			contentStream.transform(new Matrix(0f, 1f, -1f, 0f, PAGE_SIZE.getWidth(), 0f));
		}
		contentStream.setFont(TEXT_FONT, FONT_SIZE);
		contentStream.setLineWidth(0.25f);
		return contentStream;
	}
}
