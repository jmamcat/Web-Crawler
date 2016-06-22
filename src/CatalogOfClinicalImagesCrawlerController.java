import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class CatalogOfClinicalImagesCrawlerController {
	private static final Logger logger = LoggerFactory.getLogger(MyImageCrawlerController.class);

	public static void main(String[] args) throws Exception {
		String[] myArgs = { "InterData", "1", "data_catalogOfClinicalImages" };
		args = myArgs;

		if (args.length < 3) {
			logger.info("Needed parameters: ");
			logger.info("\t rootFolder (it will contain intermediate crawl data)");
			logger.info("\t numberOfCralwers (number of concurrent threads)");
			logger.info("\t storageFolder (a folder for storing downloaded images)");
			return;
		}

		// I. Setting up basic parameters
		String rootFolder = args[0];
		int numberOfCrawlers = Integer.parseInt(args[1]);
		String storageFolder = args[2];

		CrawlConfig config = new CrawlConfig();

		config.setCrawlStorageFolder(rootFolder);

		// Since images are binary content,
		// we need to set this parameter to true to make sure they are included in the crawl.
		config.setIncludeBinaryContentInCrawling(true);

		String[] crawlDomains = { "https://meded.ucsd.edu/clinicalimg/skin.htm" };

		// II. Crawling all links
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

		for (String domain : crawlDomains) {
			controller.addSeed(domain);
		}

		// Running Crawler
		CatalogOfClinicalImagesListingCrawler.configure(crawlDomains, storageFolder);
		controller.start(CatalogOfClinicalImagesListingCrawler.class, numberOfCrawlers);

		// III. Crawling all Content
		System.out.println("Starting nested crawling");

		List<String> dermPages = CatalogOfClinicalImagesListingCrawler.dermPages;

		pageFetcher = new PageFetcher(config);
		robotstxtConfig = new RobotstxtConfig();
		robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		controller = new CrawlController(config, pageFetcher, robotstxtServer);

		Collections.sort(dermPages);

		System.out.println("Check " + dermPages.size());

		List<String> comp = new ArrayList<>();

		// Seeding all links
		for (String domain : dermPages.subList(0, 3)) {
			System.out.println("Adding " + domain);
			controller.addSeed(domain);
			comp.add(domain);
		}

		// Running Crawler
		CatalogOfClinicalImagesPageCrawler.configure(comp, storageFolder);
		// controller.start(CatalogOfClinicalImagesPageCrawler.class, numberOfCrawlers);
	}
}
