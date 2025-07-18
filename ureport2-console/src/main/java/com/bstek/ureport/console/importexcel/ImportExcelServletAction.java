/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.bstek.ureport.console.importexcel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.bstek.ureport.console.RenderPageServletAction;
import com.bstek.ureport.console.cache.TempObjectCache;
import com.bstek.ureport.definition.ReportDefinition;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * @author Jacky.gao
 * @since 2017年5月25日
 */
public class ImportExcelServletAction extends RenderPageServletAction {
    private List<ExcelParser> excelParsers = new ArrayList<ExcelParser>();

    public ImportExcelServletAction() {
        excelParsers.add(new HSSFExcelParser());
        excelParsers.add(new XSSFExcelParser());
    }

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        ReportDefinition report = null;
        String errorInfo = null;
        try {
            if(req instanceof MultipartHttpServletRequest){
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) req;
                Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
                for (MultipartFile file : fileMap.values()) {
                    if (file != null && !file.isEmpty()) {
                        String fieldName = file.getOriginalFilename();
                        String name = file.getName().toLowerCase();
                        if ("_excel_file".equals(fieldName) && (name.endsWith(".xls") || name.endsWith(".xlsx"))) {
                            InputStream inputStream = file.getInputStream();
                            for (ExcelParser parser : excelParsers) {
                                if (parser.support(name)) {
                                    report = parser.parse(inputStream);
                                    break;
                                }
                            }
                            inputStream.close();
                            break;
                        }
                    }
                }
            }
            errorInfo = "请选择一个合法的Excel导入";
        } catch (Exception e) {
            e.printStackTrace();
            errorInfo = e.getMessage();
        }
        Map<String, Object> result = new HashMap<String, Object>();
        if (report != null) {
            result.put("result", true);
            TempObjectCache.putObject("classpath:template/template.ureport.xml", report);
        } else {
            result.put("result", false);
            if (errorInfo != null) {
                result.put("errorInfo", errorInfo);
            }
        }
        writeObjectToJson(resp, result);
    }

    @Override
    public String url() {
        return "/import";
    }
}
