# GWT TagList
This GWT module provides a single widget to manage and display tags.
You can either use it as a non editable TagList or enable editing and get the possibilities to create, delete and move tags via drag and drop. The widget comes along with a default style for Tags and the TagList, but can also be created with an own set of CSS resources per instance. Size of the TagList and each Tag is inherited from the TagList's parent container.
No suggestion functionality has yet been implemented.

![Showcase](/showcase.PNG)

# Test
## Maven
mvn gwt:run -P iTest

## Eclipse
1. Properties -> Project Facets: Dynamic Web Module
2. Properties -> Google -> Web Application -> This project has a war directory (src/test/resources/webapp)
3. Properties -> Google -> Web Toolkit -> Use Google Web Toolkit
4. Run as Web Application, Arguments: `-war /<PROJECT>/target/iTest/war -remoteUI "${gwt_remote_ui_server_port}:${unique_id}" -startupUrl tagListTest.html -logLevel INFO -codeServerPort 9997 -port 8888 com.virilis_software.gwt.taglist.TagListTest`
