package yyt2xls;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

public class ToExcel {

	public float progress = 0;
	public boolean withImages = false;
	public int initialCant = 4;

	public boolean local = false;
	
	public byte[] generateExcel(ArrayList<CardRow> cards) throws Exception{
		
		Workbook excel = new HSSFWorkbook();
        Sheet hoja = excel.createSheet("Precios");
        CreationHelper helper =  excel.getCreationHelper();
        Drawing drawing = hoja.createDrawingPatriarch();
        
        int initialColumn = 0;

        if(this.withImages){
        	initialColumn = 1;
        }
        
        Row headers = hoja.createRow(0);
        Cell headerId = headers.createCell(initialColumn);
        Cell headerRarity = headers.createCell(initialColumn + 1);
        Cell headerColor = headers.createCell(initialColumn + 2);
        Cell headerPrice = headers.createCell(initialColumn + 3);
        Cell headerSale = headers.createCell(initialColumn + 4);
        Cell headerStock = headers.createCell(initialColumn + 5);
        Cell headerCuantas = headers.createCell(initialColumn + 6);
        Cell headerTotal = headers.createCell(initialColumn + 7);
        Cell totalCards = headers.createCell(initialColumn + 8);
        Cell totalValue = headers.createCell(initialColumn + 9);
        headerId.setCellValue("Id");
        headerRarity.setCellValue("Rareza");
        headerColor.setCellValue("Color");
        headerPrice.setCellValue("Precio");
        headerSale.setCellValue("Sale");
        headerStock.setCellValue("Stock");
        headerCuantas.setCellValue("Cantidad");
        headerTotal.setCellValue("Total: ");
        totalCards.setCellType(CellType.FORMULA);
        totalValue.setCellType(CellType.FORMULA);
        
        if(this.withImages){
        	totalCards.setCellFormula("SUBTOTAL(109, H:H)");
        	totalValue.setCellFormula("SUBTOTAL(109, I:I)");
        }
        else{
        	totalCards.setCellFormula("SUBTOTAL(109, G:G)");
        	totalValue.setCellFormula("SUBTOTAL(109, H:H)");
        }

		CellStyle style = excel.createCellStyle();
        Font font = excel.createFont();
        font.setColor(HSSFColor.WHITE.index);
        style.setFont(font);
        
        CellStyle hlink_style = excel.createCellStyle();
        Font hlink_font = excel.createFont();
        hlink_font.setUnderline(Font.U_SINGLE);
        hlink_font.setColor(HSSFColor.BLUE.index);
        hlink_style.setFont(hlink_font);
        
        int count = 0;
        
		for (CardRow card : cards) {
			
			count++;
			
			Row fila = hoja.createRow(count);
	        Cell celdaId = fila.createCell(initialColumn);
	        Cell celdaRarity = fila.createCell(initialColumn + 1);
	        Cell celdaColor = fila.createCell(initialColumn + 2);
	        Cell celdaPrice = fila.createCell(initialColumn + 3);
       	 	Cell celdaSale = fila.createCell(initialColumn + 4);
       	 	Cell celdaStock = fila.createCell(initialColumn + 5);
       	 	Cell celdaCuantas = fila.createCell(initialColumn + 6);
       	 	Cell celdaTotal = fila.createCell(initialColumn + 7);
       	 	
       	 	HSSFHyperlink url_link = (HSSFHyperlink) helper.createHyperlink(HSSFHyperlink.LINK_URL);
            url_link.setAddress(card.url);
            celdaId.setHyperlink(url_link);
	        celdaId.setCellValue(card.cardId);
            celdaId.setCellStyle(hlink_style);
	        
	        celdaRarity.setCellValue(card.rarity);
            celdaColor.setCellValue(card.color);
       	 	celdaPrice.setCellValue(Double.parseDouble(card.price));
	        celdaSale.setCellValue(card.sale);
	        celdaStock.setCellValue(card.stock);
	        celdaCuantas.setCellValue(this.initialCant);
	        if(this.withImages){
		        celdaTotal.setCellFormula("E"  + (count + 1) + " * H" + (count + 1));		        
	        }
	        else{
		        celdaTotal.setCellFormula("D"  + (count + 1) + " * G" + (count + 1));
	        }
	        celdaTotal.setCellStyle(style);
	        
	        if(this.withImages){
		        fila.setHeightInPoints(66);
		        ClientAnchor anchor = helper.createClientAnchor();
	
		        anchor.setCol1(0);
		        anchor.setCol2(1);
		        anchor.setRow1(count);
		        anchor.setRow2(count + 1);
	
		        byte[] imgBytes = this.getCardImage(card.imgUrl);
		        
		        int pictureIndex = excel.addPicture(imgBytes, Workbook.PICTURE_TYPE_PNG);
		        drawing.createPicture(anchor, pictureIndex);
	        }
	        
	        this.progress = (float)(((float)count / (float)cards.size()) * 100);
	        
			if(local){
				System.out.println(this.progress + "% by card");
			}
		}

		hoja.setAutoFilter(new CellRangeAddress(0, 0, initialColumn, initialColumn + 6));
		
		for(int i = initialColumn; i <= initialColumn + 5; i++){
	        hoja.autoSizeColumn(i);
	        hoja.setColumnWidth(i, hoja.getColumnWidth(i) + 700);
		}

		hoja.autoSizeColumn(initialColumn + 6);
		hoja.createFreezePane(0, 1);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			excel.write(bos);
		} finally {
			excel.close();
		    bos.close();
		}
		
		byte[] bytes = bos.toByteArray();
		
		return bytes;
	}
	
	public float getProgress(){
		return this.progress;
	}

	public byte[] getCardImage(String url) throws Exception{
		
		URL imageUrl = new URL(url);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = null;
		
		is = imageUrl.openStream ();
		byte[] byteChunk = new byte[4096];
		int n;

		while ( (n = is.read(byteChunk)) > 0 ) {
			baos.write(byteChunk, 0, n);
		}
		is.close();
		
		byte[] bytes = baos.toByteArray();
		
		return bytes;
	}
	
}
