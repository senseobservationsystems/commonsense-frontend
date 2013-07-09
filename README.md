# CommonSense Frontend

A frontend for CommonSense, written in GWT. See the latest release in action at: http://common.sense-os.nl.

## Getting Started

To set up the project in Eclipse:

1. Clone this repo into your workspace.
2. In Eclipse, open the New Project wizard and start a new Java Project
3. Unselect "use default location", and select the location of the repo on your hard drive, the options in the wizard should become almost completely greyed out after this
4. Press "Next" to go to the Java Settings panel
5. In the "Source" tab, select the 'src' folder
6. The "Projects" tab should be empty
7. The "Libraries" tab should contain all the jar files that are inside the war/WEB-INF/lib/ folder
8. The "Order and Export" tab should only have the src folder selected
9. Press "Finish" to create the new project. The project probably shows many errors and warnings because the GWT library is not set up yet.
10. Right click on the new project, and select Google > Web Toolkit Settings...
11. Enable Web Toolkit for this project, and press "OK" to add the Web Toolkit library to your project and clear up the errors and warnings


