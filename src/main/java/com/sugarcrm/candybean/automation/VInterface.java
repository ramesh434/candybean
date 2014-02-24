/**
 * Candybean is a next generation automation and testing framework suite.
 * It is a collection of components that foster test automation, execution
 * configuration, data abstraction, results illustration, tag-based execution,
 * top-down and bottom-up batches, mobile variants, test translation across
 * languages, plain-language testing, and web service testing.
 * Copyright (C) 2013 SugarCRM, Inc. <candybean@sugarcrm.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sugarcrm.candybean.automation;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.TouchScreen;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteTouchScreen;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.sugarcrm.candybean.automation.control.VControl;
import com.sugarcrm.candybean.automation.control.VHook;
import com.sugarcrm.candybean.automation.control.VHook.Strategy;
import com.sugarcrm.candybean.automation.control.VSelect;
import com.sugarcrm.candybean.configuration.Configuration;
import com.sugarcrm.candybean.utilities.Utils.Pair;

/**
 * Drives the creation of multi-platform automation tests by providing a resourceful API
 * containing several helper methods to write automation tests. The {@link Candybean} configuration
 * will build a {@link VInterface} based on the platform specified in the configuration. An appropriate platform-specific
 * driver is instantiated for use to write tests.
 *
 */
public abstract class VInterface {

	protected DesiredCapabilities capabilities;
	public WebDriver wd = null;
	public final Logger logger;
	private Stack<Pair<Integer, String>> windows = new Stack<Pair<Integer, String>>();
	

	/**
	 * Instantiate VInterface;
	 *
	 * @param candybean  {@link Candybean} object
	 * @param config   {@link Configuration} for this test run
	 * @throws Exception
	 */
	protected VInterface(DesiredCapabilities capabilities)
			throws Exception {
		this.capabilities = capabilities;
		this.logger = Logger.getLogger(this.getClass().getName());
	}
	

	/**
	 * Pause the test for the specified duration.
	 *
	 * @param ms  duration of pause in milliseconds
	 * @throws Exception	 if the underlying {@link Thread#sleep} is interrupted
	 */
	public void pause(long ms) throws Exception {
		logger.info("Pausing for " + ms + "ms via thread sleep.");
		Thread.sleep(ms);
	}

	/**
	 * Display a modal dialog box to the test user.
	 *
	 * @param message	 	String to display on the dialog box
	 * @throws Exception	if the program is running headless (with no GUI)
	 */
	public void interact(String message) {
		logger.info("Interaction via popup dialog with message: " + message);
		JOptionPane.showInputDialog(message);
	}
	
	/**
	 * Takes a full screenshot and saves it to the given file.
	 * 
	 * @param file			The file to which a screenshot is saved
	 * @throws Exception	
	 */
	public void screenshot(File file) throws Exception {
		this.logger.info("Taking screenshot; saving to file: " + file.toString());
		Rectangle screen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		BufferedImage screenshot = (new Robot()).createScreenCapture(screen);
		ImageIO.write(screenshot, "png", file);
	}


	protected abstract void start() throws MalformedURLException;

	/**
	 * Close the interface and perform final cleanup.
	 *
	 * @throws Exception
	 */
	public abstract void stop() throws Exception;
	
	/**
	 * Refreshes the interface.  If refresh is undefined, it does nothing.
	 * 
	 * @throws Exception
	 */
	public void refresh() throws Exception {
		logger.info("Refreshing the interface.");
		this.wd.navigate().refresh();
	}

	/**
	 * Restarts the interface with the current interface type
	 * 
	 * @throws Exception
	 */
	public abstract void restart() throws Exception;

	/**
	 * Load a URL in the browser window.
	 *
	 * @param url	the URL to be loaded by the browser
	 * @throws Exception		<i>not thrown</i>
	 */
	public void go(String url) throws Exception {
		logger.info("Going to URL and switching to window: " + url);
		this.wd.get(url);
	}
	
	/**
	 * Returns the current URL of the current window
	 * 
	 * @return		Returns the current window's URL as a String
	 * @throws Exception
	 */
	public String getURL() throws Exception {
		String url = this.wd.getCurrentUrl();
		logger.info("Getting URL " + url);
		return url;
	}

