package fr.simple.edm.service.csvreader;

import fr.simple.edm.domain.AccountOperation;

import java.util.List;

public interface BankDataTranslator {

	List<AccountOperation> fileToAccountOperations(String file) throws Exception;

}
