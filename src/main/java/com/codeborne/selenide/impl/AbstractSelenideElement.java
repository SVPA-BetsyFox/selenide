package com.codeborne.selenide.impl;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.JQuery;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ex.ElementNotFound;
import com.codeborne.selenide.ex.ElementShould;
import com.codeborne.selenide.ex.ElementShouldNot;
import com.codeborne.selenide.impl.SelenideLogger.EventStatus;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Configuration.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selectors.byValue;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.isHtmlUnit;
import static com.codeborne.selenide.WebDriverRunner.isIE;
import static com.codeborne.selenide.impl.WebElementProxy.wrap;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.currentThread;

abstract class AbstractSelenideElement implements InvocationHandler {
  abstract WebElement getDelegate();
  abstract WebElement getActualDelegate() throws NoSuchElementException, IndexOutOfBoundsException;
  abstract String getSearchCriteria();
  protected Exception lastError;

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if ("setValue".equals(method.getName())) {
      setValue((String) args[0]);
      return proxy;
    }
    else if ("val".equals(method.getName())) {
      if (args == null || args.length == 0) {
        return getValue();
      }
      else {
        setValue((String) args[0]);
        return proxy;
      }
    }
    else if ("attr".equals(method.getName())) {
    	return getDelegate().getAttribute((String) args[0]);
    }
    else if ("name".equals(method.getName())) {
    	return getDelegate().getAttribute("name");
    }
    else if ("data".equals(method.getName())) {
    	return getDelegate().getAttribute("data-" + args[0]);
    }
   	else if ("append".equals(method.getName())) {
      append((String) args[0]);
      return proxy;
    }
    else if ("pressEnter".equals(method.getName())) {
      getDelegate().sendKeys(Keys.ENTER);
      return proxy;
    }
    else if ("pressTab".equals(method.getName())) {
      getDelegate().sendKeys(Keys.TAB);
      return proxy;
    }
    else if ("followLink".equals(method.getName())) {
      followLink();
      return null;
    }
    else if ("text".equals(method.getName())) {
      return getDelegate().getText();
    }
    else if ("innerText".equals(method.getName())) {
      return getInnerText();
    }
    else if ("innerHtml".equals(method.getName())) {
      return getInnerHtml();
    }
    else if ("should".equals(method.getName())) {
      return invokeShould(proxy, "", args);
    }
    else if ("shouldHave".equals(method.getName())) {
      return invokeShould(proxy, "have ", args);
    }
    else if ("shouldBe".equals(method.getName())) {
      return invokeShould(proxy, "be ", args);
    }
    else if ("shouldNot".equals(method.getName())) {
      return invokeShouldNot(proxy, "", args);
    }
    else if ("shouldNotHave".equals(method.getName())) {
      return invokeShouldNot(proxy, "have ", args);
    }
    else if ("shouldNotBe".equals(method.getName())) {
      return invokeShouldNot(proxy, "be ", args);
    }
    else if ("parent".equals(method.getName())) {
      return parent((SelenideElement) proxy);
    }
    else if ("closest".equals(method.getName())) {
      return closest((SelenideElement) proxy, (String) args[0]);
    }
    else if ("find".equals(method.getName()) || "$".equals(method.getName())) {
      return args.length == 1 ?
          find((SelenideElement) proxy, args[0], 0) :
          find((SelenideElement) proxy, args[0], (Integer) args[1]);
    }
    else if ("findAll".equals(method.getName()) || "$$".equals(method.getName())) {
      final SelenideElement parent = (SelenideElement) proxy;
      return new ElementsCollection(new BySelectorCollection(parent, getSelector(args[0])));
    }
    else if ("toString".equals(method.getName())) {
      return describe();
    }
    else if ("exists".equals(method.getName())) {
      return exists();
    }
    else if ("isDisplayed".equals(method.getName())) {
      return isDisplayed();
    }
    else if ("is".equals(method.getName()) || "has".equals(method.getName())) {
      return matches((Condition) args[0]);
    }
    else if ("setSelected".equals(method.getName())) {
      setSelected((Boolean) args[0]);
      return proxy;
    }
    else if ("uploadFile".equals(method.getName())) {
      return uploadFile((SelenideElement) proxy, (File[]) args[0]);
    }
    else if ("uploadFromClasspath".equals(method.getName())) {
      return uploadFromClasspath((SelenideElement) proxy, (String[]) args[0]);
    }
    else if ("selectOption".equals(method.getName())) {
      selectOptionByText(getDelegate(), (String) args[0]);
      return null;
    }
    else if ("selectOptionByValue".equals(method.getName())) {
      selectOptionByValue(getDelegate(), (String) args[0]);
      return null;
    }
    else if ("getSelectedOption".equals(method.getName())) {
      return getSelectedOption(getDelegate());
    }
    else if ("getSelectedValue".equals(method.getName())) {
      return getSelectedValue(getDelegate());
    }
    else if ("getSelectedText".equals(method.getName())) {
      return getSelectedText(getDelegate());
    }
    else if ("toWebElement".equals(method.getName())) {
      return getActualDelegate();
    }
    else if ("waitUntil".equals(method.getName())) {
      if (args[0] instanceof String) {
        waitUntil("", (String) args[0], (Condition) args[1], (Long) args[2]);
      }
      else {
        waitUntil("", (Condition) args[0], (Long) args[1]);
      }
      return proxy;
    }
    else if ("waitWhile".equals(method.getName())) {
      if (args[0] instanceof String) {
        waitWhile("", (String) args[0], (Condition) args[1], (Long) args[2]);
      }
      else {
        waitWhile("", (Condition) args[0], (Long) args[1]);
      }
      return proxy;
    }
    else if ("scrollTo".equals(method.getName())) {
      scrollTo();
      return proxy;
    }
    else if ("download".equals(method.getName())) {
      return download();
    }
    else if ("click".equals(method.getName())) {
      click();
      return null;
    }
    else if ("contextClick".equals(method.getName())) {
      contextClick();
      return null;
    }
    else if ("hover".equals(method.getName())) {
      hover();
      return null;
    }
    else if ("dragAndDropTo".equals(method.getName())) {
      dragAndDropTo((String) args[0]);
      return null;
    }
    else if ("getWrappedElement".equals(method.getName())) {
      return getActualDelegate();
    }
    else if ("isImage".equals(method.getName())) {
      return isImage();
    }