	/**
	 * Navigates the interface backward.  If backward is undefined, it does nothing.
	 * 
	 * @throws Exception
	 */
	public void backward() throws Exception {
		logger.info("Navigating the interface backward.");
		this.wd.navigate().back();
	}

	/**
	 * Returns true if the interface visibly contains the 
	 * given string in any non-visible=false element.
	 * 
	 * @param s					The target string searched 
	 * for in the interface		
	 * @param caseSensitive		Whether or not the search
	 * is case sensitive		
	 * @return		Returns true if the interface visibly 
	 * contains the given string
	 * @throws Exception
	 */
	public boolean contains(String s, boolean caseSensitive) throws Exception {
		logger.info("Searching if the interface contains the following string: " + s + " with case sensitivity: " + caseSensitive);
		if (!caseSensitive) s = s.toLowerCase();
		List<WebElement> wes = this.wd.findElements(By.xpath("//*[not(@visible='false')]"));
		for (WebElement we : wes) {
			String text = we.getText();
			if (!caseSensitive) text = text.toLowerCase();
			if (text.contains(s)) return true;
		}
		return false;
	}
	
	/**
	 * Switches focus to default content.
	 * 
	 * @throws Exception
	 */
	public void focusDefault() throws Exception {
		logger.info("Focusing to default content.");
		this.wd.switchTo().defaultContent();
	}
	
	/**
	 * Switches focus to the IFrame identified by the given zero-based index
	 * 
	 * @param index		the serial, zero-based index of the iframe to focus
	 * @throws Exception
	 */
	public void focusFrame(int index) throws Exception {
		logger.info("Focusing to frame by index: " + index);
		this.wd.switchTo().frame(index);
	}
	
	/**
	 * Switches focus to the IFrame identified by the given name or ID string
	 * 
	 * @param nameOrId	the name or ID identifying the targeted IFrame
	 * @throws Exception
	 */
	public void focusFrame(String nameOrId) throws Exception {
		logger.info("Focusing to frame by name or ID: " + nameOrId);
		this.wd.switchTo().frame(nameOrId);
	}
	
	/**
	 * Switches focus to the IFrame identified by the given {@link VControl}
	 * 
	 * @param control		The VControl representing a focus-targeted IFrame
	 * @throws Exception
	 */
	public void focusFrame(VControl control) throws Exception {
		logger.info("Focusing to frame by control: " + control.toString());
		this.wd.switchTo().frame(control.we);
	}
	
	/**
	 * Close the current browser window.
	 *
	 * @throws Exception	  <i>not thrown</i>
	 */
	public void closeWindow() throws Exception {
		logger.info("Closing window with handle: " + windows.peek());
		this.wd.close();
		this.windows.pop();
		logger.info("Refocusing to previous window with handle: " + windows.peek());
		this.wd.switchTo().window(windows.peek().y);
	}

	/**
	 * Focus a browser window by its index if it is not the current index.
	 *
	 * <p>The order of browser windows is somewhat arbitrary and not
	 * guaranteed, although window creation time ordering seems to be
	 * the most common.</p>
	 *
	 * @param index  		the window index
	 * @throws Exception	if the specified window index is out of range
	 */
	public void focusWindow(int index) throws Exception {
		if (index == windows.peek().x.intValue()) {
			logger.warning("No focus was made because the given index matched the current index: " + index);
		} else if (index < 0) {
			throw new Exception("Given focus window index is out of bounds: " + index + "; current size: " + windows.size());
		} else {
			Set<String> windowHandlesSet = this.wd.getWindowHandles();
			String[] windowHandles = windowHandlesSet.toArray(new String[] {""});
			if (index >= windowHandles.length) {
				throw new Exception("Given focus window index is out of bounds: " + index + "; current size: " + windows.size());
			} else {
				this.wd.switchTo().window(windowHandles[index]);
				windows.push(new Pair<Integer, String>(new Integer(index), this.wd.getWindowHandle()));
				logger.info("Focused by index: " + index + " to window: " + windows.peek());
			}
		}
	}

