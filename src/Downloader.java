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

import java.util.ArrayList;
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

	// Link for blackboard
	private final static String BLACKBOARD_URL = "https://blackboard.andrew.cmu.edu";
	// Login text fields and buttons
	private final static String LOGIN_USERNAME_FIELD = "j_username";
	private final static String LOGIN_PASSWORD_FIELD = "j_password";
	private final static String LOGIN_SUBMIT_NAME = "submit";

	// Blackboard buttons and other stuff
	private final static String BLACKBOARD_LOGOUT_BUTTON = "topframe.logout.label";
	private final static String CONTENT_FRAME = "contentFrame";

	// Milliseconds to wait for page to load
	private final static int MAX_WAIT = 2 * 1000;

	private static ArrayList<String> extensions = new ArrayList<String>();

	// Create new Chrome driver
	private static ChromeDriver driver = new ChromeDriver();

	public static void main(String[] args) throws InterruptedException {
		boolean worked;

		buildExtensionList();

		// Go to blackboard website which will take us to the login page for CMU
		driver.get(BLACKBOARD_URL);

		// Login with username and password
		worked = login(USERNAME, PASSWORD, driver);
		if (!worked) {
			System.out
					.println("Error logging into blackboard. Please try again");
			driver.quit();
			return;
		}

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

		driver.quit();

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
		// Open the course in a new window
		ChromeDriver c = new ChromeDriver();
		c.get(link);
		Thread.sleep(MAX_WAIT);
		login(USERNAME, PASSWORD, c);
		// Let the page load
		Thread.sleep(MAX_WAIT);

		// Switch to the frame containing the content and get the links on the
		// left bar
		c.switchTo().frame(CONTENT_FRAME);
		WebElement left_bar = c
				.findElement(By.id("courseMenuPalette_contents"));
		List<WebElement> left_bar_elements = left_bar.findElements(By
				.cssSelector("a"));

		// System.out.println(classname);
		// System.out.println(left_bar_elements.size());

		// Don't need to click announcements, so we can skip it
		for (int i = 1; i < left_bar_elements.size(); i++) {
			// do stuff
			WebElement w = left_bar_elements.get(i);
			String folder = w.getText();

			// follow the link
			c.get(w.getAttribute("href"));
			Thread.sleep(MAX_WAIT);

			// pass it the driver
			download_docs(classname, folder, c);

			// get the left bar and the links on it
			left_bar = c.findElement(By.id("courseMenuPalette_contents"));
			left_bar_elements = left_bar.findElements(By.cssSelector("a"));
		}

		c.close();
	}

	/**
	 * 
	 * @param classname
	 *            name of class on blackboard
	 * @param folder
	 *            name of folder on the left bar
	 * @param c
	 *            the chrome driver
	 */
	private static void download_docs(String classname, String folder,
			ChromeDriver c) {
		List<WebElement> sections = c.findElements(By
				.xpath("//div[@id='containerdiv']/ul/li"));

		// Loop through each section, looking for accepted extensions
		for (WebElement w : sections) {
			boolean found_doc = false;
			for (String s : extensions) {
				if (w.getText().contains(s)) {
					found_doc = true;
					System.out.println(s + " located");
				}
			}
			if (!found_doc) {
				// these links automatically download
				// need to click them so they get downloaded
				// then move them out of the downloads folder and into the
				// correct folder
				List<WebElement> links = w.findElements(By.cssSelector("a"));
				System.out.println(links.size());
			}
		}
	}

	/**
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
		String AUTH_FAILED_ID = "failed";

		// Enter in username
		WebElement usernameField = c.findElement(By.name(LOGIN_USERNAME_FIELD));
		usernameField.sendKeys(USERNAME);

		// Enter in password
		WebElement passwordField = c.findElement(By.name(LOGIN_PASSWORD_FIELD));
		passwordField.sendKeys(PASSWORD);

		// Cick submit button
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
}
