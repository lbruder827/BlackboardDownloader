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
 * 3. For each course, it will download all files and structure them the same way blackboard does.
 * 
 * 
 * 
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.openqa.selenium.By;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * 
 * @author lbruder
 *
 */
public class Downloader {

	// Location of username and password
	private static final String LOGIN_INFO = "login_info.txt";

	// Max time in milliseconds to wait for the page to load
	private static int MAX_WAIT;

	// Username and password
	private static String USERNAME;
	private static String PASSWORD;

	// Download folder strings and files
	private static File DOWNLOAD_FOLDER;
	private static File DOWNLOAD_DIR;
	private static String DOWNLAD_DIR_NAME = "bb_download_";

	private static ArrayList<String> errors = new ArrayList<String>();

	// Content iframe
	private static String CONTENT_FRAME = "contentFrame";

	// list of subfolders to visit after
	private static ArrayList<VisitLater> visiting_later = new ArrayList<VisitLater>();

	private static ChromeDriver driver = new ChromeDriver();

	public static void main(String[] args) {
		String BLACKBOARD_URL = "https://blackboard.andrew.cmu.edu";
		boolean worked;

		// ensure download folder is directory
		boolean is_directory = setup_download_folder();
		boolean login_info_good;

		try {
			login_info_good = set_login_info();
		} catch (Exception e) {
			login_info_good = false;
		}

		if (!is_directory || !login_info_good) {
			if (!is_directory) {
				System.out.println("Error setting up download folder");
			}
			if (!login_info_good) {
				System.out.println("Error while parsing " + LOGIN_INFO);
				System.out
						.println("Make sure your info is in the same format as given in the file");
			}
			driver.quit();
			return;
		}

		// Ensure that PDFs download automatically instead of being viewed
		setupAutomaticDownloads(driver);

		// Go to blackboard website which will take us to the login page for CMU
		driver.get(BLACKBOARD_URL);

		// Login with username and password and ensure it worked
		try {
			worked = login(USERNAME, PASSWORD);
		} catch (Exception e) {
			worked = false;
		}
		if (!worked) {
			System.out
					.println("Error logging into blackboard. Please try again");
			System.out
					.println("Make sure to enter in your username, password, and a number 1-5 between every use");
			System.out
					.println("Make sure your info is in the same format as given in the file");
			driver.quit();
			return;
		}

		// Create download folder only if logged in successfully
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

		// navigate the courses and download the content
		for (String classname : courseLinks.keySet()) {
			navigate_course(classname, courseLinks.get(classname));
		}

		ArrayList<VisitLater> copy = new ArrayList<VisitLater>(visiting_later);
		visiting_later.clear();

		// Visit the subfolders that we couldn't get the first time
		for (VisitLater v : copy) {
			driver.get(v.getLink());
			try {
				download_docs(v.getClassFolder(), v.getSubFolder());
			} catch (InterruptedException e) {
				System.out.println("Error downloading from subfolders");
				driver.quit();
				return;
			}
		}

		for (VisitLater v : visiting_later) {
			// let the user know that no documents were copied from these
			// subfolders because the depth was too great
			errors.add("The conetent contained under link " + v.getLink()
					+ " could not be visited because the depth was too great");
			errors.add("Please download all documents from this link and store them at "
					+ v.getClassFolder().getAbsolutePath()
					+ "/"
					+ v.getSubFolder());
		}

		// Delete all files that weren't there before
		List<File> files_after = Arrays.asList(DOWNLOAD_FOLDER.listFiles());
		for (File f : files_after) {
			if (!files_before.contains(f)) {
				f.delete();
			}
		}

		System.out.println("\n\n\n");
		System.out.println("*********** Errors ***********");
		if (errors.size() == 0) {
			System.out.println("None");
		} else {
			for (String s : errors) {
				System.out.println(s);
			}
		}

		System.out
				.println("Done! The folder containing your downloads should be in your Downloads folder located at " + DOWNLOAD_DIR);

		driver.quit();
	}

	/**
	 * @return true if the file was parsed successfully, throws exception
	 *         otherwise
	 * @throws IOException
	 *             sure
	 * 
	 */
	private static boolean set_login_info() throws IOException {
		Scanner fileScanner = new Scanner(new File(LOGIN_INFO));
		USERNAME = fileScanner.nextLine();
		PASSWORD = fileScanner.nextLine();
		MAX_WAIT = fileScanner.nextInt() * 1000;
		fileScanner.close();
		BufferedWriter writer = new BufferedWriter(new FileWriter(LOGIN_INFO));
		writer.write("username\n");
		writer.write("password\n");
		writer.write("number 1-5");
		writer.close();
		return true;
	}

