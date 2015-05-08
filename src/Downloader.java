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
	private final static String BLACKBOARD_URL_LOGGED_IN = "https://blackboard.andrew.cmu.edu/webapps/portal/frameset.jsp";

	// Login text fields and buttons
	private final static String LOGIN_USERNAME_FIELD = "j_username";
	private final static String LOGIN_PASSWORD_FIELD = "j_password";
	private final static String LOGIN_SUBMIT_NAME = "submit";

	// Blackboard buttons and other stuff
	private final static String BLACKBOARD_LOGOUT_BUTTON = "topframe.logout.label";
	private final static String CONTENT_FRAME = "contentFrame";

	// Milliseconds to wait for page to load
	private final static int MAX_WAIT = 2 * 1000;

	// Create new Chrome driver
	private static ChromeDriver driver = new ChromeDriver();

	public static void main(String[] args) throws InterruptedException {
		boolean worked;

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

		// switch to content frame
		c.switchTo().frame(CONTENT_FRAME);
		// get all of the windows on the left side
		List<WebElement> left_side_bar = c.findElements(By
				.xpath("//*[@id=\"courseMenuPalette_contents\"]/ul/li"));

		System.out.println(classname);
		System.out.println(left_side_bar.size());

		for (int i = 0; i < left_side_bar.size(); i++) {
			// do stuff
			WebElement w = left_side_bar.get(i);
			if (w.getText().contains("Announcement")) {
				left_side_bar = c.findElements(By
						.xpath("//*[@id=\"courseMenuPalette_contents\"]"));
				Thread.sleep(MAX_WAIT);
				break;
			} else {
				w.click();
				Thread.sleep(MAX_WAIT);
			}
			c.navigate().back();
			left_side_bar = c.findElements(By
					.xpath("//*[@id=\"courseMenuPalette_contents\"]"));
			Thread.sleep(MAX_WAIT);
		}

		c.close();
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
