package com.careerpilot.resume;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class ResumeParser {
    private static final List<String> SKILLS=List.of("Java","Spring Boot","JavaScript","TypeScript","React","Python","SQL","PostgreSQL","REST APIs","Docker","Kubernetes","AWS","Git","Playwright","Node.js","HTML","CSS","Figma","Data Analysis","Machine Learning","Communication","Project Management");
    private static final Pattern EMAIL=Pattern.compile("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b");
    private static final Pattern PHONE=Pattern.compile("(?<!\\w)(?:\\+?\\d[\\d .()\\-]{7,20}\\d)(?!\\w)");

    public Parsed parse(byte[] bytes,Format format){
        try {
            String text;
            if(format==Format.PDF){try(PDDocument document=Loader.loadPDF(bytes)){if(document.isEncrypted())throw new IllegalArgumentException();text=new PDFTextStripper().getText(document);}}
            else {try(XWPFDocument document=new XWPFDocument(new ByteArrayInputStream(bytes));XWPFWordExtractor extractor=new XWPFWordExtractor(document)){text=extractor.getText();}}
            text=text.replace("\u0000","").trim();if(text.isBlank())throw new IllegalArgumentException();
            String email=first(EMAIL,text);String phone=first(PHONE,text);
            String normalized=text.toLowerCase(Locale.ROOT);
            List<String> found=SKILLS.stream().filter(skill->Pattern.compile("(?i)(?<![a-z0-9])"+Pattern.quote(skill)+"(?![a-z0-9])").matcher(normalized).find()).toList();
            return new Parsed(text,email,phone,found);
        }catch(Exception exception){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"This file could not be read. Upload an unencrypted, text-based PDF or DOCX and review the extracted details.");}
    }
    private String first(Pattern pattern,String text){var matcher=pattern.matcher(text);return matcher.find()?matcher.group().trim():null;}
    public enum Format { PDF,DOCX }
    public record Parsed(String text,String email,String phone,List<String> skills){}
}
