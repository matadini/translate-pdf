package pl.matadini.translatepdf.config;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Configuration {

	private String defaultSourceLanguage = "en";
	private String defaultTargetLanguage = "en";
	private String googleCredentialsJsonPath = "";//C://Prywatne//Translator PDF-a0031a62afce.json";

}