package fr.simple.edm.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ClearValuesResponse;
import com.google.api.services.sheets.v4.model.CopyPasteRequest;
import com.google.api.services.sheets.v4.model.DuplicateSheetRequest;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import fr.simple.edm.config.GoogleSheetImporterConfiguration;
import fr.simple.edm.config.GoogleSheetImporterConfiguration.Sheet;
import fr.simple.edm.service.exception.SheetNotExistsException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoogleSheetUtils {

    private Sheets service;

    @Autowired
    public GoogleSheetUtils(GoogleCredentials googleCredentials) throws GeneralSecurityException, IOException {
        service = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(googleCredentials)
                ).build();
    }

    public void clearRange(String spreadsheetId, String range) throws IOException {
        ClearValuesRequest requestBody = new ClearValuesRequest();
        Sheets.Spreadsheets.Values.Clear request = service.spreadsheets().values().clear(spreadsheetId, range, requestBody);
        ClearValuesResponse response = request.execute();
        log.info("Sheet have been cleared : {}", response);
    }

    private com.google.api.services.sheets.v4.model.Sheet getSheetByName(String spreadsheetId, String sheetName)
            throws SheetNotExistsException, IOException {
            Optional<com.google.api.services.sheets.v4.model.Sheet> sheetId = service.spreadsheets().get(spreadsheetId).execute().getSheets().stream()
                .filter(s -> s.getProperties().getTitle().equalsIgnoreCase(sheetName))
                .findFirst();

            log.info("Found smartInsertSheetId for {} = {}", sheetName, sheetId);
            if (!sheetId.isPresent()) {
                throw new SheetNotExistsException();
            }
            return sheetId.get();
    }

    public void writeValues(String spreadsheetId, String range, List<List<Object>> values) throws IOException {
        ValueRange requestBody = new ValueRange();
        requestBody.setValues(values);

        Sheets.Spreadsheets.Values.Update request = service.spreadsheets().values()
            .update(spreadsheetId, range, requestBody);
        request.setValueInputOption("USER_ENTERED");
        UpdateValuesResponse response = request.execute();
        log.info("Data have been wrote : {}", response);
    }

    public void executeCopyPasteRequest(String spreadsheetId, GoogleSheetImporterConfiguration.Sheet source,
            GoogleSheetImporterConfiguration.Sheet destination, String pasteType)
            throws IOException, SheetNotExistsException {
            CopyPasteRequest extendFormula = new CopyPasteRequest();
            extendFormula.setSource(
                    new GridRange()
                    .setSheetId(getSheetByName(spreadsheetId, source.getName()).getProperties().getSheetId())
                    .setStartRowIndex(Integer.valueOf(source.getFirstRow()))
                    .setEndRowIndex(Integer.valueOf(source.getLastRow()) + 1)
                    .setStartColumnIndex(Integer.valueOf(source.getFirstCol()))
                    .setEndColumnIndex(Integer.valueOf(source.getLastCol()) + 1)
                    );
            extendFormula.setDestination(
                    new GridRange()
                    .setSheetId(getSheetByName(spreadsheetId, destination.getName()).getProperties().getSheetId())
                    .setStartRowIndex(Integer.valueOf(destination.getFirstRow()))
                    .setEndRowIndex(Integer.valueOf(destination.getLastRow()) + 1)
                    .setStartColumnIndex(Integer.valueOf(destination.getFirstCol()))
                    .setEndColumnIndex(Integer.valueOf(destination.getLastCol()) + 1)
                    );
            extendFormula.setPasteType(pasteType);

            List<Request> requests = new ArrayList<>();
            requests.add(new Request().setCopyPaste(extendFormula));

            BatchUpdateSpreadsheetRequest updatingRequestBody = new BatchUpdateSpreadsheetRequest();
            updatingRequestBody.setRequests(requests);

            Sheets.Spreadsheets.BatchUpdate updatingRequest = service.spreadsheets()
                .batchUpdate(spreadsheetId, updatingRequestBody);
            BatchUpdateSpreadsheetResponse updatingResponse = updatingRequest.execute();

            log.info("Data have been updated : {}", updatingResponse);
    }

    public boolean sheetExists(String spreadsheetId, String sheetName) throws IOException {
        return service.spreadsheets().get(spreadsheetId).execute().getSheets().stream()
            .anyMatch(s -> s.getProperties().getTitle().equalsIgnoreCase(sheetName));
    }

    public void duplicateSheet(String spreadsheetId, Sheet source, String targetName)
            throws SheetNotExistsException, IOException {

            com.google.api.services.sheets.v4.model.Sheet sourceSheet = getSheetByName(spreadsheetId, source.getName());

            DuplicateSheetRequest requestBody = new DuplicateSheetRequest();
            requestBody.setNewSheetName(targetName);
            requestBody.setSourceSheetId(sourceSheet.getProperties().getSheetId());
            requestBody.setInsertSheetIndex(sourceSheet.getProperties().getIndex() + 1);

            List<Request> requests = new ArrayList<>();
            requests.add(new Request().setDuplicateSheet(requestBody));

            BatchUpdateSpreadsheetRequest batchUpdateSpreadsheetRequest = new BatchUpdateSpreadsheetRequest();
            batchUpdateSpreadsheetRequest.setRequests(requests);
            Sheets.Spreadsheets.BatchUpdate request = service.spreadsheets().batchUpdate(spreadsheetId, batchUpdateSpreadsheetRequest);

            BatchUpdateSpreadsheetResponse response = request.execute();

            log.info("sheet duplicated : {}", response);
    }
}

