package fr.simple.edm.config;

import java.util.List;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;

@ConfigurationProperties(prefix = "ca")
@Data
@Primary
public class CAConfiguration {

	private String csvFilePath;

	private List<String> accountsLabel;

}
