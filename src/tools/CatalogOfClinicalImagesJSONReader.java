package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class CatalogOfClinicalImagesJSONReader {
	public static final File dir = new File("data_catalogOfClinicalImages");
	public static final File imgDir = new File(dir.getPath() + "\\Images");
	public static final File txtDir = new File(dir.getPath() + "\\Text");

	public static void main(String args[]) throws Exception {
		run();
	}

	public static void run() throws Exception {
		DBProcTools.startConnection();

		String directory;
		String name;
		String seedUrl;

		String rawTextInfo;

		String pathUrl;
		String url;
		String anchor;

		// List<String> path = new ArrayList<String>();
		String path;

		String description;
		String diagnosis;

		int dataSourceID;
		int pathUrlID;
		int pageID;
		int imageID;
		int diagnosisID;

		name = "Catalog of Clinical Images";
		seedUrl = "https://meded.ucsd.edu/clinicalimg/skin.htm";
		directory = imgDir.getPath();
		dataSourceID = DBProcTools.insertUniqueRecord("DataSource", "dataSourceID", new String[] { "name", "seedUrl", "directory", name, seedUrl, directory });

		// System.out.println(dataSourceID);

		for (String fileName : txtDir.list()) {
			JSONObject jsonObj = loadJSONFile(txtDir.getPath(), fileName);
			JSONObject webUrlInfo = (JSONObject) jsonObj.get("WebURLInfo");
			JSONObject imgInfo = (JSONObject) jsonObj.get("ImgInfo");
			JSONObject txtInfo = (JSONObject) jsonObj.get("TextInfo");

			pathUrl = "";
			pathUrl = webUrlInfo.getString("PathURL");
			pathUrlID = DBProcTools.insertUniqueRecord("PathURL", "pathUrlID", new String[] { "pathUrl", pathUrl });

			System.out.println(pathUrlID);

			url = "";
			url = webUrlInfo.getString("URL");

			anchor = "";
			anchor = webUrlInfo.getString("Anchor").replaceAll("[\\s]+", " ");

			description = "";
			String strArr[] = ((String) txtInfo.get("Description")).split(":");
			if (strArr.length == 2)
				description = strArr[1].trim();
			else if (strArr.length == 1)
				description = strArr[0].trim();
			else
				description = "ERROR";

			rawTextInfo = jsonObj.toString();

			pageID = DBProcTools.insertUniqueRecord("Page", "pageID", new String[] { "url", "anchor", "description", "rawTextInfo", "pathUrlID", "dataSourceID", url, anchor, description, rawTextInfo, String.valueOf(pathUrlID), String.valueOf(dataSourceID) });

			System.out.println(pageID);

			JSONArray imgList = (JSONArray) imgInfo.get("Images");
			String img;
			for (int i = 0; i < imgList.length(); ++i) {
				img = (String) imgList.get(i);

				path = "";
				// path.add(img);
				boolean imageExists = new File(imgDir.getPath() + "\\" + img).exists();

				if (imageExists)
					path = img;
				// path.add(img);
				else
					path = "ERROR";
				// path.add("ERROR");

				imageID = DBProcTools.insertUniqueRecord("Image", "imageID", new String[] { "path", "captureType", "pageID", path, "unknown", String.valueOf(pageID) });

				// System.out.println(imageID);
				// System.out.println(imageExists);
			}
			// path.clear();

			diagnosis = "";
			diagnosis = ((String) txtInfo.get("Diagnosis")).replaceAll("[:.]", "").trim();

			diagnosisID = DBProcTools.insertUniqueRecord("Diagnosis", "diagnosisID", new String[] { "diagnosis", diagnosis });
			// System.out.println(diagnosisID);

			DBProcTools.insertRecord("PageDiagnosisLink", new String[] { "pageID", "diagnosisID", String.valueOf(pageID), String.valueOf(diagnosisID) });

			// System.out.println();
		}

		DBProcTools.closeConnection();
	}

	public static JSONObject loadJSONFile(String dirName, String fileName) throws IOException {
		FileInputStream fis = new FileInputStream(new File(dirName + "\\" + fileName));

		// Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		String line = null;

		StringBuilder sb = new StringBuilder();

		while ((line = br.readLine()) != null) {
			sb.append(line);
			sb.append(' ');
		}

		br.close();

		JSONObject jsonObj = new JSONObject(sb.toString());
		return jsonObj;
	}
}