	/**
	 * Focus a browser window by its window title or URL if it does not
	 * match the current title or URL.
	 *
	 * <p>If more than one window has the same title or URL, the first
	 * encountered is the one that is focused.</p>
	 *
	 * @param titleOrUrl  	the exact window title or URL to be matched
	 * @throws Exception	if the specified window cannot be found
	 */
	public void focusWindow(String titleOrUrl) throws Exception {
		String curTitle = this.wd.getTitle();
		String curUrl = this.wd.getCurrentUrl();
		if (titleOrUrl.equals(curTitle) || titleOrUrl.equals(curUrl)) {
			logger.warning("No focus was made because the given string matched the current title or URL: " + titleOrUrl);
		} else {
			Set<String> windowHandlesSet = this.wd.getWindowHandles();
			String[] windowHandles = windowHandlesSet.toArray(new String[] {""});
			int i = 0;
			boolean windowFound = false;
			while (i < windowHandles.length && !windowFound) {
				WebDriver window = this.wd.switchTo().window(windowHandles[i]);
				if (window.getTitle().equals(titleOrUrl) || window.getCurrentUrl().equals(titleOrUrl)) {
					windows.push(new Pair<Integer, String>(new Integer(i), this.wd.getWindowHandle()));
					logger.info("Focused by title or URL: " + titleOrUrl + " to window: " + windows.peek());
					windowFound = true;
				}
				i++;
			}
			if (!windowFound) {
				this.wd.switchTo().window(windows.peek().y);
				throw new Exception("The given focus window string matched no title or URL: " + titleOrUrl);
			}
		}	
	}
	
	/**
	 * Navigates the interface forward.  If forward is undefined, it does nothing.
	 * 
	 * @throws Exception
	 */
	public void forward() throws Exception {
		logger.info("Navigating the interface forward.");
		this.wd.navigate().forward();
	}
	
	/**
	 * Returns a string with the contents of the windows data structure.
	 * 
	 * @return	A string representation of all focused windows, with 
	 * chronological index of focus and handle
	 */
	public String getWindowsString() {
		String s = "Reverse stack:\n";
		Iterator<Pair<Integer, String>> winIter = windows.iterator();
		while (winIter.hasNext()) {
			s += winIter.next() + "\n";
		}
		return s;
	}
	
	/**
	 * Maximize the browser window.
	 *
	 * @throws Exception	 <i>not thrown</i>
	 */
	public void maximize() {
		logger.info("Maximizing window");
		this.wd.manage().window().maximize();
	}

	/**
	 * Get a control from the current page.
	 *
	 * @param hook	 description of how to find the control
	 * @throws Exception	 <i>not thrown</i>
	 */
	public VControl getControl(VHook hook) throws Exception {
		return new VControl(this, hook);
	}

	/**
	 * Get a control from the current page by index.
	 *
	 * @param hook	 description of how to find the control
	 * @throws Exception	 <i>not thrown</i>
	 */
	public VControl getControl(VHook hook, int index) throws Exception {
		return new VControl(this, hook, index);
	}

	/**
	 * Get a control from the current page.
	 *
	 * @param strategy  method to use to search for the control
	 * @param hook		  string to find using the specified strategy
	 * @throws Exception	 <i>not thrown</i>
	 */
	public VControl getControl(Strategy strategy, String hook) throws Exception {
		return this.getControl(new VHook(strategy, hook));
	}
	
	/**
	 * Gets a list of controls from the current page
	 * @param strategy The strategy used to search for the control
	 * @param hook The associated hook for the strategy
	 * @return The list of all controls that match the strategy and hook
	 * @throws Exception
	 */
	public List<VControl> getControls(Strategy strategy, String hook) throws Exception {
		return this.getControls(strategy, new VHook(strategy, hook));
	}
	
	/**
	 * Gets a list of controls from the current page based on a VHook
	 * @param strategy The strategy used to search for the control
	 * @param hook The associated hook for the strategy
	 * @return The list of all controls that match the strategy and hook
	 * @throws Exception
	 */
	private List<VControl> getControls(Strategy strategy, VHook hook) throws Exception {
		List<VControl> controls = new ArrayList<VControl>();
		List<WebElement> wes = this.wd.findElements(VControl.makeBy(strategy, hook.hookString));
		for (WebElement we : wes)
			controls.add(new VControl(this, hook, we));
		return controls;
	}

