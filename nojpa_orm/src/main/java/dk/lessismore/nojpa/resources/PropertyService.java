/*
 * Created : on May 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package dk.lessismore.nojpa.resources;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sebastian
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PropertyService {

	private static final Logger log =
		Logger.getLogger(PropertyService.class);

	private static PropertyService instance = null;
	private static Map propertyResourcesNames = new HashMap();

	private PropertyService() {
	}

	public static PropertyService getInstance() {
		if (instance == null) {
			instance = new PropertyService();
		}
		return instance;
	}

	public void reload() {
		propertyResourcesNames.clear();
	}

	public PropertyResources getPropertyResources(Class myClass) {
		// See if the propertykey exists
		String className =
			myClass.getName().substring(myClass.getName().lastIndexOf(".") + 1);
		if (!propertyResourcesNames.containsKey(className)) {
			PropertyResources pr = new PropertyResources(className);

			propertyResourcesNames.put(className, pr);
		}
        log.info("Read " + propertyResourcesNames + ", class=" + className);
        return (PropertyResources) propertyResourcesNames.get(className);
	}

	public PropertyResources getPropertyResources(String className) {
		// See if the propertykey exists
		if (!propertyResourcesNames.containsKey(className)) {
			PropertyResources pr = new PropertyResources(className);

			propertyResourcesNames.put(className, pr);
		}
		return (PropertyResources) propertyResourcesNames.get(className);
	}

	private String getLanguageString(String original, int language) {
		int counter = 0;
		if (original != null) {
			for (int i = 0; i < language; i++) {
				counter = original.indexOf("~", counter) + 1;
			}
		}
		int end =
			(original.indexOf("~", counter) != -1
				? original.indexOf("~", counter)
				: original.length());
		//log.debug("end = " + end);
		//log.debug("counter = " + counter);
		//log.debug("ori = " + original);
		return original.substring(counter, end);
	}

	/**
	 * 
	 *  Use URLEncoder.encode(Tysk, "utf-8")
	 */
/*
	public void writePropertyResourcesToFile(
		File fileToRead,
		File fileToSave,
		int inLanguage,
		int outLanguage,
		String isoCode)
		throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(fileToRead));
		Writer writer = new FileWriter(fileToSave);

		while (reader.ready()) {
			String curLine = reader.readLine();
			if (curLine != null) {
				writer.write(
					"--------------------------------------------------\n");
				writer.write(
					"id="
						+ curLine.substring(
							0,
							(curLine.indexOf("=") != -1
								? curLine.indexOf("=")
								: curLine.length()))
						+ "\n");
				writer.write(
					"------------- ORIGINAL ---------------------------\n");
				writer.write(
					URLDecoder.decode(
						getLanguageString(curLine, inLanguage) + "\n",
						isoCode));
				writer.write(
					"------------- TYPE HERE --------------------------\n");
				writer.write(
					"{"
						+ URLDecoder.decode(
							getLanguageString(curLine, outLanguage) + "}\n",
							isoCode));
				writer.write(
					"--------------------------------------------------\n");
				writer.write(
					"--------------------------------------------------\n");
				writer.write("\n\n");
				writer.flush();
			}
		}
		writer.close();
	}

	public void saveFileToPropertyResources(
		File fileToRead,
		PropertyResources pr,
		String isoCode)
		throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(fileToRead));
		String curLine = "";
		String value = "";
		String tmp = "";
		String id = "";
		while (reader.ready()) {
			curLine = reader.readLine();
			if (curLine != null) {
				if (curLine.indexOf("id=") != -1) {
					id = curLine.substring(curLine.indexOf("id=") + 3);

					while (value.indexOf("{") == -1) {
						value = reader.readLine();
					}
					while (value.indexOf("}") == -1) {
						value = value + reader.readLine();
					}

					// Remove { and } from the string
					value = value.substring(1, value.length() - 1);
					log.debug("Found key: " + id + " " + " with value: " + value);
					tmp = (String) pr.getString(id);
					if(tmp != null){
					    log.debug("4 - old value$ " + tmp);
					    value = tmp.substring(0, tmp.lastIndexOf("~")) + "~" + URLEncoder.encode(value, isoCode);
					    log.debug("5 - new value$ " + value);
					    pr.setString(id, value);
					}
				}
			}
		}
	}
*/	
}

