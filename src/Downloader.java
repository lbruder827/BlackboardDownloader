/*
 * Author: Lucas Bruder
 * File: Downloader.java
 * Last modified: May 7, 2015
 * 
 * Used to download all course material for blackboard to assist finals studying.
 * 
 * How it works:
 * 1. When run, it will open a terminal/GUI where you input your username and password.
 * 2. After that, it will find all of your courses that are located on your home page.
 * 3. For each course, it will download all files(.pdf, .doc, .docx, .ppt, .pptx, etc.)
 *    ignoring the Announcements and Tools section on the left side.
 * 
 * 
 * 
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * 
 * @author lbruder
 *
 */
public class Downloader {

	// Username and password to login into blackboard
	private final static String USERNAME = "lbruder";
	private final static String PASSWORD = "";

	// Blackboard related links, frames, and butons
	private final static String BLACKBOARD_URL = "https://blackboard.andrew.cmu.edu";
	private final static String CONTENT_FRAME = "contentFrame";

	// Max time in milliseconds to wait for the page to load
	private final static int MAX_WAIT = 3 * 1000;

	// List of accepted extensions to download
	private static ArrayList<String> extensions = new ArrayList<String>();

	private static ChromeDriver driver = new ChromeDriver();

	// OS Specific fields like the OS and location of download folder
	private static String OS = System.getProperty("os.name");
	private static File DOWNLOAD_FOLDER;
	private static File DOWNLOAD_DIR;
	private static String DOWNLAD_DIR_NAME = "bb_download_";

	public static void main(String[] args) throws InterruptedException {
		boolean worked;
		// get list of accepted extensions
		buildExtensionList();

		// Setup OS specific fields like download folder locations
		if (OS.contains("Windows")) {
			// Default download folder for windows
			DOWNLOAD_FOLDER = new File("C:/Users/"
					+ System.getProperty("user.name") + "/Downloads/");
		} else if (OS.contains("Mac")) {
			DOWNLOAD_FOLDER = new File("/Users/"
					+ System.getProperty("user.name") + "/Downloads/");
		}

		// Make sure that the download folder is a thing
		if (!DOWNLOAD_FOLDER.isDirectory()) {
			System.out.println("Not a directory");
			driver.quit();
			return;
		}

		// Go to blackboard website which will take us to the login page for CMU
		driver.get(BLACKBOARD_URL);

		// Login with username and password and ensure it worked
		worked = login(USERNAME, PASSWORD, driver);
		if (!worked) {
			System.out
					.println("Error logging into blackboard. Please try again");
			driver.quit();
			return;
		}

		// Create download folder
		DOWNLOAD_DIR = new File(DOWNLOAD_FOLDER.getAbsolutePath() + "/"
				+ DOWNLAD_DIR_NAME + USERNAME);
		DOWNLOAD_DIR.mkdir();

		// Get the files before downloading
		List<File> files_before = Arrays.asList(DOWNLOAD_FOLDER.listFiles());

		// Switch to the frame w/ the main content and find the place where the
		// courses are listed
		driver.switchTo().frame(CONTENT_FRAME);
		List<WebElement> allElements = driver.findElements(By
				.xpath("//div[@id='_4_1termCourses_noterm']/ul/li"));

		// Get the links to all the courses
		HashMap<String, String> courseLinks = find_course_links(allElements);

		for (String classname : courseLinks.keySet()) {
			navigate_course(classname, courseLinks.get(classname));
		}

		List<File> files_after = Arrays.asList(DOWNLOAD_FOLDER.listFiles());

		driver.quit();

	}

	/**
	 * 
	 * @param classname
	 *            name of class
	 * @param link
	 *            to class website
	 * @throws InterruptedException
	 *             sure
	 */
	private static void navigate_course(String classname, String link)
			throws InterruptedException {

		System.out.println("Opening class " + classname);

		// Open the course in a new window
		ChromeDriver c = new ChromeDriver();
		setupAutomaticDownloads(c);
		c.get(link);
		// Wait for it to load
		Thread.sleep(MAX_WAIT);

		// Login
		login(USERNAME, PASSWORD, c);
		Thread.sleep(MAX_WAIT);

		// Switch to the frame containing the content and get the links on the
		// left bar containing Syllabus, Assignments, Tools, etc.
		c.switchTo().frame(CONTENT_FRAME);
		WebElement left_bar = c
				.findElement(By.id("courseMenuPalette_contents"));
		List<WebElement> left_bar_elements = left_bar.findElements(By
				.cssSelector("a"));

		// Loop through links on the left side, clicking the link
		for (int i = 1; i < left_bar_elements.size(); i++) {
			// Get folder name
			WebElement w = left_bar_elements.get(i);
			String folder = w.getText();

			// follow the link and wait to load
			c.get(w.getAttribute("href"));
			Thread.sleep(MAX_WAIT);

			// Create folder for class
			// Create download folder
			File classFolder = new File(DOWNLOAD_DIR + "/" + classname);
			classFolder.mkdir();

			// download the documents for this class
			download_docs(classFolder, folder, c);

			// get the left bar and the links on it
			left_bar = c.findElement(By.id("courseMenuPalette_contents"));
			left_bar_elements = left_bar.findElements(By.cssSelector("a"));
		}

		c.close();
	}