	/**
	 * Get a control from the current page by index.
	 *
	 * @param strategy  method to use to search for the control
	 * @param hook		  string to find using the specified strategy
	 * @throws Exception	 <i>not thrown</i>
	 */
	public VControl getControl(Strategy strategy, String hook, int index) throws Exception {
		return this.getControl(new VHook(strategy, hook), index);
	}

	/**
	 * Get a &lt;SELECT&gt; control from the current page.
	 *
	 * @param hook	 description of how to find the control
	 * @throws Exception	 <i>not thrown</i>
	 */
	public VSelect getSelect(VHook hook) throws Exception {
		return new VSelect(this, hook);
	}

	/**
	 * Get a &lt;SELECT&gt; control from the current page.
	 *
	 * @param strategy  method to use to search for the control
	 * @param hook		  string to find using the specified strategy
	 * @throws Exception	 <i>not thrown</i>
	 */
	public VSelect getSelect(Strategy strategy, String hook) throws Exception {
		return this.getSelect(new VHook(strategy, hook));
	}
	
	/**
	 * Click &quot;OK&quot; on a modal dialog box (usually referred to
	 * as a &quot;javascript dialog&quot;).
	 *
	 * @throws Exception	 if no dialog box is present
	 */
	public void acceptDialog() throws Exception {
		try {
			logger.info("Accepting dialog.");
			this.wd.switchTo().alert().accept();
		} catch(UnhandledAlertException uae) {
			logger.warning("Unhandled alert exception");
		}
	}
	
	/**
	 * Dismisses a modal dialog box (usually referred to
	 * as a &quot;javascript dialog&quot;).
	 *
	 * @throws Exception	 if no dialog box is present
	 */
	public void dismissDialog() throws Exception {
		try {
			logger.info("Dismissing dialog.");
			this.wd.switchTo().alert().dismiss();
		} catch(UnhandledAlertException uae) {
			logger.warning("Unhandled alert exception");
		}
	}

	/**
	 * Returns true if a modal dialog can be switched to 
	 * and switched back from; otherwise, returns false.
	 * 
	 * @return 	Boolean true only if a modal dialog can 
	 * be switched to, then switched back from.
	 */
	public boolean isDialogVisible() {
		try { 
			this.wd.switchTo().alert(); 
			logger.info("Dialog present?: true.");
			return true;
		} catch(UnhandledAlertException uae) {
			logger.info("(Unhandled alert in FF?) Dialog present?: true.  May have ignored dialog...");
			return true;
		} catch(NoAlertPresentException nape) {
			logger.info("Dialog present?: false.");
			return false;
		}
	}
	
    /**
	 * Encompasses a widget from the current page in order to perform
	 * an action on it.  Providing this as a more aptly named alternative
	 * to getControl as the 'thing' encompassed by this is not necessarily
	 * a 'control' and merely referencing it does nothing; an action must be
	 * performed off of it, hence not named 'getWidget'.
	 *
	 * @param hookStrategy	method to use to search for the widget
	 * @param hookString	string to find using the specified strategy
	 * @throws Exception	<i>not thrown</i>
	 */
	public VControl widget(Strategy hookStrategy, String hookString) throws Exception {
		return this.getControl(new VHook(hookStrategy, hookString));
	}
	
