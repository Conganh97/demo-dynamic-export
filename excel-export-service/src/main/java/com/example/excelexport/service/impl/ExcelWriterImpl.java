package com.example.excelexport.service.impl;

import com.example.excelexport.service.ExcelWriter;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

@Service
public class ExcelWriterImpl implements ExcelWriter {
    
    @Override
    public void writeFromTemplate(OutputStream outStream, String templateName, Map<String, Object> data) {
        try {
            InputStream inputStream = readResourceFile(templateName);
            Context context = new Context();
            for (Map.Entry<String, Object> element : data.entrySet()) {
                context.putVar(element.getKey(), element.getValue());
            }
            JxlsHelper.getInstance().processTemplate(inputStream, outStream, context);
        } catch (IOException e) {
            throw new RuntimeException("Error processing Excel template: " + templateName, e);
        } finally {
            closeAndFlushOutput(outStream);
        }
    }

    @Override
    public byte[] writeFromTemplateToByte(String pathTemplate, Map<String, Object> data) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writeFromTemplate(outputStream, pathTemplate, data);
        return outputStream.toByteArray();
    }

    private InputStream readResourceFile(String templateName) throws IOException {
        Resource resource = new ClassPathResource(templateName);
        if (!resource.exists()) {
            throw new IOException("Template file not found: " + templateName);
        }
        return resource.getInputStream();
    }

    private void closeAndFlushOutput(OutputStream outStream) {
        try {
            outStream.flush();
            outStream.close();
        } catch (IOException exception) {
            // Log the exception but don't throw it
        }
    }
}
