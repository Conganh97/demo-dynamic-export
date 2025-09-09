package com.etc.qldtlxapi.common.excel.impl;

import com.etc.qldtlxapi.common.excel.ExcelWriter;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

@Component
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
            throw new RuntimeException(e);
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
        return resource.getInputStream();
    }

    private void closeAndFlushOutput(OutputStream outStream) {
        try {
            outStream.flush();
            outStream.close();
        } catch (IOException exception) {
        }
    }
}