	/**
	 * Checks to make sure the download folder is a directory.
	 * 
	 * @return true if it is, false otherwise
	 */
	private static boolean setup_download_folder() {
		// OS Specific fields like the OS and location of download folder
		String OS = System.getProperty("os.name");

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
			return false;
		}
		return true;

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
	private static void navigate_course(String classname, String link) {

		System.out.println("Opening class " + classname);
		driver.get(link);
		// Wait for it to load
		try {
			Thread.sleep(MAX_WAIT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Switch to the frame containing the content and get the links on the
		// left bar containing Syllabus, Assignments, Tools, etc.
		driver.switchTo().frame(CONTENT_FRAME);
		WebElement left_bar = driver.findElement(By
				.id("courseMenuPalette_contents"));
		List<WebElement> left_bar_elements = left_bar.findElements(By
				.cssSelector("a"));

		// Loop through links on the left side, clicking the link
		for (int i = 1; i < left_bar_elements.size(); i++) {
			// Get folder name
			WebElement w = left_bar_elements.get(i);
			String folder = w.getText();

			// follow the link and wait to load
			driver.get(w.getAttribute("href"));
			try {
				Thread.sleep(MAX_WAIT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Create folder for class
			// Create download folder
			File classFolder = new File(DOWNLOAD_DIR + "/" + classname);
			classFolder.mkdir();

			// download the documents for this class
			try {
				download_docs(classFolder, folder);
			} catch (InterruptedException e) {
				errors.add("Error downloading documents for class " + classname
						+ " folder " + folder);
			} catch (WebDriverException x) {
				errors.add("For some reason, the link wasn't clickable");
				errors.add("Please download all documents for class: "
						+ classname + " folder " + folder + " by yourself");
			}

			// get the left bar and the links on it
			left_bar = driver.findElement(By.id("courseMenuPalette_contents"));
			left_bar_elements = left_bar.findElements(By.cssSelector("a"));
		}

	}

	private static void download_docs(File classFolder, String folder)
			throws InterruptedException {

		List<WebElement> sections = driver.findElements(By
				.xpath("//div[@id='containerdiv']/ul/li"));

		List<File> files_before = Arrays.asList(DOWNLOAD_FOLDER.listFiles());

		// Loop through each section, looking for accepted extensions
		for (int i = 0; i < sections.size(); i++) {
			WebElement w = sections.get(i);
			List<WebElement> links = w.findElements(By.cssSelector("a"));
			// loop through all links in the section
			for (int j = 0; j < links.size(); j++) {
				WebElement link = links.get(j);
				// skips html links
				if (link.getText().contains("http")) {
					continue;
				}

				String url_before_click = driver.getCurrentUrl();

				link.click();
				Thread.sleep(MAX_WAIT);

				List<WebElement> middle_div;
				try {
					// Check to see if the middle_div is still visible
					middle_div = driver.findElements(By
							.xpath("//div[@id='containerdiv']/ul/li"));
				} catch (UnhandledAlertException e) {
					// if unexpected alert pops up, dismiss it
					driver.switchTo().alert().dismiss();
					middle_div = driver.findElements(By
							.xpath("//div[@id='containerdiv']/ul/li"));
				}

				// if it isn't navigate back
				if (middle_div.size() == 0) {
					driver.navigate().back();
					Thread.sleep(1000);
				} else {
					// if we left the blackboard page, go back
					if (!driver.getCurrentUrl().contains("blackboard")) {
						driver.navigate().back();
						Thread.sleep(MAX_WAIT);
						continue;
					} else if (!url_before_click.equals(driver.getCurrentUrl())) {
						// just add it to a list of things to be evaluated later
						String subfolder = driver.findElement(
								By.xpath("//*[@id=\"pageTitleText\"]/span"))
								.getText();
						// folder + something else
						VisitLater v = new VisitLater(driver.getCurrentUrl(),
								folder + "/" + subfolder, classFolder);
						visiting_later.add(v);
						driver.navigate().back();
						Thread.sleep(MAX_WAIT);
					}
				}

				// otherwise, just refresh the links we have
				sections = driver.findElements(By
						.xpath("//div[@id='containerdiv']/ul/li"));
				w = sections.get(i);
				links = w.findElements(By.cssSelector("a"));
			}

			sections = driver.findElements(By
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
			System.out.println(f.getName() + " was downloaded.");
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
	private static boolean login(String username2, String password2)
			throws InterruptedException {
		// Login text fields and buttons
		String LOGIN_USERNAME_FIELD = "j_username";
		String LOGIN_PASSWORD_FIELD = "j_password";
		String LOGIN_SUBMIT_NAME = "submit";
		String AUTH_FAILED_ID = "failed";

		// Enter in username
		WebElement usernameField = driver.findElement(By
				.name(LOGIN_USERNAME_FIELD));
		usernameField.sendKeys(username2);

		// Enter in password
		WebElement passwordField = driver.findElement(By
				.name(LOGIN_PASSWORD_FIELD));
		passwordField.sendKeys(password2);

		// Click submit button
		WebElement submitField = driver.findElement(By.name(LOGIN_SUBMIT_NAME));
		submitField.click();

		// check to see if we logged in or not
		List<WebElement> elems = driver.findElements(By.id(AUTH_FAILED_ID));
		if (elems.isEmpty()) {
			// Let the page load
			Thread.sleep(MAX_WAIT);
			return true;
		}

		return false;
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
	 * Configures PDF files to automatically download on Chrome web browser
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

	private static class VisitLater {
		private String link;
		private String subfolder;
		private File classFolder;

		VisitLater(String link, String subfolder, File classFolder) {
			this.link = link;
			this.subfolder = subfolder;
			this.classFolder = classFolder;
		}

		public String getLink() {
			return link;
		}

		public String getSubFolder() {
			return subfolder;
		}

		public File getClassFolder() {
			return classFolder;
		}
	}
}