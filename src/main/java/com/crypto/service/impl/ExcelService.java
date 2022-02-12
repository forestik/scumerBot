package com.crypto.service.impl;

import com.crypto.dto.TokensLeftMessageDto;
import com.crypto.service.DefaultService;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Service
public class ExcelService implements DefaultService {

    private final Sheets sheets;

    private final String range;

    private final String spreadsheetId;

    public ExcelService(Sheets sheets,
                        @Value("${sheets.range}") String range,
                        @Value("${sheets.spreadsheetId}") String spreadsheetId) {
        this.sheets = sheets;
        this.range = range;
        this.spreadsheetId = spreadsheetId;
    }

    public List<TokensLeftMessageDto> getTokensInfo(String query) throws IOException {
        ValueRange response = sheets.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        List<TokensLeftMessageDto> tokensLeftMessageDtoList = new LinkedList<>();
        values.stream().filter(row -> row.get(0).toString().contains(query) || row.get(10).toString().contains(query))
                .forEach(row -> tokensLeftMessageDtoList
                        .add(new TokensLeftMessageDto(getEnrichedString(row.get(0).toString(), 14),
                                getEnrichedString(row.get(10).toString(), 8),
                                getEnrichedString(row.get(14).toString(), 8))));
        return tokensLeftMessageDtoList;
    }

    public String getExcelUrl() {
        Spreadsheet spreadsheet = null;
        try {
            spreadsheet = sheets.spreadsheets().get(spreadsheetId).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return spreadsheet != null ? spreadsheet.getSpreadsheetUrl() : "";

    }
}
