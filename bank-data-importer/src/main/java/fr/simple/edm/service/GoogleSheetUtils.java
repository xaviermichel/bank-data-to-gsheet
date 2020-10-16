package fr.simple.edm.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ClearValuesResponse;
import com.google.api.services.sheets.v4.model.CopyPasteRequest;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import fr.simple.edm.GoogleSheetImporterConfiguration;
import fr.simple.edm.service.exception.SheetNotExistsException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoogleSheetUtils {

	private Sheets service;

	@Autowired
	public GoogleSheetUtils(GoogleCredential googleCredential, JacksonFactory jacksonFactory) throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		service = new Sheets.Builder(HTTP_TRANSPORT, jacksonFactory, googleCredential).build();
	}

	public void clearRange(String spreadsheetId, String range) throws IOException {
		ClearValuesRequest requestBody = new ClearValuesRequest();
		Sheets.Spreadsheets.Values.Clear request = service.spreadsheets().values().clear(spreadsheetId, range, requestBody);
		ClearValuesResponse response = request.execute();

		log.info("Sheet have been cleared : {}", response);
	}

	public Integer getSheetIdBySheetName(String spreadsheetId, String sheetName) throws SheetNotExistsException, IOException {
		Optional<Integer> sheetId = service.spreadsheets().get(spreadsheetId).execute().getSheets().stream()
				.filter(s -> s.getProperties().getTitle().equalsIgnoreCase(sheetName))
				.map(s -> s.getProperties().getSheetId())
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

		Sheets.Spreadsheets.Values.Update request = service.spreadsheets().values().update(spreadsheetId, range, requestBody);
		request.setValueInputOption("USER_ENTERED");
		UpdateValuesResponse response = request.execute();
		log.info("Data have been wrote : {}", response);
	}

	public void executeCopyPasteRequest(String spreadsheetId, GoogleSheetImporterConfiguration.Sheet source, GoogleSheetImporterConfiguration.Sheet destination, String pasteType) throws IOException, SheetNotExistsException {
		CopyPasteRequest extendFormula = new CopyPasteRequest();
		extendFormula.setSource(
				new GridRange()
						.setSheetId(getSheetIdBySheetName(spreadsheetId, source.getName()))
						.setStartRowIndex(Integer.valueOf(source.getFirstRow()))
						.setEndRowIndex(Integer.valueOf(source.getLastRow()) + 1)
						.setStartColumnIndex(Integer.valueOf(source.getFirstCol()))
						.setEndColumnIndex(Integer.valueOf(source.getLastCol()) + 1)
		);
		extendFormula.setDestination(
				new GridRange()
						.setSheetId(getSheetIdBySheetName(spreadsheetId, destination.getName()))
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

		Sheets.Spreadsheets.BatchUpdate updatingRequest = service.spreadsheets().batchUpdate(spreadsheetId, updatingRequestBody);
		BatchUpdateSpreadsheetResponse updatingResponse = updatingRequest.execute();

		log.info("Data have been updated : {}", updatingResponse);
	}
}
