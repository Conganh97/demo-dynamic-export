package com.example.excelexport.service;

import java.util.Map;

@FunctionalInterface
public interface ExcelDataProvider {
    Map<String, Object> generateData(Map<String, Object> parameters);
}
