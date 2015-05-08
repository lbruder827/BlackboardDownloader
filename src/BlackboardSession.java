import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * 
 * @author lbruder
 *
 */
public class BlackboardSession {

	// Blackboard buttons
	private final static String BLACKBOARD_LOGOUT_BUTTON = "topframe.logout.label";

	// Login text fields and buttons
	private final static String LOGIN_USERNAME_FIELD = "j_username";
	private final static String LOGIN_PASSWORD_FIELD = "j_password";
	private final static String LOGIN_SUBMIT_NAME = "submit";

	private final String username;
	private final String password;

	// Create new HTML driver
	private WebDriver driver;

	/**
	 * 
	 * @param username
	 *            username for blackboard
	 * @param password
	 *            password for blackboard
	 */
	public BlackboardSession(String username, String password) {
		this.username = username;
		this.password = password;
		driver = new HtmlUnitDriver();
	}

	/**
	 * 
	 * @return true if the login is successful, false otherwise
	 */
	public boolean login() {
		boolean alreadyLoggedIn;

		// check to see if we already logged in
		List<WebElement> elems;
		elems = driver.findElements(By.id(BLACKBOARD_LOGOUT_BUTTON));
		if (elems.isEmpty()) {
			System.out.println("Not logged in yet");
			alreadyLoggedIn = false;
		} else {
			System.out.println("Already logged in");
			return true;
		}

		// Login
		if (!alreadyLoggedIn) {
			String AUTH_FAILED_ID = "failed";

			// Enter in username
			WebElement usernameField = driver.findElement(By
					.name(LOGIN_USERNAME_FIELD));
			usernameField.sendKeys(username);

			// Enter in password
			WebElement passwordField = driver.findElement(By
					.name(LOGIN_PASSWORD_FIELD));
			passwordField.sendKeys(password);

			// Cick submit button
			WebElement submitField = driver.findElement(By
					.name(LOGIN_SUBMIT_NAME));
			submitField.click();

			// check to see if we logged in or not
			elems = driver.findElements(By.id(AUTH_FAILED_ID));
			if (elems.isEmpty()) {
				return true;
			}

			return false;

		}
		return true;
	}

	/**
	 * Logout of blackboard.
	 */
	public void logout() {

		// do logout stuff

		// quit the HTML driver
		driver.quit();
	}

}