	/**
	 * Encompasses a widget from the current page in order to perform
	 * an action on it.  Providing this as a more aptly named alternative
	 * to getControl as the 'thing' encompassed by this is not necessarily
	 * a 'control' and merely referencing it does nothing; an action must be
	 * performed off of it, hence not named 'getWidget'.
	 *
	 * @param hook			VHook method to use to search for the widget
	 * @throws Exception	<i>not thrown</i>
	 */
	public VControl widget(VHook hook) throws Exception {
		return this.getControl(hook);
	}
	
//	private VInterface.Type parseInterfaceType(String iTypeString) throws Exception {
//		VInterface.Type iType = null;
//		for (VInterface.Type iTypeIter : VInterface.Type.values()) {
//			if (iTypeIter.name().equalsIgnoreCase(iTypeString)) {
//				iType = iTypeIter;
//				break;
//			}
//		}
//		if (iType == Type.ANDROID) throw new Exception("Android interface type not yet implemented.");
//		if (iType == Type.IOS) throw new Exception("iOS interface type not yet implemented.");
//		return iType;
//	}
	
//	private WebDriver getWebDriver(Type iType, DesiredCapabilities capabilities) throws Exception {
//        WebDriver wd = null;
//        switch (iType) {
//	        case ANDROID:
//	    		capabilities.setCapability(CapabilityType.BROWSER_NAME, "Android");
//	    		capabilities.setCapability(CapabilityType.VERSION, "4.4.2");
//	    		capabilities.setCapability("device", "Android");
//	            wd = new SwipeableWebDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
//	            break;
//	        case IOS:
//	            capabilities.setCapability(CapabilityType.BROWSER_NAME, "iOS");
//	            capabilities.setCapability(CapabilityType.VERSION, "6.0");
//	            capabilities.setCapability(CapabilityType.PLATFORM, "Mac");
//	            wd = new SwipeableWebDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
//	            break;
//	        default:
//				throw new Exception("Selenium: browser type not recognized.");
//        }
//		long implicitWait = Long.parseLong(config.getValue("perf.implicit_wait_seconds"));
//		wd.manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
//		return wd;
//		
//	}
//
//	private WebDriver getWebDriver(Type iType) throws Exception {
//        DesiredCapabilities capabilities = new DesiredCapabilities();
////        capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
//        WebDriver wd = null;
//        switch (iType) {
//		case FIREFOX:
//			String profileName = this.config.getValue("browser.firefox_profile", "default");
//			File ffBinaryPath = new File(this.config.getPathValue("browser.firefox_binary"));
//			FirefoxProfile ffProfile = (new ProfilesIni()).getProfile(profileName);
////			ffProfile.setEnableNativeEvents(false);
//			FirefoxBinary ffBinary = new FirefoxBinary(ffBinaryPath);
//			// if (System.getProperty("headless") != null) {
//			// FirefoxBinary ffBinary = new FirefoxBinary();//new
//			// File("//home//conrad//Applications//firefox-10//firefox"));
//			// ffBinary.setEnvironmentProperty("DISPLAY", ":1");
//			// webDriver = new FirefoxDriver(ffBinary, ffProfile);
//			// }
//			logger.info("Instantiating Firefox with profile name: "
//					+ profileName + " and binary path: " + ffBinaryPath);
//			wd = new FirefoxDriver(ffBinary, ffProfile);
//			break;
//		case CHROME:
//			ChromeOptions chromeOptions = new ChromeOptions();
//			String chromeDriverLogPath = this.config.getPathValue("browser.chrome_driver_log_path");
//			logger.info("chromeDriverLogPath: " + chromeDriverLogPath);
//			chromeOptions.addArguments("--log-path=" + chromeDriverLogPath);
//			String chromeDriverPath = this.config.getPathValue("browser.chrome_driver_path");
//			logger.info("chromeDriverPath: " + chromeDriverPath);
//			// chromeOptions.setBinary(new File(chromeDriverPath));
//			System.setProperty("webdriver.chrome.driver", chromeDriverPath);
//			logger.info("Instantiating Chrome with:\n    log path:"
//					+ chromeDriverLogPath + "\n    driver path: "
//					+ chromeDriverPath);
//			wd = new ChromeDriver(chromeOptions);
//			break;
//		case IE:
//			String ieDriverPath = this.config.getPathValue("browser.ie_driver_path");
//			logger.info("ieDriverPath: " + ieDriverPath);
//			System.setProperty("webdriver.ie.driver", ieDriverPath);
//			capabilities = DesiredCapabilities.internetExplorer();
//			wd = new InternetExplorerDriver(capabilities);
//			break;
//		case SAFARI:
//			throw new Exception("Selenium: safari browser not yet supported.");
//
//        default:
//			throw new Exception("Selenium: browser type not recognized.");
//		}
//		long implicitWait = Long.parseLong(config.getValue("perf.implicit_wait_seconds"));
//		if (System.getProperty("headless") == null && !iType.equals(Type.ANDROID)) {
//			java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//			wd.manage().window().setSize(new Dimension(screenSize.width, screenSize.height));
//		}
//		wd.manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
//		return wd;
//	}
	
	public class SwipeableWebDriver extends RemoteWebDriver implements HasTouchScreen {
		private RemoteTouchScreen touch;

