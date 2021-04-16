<br />
<p align="center">
  <h2 align="center">Wikitax Webserver</h2>

  <p align="center">
    This file describes how to host a web applicationfor visualizing taxonomic relationships in a tree diagram.
    <br />
    <a href="http://basecamp-demos.informatik.uni-hamburg.de:8080/wikitax/">View Demo</a>
    ·
    <a href="https://github.com/Isomorphismus42/Base.Camp21_WikipediaTaxonomien">Repository Root</a>
  </p>
</p>



<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about">About</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <ul>
        <li><a href="#local">Local</a></li>
        <li><a href="#tomcat-server">Tomcat</a></li>
      </ul>
      <li><a href="#customizing-the-code">Customization</a></li>
      <ul>
        <li><a href="#api">API</a></li>
        <li><a href="#html-text">HTML Text</a></li>
        <li><a href="#javascript-app">JS App</a></li>
      </ul>
      <li><a href="#api-documentation">API Documentation</a></li>
    <li><a href="#acknowledgements">Acknowledgements</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About
This webserver is built using the Spring framework with Spring Boot. It's goal is to host a web application that allows you to visualize data in a dynamic tree diagram.  The frontend is built with HTML, CSS and Javascript, while the backend uses Java. The HTML pages are partly filled wih content by the backend using Thymeleaf and then delivered to the user. The Javascript and CSS part are completely static. The server also hosts a public API, which is used in the Javascript application to access the database data.  
It can be run locally or deployed on a Tomcat server.

[![][wikitax-screenshot]]()

### Built With

* [Spring](https://spring.io/)
* [Thymeleaf](https://www.thymeleaf.org/)
* [Bootstrap](https://getbootstrap.com)
* [JQuery](https://jquery.com)
* [D3](https://d3js.org/)



<!-- GETTING STARTED -->
## Getting Started

You can run the webserver locally using the mvwn file without having to install Maven. 

### Prerequisites

Make sure that you IDE supports Maven. This allows you to compile the files without having to install Maven on your computer. 

Your JAVA_HOME enviromental variable needs to point to your used JDK installation. If it doesn't, change it. [This tutorial](https://javatutorial.net/set-java-home-windows-10) shows how to do it. 

### Installation

Since this project can be run using Maven Wrapper you don't have any installation to do. 

<!-- USAGE EXAMPLES -->
## Usage

You can run the server locally or deploy it to a Tomcat server. 
Before running the server you need to edit the [credentials.json](https://github.com/Isomorphismus42/Base.Camp21_WikipediaTaxonomien/blob/master/Webserver/src/resources/credentials.json "credentials.json")  file and add the connection data that points to your database. 

### Local

You can easily run the server locally without setting up any extra infrastructure. For that, navigate to the projects root folder, in which the Maven Wrapper file should be located. Run it using the following command: 

```
  mvnw spring-boot:run 
  ```
  The server should be running now under the port 8080. 
### Tomcat Server

To deploy it on a Tomcat server make sure that the \<packaging> attribute in the pom file is set to war. It should be like this, if you clone the code from this repository.  Packaging the project now should give you a .war file, which you can deploy to your server.

## Customizing the code
Parts of the projects code can be modified to suit your needs better. The HTML and CSS can be changed especially easy since they are built using Bootstrap. 

###  API
You change parts of the [ApiController](https://github.com/Isomorphismus42/Base.Camp21_WikipediaTaxonomien/blob/master/Webserver/src/java/ApiController.java "ApiController.java") code to adjust the results that are returned by the API. This applies to all SQL queries in the code. By adding or modifying conditions with the weight attribute, you can change the quality of the results returned by the API. 

Let's take a look at the [getRandom()](https://github.com/Isomorphismus42/Base.Camp21_WikipediaTaxonomien/blob/0e3bd93bb77ca6f946e57db53f3b522d4221d327/Webserver/src/java/ApiController.java#L235) method. We use following SQL query to acquire a random result from the databse:
```sql
SELECT DISTINCT parent FROM taxonomien WHERE weight >= 1 ORDER BY RAND() LIMIT 1
```
By using the 'weight >=  1' condition in the where clause, we can filter out all parent-child entries that are low valued, thus filtering out low quality results. Changing the weight requirement allows for even better results. For example you be could using the following query:
```sql
SELECT DISTINCT parent FROM taxonomien WHERE weight >= 5 ORDER BY RAND() LIMIT 1
```
### HTML Text
The info text on the Home page is added using Thymeleaf in the [HomeController](https://github.com/Isomorphismus42/Base.Camp21_WikipediaTaxonomien/blob/master/Webserver/src/java/HomeController.java "HomeController.java") class. You can simply change the content of the String to change the text on the HTML file. 
The About page is completely created by plain HTML in a static way. If you want to change the contents of it, you need to modify the [about](https://github.com/Isomorphismus42/Base.Camp21_WikipediaTaxonomien/blob/master/Webserver/src/resources/templates/about.html "about.html") file.

### Javascript App

In the actual app that visualizes the data you have the [getChildren()](https://github.com/Isomorphismus42/Base.Camp21_WikipediaTaxonomien/blob/0e3bd93bb77ca6f946e57db53f3b522d4221d327/Webserver/src/resources/static/app.js#L205) function, that retrieves data from the API. It's sending a get-Request without specifying a limit for the result amount: 

```js
$.get('./api?parent=' + parent.name).then(function(result) {
	resultJSON = JSON.parse(result);
	for(let item of resultJSON) {
		if (item == "null") {
			return
		}
		addNode(parent, item);
	}
}); 
```
This returns no more than six children of the parent, as the API defaults to a limit of six. You can add a limit paramter to change the max amount of returned children:

```js
$.get('./api?parent=' + parent.name + '&limit=' + yourLimit).then(function(result) {
	resultJSON = JSON.parse(result);
	for(let item of resultJSON) {
		if (item == "null") {
			return
		}
		addNode(parent, item);
	}
}); 
```


## API Documentation
The API use is also documented on the about page of the server. Alternatively you can see it [here](https://github.com/Isomorphismus42/Base.Camp21_WikipediaTaxonomien/blob/master/Webserver/src/APIDocs.md). 


<!-- ACKNOWLEDGEMENTS -->
## Acknowledgements
* [Collapsible Tree D3 Code](https://observablehq.com/@d3/collapsible-tree)






<!-- MARKDOWN LINKS & IMAGES -->
[wikitax-screenshot]: https://github.com/Isomorphismus42/Base.Camp21_WikipediaTaxonomien/raw/master/Wikitax.PNG
