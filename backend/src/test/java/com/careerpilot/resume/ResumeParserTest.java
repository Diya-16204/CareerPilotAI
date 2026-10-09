package com.careerpilot.resume;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.io.ByteArrayOutputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResumeParserTest {
    private final ResumeParser parser=new ResumeParser();

    @Test void extractsTextContactAndSkillsFromPdf() throws Exception {
        byte[] bytes;
        try(PDDocument document=new PDDocument();ByteArrayOutputStream output=new ByteArrayOutputStream()){
            document.addPage(new PDPage());
            try(PDPageContentStream stream=new PDPageContentStream(document,document.getPage(0))){stream.beginText();stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA),12);stream.newLineAtOffset(50,700);stream.showText("Sam Example sam@example.com Java Spring Boot PostgreSQL");stream.endText();}
            document.save(output);bytes=output.toByteArray();
        }
        var result=parser.parse(bytes,ResumeParser.Format.PDF);
        assertThat(result.text()).contains("Sam Example");
        assertThat(result.email()).isEqualTo("sam@example.com");
        assertThat(result.skills()).contains("Java","Spring Boot","PostgreSQL");
    }

    @Test void extractsTextFromDocx() throws Exception {
        byte[] bytes;
        try(XWPFDocument document=new XWPFDocument();ByteArrayOutputStream output=new ByteArrayOutputStream()){
            document.createParagraph().createRun().setText("Taylor Person\nReact TypeScript");document.write(output);bytes=output.toByteArray();
        }
        assertThat(parser.parse(bytes,ResumeParser.Format.DOCX).skills()).contains("React","TypeScript");
    }

    @Test void rejectsEmptyTextDocument(){
        assertThatThrownBy(()->parser.parse(new byte[0],ResumeParser.Format.DOCX)).isInstanceOf(ResponseStatusException.class);
    }
}