		public SwipeableWebDriver(URL remoteAddress,
				Capabilities desiredCapabilities) {
			super(remoteAddress, desiredCapabilities);
			touch = new RemoteTouchScreen(getExecuteMethod());
		}

		public TouchScreen getTouch() {
			return touch;
		}
	}

	public InterfaceType getType() {
		InterfaceType type = null;
		if(this instanceof AndroidInterface){
			type = InterfaceType.ANDROID;
		}else if (this instanceof AndroidInterface){
			type = InterfaceType.CHROME;
		}else if (this instanceof AndroidInterface){
			type = InterfaceType.FIREFOX;
		}else if (this instanceof AndroidInterface){
			type = InterfaceType.IE;
		}else if (this instanceof AndroidInterface){
			type = InterfaceType.IOS;
		}else if (this instanceof AndroidInterface){
			type = InterfaceType.SAFARI;
		}
		return type;
	}

	// ANDROID ROBOTIUM FUNCTIONALITY
	//	private AndroidInterface getAndroidControl() throws Exception {
	//		AndroidInterface vac = new AndroidInterface(this.props);
	//		return vac;
	//	}
	//	
	//	public void startApp() throws Exception {
	//		this.vac.startApp();
	//	}
	//	
	//	public void finishApp() throws Exception {
	//		this.vac.finishApp();
	//	}
	//	
	//	public void setApkPath(String aut, String messenger, String testrunner) {
	//		this.vac.setApkPath(aut, messenger, testrunner);
	//	}
	//	
	//	public void ignoreInstallAUT() throws Exception {
	//		this.vac.ignoreInstallAUT();
	//	}
	//	
	//	public void ignoreInstallMessenger() throws Exception {
	//		this.vac.ignoreInstallMessenger();
	//	}
	//	
	//	public void ignoreInstallRunner() throws Exception {
	//		this.vac.ignoreInstallRunner();
	//	}
	//	
	//	public VAControl getAControl() throws Exception{
	//		return new VAControl(this.candybean, this);
	//	}


	//	/**
	//	 * @param selectElement
	//	 * @param actionElement
	//	 */
	//	public static void allOptionsAction(Select selectElement, WebElement actionElement) {
	//		List<WebElement> options = selectElement.getOptions();
	//		for (WebElement option : options) {
	//			selectElement.selectByVisibleText(option.getText());
	//			actionElement.click();
	//		}
	//	}
	//	
	//	
	//	/**
	//	 * @param selectElement
	//	 * @param actionOptionValues
	//	 * @param actionElement
	//	 * @throws Exception
	//	 */
	//	public static void optionAction(Select selectElement, Set<String> actionOptionValues, WebElement actionElement) throws Exception {
	//		List<WebElement> allOptions = selectElement.getOptions();
	//		HashSet<String> optionValues = new HashSet<String>();
	//		for(WebElement option : allOptions) {
	//			optionValues.add(option.getText());
	////			System.out.println("Adding to options set:" + option.getText());
	//		}
	//		if(optionValues.containsAll(actionOptionValues)) {
	//			for(String option : actionOptionValues) {
	//				selectElement.selectByVisibleText(option);
	//				actionElement.click();
	//			}
	//		} else throw new Exception("Specified select option unavailable...");
	//	}
	//	
	//	
	//
	//	/**
	//	 * @param element
	//	 * @return
	//	 */
	//	public static String webElementToString(WebElement element) {
	//		List<WebElement> childElements = element.findElements(By.xpath("*"));
	//		String s = element.getTagName() + ":" + element.getText() + " ";
	//		for(WebElement we : childElements) {
	//			s += we.getTagName() + ":" + we.getText() + " ";
	//		}
	//		return s;
	//	}
	//	
	//	
	//	/**
	//	 * @param nativeOptions
	//	 * @param queryOptionNames
	//	 * @return
	//	 */
	//	public static boolean optionValuesEqual(List<WebElement> nativeOptions, Set<String> queryOptionNames) {
	//		Set<String> nativeOptionNames = new HashSet<String>();
	//		for (WebElement option : nativeOptions) {
	//			nativeOptionNames.add(option.getText());
	//		}
	//		if (nativeOptionNames.containsAll(queryOptionNames) && queryOptionNames.containsAll(nativeOptionNames)) return true;
	//		else return false;
	//	}
}