	private static void download_docs(File classFolder, String folder,
			ChromeDriver c) throws InterruptedException {

		List<WebElement> sections = c.findElements(By
				.xpath("//div[@id='containerdiv']/ul/li"));

		List<File> files_before = Arrays.asList(DOWNLOAD_FOLDER.listFiles());

		// Loop through each section, looking for accepted extensions
		for (int i = 0; i < sections.size(); i++) {

			WebElement w = sections.get(i);
			boolean found_doc = false;

			// look for extensions accepted
			for (String s : extensions) {
				if (w.getText().contains(s)) {
					// opens up the document in a new page and let it load
					w.findElement(By.cssSelector("a")).click();
					// automatically saves document

					// close the page
					found_doc = true;
				}
			}

			sections = c.findElements(By
					.xpath("//div[@id='containerdiv']/ul/li"));
		}
		Thread.sleep(MAX_WAIT);

		List<File> files_after = Arrays.asList(DOWNLOAD_FOLDER.listFiles());

		// if the sizes are the same, nothing was downloaded
		if (files_after.size() == files_before.size()) {
			return;
		}

		// Create download folder
		File new_folder = new File(classFolder.getAbsolutePath() + "/" + folder);
		new_folder.mkdir();

		for (File f : files_after) {
			if (files_before.contains(f)) {
				continue;
			}
			// file was just downloaded, add it to the right folder
			f.renameTo(new File(new_folder.getAbsoluteFile() + "/"
					+ f.getName()));
		}

		// go through the downloads folder, looking for things that have changed
		// store them to the download folder
	}

	/**
	 * 
	 * Attempts to log the user into blackboard with their username and password
	 * 
	 * @param username2
	 *            username
	 * @param password2
	 *            password
	 * @param c
	 *            chrome driver
	 * @return true if successful, false otherwise
	 * @throws InterruptedException
	 *             yeah, i guess
	 */
	private static boolean login(String username2, String password2,
			ChromeDriver c) throws InterruptedException {
		// Login text fields and buttons
		String LOGIN_USERNAME_FIELD = "j_username";
		String LOGIN_PASSWORD_FIELD = "j_password";
		String LOGIN_SUBMIT_NAME = "submit";
		String AUTH_FAILED_ID = "failed";

		// Enter in username
		WebElement usernameField = c.findElement(By.name(LOGIN_USERNAME_FIELD));
		usernameField.sendKeys(USERNAME);

		// Enter in password
		WebElement passwordField = c.findElement(By.name(LOGIN_PASSWORD_FIELD));
		passwordField.sendKeys(PASSWORD);

		// Click submit button
		WebElement submitField = c.findElement(By.name(LOGIN_SUBMIT_NAME));
		submitField.click();

		// check to see if we logged in or not
		List<WebElement> elems = c.findElements(By.id(AUTH_FAILED_ID));
		if (elems.isEmpty()) {
			// Let the page load
			Thread.sleep(MAX_WAIT);
			return true;
		}

		return false;
	}

	/**
	 * Adds to the list of extensions the extensions accepted. Right now it's
	 * just pdf, doc, and ppt.
	 */
	private static void buildExtensionList() {
		extensions.add(".pdf");
		extensions.add(".doc");
		extensions.add(".ppt");
	}

	/**
	 * 
	 * Makes a map where a key is the class name and the value is the link to
	 * that course on blackboard.
	 * 
	 * @param allElements
	 *            web elements from the 'My Course' part
	 * @return map that maps course name to a url
	 */
	private static HashMap<String, String> find_course_links(
			List<WebElement> allElements) {
		HashMap<String, String> courseLinks = new HashMap<String, String>();

		for (WebElement w : allElements) {
			// find the course name
			String courseName;
			int index = w.getText().indexOf("\n");
			if (index >= 0) {
				courseName = w.getText().substring(0, index);
			} else {
				courseName = w.getText();
			}
			// get the url for the course
			String url = w.findElement(By.cssSelector("a"))
					.getAttribute("href");
			courseLinks.put(courseName, url);
		}
		return courseLinks;
	}

	/**
	 * Configures pdf files to automatically download on chrome
	 * 
	 * @param c
	 *            chrome driver
	 */
	private static void setupAutomaticDownloads(ChromeDriver c) {
		c.get("chrome:plugins");
		c.findElement(
				By.xpath("//*[@id=\"pluginTemplate\"]/div[2]/div[2]/div[1]/table/tbody/tr/td/div[2]/span/a[1]"))
				.click();

	}
}