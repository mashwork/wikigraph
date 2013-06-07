This project is used for extracting page titles, links and structure(table of content) from wiki dump. Before you run the code, you need to make sure that you have already downloaded the dump file.

1) Regarding the dump file: You can download it here:
	http://dumps.wikimedia.org/backup-index.html
The code accepts both xml file and bz2 file. But xml file is recommended. It takes almost 30 minutes to process the xml file for one loop and it might take up to 6-7 hours if the file is a bz2.

2) Regarding the dependencies: For those jars that can be found from mvn online, mvn will download and manage the jar files automatically for you. The dependency is written in the pom.xml. But there still 2 jars that can not be found from the mvn online. These 2 jars are stored in the DependencyJar folder. Mvn need to load it from your local path. Before you compile the project by mvn, you need to change the "<localJavaLib>" in the pom.xml file. Make it the path of the DependencyJar folder.

3) Regarding the parameters: There are 4 parameters for this project: dir, output, query and iteration. For simplicity, there 4 parameters are stored in the test.java. You edit it by yourself. If you want to run the code in terminal, you can change them into "args[]".
	dir--the path of the wikidump.
	output--the path of output file. Stores extracted page titles and links in xml.
	query--the name of the page that you want to search for.
	iteration--number of iterations you want to run. Also means how deep you want to search for. Say query=="Game of Thrones" and iteration==4, then page titles and links that can be retrieved by 4 steps from the page "Game of Thrones" will be stored in the xml.

4) Regarding the table of content information: You can manually turn on the "switch" by "setPrintStructure(true)" so that the structure information will be printed to screen. If you want to save the structure information into the xml file, you need to implement it by your self OR wait for the next version.

5) Regarding the simple test file: "wiki_test.xml" is also provided in the project. It can be used for simple testing such as print links or structures(table of content information).