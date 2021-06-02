package fr.simple.edm.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.simple.edm.config.GoogleSheetImporterConfiguration;
import fr.simple.edm.domain.AccountOperation;
import fr.simple.edm.service.exception.SheetNotExistsException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class GoogleSheetTranslator {

	private final GoogleSheetImporterConfiguration googleSheetImporterConfiguration;

	private final GoogleSheetUtils googleSheetUtils;

	@Autowired
	public GoogleSheetTranslator(GoogleSheetImporterConfiguration googleSheetImporterConfiguration, GoogleSheetUtils googleSheetUtils) throws GeneralSecurityException, IOException {
		this.googleSheetImporterConfiguration = googleSheetImporterConfiguration;
		this.googleSheetUtils = googleSheetUtils;
	}

	private Object emptyIfNull(Object o) {
		if (StringUtils.isEmpty(o)) {
			return "";
		}
		return o;
	}

	public void clear() throws IOException {
		log.info("Start of clear");
		googleSheetUtils.clearRange(googleSheetImporterConfiguration.getSpreadsheetId(), googleSheetImporterConfiguration.getSmartInsertSheet().getRange());
		googleSheetUtils.clearRange(googleSheetImporterConfiguration.getSpreadsheetId(), googleSheetImporterConfiguration.getNextMonthSheet().getRange());
		log.info("End of clear");
	}


	public void reloadData(List<AccountOperation> operations) throws IOException, GeneralSecurityException, SheetNotExistsException {
		log.info("Start of reloadData");

		// construct request
		List<List<Object>> values = new ArrayList<>();
		for (AccountOperation operation : operations) {
			List<Object> operationValues = Arrays.asList(
					emptyIfNull(operation.getDate()),
					emptyIfNull(operation.getLabel()),
					emptyIfNull(operation.getDebitValue()),
					emptyIfNull(operation.getCreditValue()),
					emptyIfNull(operation.getAccountLabel())
			);
			values.add(operationValues);
		}

		googleSheetUtils.writeValues(googleSheetImporterConfiguration.getSpreadsheetId(), googleSheetImporterConfiguration.getSmartInsertSheet().getRange(), values);

		log.info("Start of filling auto categorisation");

		googleSheetUtils.executeCopyPasteRequest(
				googleSheetImporterConfiguration.getSpreadsheetId(),
				googleSheetImporterConfiguration.getSmartInsertSheetFormulaSource(),
				googleSheetImporterConfiguration.getSmartInsertSheetFormulaDestination(),
				"PASTE_FORMULA"
		);

		log.info("Start of copying to next month sheet");

		googleSheetUtils.executeCopyPasteRequest(
				googleSheetImporterConfiguration.getSpreadsheetId(),
				googleSheetImporterConfiguration.getSmartInsertSheetFullRangeCopy(),
				googleSheetImporterConfiguration.getNextMonthSheetPaste(),
				"PASTE_VALUES"
		);

		log.info("End of reloadData");
	}

}