    return delegateMethod(getDelegate(), method, args);
  }

  protected Object invokeShould(Object proxy, String prefix, Object[] args) {
    if (args[0] instanceof String) {
      return should(proxy, prefix, (String) args[0], (Condition[]) args[1]);
    }
    return should(proxy, prefix, (Condition[]) args[0]);
  }

  protected Object invokeShouldNot(Object proxy, String prefix, Object[] args) {
    if (args[0] instanceof String) {
      return shouldNot(proxy, prefix, (String) args[0], (Condition[]) args[1]);
    }
    return shouldNot(proxy, prefix, (Condition[]) args[0]);
  }

  protected Boolean isImage() {
    WebElement img = getActualDelegate();
    if (!"img".equalsIgnoreCase(img.getTagName())) {
      throw new IllegalArgumentException("Method isImage() is only applicable for img elements");
    }
    return executeJavaScript("return arguments[0].complete && " +
        "typeof arguments[0].naturalWidth != 'undefined' && " +
        "arguments[0].naturalWidth > 0", img);
  }

  protected boolean matches(Condition condition) {
    try {
      WebElement element = tryToGetElement();
      if (element != null) {
        return condition.apply(element);
      }
    }
    catch (WebDriverException elementNotFound) {
      lastError = elementNotFound;
    }
    catch (IndexOutOfBoundsException ignore) {
      lastError = ignore;
    }

    if (Cleanup.of.isInvalidSelectorError(lastError)) {
      throw Cleanup.of.wrap(lastError);
    }

    return condition.applyNull();
  }

  protected void setSelected(boolean selected) {
    WebElement element = waitForElement();
    if (element.isSelected() ^ selected) {
      element.click();
    }
  }

  protected String getInnerText() {
    WebElement element = waitUntil("", exist, timeout);
    if (isHtmlUnit()) {
      return executeJavaScript("return arguments[0].innerText", element);
    }
    else if (isIE()) {
      return element.getAttribute("innerText");
    }
    return element.getAttribute("textContent");
  }

  protected String getInnerHtml() {
    WebElement element = waitUntil("", exist, timeout);
    if (isHtmlUnit()) {
      return executeJavaScript("return arguments[0].innerHTML", element);
    }
    return element.getAttribute("innerHTML");
  }

  protected WebElement waitForElement() {
    return waitUntil("be ", visible, timeout);
  }

  protected void click() {
    try {
      SelenideLogger.beginStep(this, "click");
      waitForElement().click();
      SelenideLogger.commitStep(EventStatus.PASSED);
    }catch(Throwable t) {
      SelenideLogger.commitStep(EventStatus.FAILED);
      rethrow(t);
    }
  }

  protected void contextClick() {
    actions().contextClick(waitForElement()).perform();
  }

  protected void hover() {
    actions().moveToElement(waitForElement()).perform();
  }

  protected void dragAndDropTo(String targetCssSelector) {
    SelenideElement target = $(targetCssSelector).shouldBe(visible);
    actions().dragAndDrop(waitForElement(), target).perform();
  }

  protected void followLink() {
    WebElement link = waitForElement();
    String href = link.getAttribute("href");
    link.click();

    // JavaScript $.click() doesn't take effect for <a href>
    if (href != null) {
      open(href);
    }
  }

  protected void setValue(String text) {
    try {
      SelenideLogger.beginStep(this, "set value '" + text + "‘");
      
      WebElement element = waitForElement();
      if ("select".equalsIgnoreCase(element.getTagName())) {
        selectOptionByValue(element, text);
      }
      else if (text == null || text.isEmpty()) {
        element.clear();
      }
      else if (fastSetValue && JQuery.jQuery.isJQueryAvailable()) {
        String jsCodeToTriggerEvent =
            "arguments[0].value = arguments[1];" +
            "var element = jQuery(arguments[0]);" +
  
            "var e = jQuery.Event('keydown');  e.which = arguments[2]; element.trigger(e);" +
            "var e = jQuery.Event('keypress'); e.which = arguments[2]; element.trigger(e);" +
            "var e = jQuery.Event('keyup');    e.which = arguments[2]; element.trigger(e);";
  
        char lastChar = text.charAt(text.length() - 1);
        executeJavaScript(jsCodeToTriggerEvent, element, text, (int) lastChar);
        fireChangeEvent(element);
      }
      else if (fastSetValue) {
        executeJavaScript("arguments[0].value = arguments[1]", element, text);
        fireChangeEvent(element);
      }
      else {
        element.clear();
        element.sendKeys(text);
        fireChangeEvent(element);
      }
      SelenideLogger.commitStep(EventStatus.PASSED);
    }
    catch(Throwable t) {
      SelenideLogger.commitStep(EventStatus.FAILED);
      rethrow(t);
    }
  }

  protected void fireChangeEvent(WebElement element) {
    fireEvent(element, "change");
  }

  protected String getValue() {
    return getDelegate().getAttribute("value");
  }

  protected void append(String text) {
    WebElement element = waitForElement();
    element.sendKeys(text);
    fireChangeEvent(element);
  }

  protected void fireEvent(WebElement element, final String event) {
    final String jsCodeToTriggerEvent
        = "if (document.createEventObject) {\n" +  // IE
        "  var evt = document.createEventObject();\n" +
        "  return arguments[0].fireEvent('on' + arguments[1], evt);\n" +
        "}\n" +
        "else {\n" +
        "  var evt = document.createEvent('HTMLEvents');\n " +
        "  evt.initEvent(arguments[1], true, true );\n " +
        "  return !arguments[0].dispatchEvent(evt);\n" +
        '}';
    executeJavaScript(jsCodeToTriggerEvent, element, event);
  }

  protected Object should(Object proxy, String prefix, Condition... conditions) {
    return should(proxy, prefix, null, conditions);
  }

  protected Object should(Object proxy, String prefix, String message, Condition... conditions) {
    for (Condition condition : conditions) {
      try {
        SelenideLogger.beginStep(this, Describe.describeSubject("should", prefix, message, condition));
        waitUntil(prefix, message, condition, timeout);
        SelenideLogger.commitStep(EventStatus.PASSED);
      } catch (Throwable t) {
        SelenideLogger.commitStep(EventStatus.FAILED);
        rethrow(t);
      }
    }
    return proxy;
  }

  protected Object shouldNot(Object proxy, String prefix, Condition... conditions) {
    return shouldNot(proxy, prefix, null, conditions);
  }

  protected Object shouldNot(Object proxy, String prefix, String message, Condition... conditions) {
    for (Condition condition : conditions) {
      try {
        SelenideLogger.beginStep(this, Describe.describeSubject("should not", prefix, message, condition));
        waitWhile(prefix, message, condition, timeout);
        SelenideLogger.commitStep(EventStatus.PASSED);
      } catch (Throwable t) {
        SelenideLogger.commitStep(EventStatus.FAILED);
        rethrow(t);
      }
    }
    return proxy;
  }

  protected File uploadFromClasspath(SelenideElement inputField, String... fileName) throws URISyntaxException, IOException {
    File[] files = new File[fileName.length];
    for (int i = 0; i < fileName.length; i++) {
      files[i] = findFileInClasspath(fileName[i]);
    }

    return uploadFile(inputField, files);
  }

  protected File findFileInClasspath(String name) throws URISyntaxException {
    URL resource = currentThread().getContextClassLoader().getResource(name);
    if (resource == null) {
      throw new IllegalArgumentException("File not found in classpath: " + name);
    }
    return new File(resource.toURI());
  }

  protected File uploadFile(SelenideElement inputField, File... file) throws IOException {
    if (file.length == 0) {
      throw new IllegalArgumentException("No files to upload");
    }

    File uploadedFile = uploadFile(inputField, file[0]);

    if (file.length > 1) {
      SelenideElement form = inputField.closest("form");
      for (int i = 1; i < file.length; i++) {
        uploadFile(cloneInputField(form, inputField), file[i]);
      }
    }
    
    return uploadedFile;
  }

  protected WebElement cloneInputField(SelenideElement form, SelenideElement inputField) {
    return executeJavaScript(
        "var fileInput = document.createElement('input');" +
            "fileInput.setAttribute('type', arguments[1].getAttribute('type'));" +
            "fileInput.setAttribute('name', arguments[1].getAttribute('name'));" +
            "fileInput.style.width = '1px';" +
            "fileInput.style.height = '1px';" +
            "arguments[0].appendChild(fileInput);" +
            "return fileInput;",
        form, inputField);
  }
  
  protected File uploadFile(WebElement inputField, File file) throws IOException {
    if (!"input".equalsIgnoreCase(inputField.getTagName())) {
      throw new IllegalArgumentException("Cannot upload file because " + Describe.describe(inputField) + " is not an INPUT");
    }

    if (!file.exists()) {
      throw new IllegalArgumentException("File not found: " + file.getAbsolutePath());
    }

    String canonicalPath = file.getCanonicalPath();
    inputField.sendKeys(canonicalPath);
    return new File(canonicalPath);
  }

  protected void selectOptionByText(WebElement selectField, String optionText) {
    $(selectField).should(exist).find(byText(optionText)).shouldBe(visible);
    new Select(selectField).selectByVisibleText(optionText);
  }

  protected void selectOptionByValue(WebElement selectField, String optionValue) {
    $(selectField).should(exist).find(byValue(optionValue)).shouldBe(visible);
    new Select(selectField).selectByValue(optionValue);
  }

  protected String getSelectedValue(WebElement selectElement) {
    WebElement option = getSelectedOption(selectElement);
    return option == null ? null : option.getAttribute("value");
  }

  protected String getSelectedText(WebElement selectElement) {
    WebElement option = getSelectedOption(selectElement);
    return option == null ? null : option.getText();
  }

  protected SelenideElement getSelectedOption(WebElement selectElement) {
    return wrap(new Select(selectElement).getFirstSelectedOption());
  }

  protected boolean exists() {
    try {
      return getActualDelegate() != null;
    } catch (WebDriverException elementNotFound) {
      if (Cleanup.of.isInvalidSelectorError(elementNotFound)) {
        throw Cleanup.of.wrap(elementNotFound);
      }
      return false;
    } catch (IndexOutOfBoundsException invalidElementIndex) {
      return false;
    }
  }

  protected boolean isDisplayed() {
    try {
      WebElement element = getActualDelegate();
      return element != null && element.isDisplayed();
    } catch (WebDriverException elementNotFound) {
      if (Cleanup.of.isInvalidSelectorError(elementNotFound)) {
        throw Cleanup.of.wrap(elementNotFound);
      }
      return false;
    } catch (IndexOutOfBoundsException invalidElementIndex) {
      return false;
    }
  }

  protected String describe() {
    try {
      return Describe.describe(getActualDelegate());
    } catch (WebDriverException elementDoesNotExist) {
      return Cleanup.of.webdriverExceptionMessage(elementDoesNotExist);
    } catch (IndexOutOfBoundsException invalidElementIndex) {
      return invalidElementIndex.toString();
    }
  }

  static Object delegateMethod(WebElement delegate, Method method, Object[] args) throws Throwable {
    try {
      return method.invoke(delegate, args);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }

  protected WebElement waitUntil(String prefix, Condition condition, long timeoutMs) {
    return waitUntil(prefix, null, condition, timeoutMs);
  }

  protected WebElement waitUntil(String prefix, String message, Condition condition, long timeoutMs) {
    final long startTime = currentTimeMillis();
    WebElement element;
    do {
      lastError = null;
      element = tryToGetElement();
      if (element != null) {
        try {
          if (condition.apply(element)) {
            return element;
          }
        }
        catch (WebDriverException elementNotFound) {
          lastError = elementNotFound;
        }
        catch (IndexOutOfBoundsException ignore) {
          lastError = ignore;
        }
      }
      else if (condition.applyNull()) {
        if (Cleanup.of.isInvalidSelectorError(lastError)) {
          throw Cleanup.of.wrap(lastError);
        }
        return null;
      }
      sleep(pollingInterval);
    }
    while (currentTimeMillis() - startTime <= timeoutMs);

    if (Cleanup.of.isInvalidSelectorError(lastError)) {
      throw Cleanup.of.wrap(lastError);
    }
    else if (!exists(element)) {
      return throwElementNotFound(condition, timeoutMs);
    }
    else {
      throw new ElementShould(getSearchCriteria(), prefix, message, condition, element, lastError, timeoutMs);
    }
  }

  protected WebElement throwElementNotFound(Condition condition, long timeoutMs) {
    throw new ElementNotFound(getSearchCriteria(), condition, lastError, timeoutMs);
  }

  protected void waitWhile(String prefix, Condition condition, long timeoutMs) {
    waitWhile(prefix, null, condition, timeoutMs);
  }
  protected void waitWhile(String prefix, String message, Condition condition, long timeoutMs) {
    final long startTime = currentTimeMillis();
    WebElement element;
    do {
      lastError = null;
      element = tryToGetElement();
      if (element != null) {
        try {
          if (!condition.apply(element)) {
            return;
          }
        }
        catch (WebDriverException elementNotFound) {
          lastError = elementNotFound;
        }
        catch (IndexOutOfBoundsException ignore) {
          lastError = ignore;
        }
      }
      else if (!condition.applyNull()) {
        if (Cleanup.of.isInvalidSelectorError(lastError)) {
          throw Cleanup.of.wrap(lastError);
        }
        return;
      }
      sleep(pollingInterval);
    }
    while (currentTimeMillis() - startTime <= timeoutMs);

    if (Cleanup.of.isInvalidSelectorError(lastError)) {
      throw Cleanup.of.wrap(lastError);
    }
    else if (!exists(element)) {
      throwElementNotFound(not(condition), timeoutMs);
    }
    else {
      throw new ElementShouldNot(getSearchCriteria(), prefix, message, condition, element, lastError, timeoutMs);
    }
  }

  protected boolean exists(WebElement element) {
    try {
      if (element == null) return false;
      element.isDisplayed();
      return true;
    } catch (WebDriverException elementNotFound) {
      return false;
    }
  }

  protected WebElement tryToGetElement() {
    try {
      return getActualDelegate();
    } catch (WebDriverException elementNotFound) {
      lastError = elementNotFound;
      return null;
    } catch (IndexOutOfBoundsException ignore) {
      lastError = ignore;
      return null;
    } catch (RuntimeException e) {
      throw Cleanup.of.wrap(e);
    }
  }

  protected SelenideElement find(SelenideElement proxy, Object arg, int index) {
    return WaitingSelenideElement.wrap(proxy, getSelector(arg), index);
  }

  protected By getSelector(Object arg) {
    return arg instanceof By ? (By) arg : By.cssSelector((String) arg);
  }

  protected SelenideElement parent(SelenideElement me) {
    return find(me, By.xpath(".."), 0);
  }

  protected SelenideElement closest(SelenideElement me, String tagOrClass) {
    return tagOrClass.startsWith(".") ?
        find(me, By.xpath("ancestor::*[@class='" + tagOrClass.replaceFirst("\\.", "")+ "']"), 0) :
        find(me, By.xpath("ancestor::" + tagOrClass), 0);
  }

  protected void scrollTo() {
    Point location = getDelegate().getLocation();
    executeJavaScript("window.scrollTo(" + location.getX() + ", " + location.getY() + ')');
  }

  protected File download() throws IOException, URISyntaxException {
    return FileDownloader.instance.download(getDelegate());
  }
  
  /**
   * rethrow each throwable, also from methods without throws declaration
   * @param t
   */
  protected static void rethrow(Throwable t){
    AbstractSelenideElement.<RuntimeException>throwAny(t);
  }
  
  @SuppressWarnings("unchecked")
  private static <E extends Throwable> void throwAny(Throwable t) throws E{
    throw (E)t;
  }
}
